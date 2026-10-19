package com.keydoku

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.keydoku.data.GameRepository
import com.keydoku.game.Appearance
import com.keydoku.game.GameController
import com.keydoku.game.GameSettings
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
    fun everyAppearanceSurvivesRepositoryReloadAndSavedGameRoundTrip() {
        Appearance.entries.forEach { appearance ->
            val settings = GameSettings(sound = true, appearance = appearance)
            val controller = GameController()
            controller.updateSettings(settings)
            GameRepository(context).apply {
                saveSettings(settings)
                saveState(controller.state)
            }
            val reloaded = GameRepository(context)
            assertEquals(settings, reloaded.loadSettings())
            assertEquals(controller.state, reloaded.loadState())
        }
    }

    @Test
    fun legacyAndUnknownAppearanceKeepOtherSettingsAndSavedGame() {
        val repository = GameRepository(context)
        assertEquals(Appearance.SYSTEM, repository.loadSettings().appearance)
        val controller = GameController()
        controller.updateSettings(GameSettings(sound = true, haptics = true))
        repository.saveSettings(controller.state.settings)
        repository.saveState(controller.state)
        val settings = JSONObject(preferences.getString("settings", null)!!)
        val state = JSONObject(preferences.getString("active_state", null)!!)
        for (appearance in listOf(null, "UNKNOWN_FUTURE_VALUE")) {
            settings.put("appearance", appearance)
            state.getJSONObject("settings").put("appearance", appearance)
            preferences.edit().putString("settings", settings.toString())
                .putString("active_state", state.toString()).commit()
            assertEquals(controller.state.settings, repository.loadSettings())
            assertEquals(controller.state, repository.loadState())
        }
    }

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
