package com.keydoku.domain

import kotlin.random.Random

class SudokuGenerator(private val solver: SudokuSolver = SudokuSolver()) {
    fun generate(difficulty: Difficulty, random: Random = Random.Default): SudokuPuzzle {
        val solved = solver.solve(SudokuBoard.empty(), random)
            ?: error("Unable to generate solved Sudoku board")
        val puzzle = removeCells(solved, difficulty, random)
        return SudokuPuzzle(puzzle = puzzle, solution = solved, difficulty = difficulty)
    }

    private fun removeCells(solution: SudokuBoard, difficulty: Difficulty, random: Random): SudokuBoard {
        var puzzle = solution
        val indexes = (0 until SudokuBoard.CELL_COUNT).toMutableList().also { it.shuffle(random) }
        val targetGivens = difficulty.targetGivens

        for (index in indexes) {
            if (puzzle.givens().size <= targetGivens) break
            val cell = CellIndex.fromIndex(index)
            val candidate = puzzle.clear(cell)
            if (solver.countSolutions(candidate, limit = 2) == 1) {
                puzzle = candidate
            }
        }
        return puzzle
    }
}
