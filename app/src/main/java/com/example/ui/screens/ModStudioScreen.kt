package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventLogEntity
import com.example.data.local.ModMutatorEntity
import com.example.data.local.QuestEntity
import com.example.engine.ScriptParseResult
import com.example.engine.SkogScriptParser
import com.example.model.Quest
import com.example.ui.theme.SkCard
import com.example.ui.theme.SkCard2
import com.example.ui.theme.SkFg
import com.example.ui.theme.SkFg2
import com.example.ui.theme.SkFgDim
import com.example.ui.theme.SkFgMuted
import com.example.ui.theme.SkNeonAcid
import com.example.ui.theme.SkNeonCyan
import com.example.ui.theme.SkNeonGreen
import com.example.ui.theme.SkNeonMagenta
import com.example.ui.theme.SkNeonRed
import com.example.ui.theme.SkNeonViolet
import com.example.ui.theme.SkNeonYellow

private enum class StudioSubTab(val label: String) {
    SCRIPT_IDE("SKOGSCRIPT IDE"),
    MUTATORS("MUTATORS"),
    SQL_EXPLORER("SQL TABLES")
}

private enum class SqlTableChoice(val tableName: String, val queryLabel: String) {
    QUESTS("quests", "SELECT * FROM quests ORDER BY updatedAt DESC;"),
    MUTATORS("mod_mutators", "SELECT * FROM mod_mutators ORDER BY id ASC;"),
    EVENTS("events_log", "SELECT * FROM events_log ORDER BY id DESC LIMIT 100;")
}

@Composable
fun ModStudioScreen(
    quests: List<Quest>,
    questEntities: List<QuestEntity>,
    mutators: List<ModMutatorEntity>,
    eventLogs: List<EventLogEntity>,
    activeQuest: Quest,
    onSaveAndPlayQuest: (Quest) -> Unit,
    onSaveQuestOnly: (Quest) -> Unit,
    onDeleteQuest: (String) -> Unit,
    onToggleMutator: (String, Boolean) -> Unit,
    onResetDatabase: () -> Unit,
    onOpenAiModArchitect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(StudioSubTab.SCRIPT_IDE) }
    var editorText by remember(activeQuest.id) {
        mutableStateOf(SkogScriptParser.toScript(activeQuest))
    }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    val parseResult = remember(editorText) {
        SkogScriptParser.parse(editorText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + SubTab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MOD & SQL SCRIPT STUDIO",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SkFg
                )
                Text(
                    text = "LIVE SKOGSCRIPT DSL · ROOM SQLITE PERSISTENCE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = SkNeonCyan
                )
            }

            OutlinedButton(
                onClick = onOpenAiModArchitect,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("ask_ai_architect_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Architect",
                    tint = SkNeonMagenta,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI GEN",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = SkNeonMagenta
                )
            }
        }

        // Sub-navigation pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StudioSubTab.entries.forEach { tab ->
                val selected = currentSubTab == tab
                val icon = when (tab) {
                    StudioSubTab.SCRIPT_IDE -> Icons.Default.Code
                    StudioSubTab.MUTATORS -> Icons.Default.Tune
                    StudioSubTab.SQL_EXPLORER -> Icons.Default.Storage
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selected) SkNeonCyan else Color(0xFF3A2860),
                            RoundedCornerShape(4.dp)
                        )
                        .background(
                            if (selected) SkCard2 else SkCard,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { currentSubTab = tab }
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .testTag("studio_subtab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = if (selected) SkNeonCyan else SkFgMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.label,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (selected) SkFg else SkFgMuted
                        )
                    }
                }
            }
        }

        when (currentSubTab) {
            StudioSubTab.SCRIPT_IDE -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Select existing quest to mod or create new
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "LOAD QUEST FROM SQL TO EDIT:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = SkFgMuted
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // "+ NEW MOD" chip
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, SkNeonGreen, RoundedCornerShape(3.dp))
                                        .background(Color(0xFF102A1E), RoundedCornerShape(3.dp))
                                        .clickable {
                                            val newId = "mod-${System.currentTimeMillis() % 1000}"
                                            editorText = """
                                                # SKOGSCRIPT v1.0 — Custom Cyber Contract
                                                @quest id="$newId" title="Operation Nightfall" cabinet="honkers-hackers" author="MODDER"
                                                @desc Breach the rooftop relay and reroute encrypted packets.
                                                @arena enemies=5 speed=1.25 fire_rate=1.6 hp=120 ricochet=true

                                                @step choice xp=150
                                                prompt: Which command displays active network connections and listening sockets?
                                                choice: ss -tulpn
                                                choice: rm -rf /tmp
                                                choice: echo 1 > /proc/sys
                                                choice: mkdir -p /var/net
                                                answer: 0

                                                @step text xp=250
                                                prompt: Type the command to search for the word 'error' inside syslog.
                                                match: ^grep\s+(-i\s+)?['"]?error['"]?\s+(/var/log/)?syslog$
                                                hint: grep error syslog
                                            """.trimIndent()
                                            statusBanner = "Loaded fresh @quest template ($newId)"
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("new_mod_template_chip")
                                ) {
                                    Text(
                                        text = "+ NEW MOD",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = SkNeonGreen
                                    )
                                }

                                quests.forEach { q ->
                                    Box(
                                        modifier = Modifier
                                            .border(
                                                1.dp,
                                                if (q.isCustomMod) SkNeonYellow else Color(0xFF4A3580),
                                                RoundedCornerShape(3.dp)
                                            )
                                            .background(SkCard, RoundedCornerShape(3.dp))
                                            .clickable {
                                                editorText = SkogScriptParser.toScript(q)
                                                statusBanner = "Loaded '${q.title}' into script editor"
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (q.isCustomMod) "★ ${q.id}" else q.id,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = if (q.isCustomMod) SkNeonYellow else SkFg2
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Quick Snippet Injection Toolbar
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SnippetButton(
                                label = "+ CHOICE STEP",
                                color = SkNeonCyan,
                                onClick = {
                                    editorText = editorText.trimEnd() + "\n\n" + """
                                        @step choice xp=150
                                        prompt: Which flag makes 'ls' show hidden dotfiles?
                                        choice: ls -a
                                        choice: ls -l
                                        choice: ls -h
                                        choice: ls -R
                                        answer: 0
                                    """.trimIndent()
                                }
                            )
                            SnippetButton(
                                label = "+ TERMINAL STEP",
                                color = SkNeonViolet,
                                onClick = {
                                    editorText = editorText.trimEnd() + "\n\n" + """
                                        @step text xp=200
                                        prompt: Type the command to print the current working directory.
                                        match: ^pwd$
                                        hint: pwd
                                    """.trimIndent()
                                }
                            )
                            SnippetButton(
                                label = "⇄ SKOG / JSON",
                                color = SkNeonYellow,
                                onClick = {
                                    if ( parseResult is ScriptParseResult.Success ) {
                                        val isCurrentlyJson = editorText.trim().startsWith("{")
                                        editorText = if (isCurrentlyJson) {
                                            SkogScriptParser.toScript(parseResult.quest)
                                        } else {
                                            SkogScriptParser.toJson(parseResult.quest)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // 3. Live Compiler / Syntax Status Bar
                    item {
                        when (parseResult) {
                            is ScriptParseResult.Success -> {
                                val q = parseResult.quest
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, SkNeonGreen.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .background(Color(0xFF0E291C), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Valid",
                                            tint = SkNeonGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "VALID · ${q.stepCount} STEPS · ${q.maxXp} XP · ${q.cabinet.id.uppercase()}",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = SkNeonGreen
                                        )
                                    }
                                    Text(
                                        text = "${q.arenaModifiers.enemyCount} ICE",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = SkNeonCyan
                                    )
                                }
                            }
                            is ScriptParseResult.Error -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, SkNeonRed, RoundedCornerShape(4.dp))
                                        .background(Color(0xFF32101A), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Syntax Error",
                                        tint = SkNeonRed,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${parseResult.line?.let { "LINE $it: " } ?: ""}${parseResult.message}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = SkNeonRed
                                    )
                                }
                            }
                        }
                    }

                    // 4. Code Editor TextField
                    item {
                        OutlinedTextField(
                            value = editorText,
                            onValueChange = { editorText = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = SkFg
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkNeonCyan,
                                unfocusedBorderColor = Color(0xFF4A3580),
                                focusedContainerColor = Color(0xFF0A0614),
                                unfocusedContainerColor = Color(0xFF0A0614)
                            ),
                            minLines = 12,
                            maxLines = 22,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("skogscript_code_editor")
                        )
                    }

                    if (statusBanner != null) {
                        item {
                            Text(
                                text = "▸ $statusBanner",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SkNeonYellow
                            )
                        }
                    }

                    // 5. Action Buttons: Save to SQL & Play Immediately
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (parseResult is ScriptParseResult.Success) {
                                        val modded = parseResult.quest.copy(isCustomMod = true)
                                        statusBanner = "Saved '${modded.title}' to SQL & launching!"
                                        onSaveAndPlayQuest(modded)
                                    }
                                },
                                enabled = parseResult is ScriptParseResult.Success,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SkNeonGreen,
                                    contentColor = Color(0xFF05180E)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("save_and_play_mod_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SAVE TO SQL & PLAY",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (parseResult is ScriptParseResult.Success) {
                                        val modded = parseResult.quest.copy(isCustomMod = true)
                                        onSaveQuestOnly(modded)
                                        statusBanner = "Upserted '${modded.id}' into SQLite table `quests`."
                                    }
                                },
                                enabled = parseResult is ScriptParseResult.Success,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_mod_sql_only_button")
                            ) {
                                Text(
                                    text = "COMMIT SQL",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SkNeonCyan
                                )
                            }
                        }
                    }
                }
            }

            StudioSubTab.MUTATORS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "GLOBAL ENGINE MUTATOR HOOKS (STORED IN SQL `mod_mutators`):",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SkFgMuted
                        )
                    }

                    items(mutators, key = { it.id }) { mutator ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (mutator.isEnabled) SkCard2 else SkCard
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (mutator.isEnabled) SkNeonAcid else Color(0xFF3E2B68),
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mutator.title,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (mutator.isEnabled) SkNeonAcid else SkFg
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "[${mutator.category}]",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = SkNeonCyan
                                        )
                                    }
                                    Text(
                                        text = mutator.description,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = SkFg2
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF0D081B), RoundedCornerShape(2.dp))
                                            .border(1.dp, Color(0xFF2B1D4E), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "hook: ${mutator.scriptHook}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = SkNeonYellow
                                        )
                                    }
                                }

                                Switch(
                                    checked = mutator.isEnabled,
                                    onCheckedChange = { checked ->
                                        onToggleMutator(mutator.id, checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SkNeonAcid,
                                        checkedTrackColor = Color(0xFF153D29),
                                        uncheckedThumbColor = SkFgDim,
                                        uncheckedTrackColor = SkCard
                                    )
                                )
                            }
                        }
                    }
                }
            }

            StudioSubTab.SQL_EXPLORER -> {
                SqlDatabaseExplorerView(
                    questEntities = questEntities,
                    mutators = mutators,
                    eventLogs = eventLogs,
                    onLoadQuestIntoIde = { entity ->
                        editorText = entity.scriptSource
                        currentSubTab = StudioSubTab.SCRIPT_IDE
                    },
                    onDeleteQuest = onDeleteQuest,
                    onResetDatabase = onResetDatabase
                )
            }
        }
    }
}

@Composable
private fun SnippetButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
            .background(Color(0xFF160E2B), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = color
        )
    }
}

@Composable
private fun SqlDatabaseExplorerView(
    questEntities: List<QuestEntity>,
    mutators: List<ModMutatorEntity>,
    eventLogs: List<EventLogEntity>,
    onLoadQuestIntoIde: (QuestEntity) -> Unit,
    onDeleteQuest: (String) -> Unit,
    onResetDatabase: () -> Unit
) {
    var selectedTable by remember { mutableStateOf(SqlTableChoice.QUESTS) }
    var searchFilter by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Table Selector & Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SqlTableChoice.entries.forEach { tbl ->
                val active = selectedTable == tbl
                val count = when (tbl) {
                    SqlTableChoice.QUESTS -> questEntities.size
                    SqlTableChoice.MUTATORS -> mutators.size
                    SqlTableChoice.EVENTS -> eventLogs.size
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (active) SkNeonYellow else Color(0xFF3A2860), RoundedCornerShape(3.dp))
                        .background(if (active) Color(0xFF281D46) else SkCard, RoundedCornerShape(3.dp))
                        .clickable { selectedTable = tbl }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${tbl.tableName} ($count)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (active) SkNeonYellow else SkFg2
                    )
                }
            }

            IconButton(
                onClick = onResetDatabase,
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, SkNeonCyan, RoundedCornerShape(3.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset SQL Seed",
                    tint = SkNeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Active SQL Query Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0614), RoundedCornerShape(3.dp))
                .border(1.dp, Color(0xFF332354), RoundedCornerShape(3.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "sql> ${selectedTable.queryLabel}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = SkNeonGreen
            )
        }

        // Filter Input
        OutlinedTextField(
            value = searchFilter,
            onValueChange = { searchFilter = it },
            placeholder = {
                Text(
                    text = "WHERE column LIKE '%filter%'...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SkFgDim
                )
            },
            singleLine = true,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = SkFg),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SkNeonYellow,
                unfocusedBorderColor = Color(0xFF3B2961),
                focusedContainerColor = Color(0xFF120B24),
                unfocusedContainerColor = Color(0xFF120B24)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Rows List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (selectedTable) {
                SqlTableChoice.QUESTS -> {
                    val filtered = questEntities.filter {
                        searchFilter.isBlank() ||
                            it.id.contains(searchFilter, ignoreCase = true) ||
                            it.title.contains(searchFilter, ignoreCase = true) ||
                            it.cabinetId.contains(searchFilter, ignoreCase = true)
                    }
                    items(filtered, key = { it.id }) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF38265E), RoundedCornerShape(3.dp))
                                .background(SkCard, RoundedCornerShape(3.dp))
                                .clickable { onLoadQuestIntoIde(row) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PK: ${row.id} | ${row.title}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (row.isCustomMod) SkNeonYellow else SkFg
                                )
                                Text(
                                    text = "cabinet=${row.cabinetId} | steps=${row.stepCount} | xp=${row.maxXp} | ice=${row.enemyCount}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = SkFgMuted
                                )
                            }
                            if (questEntities.size > 1) {
                                IconButton(
                                    onClick = { onDeleteQuest(row.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Row",
                                        tint = SkNeonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                SqlTableChoice.MUTATORS -> {
                    val filtered = mutators.filter {
                        searchFilter.isBlank() ||
                            it.id.contains(searchFilter, ignoreCase = true) ||
                            it.title.contains(searchFilter, ignoreCase = true)
                    }
                    items(filtered, key = { it.id }) { row ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF38265E), RoundedCornerShape(3.dp))
                                .background(SkCard, RoundedCornerShape(3.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "PK: ${row.id} | enabled=${row.isEnabled}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (row.isEnabled) SkNeonGreen else SkFg
                            )
                            Text(
                                text = "hook='${row.scriptHook}' | xp_mult=${row.xpMultiplier}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = SkNeonCyan
                            )
                        }
                    }
                }

                SqlTableChoice.EVENTS -> {
                    val filtered = eventLogs.filter {
                        searchFilter.isBlank() ||
                            it.type.contains(searchFilter, ignoreCase = true) ||
                            it.description.contains(searchFilter, ignoreCase = true)
                    }
                    items(filtered, key = { it.id }) { row ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF2B1D4E), RoundedCornerShape(3.dp))
                                .background(Color(0xFF0E091C), RoundedCornerShape(3.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "#${row.id} [${row.timestamp}] type=${row.type} room=${row.roomCode} xp=+${row.xpDelta}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = SkNeonCyan
                            )
                            Text(
                                text = row.description,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SkFg2
                            )
                        }
                    }
                }
            }
        }
    }
}
