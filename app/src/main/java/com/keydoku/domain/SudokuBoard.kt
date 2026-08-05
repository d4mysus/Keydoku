package com.keydoku.domain

class SudokuBoard private constructor(private val cells: IntArray) {
    init {
        require(cells.size == CELL_COUNT) { "Sudoku board must contain 81 cells" }
        require(cells.all { it in 0..9 }) { "Cell values must be 0..9" }
    }

    operator fun get(index: CellIndex): Int = cells[index.index]

    fun valueAt(row: Int, column: Int): Int = cells[row * SIZE + column]

    fun withValue(index: CellIndex, value: Int): SudokuBoard {
        require(value in 0..9) { "value must be 0..9" }
        val next = cells.copyOf()
        next[index.index] = value
        return SudokuBoard(next)
    }

    fun clear(index: CellIndex): SudokuBoard = withValue(index, EMPTY)

    fun isFilled(): Boolean = cells.none { it == EMPTY }

    fun givens(): Set<Int> =
        cells.indices.filterTo(mutableSetOf()) { cells[it] != EMPTY }

    fun values(): IntArray = cells.copyOf()

    fun serialize(): String =
        cells.joinToString(separator = "") { it.toString() }

    override fun equals(other: Any?): Boolean =
        other is SudokuBoard && cells.contentEquals(other.cells)

    override fun hashCode(): Int = cells.contentHashCode()

    override fun toString(): String = serialize()

    companion object {
        const val SIZE = 9
        const val CELL_COUNT = SIZE * SIZE
        const val EMPTY = 0

        fun empty(): SudokuBoard = SudokuBoard(IntArray(CELL_COUNT))

        fun fromValues(values: IntArray): SudokuBoard = SudokuBoard(values.copyOf())

        fun fromString(serialized: String): SudokuBoard {
            require(serialized.length == CELL_COUNT) { "Serialized board must be 81 characters" }
            val values = IntArray(CELL_COUNT)
            serialized.forEachIndexed { index, char ->
                values[index] = when (char) {
                    '.', '0' -> EMPTY
                    in '1'..'9' -> char.digitToInt()
                    else -> error("Invalid board character '$char'")
                }
            }
            return SudokuBoard(values)
        }
    }
}
