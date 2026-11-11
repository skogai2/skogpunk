package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AiPersona
import com.example.model.GamePhase
import com.example.ui.screens.AiCompanionScreen
import com.example.ui.screens.CabinetsScreen
import com.example.ui.screens.ModStudioScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.SkBg
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkFgDim
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkShell
import com.example.ui.theme.SkogaiRPGTheme

enum class ArcadeTab(val title: String) {
    GAME("GAME"),
    MOD_SQL("MOD/SQL"),
    AI_CHAT("AI CHAT"),
    CABINETS("CABINETS"),
    PROFILE("PROFILE")
}

@Composable
fun SkogApp(viewModel: SkogViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val questEntities by viewModel.questEntities.collectAsStateWithLifecycle()
    val mutators by viewModel.mutators.collectAsStateWithLifecycle()
    val eventLogs by viewModel.eventLogs.collectAsStateWithLifecycle()

    val selectedPersona by viewModel.selectedPersona.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val chatHistories by viewModel.chatHistories.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ArcadeTab.GAME) }

    val isFullViewportGameplay = currentTab == ArcadeTab.GAME &&
        (uiState.room.phase == GamePhase.STEP || uiState.room.phase == GamePhase.REVEAL)

    // Handle hardware back navigation cleanly
    BackHandler(enabled = currentTab != ArcadeTab.GAME || uiState.room.phase != GamePhase.LOBBY) {
        when {
            uiState.isPaused -> viewModel.togglePause()
            currentTab != ArcadeTab.GAME -> currentTab = ArcadeTab.GAME
            uiState.room.phase != GamePhase.LOBBY -> viewModel.returnToLobby()
        }
    }

    val effectiveModifiers = remember(uiState.activeQuest, mutators) {
        viewModel.computeEffectiveArenaModifiers()
    }
    val autoRevealHint = remember(mutators) {
        viewModel.isAutoRevealHintEnabled()
    }

    SkogaiRPGTheme(activeCabinet = uiState.activeQuest.cabinet) {
        Box(modifier = Modifier.fillMaxSize().background(SkBg)) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!isFullViewportGameplay) {
                        NavigationBar(
                            containerColor = SkShell,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    uiState.activeQuest.cabinet.borderColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        ) {
                            ArcadeTab.entries.forEach { tab ->
                                val isSelected = currentTab == tab
                                val iconVector = when (tab) {
                                    ArcadeTab.GAME -> Icons.Default.SportsEsports
                                    ArcadeTab.MOD_SQL -> Icons.Default.Code
                                    ArcadeTab.AI_CHAT -> Icons.Default.SmartToy
                                    ArcadeTab.CABINETS -> Icons.Default.GridView
                                    ArcadeTab.PROFILE -> Icons.Default.Person
                                }

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 9.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = uiState.activeQuest.cabinet.primaryColor,
                                        selectedTextColor = uiState.activeQuest.cabinet.primaryColor,
                                        unselectedIconColor = SkFgDim,
                                        unselectedTextColor = SkFgMuted,
                                        indicatorColor = SkCard
                                    ),
                                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        ArcadeTab.GAME -> {
                            PlayScreen(
                                uiState = uiState,
                                allQuests = quests,
                                effectiveArenaModifiers = effectiveModifiers,
                                autoRevealHint = autoRevealHint,
                                lifetimeXp = profile.lifetimeXp,
                                onSelectQuest = { viewModel.selectQuest(it) },
                                onAnswerSubmitted = { viewModel.submitUserAnswer(it) },
                                onRevealRequested = { viewModel.revealStep() },
                                onNextStepRequested = { viewModel.advanceStep() },
                                onStartQuestRequested = { viewModel.startQuest() },
                                onReturnToLobbyRequested = { viewModel.returnToLobby() },
                                onShardCollected = { viewModel.awardArenaShardXp(it) },
                                onToggleMute = { viewModel.toggleMute() },
                                onTogglePause = { viewModel.togglePause() },
                                onPlayerDefeated = { viewModel.onPlayerDefeated() },
                                onRebootFromGameOver = { viewModel.rebootFromGameOver() },
                                onNavigateToModStudio = { currentTab = ArcadeTab.MOD_SQL },
                                onNavigateToAiCompanion = { currentTab = ArcadeTab.AI_CHAT }
                            )
                        }

                        ArcadeTab.MOD_SQL -> {
                            ModStudioScreen(
                                quests = quests,
                                questEntities = questEntities,
                                mutators = mutators,
                                eventLogs = eventLogs,
                                activeQuest = uiState.activeQuest,
                                onSaveAndPlayQuest = { moddedQuest ->
                                    viewModel.saveCustomQuestMod(moddedQuest, launchImmediately = true)
                                    currentTab = ArcadeTab.GAME
                                },
                                onSaveQuestOnly = { moddedQuest ->
                                    viewModel.saveCustomQuestMod(moddedQuest, launchImmediately = false)
                                },
                                onDeleteQuest = { id ->
                                    viewModel.deleteQuestFromSql(id)
                                },
                                onToggleMutator = { id, enabled ->
                                    viewModel.toggleSqlMutator(id, enabled)
                                },
                                onResetDatabase = {
                                    viewModel.resetSqlDatabase()
                                },
                                onOpenAiModArchitect = {
                                    viewModel.selectAiPersona(AiPersona.MOD_ARCHITECT)
                                    currentTab = ArcadeTab.AI_CHAT
                                }
                            )
                        }

                        ArcadeTab.AI_CHAT -> {
                            AiCompanionScreen(
                                selectedPersona = selectedPersona,
                                selectedModel = selectedModel,
                                messages = chatHistories[selectedPersona].orEmpty(),
                                isGenerating = isAiGenerating,
                                onSelectPersona = { viewModel.selectAiPersona(it) },
                                onSelectModel = { viewModel.selectGeminiModel(it) },
                                onSendMessage = { viewModel.sendAiMessage(it) },
                                onClearChat = { viewModel.clearAiChat() },
                                onInstallGeneratedModAndPlay = { generatedQuest ->
                                    viewModel.saveCustomQuestMod(generatedQuest, launchImmediately = true)
                                    currentTab = ArcadeTab.GAME
                                }
                            )
                        }

                        ArcadeTab.CABINETS -> {
                            CabinetsScreen(
                                allQuests = quests,
                                solvedQuestIds = profile.solvedQuestIds,
                                onQuestSelected = { selected ->
                                    viewModel.selectQuest(selected)
                                    viewModel.startQuest(selected)
                                    currentTab = ArcadeTab.GAME
                                },
                                onEditQuestScript = { selected ->
                                    viewModel.selectQuest(selected)
                                    currentTab = ArcadeTab.MOD_SQL
                                }
                            )
                        }

                        ArcadeTab.PROFILE -> {
                            ProfileScreen(
                                profile = profile,
                                crtScanlinesEnabled = uiState.crtScanlinesEnabled,
                                onUpdateHandle = { viewModel.updateHandle(it) },
                                onToggleCrtScanlines = { viewModel.toggleCrtScanlines() }
                            )
                        }
                    }

                    // Optional Retro CRT scanlines overlay
                    if (uiState.crtScanlinesEnabled) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val lineSpacing = 4.dp.toPx()
                            var y = 0f
                            while (y < size.height) {
                                drawLine(
                                    color = Color(0x22000000),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.5f
                                )
                                y += lineSpacing
                            }
                        }
                    }
                }
            }
        }
    }
}
