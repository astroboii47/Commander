package com.astroboii47.commander

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.view.KeyEvent

class OverlayActivity : ComponentActivity() {
    private val askPreviewState = mutableStateOf<String?>(null)
    private val askPromptState = mutableStateOf<String?>(null)
    private val askWaitingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = window.attributes.apply {
            dimAmount = if (AppearanceSettings.invertBlurDarkness.value) 0.58f else 0.34f
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply { blurBehindRadius = 42 }
        }
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN,
        )

        val engine = CommandEngine(this)
        askPreviewState.value = intent.getStringExtra(EXTRA_ASK_PREVIEW)
            ?: ChatGptNotificationBridge.consumeResponse(this)
        askPromptState.value = intent.getStringExtra(EXTRA_ASK_PROMPT)
            ?: if (askPreviewState.value != null) ChatGptNotificationBridge.lastPrompt(this) else null
        askWaitingState.value = intent.getBooleanExtra(EXTRA_ASK_WAITING, false) && askPreviewState.value == null
        lifecycleScope.launch(Dispatchers.IO) { engine.warmAppIndex() }
        setContent {
            AccentSelectionProvider {
            CommandOverlayApp(
                engine = engine,
                onDismiss = ::finish,
                initialQuery = intent.getStringExtra(EXTRA_INITIAL_QUERY).orEmpty(),
                initialAskPreview = askPreviewState.value,
                initialAskPrompt = askPromptState.value,
                initialAskWaiting = askWaitingState.value,
            )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
    }

    override fun onStop() {
        // A startup handoff must never outlive its command window. Otherwise
        // the accessibility service would keep routing home keys to an editor
        // that is no longer visible instead of opening a fresh overlay.
        HomeTypingHandoff.finish()
        super.onStop()
    }

    @Suppress("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (HomeTypingHandoff.isCollecting() && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            if (event.keyCode == KeyEvent.KEYCODE_DEL && HomeTypingHandoff.backspace()) return true
            if (event.isCtrlPressed || event.isMetaPressed) return super.dispatchKeyEvent(event)
            val unicode = event.getUnicodeChar(event.metaState)
            if (unicode != 0 && !Character.isISOControl(unicode)) {
                HomeTypingHandoff.append(String(Character.toChars(unicode)))
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    companion object {
        const val ACTION_OPEN_OVERLAY = "com.astroboii47.commander.OPEN_OVERLAY"
        const val EXTRA_INITIAL_QUERY = "initial_query"
        const val EXTRA_ASK_PREVIEW = "ask_preview"
        const val EXTRA_ASK_WAITING = "ask_waiting"
        const val EXTRA_ASK_PROMPT = "ask_prompt"
    }
}
