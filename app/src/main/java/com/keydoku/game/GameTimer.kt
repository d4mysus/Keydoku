package com.keydoku.game

/** Accumulates play time using a monotonic clock, including time between ticks. */
class GameTimer(private val now: () -> Long) {
    private var startedAt = 0L
    private var elapsedAtStart = 0L

    fun start(elapsedMillis: Long) {
        startedAt = now()
        elapsedAtStart = elapsedMillis
    }

    fun elapsedMillis(): Long = elapsedAtStart + (now() - startedAt).coerceAtLeast(0L)
}
