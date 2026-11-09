package com.astroboii47.commander

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class HubActivity : ComponentActivity() {
    private var accessState by mutableStateOf(false)
    private val consumedKeys = mutableSetOf<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ModuleSettings.hubEnabled.value) { finish(); return }
        enableEdgeToEdge()
        hideStatusBarNow()
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = window.attributes.apply {
            dimAmount = .52f
            if (Build.VERSION.SDK_INT >= 31) blurBehindRadius = 72
        }
        if (Build.VERSION.SDK_INT >= 31) window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        setContent {
            AccentSelectionProvider {
                MinimalHubApp(
                    hasAccess = accessState,
                    openAccessSettings = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    onDismiss = ::finish,
                    forceWallpaperGlass = true,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayResumed = true
        accessState = hasNotificationAccess()
        hideStatusBarNow()
        window.decorView.postDelayed(::hideStatusBarNow, 80)
        window.decorView.postDelayed(::hideStatusBarNow, 240)
    }

    override fun onPause() {
        overlayResumed = false
        super.onPause()
    }

    companion object {
        @Volatile var overlayResumed: Boolean = false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideStatusBarNow()
    }

    private fun hideStatusBarNow() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    @Suppress("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val enter = event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            val handled = if (enter) HubKeyBridge.handleEnter() else HubKeyBridge.handleKey(event.keyCode)
            if (handled) {
                consumedKeys += event.keyCode
                return true
            }
        }
        if (event.action == KeyEvent.ACTION_UP && consumedKeys.remove(event.keyCode)) return true
        return super.dispatchKeyEvent(event)
    }

    private fun hasNotificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners").orEmpty()
        return enabled.split(':').mapNotNull(ComponentName::unflattenFromString).any { it.packageName == packageName }
    }
}
