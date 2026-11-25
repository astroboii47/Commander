package com.astroboii47.commander

import android.graphics.Color
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.view.KeyEvent
import android.view.View
import android.content.res.Configuration
import android.os.SystemClock
import android.util.Log
import android.view.ViewTreeObserver
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.webkit.WebView
import android.app.ActivityOptions
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class OverlayActivity : ComponentActivity() {
    val windowFocusedState = mutableStateOf(false)
    private val askPreviewState = mutableStateOf<String?>(null)
    private val askPromptState = mutableStateOf<String?>(null)
    private val askWaitingState = mutableStateOf(false)
    private val switcherRequestState = mutableIntStateOf(0)
    private var currentQuery = ""
    private var commanderWidgetHost: AppWidgetHost? = null
    private var widgetHostUsers = 0
    private var preparedWebView: WebView? = null
    private var externalLaunchPending = false
    private var finishingAfterExternalLaunch = false

    override fun finish() {
        super.finish()
        if (!finishingAfterExternalLaunch) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.commander_target_enter, R.anim.commander_overlay_exit)
        }
    }

    /** Fade Commander before Android waits for the target app's first window. */
    fun launchAfterCommanderExit(intent: Intent): Boolean {
        if (externalLaunchPending) return true
        if (intent.resolveActivity(packageManager) == null) return false
        externalLaunchPending = true
        WindowCompat.getInsetsController(window, window.decorView)
            .hide(WindowInsetsCompat.Type.ime())
        val travel = 10f * resources.displayMetrics.density
        window.decorView.animate()
            .alpha(0f)
            .translationY(travel)
            .setDuration(105L)
            .withEndAction {
                runCatching {
                    startActivity(
                        intent,
                        ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.commander_target_enter,
                            0,
                        ).toBundle(),
                    )
                }.onSuccess {
                    finishingAfterExternalLaunch = true
                    finish()
                }.onFailure {
                    externalLaunchPending = false
                    window.decorView.animate().cancel()
                    window.decorView.alpha = 1f
                    window.decorView.translationY = 0f
                    android.widget.Toast.makeText(
                        applicationContext,
                        "Could not open the selected item",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
            }
            .start()
        return true
    }

    private fun widgetHost(): AppWidgetHost = commanderWidgetHost
        ?: AppWidgetHost(this, CommanderWidgets.HOST_ID).also { commanderWidgetHost = it }

    fun acquireWidgetHost() {
        widgetHostUsers++
        if (widgetHostUsers == 1) runCatching { widgetHost().startListening() }
    }

    fun releaseWidgetHost() {
        widgetHostUsers = (widgetHostUsers - 1).coerceAtLeast(0)
        if (widgetHostUsers == 0) runCatching { commanderWidgetHost?.stopListening() }
    }

    fun createWidgetView(appWidgetId: Int): AppWidgetHostView? {
        val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId) ?: return null
        return widgetHost().createView(this, appWidgetId, info).apply {
            setAppWidget(appWidgetId, info)
        }
    }

    fun prepareInbuiltWebView() {
        if (preparedWebView != null || isFinishing || isDestroyed) return
        preparedWebView = WebView(this).apply {
            // Warm the renderer and the connection used by the in-panel search.
            loadUrl("https://www.google.com/generate_204")
        }
    }

    fun takePreparedWebView(): WebView = preparedWebView?.also { preparedWebView = null } ?: WebView(this)

    fun releaseInbuiltWebView(view: WebView) {
        view.stopLoading()
        (view.parent as? android.view.ViewGroup)?.removeView(view)
        if (!isFinishing && !isDestroyed && preparedWebView == null) {
            view.loadUrl("about:blank")
            preparedWebView = view
        } else view.destroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val launchAt = SystemClock.uptimeMillis()
        super.onCreate(savedInstanceState)
        if (intent.action == Intent.ACTION_ASSIST) {
            // SystemUI may include this AOSP invocation hint. Record only the
            // integer, never screen/assist contents, while checking OEM support.
            Log.i("CommanderAssist", "invocation_type=${intent.getIntExtra("invocation_type", -1)}")
            if (intent.getIntExtra("invocation_type", -1) == 6 && AssistantPowerRouting.openSelected(this)) {
                finish()
                return
            }
        }
        HomeTypingAccessibilityService.captureAppBehindBar()
        Log.i("CommanderLaunch", "bar activity created")
        window.decorView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                window.decorView.viewTreeObserver.removeOnPreDrawListener(this)
                Log.i("CommanderLaunch", "bar first draw after ${SystemClock.uptimeMillis() - launchAt}ms")
                return true
            }
        })
        enableEdgeToEdge()

        window.setBackgroundDrawableResource(android.R.color.transparent)
        // Commander is a transient command/search surface, never a form that
        // should be filled. Excluding the complete window prevents password
        // managers from cold-starting their autofill service while the Bar
        // and keyboard are competing for the first launch frames.
        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val darkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        window.attributes = window.attributes.apply {
            dimAmount = if (AppearanceSettings.styleFor(darkMode) == BlurStyle.Inverted) 0.72f else 0.34f
        }
        // Requesting full-screen blur on a device that cannot draw it adds a
        // window effect to the launch path without producing any blur.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            getSystemService(WindowManager::class.java).isCrossWindowBlurEnabled
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply { blurBehindRadius = 42 }
        }
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED,
        )

        val engine = CommandEngine(this)
        currentQuery = savedInstanceState?.getString(STATE_QUERY)
            ?: intent.getStringExtra(EXTRA_INITIAL_QUERY).orEmpty()
        askPreviewState.value = intent.getStringExtra(EXTRA_ASK_PREVIEW)
            ?: ChatGptNotificationBridge.consumeResponse(this)
        askPromptState.value = intent.getStringExtra(EXTRA_ASK_PROMPT)
            ?: if (askPreviewState.value != null) ChatGptNotificationBridge.lastPrompt(this) else null
        askWaitingState.value = intent.getBooleanExtra(EXTRA_ASK_WAITING, false) && askPreviewState.value == null
        if (intent.getBooleanExtra(EXTRA_START_SWITCHER, false)) switcherRequestState.intValue++
        // Catalogue JSON and a possible package scan need not compete with
        // the translucent window and keyboard for the first launch frames.
        // A query typed before this runs still initializes the catalogue on
        // demand, so search behaviour is unchanged.
        lifecycleScope.launch(Dispatchers.IO) {
            delay(250)
            engine.warmAppIndex()
        }
        setContent {
            AccentSelectionProvider {
                CommandOverlayApp(
                    engine = engine,
                    onDismiss = ::finish,
                    initialQuery = currentQuery,
                    initialAskPreview = askPreviewState.value,
                    initialAskPrompt = askPromptState.value,
                    initialAskWaiting = askWaitingState.value,
                    initialSwitcherMode = intent.getBooleanExtra(EXTRA_START_SWITCHER, false),
                    initialTodoistMode = intent.getBooleanExtra(EXTRA_OPEN_TASKS, false),
                    switcherRequestToken = switcherRequestState.intValue,
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_QUERY, currentQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        inputActive = true
        if (widgetHostUsers > 0) runCatching { commanderWidgetHost?.startListening() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) Log.i("CommanderLaunch", "bar window focused")
        windowFocusedState.value = hasFocus
    }

    fun preserveQueryForMiniMode(value: String) {
        currentQuery = value
        intent.putExtra(EXTRA_INITIAL_QUERY, value)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_ASSIST) {
            Log.i("CommanderAssist", "invocation_type=${intent.getIntExtra("invocation_type", -1)}")
            if (intent.getIntExtra("invocation_type", -1) == 6 && AssistantPowerRouting.openSelected(this)) {
                finish()
                return
            }
        }
        intent.getStringExtra(EXTRA_ASK_PREVIEW)?.let {
            askPreviewState.value = it
            askWaitingState.value = false
            askPromptState.value = intent.getStringExtra(EXTRA_ASK_PROMPT)
                ?: ChatGptNotificationBridge.lastPrompt(this)
        }
        if (intent.getBooleanExtra(EXTRA_ASK_WAITING, false)) {
            askPromptState.value = intent.getStringExtra(EXTRA_ASK_PROMPT)
            askWaitingState.value = askPreviewState.value == null
        }
        if (intent.getBooleanExtra(EXTRA_START_SWITCHER, false)) switcherRequestState.intValue++
    }

    override fun onStop() {
        // A startup handoff must never outlive its command window. Otherwise
        // the accessibility service would keep routing home keys to an editor
        // that is no longer visible instead of opening a fresh overlay.
        HomeTypingHandoff.finish()
        HomeTypingAccessibilityService.clearAppBehindBar()
        inputActive = false
        runCatching { commanderWidgetHost?.stopListening() }
        super.onStop()
    }

    override fun onDestroy() {
        inputActive = false
        widgetHostUsers = 0
        runCatching { commanderWidgetHost?.stopListening() }
        preparedWebView?.destroy()
        preparedWebView = null
        super.onDestroy()
    }

    @Suppress("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (SwitcherKeyBridge.dispatch(event)) return true
        if (HomeTypingHandoff.isCollecting() && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            if (event.keyCode == KeyEvent.KEYCODE_DEL && HomeTypingHandoff.backspace()) return true
            if (event.isCtrlPressed || event.isMetaPressed) return super.dispatchKeyEvent(event)
            val unicode = event.getUnicodeChar(event.metaState)
            if (unicode != 0 && !Character.isISOControl(unicode)) {
                HomeTypingHandoff.append(String(Character.toChars(unicode)))
                return true
            }
        }
        if (HomeTypingHandoff.isCollecting() && event.action == KeyEvent.ACTION_DOWN && event.repeatCount > 0 &&
            HomeTypingSettings.holdFirstForAlt.value
        ) {
            // Home launches intentionally delay editor focus until the initial
            // typing burst is complete. Preserve the first-key hold shortcut
            // in that buffer so alternate command triggers still work.
            val alternateCodePoint = event.getUnicodeChar(
                event.metaState or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON,
            )
            val alternate = if (alternateCodePoint != 0 && !Character.isISOControl(alternateCodePoint)) {
                String(Character.toChars(alternateCodePoint))
            } else ""
            val symbol = alternate.singleOrNull()
            val isTrigger = symbol != null && (
                symbol == TriggerSettings.noteSymbol() ||
                    symbol == FileSearchSettings.trigger.value ||
                    CommandKind.entries.any { it != CommandKind.Note && it != CommandKind.Files && it.symbol == symbol } ||
                    AliasSettings.usesPrefix(this, symbol)
                )
            if (isTrigger && HomeTypingHandoff.convertHeldFirstKey(event.keyCode, alternate)) return true
        }
        return super.dispatchKeyEvent(event)
    }

    companion object {
        @Volatile private var inputActive = false
        fun isInputActive(): Boolean = inputActive
        const val ACTION_OPEN_OVERLAY = "com.astroboii47.commander.OPEN_OVERLAY"
        const val EXTRA_INITIAL_QUERY = "initial_query"
        const val EXTRA_ASK_PREVIEW = "ask_preview"
        const val EXTRA_ASK_WAITING = "ask_waiting"
        const val EXTRA_ASK_PROMPT = "ask_prompt"
        const val EXTRA_START_SWITCHER = "commander_start_switcher"
        const val EXTRA_OPEN_TASKS = "commander_open_tasks"
        private const val STATE_QUERY = "commander_query"
    }
}
