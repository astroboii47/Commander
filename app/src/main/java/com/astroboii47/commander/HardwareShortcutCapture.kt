package com.astroboii47.commander

import android.content.Context
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf

object HardwareShortcutCapture {
    val target = mutableStateOf<HardwareShortcutAction?>(null)
    val revision = mutableStateOf(0)
    private var firstEvent: KeyEvent? = null
    private val trailingKeyUps = mutableSetOf<Int>()

    fun begin(action: HardwareShortcutAction) {
        firstEvent = null
        trailingKeyUps.clear()
        target.value = action
    }

    fun cancel() {
        firstEvent = null
        target.value = null
    }

    fun bindingChanged() {
        revision.value += 1
    }

    fun onKeyEvent(context: Context, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && trailingKeyUps.remove(event.keyCode)) return true
        val action = target.value ?: return false
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            val first = firstEvent
            if (first == null) {
                firstEvent = KeyEvent(event)
            } else if (event.keyCode != first.keyCode) {
                trailingKeyUps += first.keyCode
                trailingKeyUps += event.keyCode
                HardwareShortcutSettings.saveBinding(
                    context,
                    action,
                    HardwareKeyBinding(first.keyCode, 0, event.keyCode),
                )
                firstEvent = null
                target.value = null
                bindingChanged()
            }
            return true
        }
        if (event.action == KeyEvent.ACTION_UP) {
            val first = firstEvent
            if (first != null && first.keyCode == event.keyCode) {
                HardwareShortcutSettings.saveBinding(context, action, HardwareKeyBinding(first.keyCode, 0))
                firstEvent = null
                target.value = null
                bindingChanged()
            }
            return true
        }
        return true
    }
}
