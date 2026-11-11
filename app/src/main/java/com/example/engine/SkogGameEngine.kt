package com.example.engine

import com.example.model.GamePhase
import com.example.model.Player
import com.example.model.Quest
import com.example.model.Room
import com.example.model.Step
import kotlin.random.Random

object SkogGameEngine {

    fun check(step: Step, value: Any?): Boolean {
        if (value == null) return false
        return when (step) {
            is Step.Choice -> {
                val num = when (value) {
                    is Number -> value.toInt()
                    is String -> value.trim().toIntOrNull()
                    else -> null
                }
                num == step.answerIndex
            }
            is Step.Text -> {
                val text = value.toString().trim()
                try {
                    Regex(step.regexPattern, RegexOption.IGNORE_CASE).containsMatchIn(text)
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    fun calculateRank(xp: Int, maxXp: Int): String {
        if (maxXp <= 0) return "D"
        val ratio = xp.toDouble() / maxXp.toDouble()
        return when {
            ratio >= 1.0 -> "S+"
            ratio >= 0.75 -> "A"
            ratio >= 0.50 -> "B"
            ratio > 0.0 -> "C"
            else -> "D"
        }
    }

    fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        return (1..4)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    fun simulateBotAnswer(step: Step, botName: String): Any {
        val accuracy = when (botName) {
            "Skogix" -> 0.85
            "Kenji" -> 0.75
            "Honker" -> 0.60
            else -> 0.70
        }
        val isCorrect = Random.nextDouble() < accuracy
        return when (step) {
            is Step.Choice -> {
                if (isCorrect) step.answerIndex
                else {
                    val wrongOptions = (0 until step.choices.size).filter { it != step.answerIndex }
                    if (wrongOptions.isNotEmpty()) wrongOptions.random() else step.answerIndex
                }
            }
            is Step.Text -> {
                if (isCorrect) step.hint ?: "valid_cmd"
                else "sudo rm -rf /"
            }
        }
    }
}
