package com.keydoku.game

import com.keydoku.domain.Difficulty

data class DifficultyStats(
    val gamesStarted: Int = 0,
    val gamesCompleted: Int = 0,
    val bestTimeMillis: Long? = null,
    val totalCompletedTimeMillis: Long = 0L,
) {
    val averageTimeMillis: Long?
        get() = if (gamesCompleted == 0) null else totalCompletedTimeMillis / gamesCompleted

    fun started(): DifficultyStats = copy(gamesStarted = gamesStarted + 1)

    fun completed(elapsedMillis: Long): DifficultyStats =
        copy(
            gamesCompleted = gamesCompleted + 1,
            bestTimeMillis = bestTimeMillis?.let { minOf(it, elapsedMillis) } ?: elapsedMillis,
            totalCompletedTimeMillis = totalCompletedTimeMillis + elapsedMillis,
        )
}

data class GameStats(
    val byDifficulty: Map<Difficulty, DifficultyStats> =
        Difficulty.entries.associateWith { DifficultyStats() },
) {
    fun started(difficulty: Difficulty): GameStats =
        update(difficulty) { it.started() }

    fun completed(difficulty: Difficulty, elapsedMillis: Long): GameStats =
        update(difficulty) { it.completed(elapsedMillis) }

    private fun update(
        difficulty: Difficulty,
        transform: (DifficultyStats) -> DifficultyStats,
    ): GameStats {
        val next = byDifficulty.toMutableMap()
        next[difficulty] = transform(next[difficulty] ?: DifficultyStats())
        return copy(byDifficulty = next)
    }
}
