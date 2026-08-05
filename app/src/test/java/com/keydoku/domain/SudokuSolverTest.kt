package com.keydoku.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuSolverTest {
    private val puzzle = SudokuBoard.fromString(
        "530070000" +
            "600195000" +
            "098000060" +
            "800060003" +
            "400803001" +
            "700020006" +
            "060000280" +
            "000419005" +
            "000080079"
    )

    @Test
    fun solvesKnownPuzzle() {
        val solved = SudokuSolver().solve(puzzle)

        assertNotNull(solved)
        assertTrue(SudokuValidator.isSolved(solved!!))
        assertEquals(5, solved.valueAt(0, 0))
        assertEquals(9, solved.valueAt(8, 8))
    }

    @Test
    fun countsUniqueSolution() {
        assertEquals(1, SudokuSolver().countSolutions(puzzle, limit = 2))
    }
}
