package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Cabinet
import com.example.model.GamePhase
import com.example.model.Step
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFgDim
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonGreen

@Composable
fun QuestStrip(
    steps: List<Step>,
    currentStepIndex: Int,
    phase: GamePhase,
    cabinet: Cabinet,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val isDone = index < currentStepIndex || (index == currentStepIndex && phase == GamePhase.REVEAL)
            val isActive = index == currentStepIndex && phase == GamePhase.STEP
            val isLocked = index > currentStepIndex

            val borderColor = when {
                isDone -> SkNeonGreen
                isActive -> cabinet.primaryColor
                else -> Color(0xFF332352)
            }

            val bgColor = when {
                isActive -> SkCard2
                else -> SkCard
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, borderColor, RoundedCornerShape(3.dp))
                    .background(bgColor, RoundedCornerShape(3.dp))
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val iconTint = when {
                        isDone -> SkNeonGreen
                        isActive -> cabinet.primaryColor
                        else -> SkFgDim
                    }

                    val iconVector = when {
                        isDone -> Icons.Default.Check
                        isActive -> Icons.Default.PlayArrow
                        else -> Icons.Default.Lock
                    }

                    Icon(
                        imageVector = iconVector,
                        contentDescription = "Step State",
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )

                    Column {
                        Text(
                            text = "STEP ${index + 1}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (isActive) SkFg else SkFgMuted
                        )
                        Text(
                            text = "${if (step is Step.Text) "CMD" else "PICK"} · ${step.xp}XP",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = if (isActive) cabinet.accentColor else SkFgDim
                        )
                    }
                }
            }
        }
    }
}
