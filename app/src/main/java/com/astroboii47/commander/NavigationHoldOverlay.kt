package com.astroboii47.commander

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import kotlin.math.abs

/** An opt-in, narrowly bounded touch target over the navigation handle. */
internal class NavigationHoldOverlay(private val service: AccessibilityService) {
    private val preferences = service.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var touchTarget: View? = null
    private var pressed = false
    private var triggered = false
    private var startX = 0f
    private var startY = 0f
    private val touchSlop = ViewConfiguration.get(service).scaledTouchSlop * 1.5f
    private val settingsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in setOf(KEY_ENABLED, KEY_WIDTH, KEY_HOLD)) refresh()
    }
    private val hold = Runnable {
        if (pressed && !triggered && !OverlayActivity.isInputActive()) {
            triggered = true
            touchTarget?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            service.startActivity(
                Intent(service, OverlayActivity::class.java)
                    .setAction(OverlayActivity.ACTION_OPEN_OVERLAY)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        }
    }

    fun start() {
        preferences.registerOnSharedPreferenceChangeListener(settingsListener)
        refresh()
    }

    fun stop() {
        preferences.unregisterOnSharedPreferenceChangeListener(settingsListener)
        removeTarget()
    }

    private fun refresh() {
        removeTarget()
        if (!preferences.getBoolean(KEY_ENABLED, false)) return
        val view = View(service).apply { setOnTouchListener { _, event -> handleTouch(event) } }
        val density = service.resources.displayMetrics.density
        val width = preferences.getInt(KEY_WIDTH, DEFAULT_WIDTH_DP).coerceIn(48, 144)
        val params = WindowManager.LayoutParams(
            (width * density).toInt(),
            (20 * density).toInt(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 0
        }
        runCatching { windowManager?.addView(view, params) }
            .onSuccess { touchTarget = view }
    }

    private fun removeTarget() {
        pressed = false
        triggered = false
        handler.removeCallbacks(hold)
        touchTarget?.let { view -> runCatching { windowManager?.removeView(view) } }
        touchTarget = null
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressed = true
                triggered = false
                startX = event.x
                startY = event.y
                handler.postDelayed(hold, preferences.getInt(KEY_HOLD, DEFAULT_HOLD_MS).coerceIn(350, 850).toLong())
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - startX) > touchSlop || abs(event.y - startY) > touchSlop) {
                    pressed = false
                    handler.removeCallbacks(hold)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressed = false
                handler.removeCallbacks(hold)
            }
        }
        return true
    }

    companion object {
        const val PREFS = "navigation_hold_gesture"
        const val KEY_ENABLED = "enabled"
        const val KEY_WIDTH = "width_dp"
        const val KEY_HOLD = "hold_ms"
        const val DEFAULT_WIDTH_DP = 72
        const val DEFAULT_HOLD_MS = 500
    }
}
