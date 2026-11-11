package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
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
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFgDim
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonYellow
import com.example.ui.theme.SkShell

@Composable
fun HudBar(
    cabinet: Cabinet,
    roomCode: String,
    playerXp: Int,
    crewCount: Int,
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cabinet.borderColor, RoundedCornerShape(4.dp))
            .background(SkShell, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left brand icon & text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, cabinet.primaryColor, RoundedCornerShape(3.dp))
                        .background(SkCard2, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Avatar Chip",
                        tint = cabinet.primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = cabinet.title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SkFg
                    )
                    Text(
                        text = "▸ ${cabinet.subtitle}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        color = cabinet.accentColor
                    )
                }
            }

            // Right stats: ROOM, XP, STEP dots
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Room Code Badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ROOM",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = SkFgMuted
                    )
                    Text(
                        text = roomCode,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SkNeonCyan
                    )
                }

                // XP counter
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "XP",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = SkFgMuted
                    )
                    Text(
                        text = String.format("%,06d", playerXp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SkNeonYellow
                    )
                }

                // Step meter dots
                if (totalSteps > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = SkFgMuted
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.height(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until totalSteps) {
                                val isDoneOrActive = i < currentStep
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (isDoneOrActive) cabinet.primaryColor else SkFgDim,
                                            RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
