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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.GameUiState
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
fun CrewScreen(
    uiState: GameUiState,
    onGenerateNewRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onToggleCrewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var roomCodeInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ARCADE CREW & ROOM",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = SkFg
                )
                Text(
                    text = "LOCAL ROOM DISCOVERY · MULTI-HACKER SIMULATION",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = SkFgMuted
                )
            }
        }

        // Room Code & Management Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SkCard),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SkNeonCyan.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CURRENT ROOM CODE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = SkFgMuted
                            )
                            Text(
                                text = uiState.room.code,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                letterSpacing = 2.sp,
                                color = SkNeonCyan
                            )
                        }

                        IconButton(
                            onClick = onGenerateNewRoom,
                            modifier = Modifier
                                .border(1.dp, SkNeonCyan, RoundedCornerShape(3.dp))
                                .background(SkCard2, RoundedCornerShape(3.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Roll new room",
                                tint = SkNeonCyan
                            )
                        }
                    }

                    // Join Room
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = roomCodeInput,
                            onValueChange = {
                                if (it.length <= 4) roomCodeInput = it.uppercase()
                            },
                            placeholder = {
                                Text(
                                    text = "4-LETTER CODE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = SkFgDim
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkNeonCyan,
                                unfocusedBorderColor = Color(0xFF4A3580),
                                focusedTextColor = SkFg,
                                unfocusedTextColor = SkFg2,
                                focusedContainerColor = Color(0xFF140D28),
                                unfocusedContainerColor = Color(0xFF140D28)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("join_room_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (roomCodeInput.isNotBlank()) {
                                    onJoinRoom(roomCodeInput)
                                    roomCodeInput = ""
                                }
                            },
                            enabled = roomCodeInput.length == 4,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkNeonCyan,
                                contentColor = Color(0xFF0B0716)
                            ),
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.testTag("join_room_button")
                        ) {
                            Text(
                                text = "CONNECT",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Crew Mode Switch
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
                            text = "AI HACKER CREW SIMULATION",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SkFg
                        )
                        Text(
                            text = "Simulate local co-op players (Skogix & Kenji) competing for MVP on the cabinet leaderboard.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SkFgMuted
                        )
                    }

                    Switch(
                        checked = uiState.isCrewModeEnabled,
                        onCheckedChange = { onToggleCrewMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SkNeonCyan,
                            checkedTrackColor = Color(0xFF1D3550),
                            uncheckedThumbColor = SkFgDim,
                            uncheckedTrackColor = Color(0xFF211640)
                        )
                    )
                }
            }
        }

        // Crew Roster
        item {
            Text(
                text = "HACKERS IN ROOM (${uiState.room.players.size})",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SkNeonYellow
            )
        }

        items(uiState.room.playerList) { player ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3C2864), RoundedCornerShape(3.dp))
                    .background(SkCard2, RoundedCornerShape(3.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (player.connected) SkNeonGreen else SkFgDim,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (player.id == "user") SkNeonViolet else SkFg
                                )
                                if (player.id == "user") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(YOU)",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = SkNeonViolet
                                    )
                                } else if (player.isBot) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "[AI CREW]",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = SkFgMuted
                                    )
                                }
                            }
                            Text(
                                text = "STATUS: ${if (player.connected) "ONLINE" else "DISCONNECTED"}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = SkFgMuted
                            )
                        }
                    }

                    Text(
                        text = "${player.xp} XP",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SkNeonCyan
                    )
                }
            }
        }
    }
}
