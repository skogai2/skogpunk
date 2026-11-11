package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CyberSynthEngine
import com.example.data.AiCompanionRepository
import com.example.data.AiPersona
import com.example.data.ChatMessage
import com.example.data.GameRepository
import com.example.data.GeminiModelOption
import com.example.data.QuestsRepository
import com.example.data.local.EventLogEntity
import com.example.data.local.ModMutatorEntity
import com.example.data.local.QuestEntity
import com.example.engine.SkogGameEngine
import com.example.model.ArenaModifiers
import com.example.model.GamePhase
import com.example.model.Player
import com.example.model.Profile
import com.example.model.Quest
import com.example.model.Room
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG = "SkogViewModel"

data class GameUiState(
    val room: Room = Room(code = "SKOG"),
    val activeQuest: Quest = QuestsRepository.allQuests.first(),
    val currentStepIndex: Int = 0,
    val userAnswer: Any? = null,
    val isUserCorrect: Boolean? = null,
    val userEarnedXpInQuest: Int = 0,
    val isCrewModeEnabled: Boolean = true,
    val crtScanlinesEnabled: Boolean = false,
    val isMuted: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false
)

class SkogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)
    private val aiRepository = AiCompanionRepository()

    val quests: StateFlow<List<Quest>> = repository.questsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuestsRepository.allQuests)

    val questEntities: StateFlow<List<QuestEntity>> = repository.questEntitiesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mutators: StateFlow<List<ModMutatorEntity>> = repository.mutatorsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuestsRepository.defaultMutators)

    val eventLogs: StateFlow<List<EventLogEntity>> = repository.eventLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<Profile> = combine(
        repository.profile,
        eventLogs
    ) { baseProfile, sqlLogs ->
        baseProfile.copy(eventLogs = sqlLogs.map { it.toDomainLog() }.reversed())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.profile.value)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Multi-turn Gemini Chat State
    private val _selectedPersona = MutableStateFlow(AiPersona.MOD_ARCHITECT)
    val selectedPersona: StateFlow<AiPersona> = _selectedPersona.asStateFlow()

    private val _selectedModel = MutableStateFlow(GeminiModelOption.FLASH_3_8)
    val selectedModel: StateFlow<GeminiModelOption> = _selectedModel.asStateFlow()

    private val _chatHistories = MutableStateFlow<Map<AiPersona, List<ChatMessage>>>(
        mapOf(
            AiPersona.MOD_ARCHITECT to listOf(
                ChatMessage(
                    isUser = false,
                    authorName = AiPersona.MOD_ARCHITECT.displayName,
                    text = "Welcome to the SkogScript Mod Studio! Tell me any topic (e.g. 'Docker containers', 'Git rebase', 'SSH security', or 'SQL joins') and I'll generate a complete, playable @quest mod you can install to the SQL database with 1 tap."
                )
            ),
            AiPersona.SKOGIX_BUDDY to listOf(
                ChatMessage(
                    isUser = false,
                    authorName = AiPersona.SKOGIX_BUDDY.displayName,
                    text = "Yo hacker! I'm Skogix, your co-op netrunner. Stuck on a bash loop, cron schedule, or cURL flag in your current contract? Ask me for a tactical hint!"
                )
            ),
            AiPersona.ROOT_SENSEI to listOf(
                ChatMessage(
                    isUser = false,
                    authorName = AiPersona.ROOT_SENSEI.displayName,
                    text = "Greetings, initiate. Ask me about UNIX pipelines, regular expressions, process signals, or SQLite query optimization."
                )
            )
        )
    )
    val chatHistories: StateFlow<Map<AiPersona, List<ChatMessage>>> = _chatHistories.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    init {
        initializeInitialRoom()
    }

    private fun initializeInitialRoom() {
        val initialHandle = repository.profile.value.handle
        val initialPlayers = mapOf(
            "user" to Player(id = "user", name = initialHandle, connected = true, isHost = true),
            "bot_skogix" to Player(id = "bot_skogix", name = "Skogix", connected = true, isBot = true),
            "bot_kenji" to Player(id = "bot_kenji", name = "Kenji", connected = true, isBot = true)
        )
        _uiState.update {
            it.copy(
                room = Room(
                    code = "SKOG",
                    phase = GamePhase.LOBBY,
                    questId = it.activeQuest.id,
                    stepIndex = 0,
                    players = initialPlayers
                )
            )
        }
    }

    fun computeEffectiveArenaModifiers(): ArenaModifiers {
        val base = _uiState.value.activeQuest.arenaModifiers
        val activeMuts = mutators.value.filter { it.isEnabled }
        val fireRateBonus = activeMuts.sumOf { it.fireRateBonus.toDouble() }.toFloat()
        val shieldBonus = activeMuts.sumOf { it.shieldBonus }
        val ricochet = base.ricochetShots || activeMuts.any { it.ricochetEnabled }
        return base.copy(
            playerFireRateMultiplier = (base.playerFireRateMultiplier + fireRateBonus).coerceIn(0.5f, 4.5f),
            shieldHp = (base.shieldHp + shieldBonus).coerceIn(25, 600),
            ricochetShots = ricochet
        )
    }

    fun isAutoRevealHintEnabled(): Boolean {
        return mutators.value.any { it.isEnabled && it.revealHintPrefix }
    }

    private fun computeXpMultiplier(): Float {
        var mult = 1.0f
        for (m in mutators.value) {
            if (m.isEnabled && m.xpMultiplier > 1.0f) {
                mult *= m.xpMultiplier
            }
        }
        return mult
    }

    fun selectQuest(quest: Quest) {
        _uiState.update {
            it.copy(
                activeQuest = quest,
                room = it.room.copy(
                    questId = quest.id,
                    phase = GamePhase.LOBBY,
                    stepIndex = 0
                ),
                currentStepIndex = 0,
                userAnswer = null,
                isUserCorrect = null,
                userEarnedXpInQuest = 0,
                isPaused = false,
                isGameOver = false
            )
        }
    }

    fun startQuest(quest: Quest? = null) {
        val targetQuest = quest ?: _uiState.value.activeQuest
        val currentPlayers = _uiState.value.room.players.mapValues { (_, p) ->
            p.copy(answer = null, xp = 0)
        }

        repository.appendLog(
            type = "quest_started",
            roomCode = _uiState.value.room.code,
            description = "Contract initiated: ${targetQuest.title} (${targetQuest.stepCount} steps)"
        )

        _uiState.update {
            it.copy(
                activeQuest = targetQuest,
                currentStepIndex = 0,
                userAnswer = null,
                isUserCorrect = null,
                userEarnedXpInQuest = 0,
                isPaused = false,
                isGameOver = false,
                room = it.room.copy(
                    phase = GamePhase.STEP,
                    questId = targetQuest.id,
                    stepIndex = 0,
                    players = currentPlayers
                )
            )
        }
    }

    fun submitUserAnswer(answer: Any) {
        val currentQuest = _uiState.value.activeQuest
        val step = currentQuest.steps.getOrNull(_uiState.value.currentStepIndex) ?: return

        val userCorrect = SkogGameEngine.check(step, answer)

        val updatedPlayers = _uiState.value.room.players.toMutableMap()
        val currentUser = updatedPlayers["user"] ?: Player("user", repository.profile.value.handle)
        updatedPlayers["user"] = currentUser.copy(answer = answer)

        if (_uiState.value.isCrewModeEnabled) {
            updatedPlayers.forEach { (id, player) ->
                if (player.isBot && player.connected && player.answer == null) {
                    val botAns = SkogGameEngine.simulateBotAnswer(step, player.name)
                    updatedPlayers[id] = player.copy(answer = botAns)
                }
            }
        }

        _uiState.update {
            it.copy(
                userAnswer = answer,
                isUserCorrect = userCorrect,
                room = it.room.copy(players = updatedPlayers)
            )
        }

        if (_uiState.value.room.allLiveAnswered) {
            revealStep()
        }
    }

    fun revealStep() {
        val state = _uiState.value
        val step = state.activeQuest.steps.getOrNull(state.currentStepIndex) ?: return
        val xpMult = computeXpMultiplier()

        var userGainedXp = 0
        val scoredPlayers = state.room.players.mapValues { (_, player) ->
            val correct = SkogGameEngine.check(step, player.answer)
            val addedXp = if (correct) (step.xp * xpMult).roundToInt() else 0
            if (player.id == "user") {
                userGainedXp = addedXp
            }
            player.copy(xp = player.xp + addedXp)
        }

        if (state.isUserCorrect == true) {
            CyberSynthEngine.playAccessGranted()
        } else {
            CyberSynthEngine.playAccessDenied()
        }

        val totalEarned = state.userEarnedXpInQuest + userGainedXp

        repository.appendLog(
            type = "step_revealed",
            roomCode = state.room.code,
            description = "Step ${state.currentStepIndex + 1} revealed: ${if (state.isUserCorrect == true) "GRANTED (+$userGainedXp XP)" else "DENIED"}",
            xpDelta = userGainedXp
        )

        _uiState.update {
            it.copy(
                userEarnedXpInQuest = totalEarned,
                room = it.room.copy(
                    phase = GamePhase.REVEAL,
                    players = scoredPlayers
                )
            )
        }
    }

    fun advanceStep() {
        val state = _uiState.value
        val nextIndex = state.currentStepIndex + 1
        val isDone = nextIndex >= state.activeQuest.steps.size

        if (isDone) {
            CyberSynthEngine.playAccessGranted()
            repository.recordCompletedQuest(
                questId = state.activeQuest.id,
                roomCode = state.room.code,
                earnedXp = state.userEarnedXpInQuest
            )
            _uiState.update {
                it.copy(
                    room = it.room.copy(phase = GamePhase.DONE)
                )
            }
        } else {
            val clearedPlayers = state.room.players.mapValues { (_, p) ->
                p.copy(answer = null)
            }
            _uiState.update {
                it.copy(
                    currentStepIndex = nextIndex,
                    userAnswer = null,
                    isUserCorrect = null,
                    room = it.room.copy(
                        phase = GamePhase.STEP,
                        stepIndex = nextIndex,
                        players = clearedPlayers
                    )
                )
            }
        }
    }

    fun awardArenaShardXp(shardPoints: Int) {
        val scaled = (shardPoints * computeXpMultiplier()).roundToInt().coerceAtLeast(5)
        _uiState.update { state ->
            val userPlayer = state.room.players["user"]
            val updatedPlayers = if (userPlayer != null) {
                state.room.players + ("user" to userPlayer.copy(xp = userPlayer.xp + scaled))
            } else {
                state.room.players
            }
            state.copy(
                userEarnedXpInQuest = state.userEarnedXpInQuest + scaled,
                room = state.room.copy(players = updatedPlayers)
            )
        }
    }

    fun onPlayerDefeated() {
        CyberSynthEngine.playAccessDenied()
        _uiState.update { it.copy(isGameOver = true) }
    }

    fun rebootFromGameOver() {
        _uiState.update { it.copy(isGameOver = false, isPaused = false) }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun toggleMute() {
        val nextMuted = !_uiState.value.isMuted
        CyberSynthEngine.isMuted = nextMuted
        _uiState.update { it.copy(isMuted = nextMuted) }
    }

    fun returnToLobby() {
        _uiState.update {
            it.copy(
                currentStepIndex = 0,
                userAnswer = null,
                isUserCorrect = null,
                isPaused = false,
                isGameOver = false,
                room = it.room.copy(phase = GamePhase.LOBBY, stepIndex = 0)
            )
        }
    }

    // --- Mod Studio & SQL Actions ---
    fun saveCustomQuestMod(quest: Quest, launchImmediately: Boolean) {
        viewModelScope.launch {
            repository.saveCustomQuest(quest, _uiState.value.room.code)
            if (launchImmediately) {
                startQuest(quest)
            } else {
                selectQuest(quest)
            }
        }
    }

    fun deleteQuestFromSql(questId: String) {
        viewModelScope.launch {
            repository.deleteQuest(questId, _uiState.value.room.code)
        }
    }

    fun toggleSqlMutator(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleMutator(id, enabled, _uiState.value.room.code)
        }
    }

    fun resetSqlDatabase() {
        viewModelScope.launch {
            repository.resetFactoryDatabase(_uiState.value.room.code)
        }
    }

    // --- Room & Profile Actions ---
    fun createNewRoom() {
        val newCode = SkogGameEngine.generateRoomCode()
        repository.appendLog("room_opened", newCode, "New cabinet room opened: $newCode")
        _uiState.update {
            it.copy(
                room = it.room.copy(code = newCode, phase = GamePhase.LOBBY)
            )
        }
    }

    fun joinRoom(code: String) {
        val clean = code.trim().uppercase()
        if (clean.length == 4) {
            repository.appendLog("room_joined", clean, "Jacked into room: $clean")
            _uiState.update {
                it.copy(
                    room = it.room.copy(code = clean, phase = GamePhase.LOBBY)
                )
            }
        }
    }

    fun toggleCrewMode() {
        _uiState.update {
            it.copy(isCrewModeEnabled = !it.isCrewModeEnabled)
        }
    }

    fun toggleCrtScanlines() {
        _uiState.update {
            it.copy(crtScanlinesEnabled = !it.crtScanlinesEnabled)
        }
    }

    fun updateHandle(newHandle: String) {
        repository.updateHandle(newHandle)
        _uiState.update {
            val updatedPlayers = it.room.players.toMutableMap()
            updatedPlayers["user"] = (updatedPlayers["user"] ?: Player("user", newHandle)).copy(name = newHandle)
            it.copy(room = it.room.copy(players = updatedPlayers))
        }
    }

    // --- Multi-Turn Gemini Chat Actions ---
    fun selectAiPersona(persona: AiPersona) {
        _selectedPersona.value = persona
    }

    fun selectGeminiModel(model: GeminiModelOption) {
        _selectedModel.value = model
    }

    fun clearAiChat() {
        val persona = _selectedPersona.value
        val welcome = _chatHistories.value[persona]?.firstOrNull()
        _chatHistories.update { map ->
            map + (persona to listOfNotNull(welcome))
        }
    }

    fun sendAiMessage(userText: String) {
        if (userText.isBlank() || _isAiGenerating.value) return
        val persona = _selectedPersona.value
        val modelOpt = _selectedModel.value
        val previousMessages = _chatHistories.value[persona].orEmpty()

        val userMsg = ChatMessage(
            isUser = true,
            authorName = repository.profile.value.handle,
            text = userText
        )
        val placeholderId = "ai_reply_${System.nanoTime()}"
        val streamingPlaceholder = ChatMessage(
            id = placeholderId,
            isUser = false,
            authorName = "${persona.displayName} (${modelOpt.label})",
            text = "…",
            isStreaming = true
        )

        _chatHistories.update { map ->
            map + (persona to (previousMessages + userMsg + streamingPlaceholder))
        }

        val activeQ = _uiState.value.activeQuest
        val activeStep = activeQ.steps.getOrNull(_uiState.value.currentStepIndex)
        val contextSummary = buildString {
            appendLine("Active Quest: '${activeQ.title}' (id=${activeQ.id}, cabinet=${activeQ.cabinet.id})")
            if (activeStep != null) {
                appendLine("Current Contract Step ${_uiState.value.currentStepIndex + 1}: ${activeStep.prompt}")
            }
        }

        viewModelScope.launch {
            _isAiGenerating.value = true
            try {
                aiRepository.streamChatReply(
                    persona = persona,
                    modelOption = modelOpt,
                    history = previousMessages,
                    userPrompt = userText,
                    activeQuestContext = contextSummary
                ).collect { partialText ->
                    _chatHistories.update { map ->
                        val list = map[persona].orEmpty().map { m ->
                            if (m.id == placeholderId) {
                                m.copy(text = partialText, isStreaming = true)
                            } else m
                        }
                        map + (persona to list)
                    }
                }

                // Mark streaming complete
                _chatHistories.update { map ->
                    val list = map[persona].orEmpty().map { m ->
                        if (m.id == placeholderId) {
                            m.copy(isStreaming = false)
                        } else m
                    }
                    map + (persona to list)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Companion chat failed", e)
                val errText = e.localizedMessage ?: "Failed to reach Gemini AI service."
                _chatHistories.update { map ->
                    val list = map[persona].orEmpty().map { m ->
                        if (m.id == placeholderId) {
                            m.copy(
                                text = "ERROR: $errText",
                                isStreaming = false,
                                isError = true
                            )
                        } else m
                    }
                    map + (persona to list)
                }
            } finally {
                _isAiGenerating.value = false
            }
        }
    }
}
