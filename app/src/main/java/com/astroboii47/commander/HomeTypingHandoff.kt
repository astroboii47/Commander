package com.astroboii47.commander

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf

/** Buffers physical keys while the home-triggered overlay is gaining focus. */
object HomeTypingHandoff {
    val text = mutableStateOf("")
    @Volatile private var collecting = false
    @Volatile private var lastKeyAt = 0L
    @Volatile private var startedAt = 0L
    @Volatile private var firstKeyCode = KeyEvent.KEYCODE_UNKNOWN
    @Volatile private var firstKeyConverted = false

    fun begin(initial: String, keyCode: Int = KeyEvent.KEYCODE_UNKNOWN) {
        text.value = initial
        val now = SystemClock.elapsedRealtime()
        lastKeyAt = now
        startedAt = now
        firstKeyCode = keyCode
        firstKeyConverted = false
        collecting = true
    }

    fun convertHeldFirstKey(keyCode: Int, alternate: String): Boolean {
        if (!isCollecting() || firstKeyConverted || keyCode != firstKeyCode || alternate.isEmpty()) return false
        if (text.value.length != 1) return false
        text.value = alternate
        lastKeyAt = SystemClock.elapsedRealtime()
        firstKeyConverted = true
        return true
    }

    fun isCollecting(): Boolean {
        val now = SystemClock.elapsedRealtime()
        // The handoff exists only while an overlay gains focus. The absolute
        // deadline prevents an orphaned window from extending its own lifetime
        // forever by continuing to consume physical keys.
        if (collecting && (now - lastKeyAt > 3_000L || now - startedAt > 5_000L)) {
            finish()
        }
        return collecting
    }

    fun append(value: String) {
        if (isCollecting()) {
            text.value += value
            lastKeyAt = SystemClock.elapsedRealtime()
        }
    }

    /** Returns true when a buffered character was removed. */
    fun backspace(): Boolean {
        if (!isCollecting() || text.value.isEmpty()) return false
        text.value = text.value.dropLast(1)
        lastKeyAt = SystemClock.elapsedRealtime()
        return true
    }

    fun finish() {
        collecting = false
    }
}
