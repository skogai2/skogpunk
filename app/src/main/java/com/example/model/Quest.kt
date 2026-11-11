package com.example.model

data class ArenaModifiers(
    val enemyCount: Int = 4,
    val enemySpeedMultiplier: Float = 1.0f,
    val playerFireRateMultiplier: Float = 1.0f,
    val shieldHp: Int = 100,
    val gravityWell: Boolean = false,
    val ricochetShots: Boolean = false
)

data class Quest(
    val id: String,
    val title: String,
    val cabinet: Cabinet,
    val description: String,
    val steps: List<Step>,
    val author: String = "SKOGAI",
    val isCustomMod: Boolean = false,
    val arenaModifiers: ArenaModifiers = ArenaModifiers(),
    val rawScript: String = ""
) {
    val maxXp: Int
        get() = steps.sumOf { it.xp }

    val stepCount: Int
        get() = steps.size
}
