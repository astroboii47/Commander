package com.astroboii47.commander

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

class HomeTypingAccessibilityService : AccessibilityService() {
    private var foregroundPackage: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var automationGeneration = 0
    private var heldHomeKeyCode = KeyEvent.KEYCODE_UNKNOWN
    private var heldHomeKeyConversion: Runnable? = null

    override fun onServiceConnected() {
        activeService = WeakReference(this)
        setContentMonitoring(false)
    }

    override fun onDestroy() {
        if (activeService?.get() === this) activeService = null
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() == "com.facebook.orca" &&
            (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        ) {
            tryFillMessengerDraft(rootInActiveWindow ?: event.source)
        }
        if (event?.packageName?.toString() == "com.openai.chatgpt" &&
            (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        ) {
            if (ChatGptNotificationBridge.trySubmitFromAccessibility(this, rootInActiveWindow ?: event.source)) {
                // Return to the activity that was behind ChatGPT. Keeping a
                // translucent Command window over ChatGPT suppresses response
                // notifications on some ChatGPT builds and looks like a flash.
                performGlobalAction(GLOBAL_ACTION_BACK)
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(
                        Intent(this, OverlayActivity::class.java)
                            .setAction(OverlayActivity.ACTION_OPEN_OVERLAY)
                            .putExtra(OverlayActivity.EXTRA_ASK_WAITING, true)
                            .putExtra(OverlayActivity.EXTRA_ASK_PROMPT, ChatGptNotificationBridge.lastPrompt(this))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    )
                }, 700L)
            }
        }
        if (event?.packageName?.toString() == "com.onepassword.android" &&
            (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        ) {
            if (OnePasswordSearchBridge.tryFill(this, rootInActiveWindow ?: event.source)) {
                finishAutomationMonitoring()
            }
        }
        if (event?.packageName?.toString() == "com.google.android.gm" &&
            (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        ) {
            if (GmailSearchBridge.tryFill(this, rootInActiveWindow ?: event.source)) {
                finishAutomationMonitoring()
            }
        }
        // TYPE_WINDOWS_CHANGED is also emitted for transient System UI panels,
        // IME surfaces and accessibility overlays. Do not let those replace the
        // last real activity package or home typing will appear to stop at random.
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.packageName?.toString()?.let { foregroundPackage = it }
        }
    }

    override fun onInterrupt() = Unit

    private fun enableAutomationMonitoring(durationMs: Long) {
        val generation = ++automationGeneration
        setContentMonitoring(true)
        mainHandler.postDelayed({
            if (automationGeneration == generation) setContentMonitoring(false)
        }, durationMs)
    }

    private fun finishAutomationMonitoring() {
        automationGeneration++
        setContentMonitoring(false)
    }

    private fun setContentMonitoring(enabled: Boolean) {
        val current = serviceInfo ?: return
        val wanted = BASE_EVENT_TYPES or if (enabled) AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED else 0
        if (current.eventTypes == wanted) return
        current.eventTypes = wanted
        serviceInfo = current
    }

    private fun tryFillMessengerDraft(root: AccessibilityNodeInfo?) {
        val draft = MessengerDraftBridge.pending(this) ?: return
        if (root == null) return
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        fun collect(node: AccessibilityNodeInfo) {
            if (node.isEditable && node.isEnabled) candidates += node
            for (index in 0 until node.childCount) node.getChild(index)?.let(::collect)
        }
        collect(root)
        val composer = candidates.lastOrNull { node ->
            val hint = if (android.os.Build.VERSION.SDK_INT >= 26) node.hintText?.toString().orEmpty() else ""
            val description = node.contentDescription?.toString().orEmpty()
            hint.contains("message", true) || description.contains("message", true) || node.className?.toString()?.contains("EditText") == true
        } ?: return
        val arguments = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, draft)
        }
        if (composer.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
            composer.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            MessengerDraftBridge.clear(this)
            finishAutomationMonitoring()
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!HomeTypingSettings.enabled.value) return false
        if (event.action == KeyEvent.ACTION_UP) {
            if (event.keyCode == heldHomeKeyCode) cancelHeldHomeKeyConversion()
            return false
        }
        if (event.action != KeyEvent.ACTION_DOWN) return false
        if (event.repeatCount > 0) {
            if (!HomeTypingSettings.holdFirstForAlt.value || !HomeTypingHandoff.isCollecting()) return false
            val alternateCodePoint = event.getUnicodeChar(event.metaState or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON)
            if (alternateCodePoint == 0 || Character.isISOControl(alternateCodePoint)) return false
            val alternate = String(Character.toChars(alternateCodePoint))
            val symbol = alternate.singleOrNull() ?: return false
            val isCommandTrigger = symbol == TriggerSettings.noteSymbol() ||
                symbol == FileSearchSettings.trigger.value ||
                CommandKind.entries.any { it != CommandKind.Note && it != CommandKind.Files && it.symbol == symbol } ||
                AliasSettings.usesPrefix(this, symbol)
            return isCommandTrigger && HomeTypingHandoff.convertHeldFirstKey(event.keyCode, alternate)
        }
        // Once the first home key starts Command, the launcher loses focus
        // before Command's editor/input connection is ready. Capture keys in
        // that short transition instead of allowing them to disappear.
        if (HomeTypingHandoff.isCollecting()) {
            if (event.keyCode != heldHomeKeyCode) cancelHeldHomeKeyConversion()
            if (event.keyCode == KeyEvent.KEYCODE_DEL) {
                if (HomeTypingHandoff.backspace()) return true
                HomeTypingHandoff.finish()
                return false
            }
            if (event.isCtrlPressed || event.isMetaPressed) return false
            val unicode = event.getUnicodeChar(event.metaState)
            if (unicode == 0 || Character.isISOControl(unicode)) return false
            HomeTypingHandoff.append(String(Character.toChars(unicode)))
            return true
        }
        val homePackage = packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY,
        )?.activityInfo?.packageName ?: return false
        if (!isHomeFocused(homePackage)) return false
        // Alt is how compact physical keyboards produce punctuation such as
        // @, -, and ?. Accept it when Android resolves a printable character.
        if (event.isCtrlPressed || event.isMetaPressed) return false
        val unicode = event.getUnicodeChar(event.metaState)
        if (unicode == 0 || Character.isISOControl(unicode)) return false
        val initial = String(Character.toChars(unicode))
        HomeTypingHandoff.begin(initial, event.keyCode)
        scheduleHeldHomeKeyConversion(event)
        startActivity(
            Intent(this, OverlayActivity::class.java)
                .setAction(OverlayActivity.ACTION_OPEN_OVERLAY)
                .putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, initial)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
        return true
    }

    private fun scheduleHeldHomeKeyConversion(event: KeyEvent) {
        cancelHeldHomeKeyConversion()
        if (!HomeTypingSettings.holdFirstForAlt.value) return
        val alternateCodePoint = event.getUnicodeChar(
            event.metaState or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON,
        )
        if (alternateCodePoint == 0 || Character.isISOControl(alternateCodePoint)) return
        val alternate = String(Character.toChars(alternateCodePoint))
        val symbol = alternate.singleOrNull() ?: return
        val isCommandTrigger = symbol == TriggerSettings.noteSymbol() ||
            symbol == FileSearchSettings.trigger.value ||
            CommandKind.entries.any { it != CommandKind.Note && it != CommandKind.Files && it.symbol == symbol } ||
            AliasSettings.usesPrefix(this, symbol)
        if (!isCommandTrigger) return
        heldHomeKeyCode = event.keyCode
        heldHomeKeyConversion = Runnable {
            HomeTypingHandoff.convertHeldFirstKey(event.keyCode, alternate)
            heldHomeKeyConversion = null
            heldHomeKeyCode = KeyEvent.KEYCODE_UNKNOWN
        }.also { mainHandler.postDelayed(it, HOLD_ALT_DELAY_MS) }
    }

    private fun cancelHeldHomeKeyConversion() {
        heldHomeKeyConversion?.let(mainHandler::removeCallbacks)
        heldHomeKeyConversion = null
        heldHomeKeyCode = KeyEvent.KEYCODE_UNKNOWN
    }

    private fun isHomeFocused(homePackage: String): Boolean {
        // Prefer the focused application window. Unlike rootInActiveWindow this
        // is not displaced by a notification shade, keyboard surface or other
        // short-lived accessibility overlay layered over the launcher.
        val focusedApplicationPackages = windows.asSequence()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION && (it.isFocused || it.isActive) }
            .mapNotNull { it.root?.packageName?.toString() }
            .toSet()
        if (homePackage in focusedApplicationPackages) return true
        if (focusedApplicationPackages.isNotEmpty()) return false

        // Some vendor builds briefly expose no focused accessibility window
        // while focus is settling. Fall back only in that gap.
        return rootInActiveWindow?.packageName?.toString() == homePackage ||
            foregroundPackage == homePackage
    }

    companion object {
        private const val BASE_EVENT_TYPES =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
        private const val HOLD_ALT_DELAY_MS = 430L
        private var activeService: WeakReference<HomeTypingAccessibilityService>? = null

        fun monitorAutomation(durationMs: Long) {
            activeService?.get()?.enableAutomationMonitoring(durationMs)
        }

        fun automationFinished() {
            activeService?.get()?.finishAutomationMonitoring()
        }

        fun isConnected(): Boolean = activeService?.get() != null
    }
}
