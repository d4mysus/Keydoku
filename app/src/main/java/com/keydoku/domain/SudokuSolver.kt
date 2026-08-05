package com.keydoku.domain

import kotlin.random.Random

class SudokuSolver {
    fun solve(board: SudokuBoard, random: Random? = null): SudokuBoard? {
        if (SudokuValidator.conflictIndexes(board).isNotEmpty()) return null
        val values = board.values()
        return if (fill(values, random)) SudokuBoard.fromValues(values) else null
    }

    fun countSolutions(board: SudokuBoard, limit: Int = 2): Int {
        if (SudokuValidator.conflictIndexes(board).isNotEmpty()) return 0
        val values = board.values()
        return count(values, limit, 0)
    }

    fun candidates(board: SudokuBoard, index: CellIndex): Set<Int> {
        if (board[index] != SudokuBoard.EMPTY) return emptySet()
        return (1..9).filterTo(mutableSetOf()) { value ->
            SudokuValidator.isMoveValid(board, index, value)
        }
    }

    private fun fill(values: IntArray, random: Random?): Boolean {
        val index = bestEmptyIndex(values) ?: return true
        val numbers = (1..9).toMutableList()
        if (random != null) numbers.shuffle(random)
        for (value in numbers) {
            if (isAllowed(values, index, value)) {
                values[index] = value
                if (fill(values, random)) return true
                values[index] = SudokuBoard.EMPTY
            }
        }
        return false
    }

    private fun count(values: IntArray, limit: Int, found: Int): Int {
        if (found >= limit) return found
        val index = bestEmptyIndex(values) ?: return found + 1
        var total = found
        for (value in 1..9) {
            if (isAllowed(values, index, value)) {
                values[index] = value
                total = count(values, limit, total)
                values[index] = SudokuBoard.EMPTY
                if (total >= limit) return total
            }
        }
        return total
    }

    private fun bestEmptyIndex(values: IntArray): Int? {
        var bestIndex: Int? = null
        var bestCount = Int.MAX_VALUE
        for (index in values.indices) {
            if (values[index] == SudokuBoard.EMPTY) {
                var count = 0
                for (value in 1..9) {
                    if (isAllowed(values, index, value)) count++
                }
                if (count == 0) return index
                if (count < bestCount) {
                    bestCount = count
                    bestIndex = index
                }
            }
        }
        return bestIndex
    }

    private fun isAllowed(values: IntArray, index: Int, value: Int): Boolean {
        val row = index / 9
        val column = index % 9
        for (cursor in 0..8) {
            if (values[row * 9 + cursor] == value) return false
            if (values[cursor * 9 + column] == value) return false
        }
        val startRow = (row / 3) * 3
        val startColumn = (column / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startColumn until startColumn + 3) {
                if (values[r * 9 + c] == value) return false
            }
        }
        return true
    }
}
