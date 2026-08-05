package com.keydoku.domain

data class CellIndex(val row: Int, val column: Int) {
    init {
        require(row in 0..8) { "row must be 0..8" }
        require(column in 0..8) { "column must be 0..8" }
    }

    val index: Int = row * 9 + column

    fun move(deltaRow: Int, deltaColumn: Int): CellIndex {
        val nextRow = (row + deltaRow).coerceIn(0, 8)
        val nextColumn = (column + deltaColumn).coerceIn(0, 8)
        return CellIndex(nextRow, nextColumn)
    }

    companion object {
        fun fromIndex(index: Int): CellIndex {
            require(index in 0 until 81) { "index must be 0..80" }
            return CellIndex(index / 9, index % 9)
        }
    }
}
