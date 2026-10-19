package com.keydoku.game

import com.keydoku.domain.CellIndex
import com.keydoku.domain.Difficulty
import com.keydoku.domain.SudokuGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.random.Random

class GameControllerTest {
    @Test
    fun appearanceDefaultsToSystemAndChangesPreservePuzzleAndHistory() {
        val controller = seededController()
        assertEquals(Appearance.SYSTEM, controller.state.settings.appearance)
        controller.select(firstEditable(controller))
        controller.toggleNotesMode()
        controller.enterDigit(3)
        controller.enterDigit(6)
        controller.undo()
        controller.updateElapsed(12_000L)
        val before = controller.state
        Appearance.entries.forEach { appearance ->
            val settings = before.settings.copy(appearance = appearance)
            controller.updateSettings(settings)
            assertEquals(before.copy(settings = settings), controller.state)
            assertTrue(controller.canUndo())
            assertTrue(controller.canRedo())
        }
        controller.redo()
        assertEquals(setOf(3, 6), controller.state.notes[before.selected.index])
        assertEquals(Appearance.DARK, controller.state.settings.appearance)
        assertEquals(12_000L, controller.state.elapsedMillis)
        controller.undo()
        assertEquals(before.notes, controller.state.notes)
        assertEquals(Appearance.DARK, controller.state.settings.appearance)
    }

    @Test
    fun undoAndRedoPreserveElapsedTimeAndSettings() {
        val controller = seededController()
        controller.select(firstEditable(controller))
        controller.updateElapsed(10_000L)
        controller.enterDigit(4)
        val settings = controller.state.settings.copy(sound = true)
        controller.updateSettings(settings)
        controller.updateElapsed(20_000L)
        controller.undo()
        assertEquals(20_000L, controller.state.elapsedMillis)
        assertEquals(settings, controller.state.settings)
        controller.updateElapsed(30_000L)
        controller.redo()
        assertEquals(30_000L, controller.state.elapsedMillis)
        assertEquals(settings, controller.state.settings)
    }

    @Test
    fun pausedHintDoesNotModifyPuzzleOrHistory() {
        val controller = seededController()
        controller.togglePause()
        val before = controller.state
        controller.hint()
        assertEquals(before, controller.state)
        assertFalse(controller.canUndo())
    }

    @Test
    fun completionIsRecordedOnlyOnceAcrossUndoRedoAndRestore() {
        val controller = seededController()
        while (!controller.state.completed) controller.hint()
        assertTrue(controller.recordCompletion())
        controller.undo()
        assertFalse(controller.recordCompletion())
        controller.redo()
        assertFalse(controller.recordCompletion())
        val restored = seededController()
        restored.restore(controller.state)
        assertFalse(restored.recordCompletion())
        restored.newGame(Difficulty.EASY)
        while (!restored.state.completed) restored.hint()
        assertTrue(restored.recordCompletion())
    }

    @Test
    fun notesModeTogglesCandidatesWithoutSettingValue() {
        val controller = seededController()
        val editable = firstEditable(controller)
        controller.select(editable)
        controller.toggleNotesMode()

        controller.enterDigit(3)

        assertEquals(0, controller.state.current[editable])
        assertTrue(controller.state.notes[editable.index].contains(3))
    }

    @Test
    fun undoAndRedoRestoreEnteredDigit() {
        val controller = seededController()
        val editable = firstEditable(controller)
        controller.select(editable)

        controller.enterDigit(4)
        controller.undo()

        assertEquals(0, controller.state.current[editable])

        controller.redo()

        assertEquals(4, controller.state.current[editable])
    }

    @Test
    fun hintUsesSolutionForSelectedCell() {
        val controller = seededController()
        val editable = firstEditable(controller)
        val solutionValue = controller.state.solution[editable]
        controller.select(editable)

        controller.hint()

        assertEquals(solutionValue, controller.state.current[editable])
    }

    private fun seededController(): GameController =
        GameController(SudokuGenerator(), Random(3)).apply {
            newGame(Difficulty.EASY)
        }

    private fun firstEditable(controller: GameController): CellIndex =
        (0 until 81)
            .first { it !in controller.state.givenIndexes }
            .let(CellIndex::fromIndex)
}
