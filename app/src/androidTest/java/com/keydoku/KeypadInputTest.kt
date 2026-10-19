package com.keydoku

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.keydoku.data.GameRepository
import com.keydoku.game.Appearance
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeypadInputTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun appearanceIsKeypadAccessibleAndPersistsWithoutLeavingItsScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_6)
        activityRule.scenario.onActivity { activity ->
            fun buttons(view: View): List<Button> = when (view) {
                is Button -> listOf(view)
                is ViewGroup -> (0 until view.childCount).flatMap { buttons(view.getChildAt(it)) }
                else -> emptyList()
            }
            buttons(activity.findViewById(R.id.menu_screen))
                .first { it.text.contains("Appearance:") }.performClick()
        }
        for ((key, appearance) in listOf(
            KeyEvent.KEYCODE_3 to Appearance.DARK,
            KeyEvent.KEYCODE_2 to Appearance.LIGHT,
            KeyEvent.KEYCODE_1 to Appearance.SYSTEM,
        )) {
            instrumentation.sendKeyDownUpSync(key)
            activityRule.scenario.onActivity { activity ->
                assertEquals(appearance, GameRepository(activity).loadSettings().appearance)
                assertEquals("Appearance", activity.findViewById<TextView>(R.id.menu_title).text)
                val board = activity.findViewById<View>(R.id.sudoku_board)
                val dark = appearance == Appearance.DARK || (appearance == Appearance.SYSTEM &&
                    android.os.Build.VERSION.SDK_INT >= 29 &&
                    activity.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES)
                assertEquals(if (dark) 0xFF181C1F.toInt() else 0xFFFAF9F6.toInt(),
                    board.context.getColor(R.color.keydoku_background))
            }
        }
    }

    @Test
    fun numericAndDpadKeysAreHandled() {
        activityRule.scenario.onActivity { activity ->
            activity.findViewById<android.view.View>(R.id.sudoku_board).requestFocus()
        }

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_5)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_STAR)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_3)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_POUND)
    }

    @Test
    fun menuKeyShowsDedicatedMenu() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU)

        activityRule.scenario.onActivity { activity ->
            assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.menu_screen).visibility)
        }
    }

    @Test
    fun numericShortcutOpensStatsScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_5)

        activityRule.scenario.onActivity { activity ->
            val title = activity.findViewById<TextView>(R.id.menu_title).text.toString()
            assertEquals("Stats", title)
        }
    }

    @Test
    fun numericShortcutOpensDonateScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_7)

        activityRule.scenario.onActivity { activity ->
            val title = activity.findViewById<TextView>(R.id.menu_title).text.toString()
            assertEquals("Donate", title)
        }
    }

    @Test
    fun donateBitcoinShortcutOpensBitcoinScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_7)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_1)

        activityRule.scenario.onActivity { activity ->
            val title = activity.findViewById<TextView>(R.id.menu_title).text.toString()
            assertEquals("Bitcoin", title)
        }
    }

    @Test
    fun donateCoffeeShortcutOpensCoffeeScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_7)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_2)

        activityRule.scenario.onActivity { activity ->
            val title = activity.findViewById<TextView>(R.id.menu_title).text.toString()
            assertEquals("Buy Me a Coffee", title)
        }
    }
}
