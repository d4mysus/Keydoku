package com.keydoku

import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeypadInputTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

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
