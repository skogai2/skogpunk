package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Profile
import com.example.ui.components.PlaqueBadge
import com.example.ui.components.PlaqueStyle
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFg2
import com.example.ui.theme.SkFgDim
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonGreen
import com.example.ui.theme.SkNeonViolet
import com.example.ui.theme.SkNeonYellow

@Composable
fun ProfileScreen(
    profile: Profile,
    crtScanlinesEnabled: Boolean,
    onUpdateHandle: (String) -> Unit,
    onToggleCrtScanlines: () -> Unit,
    modifier: Modifier = Modifier
) {
    var handleInput by remember(profile.handle) { mutableStateOf(profile.handle) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "HACKER PROFILE & LOG",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = SkFg
                )
                Text(
                    text = "REP STATISTICS · APPEND-ONLY EVENT LOG STREAM",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = SkFgMuted
                )
            }
        }

        // Stats Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlaqueBadge(
                    label = "LIFETIME XP",
                    value = String.format("%,d", profile.lifetimeXp),
                    sublabel = "TOTAL HARVEST",
                    style = PlaqueStyle.VIOLET,
                    modifier = Modifier.weight(1f)
                )

                PlaqueBadge(
                    label = "HACKER RANK",
                    value = profile.rank,
                    sublabel = "${profile.solvedQuestIds.size} QUESTS CLEARED",
                    style = PlaqueStyle.CYAN,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Handle editor
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SkCard),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF4A3580), RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TERMINAL HANDLE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = SkFgMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = handleInput,
                            onValueChange = { handleInput = it.take(14).uppercase() },
                            placeholder = { Text("HANDLE", color = SkFgDim) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkNeonViolet,
                                unfocusedBorderColor = Color(0xFF4A3580),
                                focusedTextColor = SkFg,
                                unfocusedTextColor = SkFg2,
                                focusedContainerColor = Color(0xFF140D28),
                                unfocusedContainerColor = Color(0xFF140D28)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("handle_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onUpdateHandle(handleInput) },
                            enabled = handleInput.isNotBlank() && handleInput != profile.handle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkNeonViolet,
                                contentColor = Color(0xFF1A0420)
                            ),
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.testTag("save_handle_button")
                        ) {
                            Text(
                                text = "SAVE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Settings / CRT Visuals
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SkCard),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF4A3580), RoundedCornerShape(4.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CRT SCANLINE FX",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SkFg
                        )
                        Text(
                            text = "Overlay retro arcade phosphor scanlines on the HUD interface.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SkFgMuted
                        )
                    }

                    Switch(
                        checked = crtScanlinesEnabled,
                        onCheckedChange = { onToggleCrtScanlines() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SkNeonGreen,
                            checkedTrackColor = Color(0xFF1B3D2B),
                            uncheckedThumbColor = SkFgDim,
                            uncheckedTrackColor = Color(0xFF211640)
                        )
                    )
                }
            }
        }

        // Append-Only Event Log Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EVENTS.JSONL (APPEND-ONLY LOG)",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SkNeonGreen
                )
                Text(
                    text = "${profile.eventLogs.size} ENTRIES",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = SkFgMuted
                )
            }
        }

        // Log Items
        items(profile.eventLogs.reversed()) { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2B1D4E), RoundedCornerShape(2.dp))
                    .background(Color(0xFF0F0A1E), RoundedCornerShape(2.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[${log.timestamp}] ${log.type.uppercase()}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = SkNeonCyan
                        )
                        if (log.xpDelta > 0) {
                            Text(
                                text = "+${log.xpDelta} XP",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = SkNeonYellow
                            )
                        }
                    }
                    Text(
                        text = log.description,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SkFg2
                    )
                }
            }
        }
    }
}
