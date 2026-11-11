package com.example.model

sealed interface Step {
    val prompt: String
    val xp: Int

    data class Choice(
        override val prompt: String,
        val choices: List<String>,
        val answerIndex: Int,
        override val xp: Int
    ) : Step

    data class Text(
        override val prompt: String,
        val regexPattern: String,
        val hint: String? = null,
        override val xp: Int
    ) : Step
}
