package com.keydoku.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuBoardTest {
    @Test
    fun parsesAndSerializesBoard() {
        val raw = "530070000" +
            "600195000" +
            "098000060" +
            "800060003" +
            "400803001" +
            "700020006" +
            "060000280" +
            "000419005" +
            "000080079"

        val board = SudokuBoard.fromString(raw)

        assertEquals(5, board.valueAt(0, 0))
        assertEquals(0, board.valueAt(0, 2))
        assertEquals(raw, board.serialize())
    }

    @Test
    fun detectsConflicts() {
        val board = SudokuBoard.empty()
            .withValue(CellIndex(0, 0), 7)
            .withValue(CellIndex(0, 3), 7)

        assertTrue(SudokuValidator.conflictIndexes(board).containsAll(listOf(0, 3)))
    }
}
