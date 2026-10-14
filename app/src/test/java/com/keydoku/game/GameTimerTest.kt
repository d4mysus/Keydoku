package com.keydoku.game

import org.junit.Assert.assertEquals
import org.junit.Test

class GameTimerTest {
    @Test
    fun rapidActionsPreserveTimeBetweenTicks() {
        var clock = 1_000L
        val timer = GameTimer { clock }
        timer.start(0L)
        repeat(10) {
            clock += 100L
            timer.start(timer.elapsedMillis())
        }
        assertEquals(1_000L, timer.elapsedMillis())
    }

    @Test
    fun restartingAfterBackgroundExcludesTimeAway() {
        var clock = 1_000L
        val timer = GameTimer { clock }
        timer.start(5_000L)
        clock += 250L
        val saved = timer.elapsedMillis()
        clock += 60_000L
        timer.start(saved)
        clock += 150L
        assertEquals(5_400L, timer.elapsedMillis())
    }
}
