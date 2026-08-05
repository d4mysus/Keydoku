package com.keydoku.domain

object SudokuValidator {
    fun isSolved(board: SudokuBoard): Boolean =
        board.isFilled() && conflictIndexes(board).isEmpty()

    fun isMoveValid(board: SudokuBoard, index: CellIndex, value: Int): Boolean {
        if (value == SudokuBoard.EMPTY) return true
        if (value !in 1..9) return false
        for (column in 0..8) {
            if (column != index.column && board.valueAt(index.row, column) == value) return false
        }
        for (row in 0..8) {
            if (row != index.row && board.valueAt(row, index.column) == value) return false
        }
        val startRow = (index.row / 3) * 3
        val startColumn = (index.column / 3) * 3
        for (row in startRow until startRow + 3) {
            for (column in startColumn until startColumn + 3) {
                if ((row != index.row || column != index.column) && board.valueAt(row, column) == value) {
                    return false
                }
            }
        }
        return true
    }

    fun conflictIndexes(board: SudokuBoard): Set<Int> {
        val conflicts = mutableSetOf<Int>()
        val values = board.values()
        fun markUnit(indexes: List<Int>) {
            val seen = mutableMapOf<Int, MutableList<Int>>()
            indexes.forEach { index ->
                val value = values[index]
                if (value != SudokuBoard.EMPTY) {
                    seen.getOrPut(value) { mutableListOf() }.add(index)
                }
            }
            seen.values.filter { it.size > 1 }.forEach { conflicts.addAll(it) }
        }

        for (row in 0..8) {
            markUnit((0..8).map { column -> row * 9 + column })
        }
        for (column in 0..8) {
            markUnit((0..8).map { row -> row * 9 + column })
        }
        for (boxRow in 0..2) {
            for (boxColumn in 0..2) {
                val indexes = mutableListOf<Int>()
                for (row in boxRow * 3 until boxRow * 3 + 3) {
                    for (column in boxColumn * 3 until boxColumn * 3 + 3) {
                        indexes += row * 9 + column
                    }
                }
                markUnit(indexes)
            }
        }
        return conflicts
    }
}
