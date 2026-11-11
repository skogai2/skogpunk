package com.example.model

data class LogEntry(
    val timestamp: String,
    val type: String,
    val description: String,
    val xpDelta: Int = 0
)

data class Profile(
    val handle: String,
    val lifetimeXp: Int = 0,
    val solvedQuestIds: Set<String> = emptySet(),
    val contractsCompleted: Int = 0,
    val eventLogs: List<LogEntry> = emptyList()
) {
    val rank: String
        get() = when {
            lifetimeXp >= 2500 -> "S+"
            lifetimeXp >= 1500 -> "A"
            lifetimeXp >= 800 -> "B"
            lifetimeXp >= 300 -> "C"
            else -> "D"
        }
}
