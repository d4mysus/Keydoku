package com.keydoku

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.keydoku.data.GameRepository
import com.keydoku.domain.Difficulty
import com.keydoku.game.Appearance
import com.keydoku.game.GameController
import com.keydoku.game.GameSettings
import com.keydoku.game.GameState
import com.keydoku.game.GameStats
import com.keydoku.game.GameTimer
import com.keydoku.ui.SudokuBoardView

class MainActivity : Activity() {
    private val donateBitcoinAddress = "bc1qukl3zq2vcm0y7ntzzsep9zdkwfps9twuen46vl"
    private val buyMeCoffeeUrl = "https://buymeacoffee.com/d4mysus"
    private lateinit var repository: GameRepository
    private lateinit var controller: GameController
    private lateinit var uiContext: Context
    private var appliedNightMode: Int? = null
    private var redrawMenu: (() -> Unit)? = null
    private var activeDialog: AlertDialog? = null
    private var redrawDialog: (() -> Unit)? = null
    private lateinit var gameScreen: LinearLayout
    private lateinit var menuScreen: LinearLayout
    private lateinit var boardView: SudokuBoardView
    private lateinit var statusView: TextView
    private lateinit var stripPrimaryView: TextView
    private lateinit var stripKeysView: TextView
    private var currentMenuButtons: List<Button> = emptyList()
    private val menuButtonsByShortcut = mutableMapOf<Int, Button>()

    private var currentScreen = Screen.GAME
    private var stats = GameStats()
    private var toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_MUSIC, 40)
    }.getOrNull()
    private val timer = GameTimer(SystemClock::elapsedRealtime)
    private var completionDialogShown = false
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            val state = controller.state
            if (currentScreen == Screen.GAME && !state.paused && !state.completed) {
                captureElapsed()
                render()
                repository.saveState(controller.state)
            }
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = GameRepository(this)
        controller = GameController()
        stats = repository.loadStats()

        val settings = repository.loadSettings()
        val restored = repository.loadState()
        if (restored != null) {
            controller.restore(restored.copy(settings = settings))
        } else {
            controller.newGame(Difficulty.EASY, settings)
            stats = stats.started(Difficulty.EASY)
            repository.saveStats(stats)
        }

        applyAppearance()
        registerBackMenu()
        resetTimerAnchor()
        showGameScreen()
        if (controller.state.completed) {
            boardView.post {
                completionDialogShown = true
                showCompletedDialog(controller.state)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyAppearance()
        resetTimerAnchor()
        handler.post(tick)
    }

    override fun onPause() {
        captureElapsed()
        super.onPause()
        handler.removeCallbacks(tick)
        repository.saveState(controller.state)
        repository.saveSettings(controller.state.settings)
        repository.saveStats(stats)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
        toneGenerator = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppearance()
    }

    @Suppress("DEPRECATION") // Native system-bar flags support the app's API 23 minimum.
    private fun applyAppearance() {
        val nightMode = when (controller.state.settings.appearance) {
            Appearance.LIGHT -> Configuration.UI_MODE_NIGHT_NO
            Appearance.DARK -> Configuration.UI_MODE_NIGHT_YES
            Appearance.SYSTEM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                Configuration.UI_MODE_NIGHT_YES
            } else Configuration.UI_MODE_NIGHT_NO
        }
        if (nightMode == appliedNightMode) return
        val focusedIndex = currentMenuButtons.indexOfFirst { it.hasFocus() }
        val menu = redrawMenu
        val dialog = redrawDialog.takeIf { activeDialog?.isShowing == true }
        activeDialog?.dismiss()
        val configuration = Configuration(resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        }
        // A local resource context keeps manual overrides separate from system configuration.
        uiContext = ContextThemeWrapper(this, R.style.AppTheme).apply {
            applyOverrideConfiguration(configuration)
        }
        appliedNightMode = nightMode
        val background = color(R.color.keydoku_background)
        window.setBackgroundDrawable(ColorDrawable(background))
        window.statusBarColor = background
        window.navigationBarColor = background
        val lightBars = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.decorView.systemUiVisibility = if (nightMode == Configuration.UI_MODE_NIGHT_NO) {
            window.decorView.systemUiVisibility or lightBars
        } else {
            window.decorView.systemUiVisibility and lightBars.inv()
        }
        setContentView(buildLayout())
        render()
        if (currentScreen == Screen.GAME) {
            boardView.requestFocus()
        } else {
            menu?.invoke()
            menuScreen.post { currentMenuButtons.getOrNull(focusedIndex)?.requestFocus() }
        }
        dialog?.invoke()
    }

    private fun color(id: Int): Int = uiContext.getColor(id)

    @Deprecated("Use OnBackPressedDispatcher in new AndroidX activities.")
    // Legacy devices use this path; API 33+ uses registerBackMenu's platform callback.
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        handleBack()
    }

    private fun registerBackMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                handleBack()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        captureElapsed()
        if (keyCode in MENU_KEY_CODES) {
            handleBack()
            return true
        }
        if (currentScreen != Screen.GAME) {
            if (handleMenuKey(keyCode)) return true
            return super.onKeyUp(keyCode, event)
        }

        val before = controller.state
        val handled = when (keyCode) {
            KeyEvent.KEYCODE_1 -> controller.enterDigit(1).handled()
            KeyEvent.KEYCODE_2 -> controller.enterDigit(2).handled()
            KeyEvent.KEYCODE_3 -> controller.enterDigit(3).handled()
            KeyEvent.KEYCODE_4 -> controller.enterDigit(4).handled()
            KeyEvent.KEYCODE_5 -> controller.enterDigit(5).handled()
            KeyEvent.KEYCODE_6 -> controller.enterDigit(6).handled()
            KeyEvent.KEYCODE_7 -> controller.enterDigit(7).handled()
            KeyEvent.KEYCODE_8 -> controller.enterDigit(8).handled()
            KeyEvent.KEYCODE_9 -> controller.enterDigit(9).handled()
            KeyEvent.KEYCODE_0,
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_CLEAR -> controller.clearSelected().handled()
            KeyEvent.KEYCODE_DPAD_UP -> controller.moveSelection(-1, 0).handled()
            KeyEvent.KEYCODE_DPAD_DOWN -> controller.moveSelection(1, 0).handled()
            KeyEvent.KEYCODE_DPAD_LEFT -> controller.moveSelection(0, -1).handled()
            KeyEvent.KEYCODE_DPAD_RIGHT -> controller.moveSelection(0, 1).handled()
            KeyEvent.KEYCODE_STAR -> controller.toggleNotesMode().handled()
            KeyEvent.KEYCODE_POUND -> controller.hint().handled()
            KeyEvent.KEYCODE_VOLUME_DOWN -> controller.undo().handled()
            KeyEvent.KEYCODE_VOLUME_UP -> controller.redo().handled()
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                boardView.requestFocus()
                true
            }
            else -> false
        }
        if (handled) {
            afterAction(before)
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun buildLayout(): View {
        val root = FrameLayout(uiContext).apply {
            setBackgroundColor(color(R.color.keydoku_background))
            setOnApplyWindowInsetsListener { view, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                } else {
                    @Suppress("DEPRECATION")
                    view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                }
                insets
            }
        }
        gameScreen = buildGameScreen()
        menuScreen = LinearLayout(uiContext).apply {
            id = R.id.menu_screen
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setBackgroundColor(color(R.color.keydoku_background))
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        root.addView(gameScreen)
        root.addView(menuScreen)
        root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val availableWidth = root.width - root.paddingLeft - root.paddingRight - dp(12)
            val availableHeight = root.height - root.paddingTop - root.paddingBottom - dp(118)
            val size = minOf(availableWidth, availableHeight).coerceAtLeast(0)
            if (boardView.layoutParams.height != size) {
                boardView.layoutParams = boardView.layoutParams.apply { height = size }
            }
        }
        return root
    }

    private fun buildGameScreen(): LinearLayout {
        val root = LinearLayout(uiContext).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(color(R.color.keydoku_background))
            setPadding(dp(6), dp(2), dp(6), dp(4))
        }

        val topBar = LinearLayout(uiContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleView = TextView(uiContext).apply {
            text = "Keydoku"
            textSize = 18f
            setTextColor(color(R.color.keydoku_ink))
            setTypeface(typeface, Typeface.BOLD)
        }
        statusView = TextView(uiContext).apply {
            textSize = 13f
            gravity = Gravity.END
            setTextColor(color(R.color.keydoku_muted))
        }
        topBar.addView(titleView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        topBar.addView(statusView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f))

        boardView = SudokuBoardView(uiContext).apply {
            id = R.id.sudoku_board
            onCellSelected = { index ->
                captureElapsed()
                afterAction(controller.state, controller.select(index))
            }
        }

        val statusStrip = LinearLayout(uiContext).apply {
            id = R.id.status_strip
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(6), dp(4), 0)
        }
        stripPrimaryView = TextView(uiContext).apply {
            textSize = 18f
            setTextColor(color(R.color.keydoku_ink))
            gravity = Gravity.CENTER
        }
        stripKeysView = TextView(uiContext).apply {
            textSize = 14f
            setTextColor(color(R.color.keydoku_muted))
            gravity = Gravity.CENTER
        }
        statusStrip.addView(stripPrimaryView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        statusStrip.addView(stripKeysView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        root.addView(topBar)
        root.addView(
            boardView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, preferredBoardSize()).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )
        root.addView(statusStrip, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    private fun handleBack() {
        captureElapsed()
        when (currentScreen) {
            Screen.GAME -> showGameMenu()
            Screen.MAIN_MENU -> showGameScreen()
            Screen.DONATE -> showGameMenu()
            Screen.DONATE_BITCOIN,
            Screen.DONATE_COFFEE -> showDonateScreen()
            Screen.PRIVACY -> showSettingsScreen()
            Screen.APPEARANCE -> showSettingsScreen()
            else -> showGameMenu()
        }
    }

    private fun handleMenuKey(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                currentFocusedMenuButton()?.performClick()
                return true
            }
        }

        val shortcut = keyCodeToDigit(keyCode) ?: return false
        val button = menuButtonsByShortcut[shortcut] ?: return true
        if (button.isEnabled) {
            button.performClick()
        }
        return true
    }

    private fun currentFocusedMenuButton(): Button? =
        currentMenuButtons.firstOrNull { it.hasFocus() } ?: currentMenuButtons.firstOrNull { it.isEnabled }

    private fun showGameScreen() {
        currentScreen = Screen.GAME
        menuScreen.visibility = View.GONE
        gameScreen.visibility = View.VISIBLE
        resetTimerAnchor()
        render()
        boardView.requestFocus()
    }

    private fun showGameMenu() {
        currentScreen = Screen.MAIN_MENU
        showMenuScreen(
            title = "Keydoku",
            actions = listOf(
                MenuAction("Continue", shortcut = 1) { showGameScreen() },
                MenuAction("New Puzzle", shortcut = 2) { showDifficultyScreen() },
                MenuAction("Undo", shortcut = 3, enabled = controller.canUndo(), startsGroup = true) {
                    afterAction(controller.state, controller.undo())
                    showGameScreen()
                },
                MenuAction("Redo", shortcut = 4, enabled = controller.canRedo()) {
                    afterAction(controller.state, controller.redo())
                    showGameScreen()
                },
                MenuAction("Stats", shortcut = 5, startsGroup = true) { showStatsScreen() },
                MenuAction("Settings", shortcut = 6) { showSettingsScreen() },
                MenuAction("Donate", shortcut = 7) { showDonateScreen() },
                MenuAction("Controls", shortcut = 0) { showHelpScreen() },
            )
        )
    }

    private fun showDifficultyScreen() {
        currentScreen = Screen.DIFFICULTY
        showMenuScreen(
            title = "New Puzzle",
            body = "Choose difficulty",
            actions = Difficulty.entries.map { difficulty ->
                MenuAction("${difficulty.displayName}  ${difficulty.targetGivens} givens") {
                    startNewGame(difficulty)
                }
            } + MenuAction("Back") { showGameMenu() }
        )
    }

    private fun startNewGame(difficulty: Difficulty) {
        if (!controller.state.completed && controller.state.elapsedMillis > 0L) {
            activeDialog = AlertDialog.Builder(uiContext)
                .setTitle("Start new game?")
                .setMessage("This will replace your current ${controller.state.difficulty.displayName} puzzle.")
                .setPositiveButton("Start") { _, _ -> createNewGame(difficulty) }
                .setNegativeButton("Cancel", null)
                .show()
            redrawDialog = { startNewGame(difficulty) }
        } else {
            createNewGame(difficulty)
        }
    }

    private fun createNewGame(difficulty: Difficulty) {
        controller.newGame(difficulty, controller.state.settings)
        stats = stats.started(difficulty)
        completionDialogShown = false
        resetTimerAnchor()
        renderAndSave()
        showGameScreen()
    }

    private fun showStatsScreen() {
        currentScreen = Screen.STATS
        val body = Difficulty.entries.joinToString(separator = "\n") { difficulty ->
            val item = stats.byDifficulty[difficulty]
            "${difficulty.displayName}: ${item?.gamesCompleted ?: 0}/${item?.gamesStarted ?: 0} done\n" +
                "best ${formatTime(item?.bestTimeMillis ?: 0L)}, avg ${formatTime(item?.averageTimeMillis ?: 0L)}"
        }
        showMenuScreen(
            title = "Stats",
            body = body,
            actions = listOf(MenuAction("Back") { showGameMenu() })
        )
    }

    private fun showSettingsScreen() {
        currentScreen = Screen.SETTINGS
        val settings = controller.state.settings
        showMenuScreen(
            title = "Settings",
            body = "OK changes a setting",
            actions = listOf(
                toggleAction("Mistake check", settings.mistakeChecking) {
                    settings.copy(mistakeChecking = !settings.mistakeChecking)
                },
                toggleAction("Related cells", settings.highlightRelated) {
                    settings.copy(highlightRelated = !settings.highlightRelated)
                },
                toggleAction("Matching digits", settings.highlightMatchingDigits) {
                    settings.copy(highlightMatchingDigits = !settings.highlightMatchingDigits)
                },
                toggleAction("Haptics", settings.haptics) {
                    settings.copy(haptics = !settings.haptics)
                },
                toggleAction("Sound", settings.sound) {
                    settings.copy(sound = !settings.sound)
                },
                MenuAction("Appearance: ${settings.appearance.displayName}") { showAppearanceScreen() },
                MenuAction("Privacy") { showPrivacyScreen() },
                MenuAction("Back") { showGameMenu() },
            )
        )
    }

    private fun toggleAction(
        label: String,
        enabled: Boolean,
        nextSettings: () -> GameSettings,
    ): MenuAction =
        MenuAction("$label: ${if (enabled) "On" else "Off"}") {
            val settings = nextSettings()
            controller.updateSettings(settings)
            repository.saveSettings(settings)
            renderAndSave()
            showSettingsScreen()
        }

    private fun showAppearanceScreen() {
        currentScreen = Screen.APPEARANCE
        showMenuScreen(
            title = "Appearance",
            actions = Appearance.entries.mapIndexed { index, appearance ->
                val selected = appearance == controller.state.settings.appearance
                MenuAction(appearance.displayName + if (selected) " (selected)" else "", shortcut = index + 1) {
                    val settings = controller.state.settings.copy(appearance = appearance)
                    controller.updateSettings(settings)
                    repository.saveSettings(settings)
                    applyAppearance()
                    renderAndSave()
                    showAppearanceScreen()
                    menuScreen.post { currentMenuButtons[index].requestFocus() }
                }
            } + MenuAction("Back", shortcut = 0) { showSettingsScreen() }
        )
    }

    private fun showHelpScreen() {
        currentScreen = Screen.HELP
        showMenuScreen(
            title = "Controls",
            body = "D-pad: move cell\n" +
                "1-9: enter digit\n" +
                "0/delete: clear\n" +
                "*: toggle Notes mode\n" +
                "In Notes, 5 then 6 marks candidates 5 and 6\n" +
                "#: hint\n" +
                "Vol - / Vol +: undo / redo\n" +
                "Back/Menu: menu",
            actions = listOf(MenuAction("Back") { showGameMenu() })
        )
    }

    private fun showDonateScreen() {
        currentScreen = Screen.DONATE
        showMenuScreen(
            title = "Donate",
            body = "Pick a method",
            actions = listOf(
                MenuAction("Bitcoin", shortcut = 1) { showBitcoinDonateScreen() },
                MenuAction("Buy Me a Coffee", shortcut = 2) { showBuyMeCoffeeScreen() },
                MenuAction("Back", shortcut = 0) { showGameMenu() }
            )
        )
    }

    private fun showBitcoinDonateScreen() {
        currentScreen = Screen.DONATE_BITCOIN
        showMenuScreen(
            title = "Bitcoin",
            body = donateBitcoinAddress,
            actions = listOf(
                MenuAction("Copy address", shortcut = 1) { copyDonateAddress() },
                MenuAction("Back", shortcut = 0) { showDonateScreen() }
            ),
            imageBitmap = buildQrBitmap("bitcoin:$donateBitcoinAddress", dp(152)),
            bodyCentered = true
        )
    }

    private fun showBuyMeCoffeeScreen() {
        currentScreen = Screen.DONATE_COFFEE
        showMenuScreen(
            title = "Buy Me a Coffee",
            body = buyMeCoffeeUrl,
            actions = listOf(
                MenuAction("Open link", shortcut = 1) { openBuyMeCoffeeLink() },
                MenuAction("Copy link", shortcut = 2) { copyBuyMeCoffeeLink() },
                MenuAction("Back", shortcut = 0) { showDonateScreen() }
            ),
            imageBitmap = buildQrBitmap(buyMeCoffeeUrl, dp(152)),
            bodyCentered = true
        )
    }

    private fun showPrivacyScreen() {
        currentScreen = Screen.PRIVACY
        showMenuScreen(
            title = "Privacy",
            body = "Keydoku stores puzzles, settings, and stats only on this device.\n\n" +
                "It does not collect, share, sell, or transmit personal data.\n\n" +
                "Donate links open outside Keydoku and do not unlock app features.",
            actions = listOf(MenuAction("Back") { showSettingsScreen() })
        )
    }

    private fun copyDonateAddress() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Keydoku Bitcoin", donateBitcoinAddress))
        Toast.makeText(this, "Bitcoin address copied", Toast.LENGTH_SHORT).show()
    }

    private fun copyBuyMeCoffeeLink() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Keydoku Buy Me a Coffee", buyMeCoffeeUrl))
        Toast.makeText(this, "Donation link copied", Toast.LENGTH_SHORT).show()
    }

    private fun openBuyMeCoffeeLink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(buyMeCoffeeUrl))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            copyBuyMeCoffeeLink()
        }
    }

    private fun showMenuScreen(
        title: String,
        body: String = "",
        actions: List<MenuAction>,
        imageBitmap: Bitmap? = null,
        bodyCentered: Boolean = false,
    ) {
        redrawMenu = { showMenuScreen(title, body, actions, imageBitmap, bodyCentered) }
        gameScreen.visibility = View.GONE
        menuScreen.visibility = View.VISIBLE
        menuScreen.removeAllViews()
        currentMenuButtons = emptyList()
        menuButtonsByShortcut.clear()

        val titleView = TextView(uiContext).apply {
            id = R.id.menu_title
            text = title
            textSize = 24f
            setTextColor(color(R.color.keydoku_ink))
            setTypeface(typeface, Typeface.BOLD)
        }
        val bodyView = TextView(uiContext).apply {
            text = body
            textSize = 14f
            setTextColor(color(R.color.keydoku_muted))
            gravity = if (bodyCentered) Gravity.CENTER_HORIZONTAL else Gravity.START
            setLineSpacing(dp(2).toFloat(), 1.0f)
            setPadding(0, dp(4), 0, dp(6))
        }
        val imageView = imageBitmap?.let { bitmap ->
            ImageView(uiContext).apply {
                setImageBitmap(bitmap)
                setBackgroundColor(Color.WHITE)
                adjustViewBounds = true
                setPadding(dp(4), dp(4), dp(4), dp(4))
            }
        }
        val list = LinearLayout(uiContext).apply {
            orientation = LinearLayout.VERTICAL
        }
        val footerView = TextView(uiContext).apply {
            text = if (currentScreen == Screen.SETTINGS) {
                "↑↓ Move     OK Change     Back Return"
            } else {
                "↑↓ Move     OK Select     Back Return"
            }
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(color(R.color.keydoku_muted))
            setPadding(0, dp(6), 0, 0)
        }

        menuScreen.addView(titleView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        imageView?.let {
            menuScreen.addView(
                it,
                LinearLayout.LayoutParams(dp(160), dp(160)).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = dp(4)
                    bottomMargin = dp(2)
                }
            )
        }
        if (body.isNotBlank()) {
            menuScreen.addView(bodyView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        } else {
            menuScreen.addView(View(uiContext), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6)))
        }
        menuScreen.addView(ScrollView(uiContext).apply { addView(list) },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        menuScreen.addView(footerView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val buttons = mutableListOf<Button>()
        actions.forEachIndexed { index, action ->
            val button = menuButton(action)
            list.addView(button)
            buttons += button
            action.shortcut?.let { menuButtonsByShortcut[it] = button }
            if (index == 0) {
                button.post { button.requestFocus() }
            }
        }
        currentMenuButtons = buttons
    }

    private fun menuButton(action: MenuAction): Button =
        Button(uiContext).apply {
            text = menuLabel(action, focused = false)
            textSize = 15f
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = action.enabled
            isFocusableInTouchMode = action.enabled
            isAllCaps = false
            includeFontPadding = false
            setTypeface(typeface, Typeface.BOLD)
            isEnabled = action.enabled
            setTextColor(menuTextColors())
            background = menuButtonBackground()
            stateListAnimator = null
            minHeight = 0
            minimumHeight = 0
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener { if (action.enabled) action.onClick() }
            setOnFocusChangeListener { _, hasFocus ->
                text = menuLabel(action, hasFocus)
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)).apply {
                setMargins(0, if (action.startsGroup) dp(8) else dp(3), 0, dp(2))
            }
        }

    private fun buildQrBitmap(content: String, size: Int): Bitmap {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (y in 0 until size) {
            for (x in 0 until size) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.rgb(29, 29, 27) else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun menuLabel(action: MenuAction, focused: Boolean): String {
        val prefix = if (focused && action.enabled) "> " else "  "
        val shortcut = action.shortcut?.toString()?.plus(" ") ?: ""
        return prefix + shortcut + action.label
    }

    private fun menuButtonBackground(): StateListDrawable =
        StateListDrawable().apply {
            addState(
                intArrayOf(-android.R.attr.state_enabled),
                roundedFill(color(R.color.menu_disabled), strokeColor = color(R.color.menu_disabled_stroke))
            )
            addState(
                intArrayOf(android.R.attr.state_focused),
                roundedFill(color(R.color.menu_focused), strokeColor = color(R.color.menu_focused_stroke))
            )
            addState(
                intArrayOf(android.R.attr.state_pressed),
                roundedFill(color(R.color.menu_pressed), strokeColor = color(R.color.menu_focused_stroke))
            )
            addState(
                intArrayOf(),
                roundedFill(color(R.color.menu_surface), strokeColor = color(R.color.menu_stroke))
            )
        }

    private fun roundedFill(color: Int, strokeColor: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(6).toFloat()
            setColor(color)
            setStroke(dp(1), strokeColor)
        }

    private fun menuTextColors(): ColorStateList =
        ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(),
            ),
            intArrayOf(color(R.color.menu_disabled_text), color(R.color.menu_focused_text), color(R.color.menu_focused_text), color(R.color.keydoku_ink))
        )

    private fun showCompletedDialog(state: GameState) {
        activeDialog = AlertDialog.Builder(uiContext)
            .setTitle("Puzzle complete")
            .setMessage("${state.difficulty.displayName} finished in ${formatTime(state.elapsedMillis)}")
            .setPositiveButton("New game") { _, _ -> showDifficultyScreen() }
            .setNegativeButton("Stats") { _, _ -> showStatsScreen() }
            .setNeutralButton("Board") { _, _ -> showGameScreen() }
            .show()
        redrawDialog = { showCompletedDialog(controller.state) }
    }

    private fun afterAction(before: GameState, after: GameState = controller.state) {
        if (controller.recordCompletion()) {
            stats = stats.completed(after.difficulty, after.elapsedMillis)
        }
        if (before != after) {
            feedback(after.settings)
        }
        resetTimerAnchor()
        renderAndSave()
        if (!before.completed && after.completed && !completionDialogShown) {
            completionDialogShown = true
            showCompletedDialog(after)
        }
    }

    private fun feedback(settings: GameSettings) {
        if (settings.haptics) {
            boardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (settings.sound) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        }
    }

    private fun renderAndSave() {
        render()
        repository.saveState(controller.state)
        repository.saveStats(stats)
    }

    private fun render() {
        val state = controller.state
        boardView.render(state)
        statusView.text = buildString {
            append(state.difficulty.displayName)
            append("  ")
            append(formatTime(state.elapsedMillis))
            if (state.paused) append("  Paused")
            if (state.completed) append("  Done")
        }
        stripPrimaryView.text = buildPrimaryStrip(state)
        stripKeysView.text = "* Notes    # Hint    Back Menu"
    }

    private fun buildPrimaryStrip(state: GameState): String {
        val selected = state.selected
        val mode = if (state.notesMode) "Notes" else "Entry"
        val notes = state.notes[selected.index].sorted().joinToString(" ")
        val notesText = if (notes.isBlank()) "" else "  Cell notes $notes"
        val conflicts = if (state.conflictIndexes.isEmpty()) "" else "  Conflicts ${state.conflictIndexes.size}"
        return "R${selected.row + 1} C${selected.column + 1}  $mode$notesText$conflicts"
    }

    private fun resetTimerAnchor() {
        timer.start(controller.state.elapsedMillis)
    }

    private fun captureElapsed() {
        if (::controller.isInitialized && currentScreen == Screen.GAME &&
            !controller.state.paused && !controller.state.completed) {
            controller.updateElapsed(timer.elapsedMillis())
        }
    }

    private fun formatTime(millis: Long): String {
        if (millis <= 0L) return "--:--"
        val totalSeconds = millis / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun GameState.handled(): Boolean = true

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun preferredBoardSize(): Int {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels - dp(12)
        val heightBudget = metrics.heightPixels - dp(118)
        return minOf(width, heightBudget)
    }

    private fun keyCodeToDigit(keyCode: Int): Int? =
        when (keyCode) {
            KeyEvent.KEYCODE_0 -> 0
            KeyEvent.KEYCODE_1 -> 1
            KeyEvent.KEYCODE_2 -> 2
            KeyEvent.KEYCODE_3 -> 3
            KeyEvent.KEYCODE_4 -> 4
            KeyEvent.KEYCODE_5 -> 5
            KeyEvent.KEYCODE_6 -> 6
            KeyEvent.KEYCODE_7 -> 7
            KeyEvent.KEYCODE_8 -> 8
            KeyEvent.KEYCODE_9 -> 9
            else -> null
        }

    private data class MenuAction(
        val label: String,
        val shortcut: Int? = null,
        val enabled: Boolean = true,
        val startsGroup: Boolean = false,
        val onClick: () -> Unit,
    )

    private enum class Screen {
        GAME,
        MAIN_MENU,
        DIFFICULTY,
        STATS,
        SETTINGS,
        APPEARANCE,
        HELP,
        DONATE,
        DONATE_BITCOIN,
        DONATE_COFFEE,
        PRIVACY,
    }

    companion object {
        private val MENU_KEY_CODES = setOf(
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_SOFT_LEFT,
            KeyEvent.KEYCODE_SOFT_RIGHT,
        )
    }
}
