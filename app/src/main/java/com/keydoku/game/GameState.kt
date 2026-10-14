package com.keydoku.game

import com.keydoku.domain.CellIndex
import com.keydoku.domain.Difficulty
import com.keydoku.domain.SudokuBoard
import com.keydoku.domain.SudokuPuzzle
import com.keydoku.domain.SudokuValidator

data class GameState(
    val puzzle: SudokuBoard,
    val solution: SudokuBoard,
    val current: SudokuBoard,
    val difficulty: Difficulty,
    val selected: CellIndex = CellIndex(0, 0),
    val notes: List<Set<Int>> = List(SudokuBoard.CELL_COUNT) { emptySet() },
    val notesMode: Boolean = false,
    val elapsedMillis: Long = 0L,
    val paused: Boolean = false,
    val completed: Boolean = false,
    val settings: GameSettings = GameSettings(),
    val completionRecorded: Boolean = false,
) {
    val givenIndexes: Set<Int> = puzzle.givens()
    val conflictIndexes: Set<Int> = SudokuValidator.conflictIndexes(current)

    init {
        require(notes.size == SudokuBoard.CELL_COUNT) { "notes must have 81 entries" }
    }

    companion object {
        fun fromPuzzle(puzzle: SudokuPuzzle, settings: GameSettings = GameSettings()): GameState =
            GameState(
                puzzle = puzzle.puzzle,
                solution = puzzle.solution,
                current = puzzle.puzzle,
                difficulty = puzzle.difficulty,
                settings = settings,
            )
    }
}
