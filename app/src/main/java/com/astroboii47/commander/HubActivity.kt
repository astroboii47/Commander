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

class HubActivity : ComponentActivity() {
    private var accessState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccentSelectionProvider {
            MinimalHubApp(
                hasAccess = accessState,
                openAccessSettings = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                onDismiss = ::finish,
            )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accessState = hasNotificationAccess()
    }

    @Suppress("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val enter = event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
        if (enter && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 && HubKeyBridge.handleEnter()) return true
        if (!enter && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 && HubKeyBridge.handleKey(event.keyCode)) return true
        // Consume the matching release only while a reply editor is active.
        if (enter && event.action == KeyEvent.ACTION_UP && HubKeyBridge.canSend?.invoke() == true) return true
        return super.dispatchKeyEvent(event)
    }

    private fun hasNotificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners").orEmpty()
        return enabled.split(':').mapNotNull(ComponentName::unflattenFromString).any { it.packageName == packageName }
    }
}
