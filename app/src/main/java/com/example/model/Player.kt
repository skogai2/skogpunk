package com.example.model

data class Player(
    val id: String,
    val name: String,
    val connected: Boolean = true,
    val answer: Any? = null,
    val xp: Int = 0,
    val isBot: Boolean = false,
    val isHost: Boolean = false
) {
    val answered: Boolean
        get() = answer != null
}
