package com.keydoku.game

import com.keydoku.domain.CellIndex
import com.keydoku.domain.Difficulty
import com.keydoku.domain.SudokuBoard
import com.keydoku.domain.SudokuGenerator
import kotlin.random.Random

class GameController(
    private val generator: SudokuGenerator = SudokuGenerator(),
    private val random: Random = Random.Default,
) {
    private val undoStack = ArrayDeque<GameState>()
    private val redoStack = ArrayDeque<GameState>()

    var state: GameState = GameState.fromPuzzle(generator.generate(Difficulty.EASY, random))
        private set

    fun restore(restored: GameState) {
        undoStack.clear()
        redoStack.clear()
        state = restored
    }

    fun newGame(difficulty: Difficulty, settings: GameSettings = state.settings): GameState {
        undoStack.clear()
        redoStack.clear()
        state = GameState.fromPuzzle(generator.generate(difficulty, random), settings)
        return state
    }

    fun select(index: CellIndex): GameState {
        state = state.copy(selected = index)
        return state
    }

    fun moveSelection(deltaRow: Int, deltaColumn: Int): GameState =
        select(state.selected.move(deltaRow, deltaColumn))

    fun toggleNotesMode(): GameState {
        state = state.copy(notesMode = !state.notesMode)
        return state
    }

    fun togglePause(): GameState {
        state = state.copy(paused = !state.paused)
        return state
    }

    fun updateElapsed(elapsedMillis: Long): GameState {
        state = state.copy(elapsedMillis = elapsedMillis.coerceAtLeast(0L))
        return state
    }

    fun updateSettings(settings: GameSettings): GameState {
        state = state.copy(settings = settings)
        return state
    }

    fun enterDigit(value: Int): GameState {
        require(value in 1..9) { "value must be 1..9" }
        if (!canEditSelected()) return state
        return if (state.notesMode) toggleNote(value) else setSelectedValue(value)
    }

    fun clearSelected(): GameState {
        if (!canEditSelected()) return state
        val index = state.selected.index
        val nextNotes = state.notes.toMutableList()
        nextNotes[index] = emptySet()
        return commit(
            state.copy(
                current = state.current.clear(state.selected),
                notes = nextNotes,
                completed = false,
            )
        )
    }

    fun hint(): GameState {
        if (state.paused || state.completed) return state
        val targetIndex = if (state.current[state.selected] == SudokuBoard.EMPTY && canEditSelected()) {
            state.selected
        } else {
            val currentValues = state.current.values()
            (0 until SudokuBoard.CELL_COUNT)
                .firstOrNull { currentValues[it] == SudokuBoard.EMPTY && it !in state.givenIndexes }
                ?.let(CellIndex::fromIndex)
        } ?: return state
        val value = state.solution[targetIndex]
        val nextNotes = state.notes.toMutableList()
        nextNotes[targetIndex.index] = emptySet()
        val nextBoard = state.current.withValue(targetIndex, value)
        return commit(
            state.copy(
                selected = targetIndex,
                current = nextBoard,
                notes = nextNotes,
                completed = nextBoard == state.solution,
            )
        )
    }

    fun undo(): GameState {
        if (undoStack.isEmpty()) return state
        redoStack.addLast(state)
        state = restoreMove(undoStack.removeLast())
        return state
    }

    fun redo(): GameState {
        if (redoStack.isEmpty()) return state
        undoStack.addLast(state)
        state = restoreMove(redoStack.removeLast())
        return state
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun recordCompletion(): Boolean {
        if (!state.completed || state.completionRecorded) return false
        state = state.copy(completionRecorded = true)
        return true
    }

    private fun restoreMove(move: GameState): GameState = move.copy(
        elapsedMillis = state.elapsedMillis,
        settings = state.settings,
        paused = state.paused,
        notesMode = state.notesMode,
        completionRecorded = state.completionRecorded,
    )

    private fun toggleNote(value: Int): GameState {
        val index = state.selected.index
        val nextNotes = state.notes.toMutableList()
        val notesForCell = nextNotes[index].toMutableSet()
        if (!notesForCell.add(value)) notesForCell.remove(value)
        nextNotes[index] = notesForCell.toSortedSet()
        return commit(state.copy(notes = nextNotes))
    }

    private fun setSelectedValue(value: Int): GameState {
        val index = state.selected.index
        val nextNotes = state.notes.toMutableList()
        nextNotes[index] = emptySet()
        val nextBoard = state.current.withValue(state.selected, value)
        return commit(
            state.copy(
                current = nextBoard,
                notes = nextNotes,
                completed = nextBoard == state.solution,
            )
        )
    }

    private fun canEditSelected(): Boolean =
        !state.completed && !state.paused && state.selected.index !in state.givenIndexes

    private fun commit(next: GameState): GameState {
        undoStack.addLast(state)
        redoStack.clear()
        state = next
        return state
    }
}
