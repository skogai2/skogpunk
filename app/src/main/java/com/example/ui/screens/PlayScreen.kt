package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SkogGameEngine
import com.example.model.ArenaModifiers
import com.example.model.GamePhase
import com.example.model.Quest
import com.example.ui.GameUiState
import com.example.ui.components.ContractDialogueCard
import com.example.ui.components.CyberBreachArena
import com.example.ui.components.PlaqueBadge
import com.example.ui.components.PlaqueStyle
import com.example.ui.components.QuestStrip
import com.example.ui.theme.SkBg
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFg2
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonAcid
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonGreen
import com.example.ui.theme.SkNeonMagenta
import com.example.ui.theme.SkNeonRed
import com.example.ui.theme.SkNeonYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PlayScreen(
    uiState: GameUiState,
    allQuests: List<Quest>,
    effectiveArenaModifiers: ArenaModifiers,
    autoRevealHint: Boolean,
    lifetimeXp: Int,
    onSelectQuest: (Quest) -> Unit,
    onAnswerSubmitted: (Any) -> Unit,
    onRevealRequested: () -> Unit,
    onNextStepRequested: () -> Unit,
    onStartQuestRequested: () -> Unit,
    onReturnToLobbyRequested: () -> Unit,
    onShardCollected: (Int) -> Unit,
    onToggleMute: () -> Unit,
    onTogglePause: () -> Unit,
    onPlayerDefeated: () -> Unit,
    onRebootFromGameOver: () -> Unit,
    onNavigateToModStudio: () -> Unit,
    onNavigateToAiCompanion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cabinet = uiState.activeQuest.cabinet
    val step = uiState.activeQuest.steps.getOrNull(uiState.currentStepIndex)

    // In STEP phase, player can toggle the terminal deck overlay over the 60fps Cyber-Breach Arena
    var showTerminalOverlay by remember(uiState.currentStepIndex, uiState.room.phase) {
        mutableStateOf(uiState.room.phase == GamePhase.REVEAL)
    }

    Box(modifier = modifier.fillMaxSize().background(SkBg)) {
        when (uiState.room.phase) {
            GamePhase.LOBBY -> {
                // FULL-VIEWPORT TITLE SCREEN SHELL
                TitleScreenShell(
                    activeQuest = uiState.activeQuest,
                    allQuests = allQuests,
                    roomCode = uiState.room.code,
                    lifetimeXp = lifetimeXp,
                    isMuted = uiState.isMuted,
                    onSelectQuest = onSelectQuest,
                    onStartMission = onStartQuestRequested,
                    onOpenModStudio = onNavigateToModStudio,
                    onOpenAiCompanion = onNavigateToAiCompanion,
                    onToggleMute = onToggleMute
                )
            }

            GamePhase.STEP, GamePhase.REVEAL -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. World-Matched Perimeter Top Bar with Tabular-Nums Readouts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cabinet.borderColor.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .background(Color(0xE6140B28), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.activeQuest.title.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SkFg,
                                maxLines = 1
                            )
                            Text(
                                text = "STEP ${uiState.currentStepIndex + 1}/${uiState.activeQuest.stepCount} · XP ${String.format("%05d", uiState.userEarnedXpInQuest)}",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = cabinet.accentColor,
                                    fontFeatureSettings = "tnum"
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Toggle Terminal Deck Button
                            Button(
                                onClick = { showTerminalOverlay = !showTerminalOverlay },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showTerminalOverlay) cabinet.primaryColor else SkCard2,
                                    contentColor = if (showTerminalOverlay) Color(0xFF1A0420) else SkFg
                                ),
                                shape = RoundedCornerShape(3.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("toggle_terminal_deck_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Terminal",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showTerminalOverlay) "ARENA" else "TERMINAL",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }

                            // Quick Mod Button
                            IconButton(
                                onClick = onNavigateToModStudio,
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.dp, SkNeonCyan.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Mod Script",
                                    tint = SkNeonCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Mute Toggle
                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.dp, Color(0xFF4A3580), RoundedCornerShape(3.dp))
                                    .testTag("mute_audio_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Mute Toggle",
                                    tint = if (uiState.isMuted) SkFgMuted else SkNeonGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Pause Button
                            IconButton(
                                onClick = onTogglePause,
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.dp, SkNeonYellow.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                                    .testTag("pause_game_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause Game",
                                    tint = SkNeonYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // 2. Compact Step Strip
                    QuestStrip(
                        steps = uiState.activeQuest.steps,
                        currentStepIndex = uiState.currentStepIndex,
                        phase = uiState.room.phase,
                        cabinet = cabinet
                    )

                    // 3. Active 60fps Cyber-Breach Playfield + Optional Terminal Deck Overlay
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        CyberBreachArena(
                            cabinet = cabinet,
                            modifiers = effectiveArenaModifiers,
                            isPaused = uiState.isPaused || uiState.isGameOver || showTerminalOverlay || uiState.room.phase == GamePhase.REVEAL,
                            stepIndex = uiState.currentStepIndex,
                            onShardCollected = onShardCollected,
                            onFirewallBreached = {
                                showTerminalOverlay = true
                            },
                            onPlayerDefeated = onPlayerDefeated,
                            onOpenTerminalModal = {
                                showTerminalOverlay = true
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Terminal Contract Overlay Deck when Hacking Node or in Reveal phase
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showTerminalOverlay || uiState.room.phase == GamePhase.REVEAL,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            if (step != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .border(1.5.dp, cabinet.primaryColor, RoundedCornerShape(6.dp))
                                        .background(Color(0xF5120A24), RoundedCornerShape(6.dp))
                                        .padding(14.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "▸ FIREWALL TERMINAL CONTRACT",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = SkNeonCyan
                                        )
                                        if (uiState.room.phase == GamePhase.STEP) {
                                            IconButton(
                                                onClick = { showTerminalOverlay = false },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Return to Arena",
                                                    tint = SkFgMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (uiState.room.phase == GamePhase.REVEAL) {
                                        val granted = uiState.isUserCorrect == true
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    if (granted) SkNeonGreen else SkNeonRed,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .background(
                                                    if (granted) Color(0xFF0E2B1C) else Color(0xFF33101B),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = if (granted) "ACCESS GRANTED! +${step.xp} XP" else "ACCESS DENIED — FIREWALL HELD",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (granted) SkNeonGreen else SkNeonRed
                                                )
                                            }
                                        }
                                    }

                                    ContractDialogueCard(
                                        step = step,
                                        stepIndex = uiState.currentStepIndex,
                                        phase = uiState.room.phase,
                                        cabinet = cabinet,
                                        userAnswer = uiState.userAnswer,
                                        isCorrect = uiState.isUserCorrect,
                                        onAnswerSubmitted = onAnswerSubmitted
                                    )

                                    if (autoRevealHint && step is com.example.model.Step.Text && step.hint != null) {
                                        Text(
                                            text = "AUTO-DECRYPTOR MUTATOR HINT: ${step.hint}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = SkNeonAcid
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (uiState.room.phase == GamePhase.STEP && uiState.userAnswer != null) {
                                            Button(
                                                onClick = onRevealRequested,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = cabinet.accentColor,
                                                    contentColor = Color(0xFF1A0420)
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("reveal_step_button")
                                            ) {
                                                Text(
                                                    text = "VERIFY & REVEAL",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        } else if (uiState.room.phase == GamePhase.REVEAL) {
                                            Button(
                                                onClick = {
                                                    showTerminalOverlay = false
                                                    onNextStepRequested()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = cabinet.primaryColor,
                                                    contentColor = Color(0xFF1A0420)
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("next_step_button")
                                            ) {
                                                Text(
                                                    text = if (uiState.currentStepIndex + 1 >= uiState.activeQuest.stepCount)
                                                        "COMPLETE MISSION" else "NEXT WAVE & STEP",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = onNavigateToAiCompanion,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SmartToy,
                                                contentDescription = "Ask Skogix AI",
                                                tint = SkNeonMagenta,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "ASK AI",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                color = SkNeonMagenta
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            GamePhase.DONE -> {
                VictoryModalShell(
                    uiState = uiState,
                    lifetimeXp = lifetimeXp,
                    onReplay = onStartQuestRequested,
                    onReturnToTitle = onReturnToLobbyRequested,
                    onOpenModStudio = onNavigateToModStudio
                )
            }
        }

        // PAUSE MODAL OVERLAY
        if (uiState.isPaused) {
            GameShellModalOverlay(
                title = "SIMULATION PAUSED",
                subtitle = "CONTRACT: ${uiState.activeQuest.title.uppercase()}",
                accentColor = SkNeonYellow,
                primaryLabel = "▶ RESUME MISSION",
                onPrimaryClick = onTogglePause,
                secondaryLabel = "⚙ EDIT QUEST SCRIPT IN STUDIO",
                onSecondaryClick = {
                    onTogglePause()
                    onNavigateToModStudio()
                },
                tertiaryLabel = "✕ ABORT TO TITLE SCREEN",
                onTertiaryClick = {
                    onTogglePause()
                    onReturnToLobbyRequested()
                }
            )
        }

        // GAME OVER MODAL OVERLAY
        if (uiState.isGameOver) {
            GameShellModalOverlay(
                title = "FIREWALL BREACHED — DRONE OFFLINE",
                subtitle = "ICE SENTRIES OVERWHELMED YOUR SHIELD CAPACITOR",
                accentColor = SkNeonRed,
                primaryLabel = "⚡ REBOOT SHIELD & CONTINUE",
                onPrimaryClick = onRebootFromGameOver,
                secondaryLabel = "⚙ TUNE ARENA MODIFIERS IN STUDIO",
                onSecondaryClick = {
                    onRebootFromGameOver()
                    onNavigateToModStudio()
                },
                tertiaryLabel = "✕ RETURN TO TITLE SCREEN",
                onTertiaryClick = {
                    onRebootFromGameOver()
                    onReturnToLobbyRequested()
                }
            )
        }
    }
}

@Composable
private fun TitleScreenShell(
    activeQuest: Quest,
    allQuests: List<Quest>,
    roomCode: String,
    lifetimeXp: Int,
    isMuted: Boolean,
    onSelectQuest: (Quest) -> Unit,
    onStartMission: () -> Unit,
    onOpenModStudio: () -> Unit,
    onOpenAiCompanion: () -> Unit,
    onToggleMute: () -> Unit
) {
    val cabinet = activeQuest.cabinet
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var startNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (startNanos == 0L) startNanos = nanos
                animTime = (nanos - startNanos) / 1_000_000_000f
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Live-World Atmospheric Camera Pan & Hero Centerpiece Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Deep night-violet radial atmosphere
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cabinet.primaryColor.copy(alpha = 0.22f),
                        Color(0xFF120824),
                        Color(0xFF07040E)
                    ),
                    center = Offset(w * 0.5f, h * 0.32f),
                    radius = w * 0.85f
                ),
                size = size
            )

            // Animated perspective horizon grid
            val horizonY = h * 0.42f
            val numRays = 14
            for (i in -numRays..numRays) {
                val topX = w * 0.5f + i * (w * 0.04f)
                val botX = w * 0.5f + i * (w * 0.16f) + sin(animTime * 0.6f) * 35f
                drawLine(
                    color = cabinet.borderColor.copy(alpha = 0.22f),
                    start = Offset(topX, horizonY),
                    end = Offset(botX, h),
                    strokeWidth = 1.5f
                )
            }

            val scroll = (animTime * 40f) % 36f
            var y = horizonY + scroll
            while (y < h) {
                drawLine(
                    color = cabinet.accentColor.copy(alpha = 0.16f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += 36f
            }

            // Orbiting Cyber-Core Emblem in upper negative space
            val cx = w * 0.5f + cos(animTime * 1.1f) * 18f
            val cy = h * 0.20f + sin(animTime * 1.6f) * 10f
            drawCircle(
                color = cabinet.primaryColor.copy(alpha = 0.18f),
                radius = 64f + sin(animTime * 3f) * 6f,
                center = Offset(cx, cy)
            )
            val hexPath = Path().apply {
                for (k in 0 until 6) {
                    val a = animTime * 0.8f + k * 1.047197f
                    val px = cx + cos(a) * 44f
                    val py = cy + sin(a) * 44f
                    if (k == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(hexPath, color = cabinet.primaryColor, style = Stroke(width = 2.5f))
            drawCircle(color = SkNeonCyan, radius = 8f, center = Offset(cx, cy))
        }

        // Top-right mute & status pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROOM $roomCode · REP ${String.format("%,06d", lifetimeXp)} XP",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SkNeonCyan,
                    fontFeatureSettings = "tnum"
                )
            )

            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(34.dp)
                    .border(1.dp, cabinet.borderColor, RoundedCornerShape(4.dp))
                    .background(Color(0xAA140A26), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute Toggle",
                    tint = if (isMuted) SkFgMuted else SkNeonGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Center-Bottom Title Lockup + Quest Picker + Primary Start CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bespoke Title Lockup over negative space
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "▸ ${cabinet.title} · ${cabinet.subtitle}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = cabinet.accentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SKOG.AI ARCADE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp,
                    color = SkFg
                )
                Text(
                    text = "60FPS CYBER-BREACH & LIVE MODDABLE TERMINAL RPG",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = SkFg2
                )
            }

            // Horizontal Quest / Mod Selector Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allQuests.forEach { quest ->
                    val isSelected = quest.id == activeQuest.id
                    Box(
                        modifier = Modifier
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) quest.cabinet.primaryColor else Color(0xFF3E2A68),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (isSelected) Color(0xE6251648) else Color(0xCC140C28),
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onSelectQuest(quest) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("title_quest_chip_${quest.id}")
                    ) {
                        Column {
                            Text(
                                text = (if (quest.isCustomMod) "★ " else "") + quest.title,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) SkFg else SkFg2
                            )
                            Text(
                                text = "${quest.cabinet.title} · ${quest.stepCount} STEPS · ${quest.maxXp} XP",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = if (quest.isCustomMod) SkNeonYellow else quest.cabinet.accentColor
                            )
                        }
                    }
                }
            }

            // Selected Contract Summary Plaque
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cabinet.borderColor.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .background(Color(0xD9160D2A), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeQuest.title.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = cabinet.primaryColor
                        )
                        Text(
                            text = "${activeQuest.arenaModifiers.enemyCount} ICE · ${activeQuest.maxXp} XP",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SkNeonYellow,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                    Text(
                        text = activeQuest.description,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SkFg2,
                        maxLines = 2
                    )
                }
            }

            // Primary Start CTA + Mod Studio / AI Architect Actions
            Button(
                onClick = onStartMission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = cabinet.primaryColor,
                    contentColor = Color(0xFF14041C)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_quest_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "JACK IN · START CYBER-BREACH",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenModStudio,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_mod_studio_cta")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = SkNeonCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SCRIPT / MOD",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = SkNeonCyan
                    )
                }

                OutlinedButton(
                    onClick = onOpenAiCompanion,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_ai_companion_cta")
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = SkNeonMagenta,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI ARCHITECT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = SkNeonMagenta
                    )
                }
            }

            // Single flat control-hint line
            Text(
                text = "DRAG Move · AUTO-PULSE Fire · DOUBLE-TAP EMP Dash · HACK Terminal",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = SkFgMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VictoryModalShell(
    uiState: GameUiState,
    lifetimeXp: Int,
    onReplay: () -> Unit,
    onReturnToTitle: () -> Unit,
    onOpenModStudio: () -> Unit
) {
    val rank = SkogGameEngine.calculateRank(
        xp = uiState.userEarnedXpInQuest,
        maxXp = uiState.activeQuest.maxXp
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, SkNeonGreen, RoundedCornerShape(4.dp))
                .background(Color(0xFF0E291C), RoundedCornerShape(4.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MISSION ACCOMPLISHED!",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = SkNeonGreen
                )
                Text(
                    text = uiState.activeQuest.title.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SkFg2
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlaqueBadge(
                label = "MISSION RANK",
                value = rank,
                sublabel = "${uiState.userEarnedXpInQuest} / ${uiState.activeQuest.maxXp} XP",
                style = PlaqueStyle.VIOLET,
                modifier = Modifier.weight(1f)
            )
            PlaqueBadge(
                label = "LIFETIME REP",
                value = String.format("%,d", lifetimeXp),
                sublabel = "SQL RECORDED",
                style = PlaqueStyle.CYAN,
                modifier = Modifier.weight(1f)
            )
        }

        // Crew Scoreboard
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF4A3580), RoundedCornerShape(4.dp))
                .background(SkCard, RoundedCornerShape(4.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "CREW ROSTER SCOREBOARD",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = SkNeonYellow
            )

            uiState.room.playerList.sortedByDescending { it.xp }.forEachIndexed { idx, p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (idx == 0) SkCard2 else Color(0xFF160E2B), RoundedCornerShape(3.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (idx == 0) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "MVP",
                                tint = SkNeonYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${idx + 1}. ${p.name}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (idx == 0) SkNeonYellow else SkFg
                        )
                    }
                    Text(
                        text = "${p.xp} XP",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SkNeonCyan,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onReplay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = uiState.activeQuest.cabinet.primaryColor,
                    contentColor = Color(0xFF1A0420)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "REPLAY MISSION",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            OutlinedButton(
                onClick = onOpenModStudio,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "MOD THIS QUEST",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SkNeonCyan
                )
            }
        }

        OutlinedButton(
            onClick = onReturnToTitle,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "RETURN TO TITLE SCREEN",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SkFg
            )
        }
    }
}

@Composable
private fun GameShellModalOverlay(
    title: String,
    subtitle: String,
    accentColor: Color,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    secondaryLabel: String,
    onSecondaryClick: () -> Unit,
    tertiaryLabel: String,
    onTertiaryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD907040F))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .border(2.dp, accentColor, RoundedCornerShape(6.dp))
                .background(SkCard, RoundedCornerShape(6.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = accentColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SkFg2,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onPrimaryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color(0xFF14041C)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = primaryLabel,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = onSecondaryClick,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = secondaryLabel,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SkNeonCyan
                )
            }

            OutlinedButton(
                onClick = onTertiaryClick,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tertiaryLabel,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SkFgMuted
                )
            }
        }
    }
}
