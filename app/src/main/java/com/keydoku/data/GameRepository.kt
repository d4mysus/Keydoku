package com.keydoku.data

import android.content.Context
import com.keydoku.domain.CellIndex
import com.keydoku.domain.Difficulty
import com.keydoku.domain.SudokuBoard
import com.keydoku.game.Appearance
import com.keydoku.game.DifficultyStats
import com.keydoku.game.GameSettings
import com.keydoku.game.GameState
import com.keydoku.game.GameStats
import org.json.JSONObject

class GameRepository(context: Context) {
    private val preferences = context.getSharedPreferences("keydoku", Context.MODE_PRIVATE)

    fun loadState(): GameState? =
        preferences.getString(KEY_STATE, null)?.let(::decodeState)

    fun saveState(state: GameState) {
        preferences.edit()
            .putString(KEY_STATE, encodeState(state))
            .apply()
    }

    fun clearState() {
        preferences.edit().remove(KEY_STATE).apply()
    }

    fun loadSettings(): GameSettings =
        preferences.getString(KEY_SETTINGS, null)?.let(::decodeSettings) ?: GameSettings()

    fun saveSettings(settings: GameSettings) {
        preferences.edit()
            .putString(KEY_SETTINGS, encodeSettings(settings))
            .apply()
    }

    fun loadStats(): GameStats =
        preferences.getString(KEY_STATS, null)?.let(::decodeStats) ?: GameStats()

    fun saveStats(stats: GameStats) {
        preferences.edit()
            .putString(KEY_STATS, encodeStats(stats))
            .apply()
    }

    private fun encodeState(state: GameState): String =
        JSONObject()
            .put("puzzle", state.puzzle.serialize())
            .put("solution", state.solution.serialize())
            .put("current", state.current.serialize())
            .put("difficulty", state.difficulty.name)
            .put("selected", state.selected.index)
            .put("notes", encodeNotes(state.notes))
            .put("notesMode", state.notesMode)
            .put("elapsedMillis", state.elapsedMillis)
            .put("paused", state.paused)
            .put("completed", state.completed)
            .put("completionRecorded", state.completionRecorded)
            .put("settings", JSONObject(encodeSettings(state.settings)))
            .toString()

    private fun decodeState(raw: String): GameState? =
        runCatching {
            val json = JSONObject(raw)
            GameState(
                puzzle = SudokuBoard.fromString(json.getString("puzzle")),
                solution = SudokuBoard.fromString(json.getString("solution")),
                current = SudokuBoard.fromString(json.getString("current")),
                difficulty = Difficulty.fromName(json.getString("difficulty")),
                selected = CellIndex.fromIndex(json.optInt("selected", 0)),
                notes = decodeNotes(json.optString("notes", "")),
                notesMode = json.optBoolean("notesMode", false),
                elapsedMillis = json.optLong("elapsedMillis", 0L),
                paused = json.optBoolean("paused", false),
                completed = json.optBoolean("completed", false),
                completionRecorded = json.optBoolean("completionRecorded", json.optBoolean("completed", false)),
                settings = decodeSettings(json.optJSONObject("settings")?.toString()),
            )
        }.getOrNull()

    private fun encodeSettings(settings: GameSettings): String =
        JSONObject()
            .put("mistakeChecking", settings.mistakeChecking)
            .put("highlightRelated", settings.highlightRelated)
            .put("highlightMatchingDigits", settings.highlightMatchingDigits)
            .put("haptics", settings.haptics)
            .put("sound", settings.sound)
            .put("appearance", settings.appearance.name)
            .toString()

    private fun decodeSettings(raw: String?): GameSettings =
        runCatching {
            if (raw == null) return GameSettings()
            val json = JSONObject(raw)
            GameSettings(
                mistakeChecking = json.optBoolean("mistakeChecking", true),
                highlightRelated = json.optBoolean("highlightRelated", true),
                highlightMatchingDigits = json.optBoolean("highlightMatchingDigits", true),
                haptics = json.optBoolean("haptics", false),
                sound = json.optBoolean("sound", false),
                appearance = Appearance.entries.firstOrNull { it.name == json.optString("appearance") }
                    ?: Appearance.SYSTEM,
            )
        }.getOrDefault(GameSettings())

    private fun encodeStats(stats: GameStats): String {
        val root = JSONObject()
        Difficulty.entries.forEach { difficulty ->
            val item = stats.byDifficulty[difficulty] ?: DifficultyStats()
            root.put(
                difficulty.name,
                JSONObject()
                    .put("started", item.gamesStarted)
                    .put("completed", item.gamesCompleted)
                    .put("best", item.bestTimeMillis ?: -1L)
                    .put("total", item.totalCompletedTimeMillis)
            )
        }
        return root.toString()
    }

    private fun decodeStats(raw: String): GameStats =
        runCatching {
            val root = JSONObject(raw)
            val stats = Difficulty.entries.associateWith { difficulty ->
                val item = root.optJSONObject(difficulty.name)
                DifficultyStats(
                    gamesStarted = item?.optInt("started", 0) ?: 0,
                    gamesCompleted = item?.optInt("completed", 0) ?: 0,
                    bestTimeMillis = item?.optLong("best", -1L)?.takeIf { it >= 0 },
                    totalCompletedTimeMillis = item?.optLong("total", 0L) ?: 0L,
                )
            }
            GameStats(stats)
        }.getOrDefault(GameStats())

    private fun encodeNotes(notes: List<Set<Int>>): String =
        notes.joinToString(separator = "|") { cellNotes ->
            cellNotes.sorted().joinToString(separator = "")
        }

    private fun decodeNotes(raw: String): List<Set<Int>> {
        val parts = raw.split("|")
        if (parts.size != SudokuBoard.CELL_COUNT) return List(SudokuBoard.CELL_COUNT) { emptySet() }
        return parts.map { token ->
            token.mapNotNull { char ->
                char.takeIf { it in '1'..'9' }?.digitToInt()
            }.toSet()
        }
    }

    companion object {
        private const val KEY_STATE = "active_state"
        private const val KEY_SETTINGS = "settings"
        private const val KEY_STATS = "stats"
    }
}
