package com.example.model

enum class GamePhase {
    LOBBY,
    STEP,
    REVEAL,
    DONE
}

data class Room(
    val code: String,
    val phase: GamePhase = GamePhase.LOBBY,
    val questId: String? = null,
    val stepIndex: Int = 0,
    val players: Map<String, Player> = emptyMap()
) {
    val playerList: List<Player>
        get() = players.values.toList()

    val livePlayers: List<Player>
        get() = players.values.filter { it.connected }

    val allLiveAnswered: Boolean
        get() = livePlayers.isNotEmpty() && livePlayers.all { it.answered }
}
