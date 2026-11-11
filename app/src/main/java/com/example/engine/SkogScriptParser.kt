package com.example.engine

import com.example.model.ArenaModifiers
import com.example.model.Cabinet
import com.example.model.Quest
import com.example.model.Step
import org.json.JSONArray
import org.json.JSONObject

sealed interface ScriptParseResult {
    data class Success(val quest: Quest) : ScriptParseResult
    data class Error(val message: String, val line: Int? = null) : ScriptParseResult
}

object SkogScriptParser {

    /**
     * Parses either a SkogScript (.skog) DSL string or a JSON quest object into a playable [Quest].
     */
    fun parse(rawInput: String, defaultAuthor: String = "MODDER"): ScriptParseResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return ScriptParseResult.Error("Script is empty. Add a @quest header and at least one @step.")
        }

        // If user pasted JSON format (e.g. quests/first-boot.json), parse as JSON automatically
        if (trimmed.startsWith("{")) {
            return parseJsonQuest(trimmed, defaultAuthor)
        }

        return parseDslQuest(trimmed, defaultAuthor)
    }

    private fun parseDslQuest(input: String, defaultAuthor: String): ScriptParseResult {
        var questId = "custom-${System.currentTimeMillis() % 10000}"
        var title = "Custom Cyber Contract"
        var cabinet = Cabinet.SUDO_HERO
        var description = "Custom scripted contract loaded from SkogScript DSL."
        var author = defaultAuthor
        var arena = ArenaModifiers()

        val steps = mutableListOf<Step>()

        // Current step accumulator
        var currentStepKind: String? = null
        var currentStepXp = 150
        var currentPrompt = ""
        val currentChoices = mutableListOf<String>()
        var currentAnswerIndex = 0
        var currentMatch = ".*"
        var currentHint: String? = null

        fun flushStep(): String? {
            val kind = currentStepKind ?: return null
            if (currentPrompt.isBlank()) {
                return "Step ${steps.size + 1} is missing a 'prompt:' line."
            }
            when (kind) {
                "choice" -> {
                    if (currentChoices.size < 2) {
                        return "Choice step '${currentPrompt.take(24)}...' needs at least 2 'choice:' lines."
                    }
                    val safeAns = currentAnswerIndex.coerceIn(0, currentChoices.size - 1)
                    steps.add(
                        Step.Choice(
                            prompt = currentPrompt,
                            choices = currentChoices.toList(),
                            answerIndex = safeAns,
                            xp = currentStepXp.coerceAtLeast(10)
                        )
                    )
                }
                "text" -> {
                    steps.add(
                        Step.Text(
                            prompt = currentPrompt,
                            regexPattern = currentMatch.ifBlank { ".*" },
                            hint = currentHint,
                            xp = currentStepXp.coerceAtLeast(10)
                        )
                    )
                }
            }
            currentStepKind = null
            currentStepXp = 150
            currentPrompt = ""
            currentChoices.clear()
            currentAnswerIndex = 0
            currentMatch = ".*"
            currentHint = null
            return null
        }

        val lines = input.lines()
        for ((index, rawLine) in lines.withIndex()) {
            val lineNum = index + 1
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

            when {
                line.startsWith("@quest", ignoreCase = true) -> {
                    val attrs = parseAttributes(line.removePrefix("@quest").removePrefix("@QUEST").trim())
                    attrs["id"]?.let { if (it.isNotBlank()) questId = it.lowercase().replace(" ", "-") }
                    attrs["title"]?.let { if (it.isNotBlank()) title = it }
                    attrs["cabinet"]?.let { cabinet = Cabinet.fromId(it) }
                    attrs["author"]?.let { if (it.isNotBlank()) author = it }
                }

                line.startsWith("@desc", ignoreCase = true) -> {
                    description = line.replaceFirst(Regex("^@desc\\s*:?\\s*", RegexOption.IGNORE_CASE), "").trim()
                }

                line.startsWith("@arena", ignoreCase = true) -> {
                    val attrs = parseAttributes(line.removePrefix("@arena").removePrefix("@ARENA").trim())
                    val enemies = attrs["enemies"]?.toIntOrNull() ?: attrs["enemy_count"]?.toIntOrNull() ?: arena.enemyCount
                    val speed = attrs["speed"]?.toFloatOrNull() ?: attrs["enemy_speed"]?.toFloatOrNull() ?: arena.enemySpeedMultiplier
                    val fireRate = attrs["fire_rate"]?.toFloatOrNull() ?: attrs["player_fire_rate"]?.toFloatOrNull() ?: arena.playerFireRateMultiplier
                    val hp = attrs["hp"]?.toIntOrNull() ?: attrs["shield_hp"]?.toIntOrNull() ?: arena.shieldHp
                    val gravity = attrs["gravity"]?.toBooleanStrictOrNull() ?: attrs["gravity_well"]?.toBooleanStrictOrNull() ?: arena.gravityWell
                    val ricochet = attrs["ricochet"]?.toBooleanStrictOrNull() ?: arena.ricochetShots

                    arena = ArenaModifiers(
                        enemyCount = enemies.coerceIn(1, 16),
                        enemySpeedMultiplier = speed.coerceIn(0.4f, 3.0f),
                        playerFireRateMultiplier = fireRate.coerceIn(0.5f, 4.0f),
                        shieldHp = hp.coerceIn(25, 500),
                        gravityWell = gravity,
                        ricochetShots = ricochet
                    )
                }

                line.startsWith("@step", ignoreCase = true) -> {
                    val err = flushStep()
                    if (err != null) return ScriptParseResult.Error(err, lineNum)

                    val rest = line.replaceFirst(Regex("^@step\\s+", RegexOption.IGNORE_CASE), "").trim()
                    val tokens = rest.split(Regex("\\s+"))
                    val kindToken = tokens.firstOrNull()?.lowercase() ?: "choice"
                    currentStepKind = if (kindToken == "text" || kindToken == "cmd" || kindToken == "terminal") "text" else "choice"
                    val attrs = parseAttributes(rest)
                    currentStepXp = attrs["xp"]?.toIntOrNull() ?: 150
                }

                line.startsWith("prompt:", ignoreCase = true) -> {
                    currentPrompt = line.substringAfter(":").trim()
                }

                line.startsWith("choice:", ignoreCase = true) -> {
                    val c = line.substringAfter(":").trim()
                    if (c.isNotEmpty()) currentChoices.add(c)
                }

                line.startsWith("answer:", ignoreCase = true) -> {
                    val ansStr = line.substringAfter(":").trim()
                    currentAnswerIndex = ansStr.toIntOrNull() ?: 0
                }

                line.startsWith("match:", ignoreCase = true) -> {
                    currentMatch = line.substringAfter(":").trim()
                }

                line.startsWith("hint:", ignoreCase = true) -> {
                    currentHint = line.substringAfter(":").trim().ifEmpty { null }
                }
            }
        }

        val finalErr = flushStep()
        if (finalErr != null) return ScriptParseResult.Error(finalErr, lines.size)

        if (steps.isEmpty()) {
            return ScriptParseResult.Error("No @step blocks found. Add at least one '@step choice' or '@step text' block.")
        }

        val quest = Quest(
            id = questId,
            title = title,
            cabinet = cabinet,
            description = description,
            steps = steps,
            author = author,
            isCustomMod = true,
            arenaModifiers = arena,
            rawScript = input
        )
        return ScriptParseResult.Success(quest)
    }

    private fun parseJsonQuest(jsonStr: String, defaultAuthor: String): ScriptParseResult {
        return try {
            val obj = JSONObject(jsonStr)
            val id = obj.optString("id", "json-mod-${System.currentTimeMillis() % 1000}")
            val title = obj.optString("title", "Imported JSON Contract")
            val cabinet = Cabinet.fromId(obj.optString("cabinet", "sudo-hero"))
            val desc = obj.optString("description", "Imported from JSON quest specification.")
            val author = obj.optString("author", defaultAuthor)

            val arenaObj = obj.optJSONObject("arena")
            val arena = if (arenaObj != null) {
                ArenaModifiers(
                    enemyCount = arenaObj.optInt("enemies", 4).coerceIn(1, 16),
                    enemySpeedMultiplier = arenaObj.optDouble("speed", 1.0).toFloat().coerceIn(0.4f, 3.0f),
                    playerFireRateMultiplier = arenaObj.optDouble("fire_rate", 1.0).toFloat().coerceIn(0.5f, 4.0f),
                    shieldHp = arenaObj.optInt("hp", 100).coerceIn(25, 500),
                    gravityWell = arenaObj.optBoolean("gravity", false),
                    ricochetShots = arenaObj.optBoolean("ricochet", false)
                )
            } else {
                ArenaModifiers()
            }

            val stepsArr = obj.optJSONArray("steps") ?: JSONArray()
            val steps = mutableListOf<Step>()
            for (i in 0 until stepsArr.length()) {
                val s = stepsArr.getJSONObject(i)
                val kind = s.optString("kind", "choice")
                val prompt = s.optString("prompt", "Solve contract step ${i + 1}")
                val xp = s.optInt("xp", 150)
                if (kind == "text") {
                    steps.add(
                        Step.Text(
                            prompt = prompt,
                            regexPattern = s.optString("match", ".*"),
                            hint = s.optString("hint").takeIf { it.isNotBlank() },
                            xp = xp
                        )
                    )
                } else {
                    val choicesArr = s.optJSONArray("choices") ?: JSONArray()
                    val choices = (0 until choicesArr.length()).map { choicesArr.getString(it) }
                    steps.add(
                        Step.Choice(
                            prompt = prompt,
                            choices = if (choices.size >= 2) choices else listOf("Option A", "Option B"),
                            answerIndex = s.optInt("answer", 0),
                            xp = xp
                        )
                    )
                }
            }

            if (steps.isEmpty()) {
                return ScriptParseResult.Error("JSON quest contains 0 steps in 'steps' array.")
            }

            val quest = Quest(
                id = id,
                title = title,
                cabinet = cabinet,
                description = desc,
                steps = steps,
                author = author,
                isCustomMod = true,
                arenaModifiers = arena
            )
            ScriptParseResult.Success(quest.copy(rawScript = toScript(quest)))
        } catch (e: Exception) {
            ScriptParseResult.Error("Invalid JSON syntax: ${e.localizedMessage ?: "check brackets and quotes"}")
        }
    }

    /**
     * Converts any [Quest] into clean, human-editable SkogScript DSL.
     */
    fun toScript(quest: Quest): String {
        if (quest.rawScript.isNotBlank() && quest.rawScript.trim().startsWith("@quest")) {
            return quest.rawScript
        }
        val sb = StringBuilder()
        sb.appendLine("# SKOGSCRIPT v1.0 — Live Quest & Cyber-Arena Mod")
        sb.appendLine("@quest id=\"${quest.id}\" title=\"${quest.title}\" cabinet=\"${quest.cabinet.id}\" author=\"${quest.author}\"")
        sb.appendLine("@desc ${quest.description}")
        val a = quest.arenaModifiers
        sb.appendLine("@arena enemies=${a.enemyCount} speed=${a.enemySpeedMultiplier} fire_rate=${a.playerFireRateMultiplier} hp=${a.shieldHp} gravity=${a.gravityWell} ricochet=${a.ricochetShots}")
        sb.appendLine()

        quest.steps.forEachIndexed { idx, step ->
            when (step) {
                is Step.Choice -> {
                    sb.appendLine("# --- Contract Step ${idx + 1} (Multiple Choice) ---")
                    sb.appendLine("@step choice xp=${step.xp}")
                    sb.appendLine("prompt: ${step.prompt}")
                    step.choices.forEach { c ->
                        sb.appendLine("choice: $c")
                    }
                    sb.appendLine("answer: ${step.answerIndex}")
                    sb.appendLine()
                }
                is Step.Text -> {
                    sb.appendLine("# --- Contract Step ${idx + 1} (Terminal Command) ---")
                    sb.appendLine("@step text xp=${step.xp}")
                    sb.appendLine("prompt: ${step.prompt}")
                    sb.appendLine("match: ${step.regexPattern}")
                    if (!step.hint.isNullOrBlank()) {
                        sb.appendLine("hint: ${step.hint}")
                    }
                    sb.appendLine()
                }
            }
        }
        return sb.toString().trimEnd()
    }

    /**
     * Converts any [Quest] into standard JSON format.
     */
    fun toJson(quest: Quest): String {
        val root = JSONObject()
        root.put("id", quest.id)
        root.put("title", quest.title)
        root.put("cabinet", quest.cabinet.id)
        root.put("description", quest.description)
        root.put("author", quest.author)

        val arenaObj = JSONObject()
        arenaObj.put("enemies", quest.arenaModifiers.enemyCount)
        arenaObj.put("speed", quest.arenaModifiers.enemySpeedMultiplier.toDouble())
        arenaObj.put("fire_rate", quest.arenaModifiers.playerFireRateMultiplier.toDouble())
        arenaObj.put("hp", quest.arenaModifiers.shieldHp)
        arenaObj.put("gravity", quest.arenaModifiers.gravityWell)
        arenaObj.put("ricochet", quest.arenaModifiers.ricochetShots)
        root.put("arena", arenaObj)

        val stepsArr = JSONArray()
        quest.steps.forEach { step ->
            val sObj = JSONObject()
            when (step) {
                is Step.Choice -> {
                    sObj.put("kind", "choice")
                    sObj.put("prompt", step.prompt)
                    sObj.put("choices", JSONArray(step.choices))
                    sObj.put("answer", step.answerIndex)
                    sObj.put("xp", step.xp)
                }
                is Step.Text -> {
                    sObj.put("kind", "text")
                    sObj.put("prompt", step.prompt)
                    sObj.put("match", step.regexPattern)
                    if (step.hint != null) sObj.put("hint", step.hint)
                    sObj.put("xp", step.xp)
                }
            }
            stepsArr.put(sObj)
        }
        root.put("steps", stepsArr)
        return root.toString(2)
    }

    /**
     * Extracts a `@quest ...` block from a markdown/chat message if present.
     */
    fun extractScriptFromText(messageText: String): String? {
        val fencedRegex = Regex("```(?:skog|skogscript|txt|bash)?\\s*([\\s\\S]*?@quest[\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val fencedMatch = fencedRegex.find(messageText)
        if (fencedMatch != null) {
            return fencedMatch.groupValues[1].trim()
        }
        if (messageText.contains("@quest", ignoreCase = true) && messageText.contains("@step", ignoreCase = true)) {
            val startIdx = messageText.indexOf("@quest", ignoreCase = true)
            return messageText.substring(startIdx).trim()
        }
        return null
    }

    private fun parseAttributes(input: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = Regex("""(\w+)\s*=\s*(?:"([^"]*)"|'([^']*)'|(\S+))""")
        regex.findAll(input).forEach { match ->
            val key = match.groupValues[1].lowercase()
            val value = match.groupValues[2].ifEmpty {
                match.groupValues[3].ifEmpty { match.groupValues[4] }
            }
            result[key] = value
        }
        return result
    }
}
