package com.keydoku.game

import com.keydoku.domain.CellIndex
import com.keydoku.domain.Difficulty
import com.keydoku.domain.SudokuGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameControllerTest {
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
