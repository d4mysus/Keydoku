package com.keydoku

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.keydoku.data.GameRepository
import com.keydoku.game.GameController
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences = context.getSharedPreferences("keydoku", Context.MODE_PRIVATE)

    @Before
    fun clearBefore() { preferences.edit().clear().commit() }

    @After
    fun clearAfter() { preferences.edit().clear().commit() }

    @Test
    fun completionMarkerSurvivesSavingAnUndonePuzzle() {
        val controller = GameController()
        while (!controller.state.completed) controller.hint()
        assertTrue(controller.recordCompletion())
        controller.undo()
        val repository = GameRepository(context)
        repository.saveState(controller.state)
        val restored = repository.loadState()!!
        assertFalse(restored.completed)
        assertTrue(restored.completionRecorded)
        assertEquals(controller.state, restored)
    }

    @Test
    fun oldCompletedSaveDoesNotCountCompletionAgain() {
        val controller = GameController()
        while (!controller.state.completed) controller.hint()
        val repository = GameRepository(context)
        repository.saveState(controller.state)
        val legacy = JSONObject(preferences.getString("active_state", null)!!)
        legacy.remove("completionRecorded")
        preferences.edit().putString("active_state", legacy.toString()).commit()
        controller.restore(repository.loadState()!!)
        assertFalse(controller.recordCompletion())
    }
}
