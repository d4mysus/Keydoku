package com.keydoku.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuGeneratorTest {
    @Test
    fun generatesValidUniquePuzzle() {
        val generator = SudokuGenerator()
        val puzzle = generator.generate(Difficulty.EASY, Random(7))

        assertTrue(SudokuValidator.isSolved(puzzle.solution))
        assertEquals(1, SudokuSolver().countSolutions(puzzle.puzzle, limit = 2))
        assertTrue(puzzle.puzzle.givens().size >= Difficulty.EASY.targetGivens)
    }
}
