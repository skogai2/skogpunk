package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Cabinet
import com.example.model.Quest
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonGreen
import com.example.ui.theme.SkNeonYellow

@Composable
fun CabinetsScreen(
    allQuests: List<Quest>,
    solvedQuestIds: Set<String>,
    onQuestSelected: (Quest) -> Unit,
    onEditQuestScript: (Quest) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCabinet by remember { mutableStateOf<Cabinet?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ARCADE CABINETS & MODS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = SkFg
                )
                Text(
                    text = "ONE CHASSIS · FOUR ACCENTS · LIVE SQL QUEST REGISTRY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = SkFgMuted
                )
            }
        }

        items(Cabinet.entries) { cabinet ->
            val quests = allQuests.filter { it.cabinet == cabinet }
            val isExpanded = selectedCabinet == cabinet

            Card(
                colors = CardDefaults.cardColors(containerColor = SkCard),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isExpanded) cabinet.primaryColor else cabinet.borderColor.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .clickable {
                        selectedCabinet = if (isExpanded) null else cabinet
                    }
                    .testTag("cabinet_card_${cabinet.id}")
            ) {
                Column {
                    // Preview Scene Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    ) {
                        Image(
                            painter = painterResource(id = cabinet.drawableRes),
                            contentDescription = "${cabinet.title} banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x770B0716))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = cabinet.title,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SkFg
                            )
                            Text(
                                text = "▸ ${cabinet.subtitle}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = cabinet.accentColor
                            )
                        }
                    }

                    // Content & Quests
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${quests.size} CONTRACTS IN SQL",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SkFgMuted
                            )
                            val totalCabinetXp = quests.sumOf { it.maxXp }
                            Text(
                                text = "POOL: $totalCabinetXp XP",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SkNeonYellow
                            )
                        }

                        quests.forEach { quest ->
                            val isSolved = solvedQuestIds.contains(quest.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (quest.isCustomMod) SkNeonYellow.copy(alpha = 0.7f) else Color(0xFF38265E),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .background(SkCard2, RoundedCornerShape(3.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isSolved) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Cleared",
                                                    tint = SkNeonGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = (if (quest.isCustomMod) "★ " else "") + quest.title,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = when {
                                                    isSolved -> SkNeonGreen
                                                    quest.isCustomMod -> SkNeonYellow
                                                    else -> SkFg
                                                }
                                            )
                                        }
                                        Text(
                                            text = "${quest.stepCount} STEPS · ${quest.maxXp} XP · BY ${quest.author}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = SkFgMuted
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onEditQuestScript(quest) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .border(1.dp, SkNeonCyan.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                                .testTag("mod_quest_${quest.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Code,
                                                contentDescription = "Mod Script",
                                                tint = SkNeonCyan,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        Button(
                                            onClick = { onQuestSelected(quest) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = cabinet.primaryColor,
                                                contentColor = Color(0xFF1A0420)
                                            ),
                                            shape = RoundedCornerShape(3.dp),
                                            modifier = Modifier.testTag("launch_quest_${quest.id}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = if (isSolved) "REPLAY" else "START",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
