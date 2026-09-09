package com.astroboii47.commander

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.graphics.Rect
import android.graphics.Path
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

class HomeTypingAccessibilityService : AccessibilityService() {
    private var foregroundPackage: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var automationGeneration = 0
    private var automationContentMonitoring = false
    private var keyControlContentMonitoring = false
    private var heldHomeKeyCode = KeyEvent.KEYCODE_UNKNOWN
    private var heldHomeKeyConversion: Runnable? = null
    private data class PendingHardwareChord(
        val action: HardwareShortcutAction,
        val keys: Set<Int>,
        val newestDownTime: Long,
    )
    private val hardwareShortcutKeysDown = mutableMapOf<Int, Long>()
    private var pendingHardwareChord: PendingHardwareChord? = null
    private val consumedHardwareChordKeys = mutableSetOf<Int>()
    private var holdSwitcherPrimaryKey = KeyEvent.KEYCODE_UNKNOWN
    private var holdSwitcherStepKey = KeyEvent.KEYCODE_UNKNOWN
    private var holdSwitcherReverseKey = KeyEvent.KEYCODE_UNKNOWN
    private var holdSwitcherDismissKey = KeyEvent.KEYCODE_UNKNOWN
    private val keyControl by lazy { KeyControlSession(this) }
    private enum class SplitStage { WaitingForApp, OpeningRecents, OpeningMenu }
    private data class PendingSplit(
        val taskId: Int,
        val packageName: String,
        val generation: Int,
        val stage: SplitStage = SplitStage.WaitingForApp,
    )
    private var pendingSplit: PendingSplit? = null
    private var splitGeneration = 0

    override fun onServiceConnected() {
        activeService = WeakReference(this)
        automationContentMonitoring = false
        keyControlContentMonitoring = false
        updateContentMonitoring()
    }

    override fun onDestroy() {
        keyControl.stop()
        if (activeService?.get() === this) activeService = null
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        keyControl.onAccessibilityEvent(event)
        handlePendingSplit(event)
        if (event?.packageName?.toString() == "com.facebook.orca" &&
            (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        ) {
            val root = rootInActiveWindow ?: event.source
            tryPrepareMessengerShare(root)
            tryFillMessengerDraft(root)
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
                    launchOverlay(
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
            event.packageName?.toString()?.let { packageName ->
                foregroundPackage = packageName
            }
        }
    }

    private fun launchOverlay(intent: Intent) {
        startActivity(intent)
    }

    private fun handlePendingSplit(event: AccessibilityEvent?) {
        val split = pendingSplit ?: return
        val packageName = event?.packageName?.toString().orEmpty()
        when (split.stage) {
            SplitStage.WaitingForApp -> {
                if (packageName != split.packageName ||
                    event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                ) return
                pendingSplit = split.copy(stage = SplitStage.OpeningRecents)
                enableAutomationMonitoring(4_000L)
                mainHandler.post { performGlobalAction(GLOBAL_ACTION_RECENTS) }
            }
            SplitStage.OpeningRecents -> {
                if (packageName != RECENTS_PACKAGE) return
                attemptOpenRecentsTaskMenu(split.generation, 0)
            }
            SplitStage.OpeningMenu -> {
                if (packageName != RECENTS_PACKAGE) return
                attemptSelectSplitScreen(split.generation, 0)
            }
        }
    }

    private fun attemptOpenRecentsTaskMenu(generation: Int, attempt: Int) {
        val split = pendingSplit ?: return
        if (split.generation != generation || split.stage != SplitStage.OpeningRecents) return
        val root = rootInActiveWindow
        val task = root?.let(::centeredRecentsTask)
        val nativeSplitAction = task?.actionList?.firstOrNull { action ->
            action.label?.toString()?.equals("Split screen", true) == true
        }
        val nativeActionResult = if (task != null && nativeSplitAction != null) {
            task.performAction(nativeSplitAction.id)
        } else false
        if (nativeActionResult) {
            cancelPendingSplit()
            return
        }
        val icon = task?.let { findNode(it) { node ->
            node.viewIdResourceName == "$RECENTS_PACKAGE:id/icon" && node.isClickable
        } }
        if (task != null && icon != null && tapRecentsTaskIcon(task, icon, generation)) {
            // The gesture callback advances the state after Launcher receives
            // the tap. AGUI reports the icon with inverted vertical bounds, so
            // ACTION_CLICK cannot be trusted here.
        } else if (attempt < 12) {
            mainHandler.postDelayed({ attemptOpenRecentsTaskMenu(generation, attempt + 1) }, 50L)
        } else {
            cancelPendingSplit()
        }
    }

    private fun tapRecentsTaskIcon(
        task: AccessibilityNodeInfo,
        icon: AccessibilityNodeInfo,
        generation: Int,
    ): Boolean {
        val taskBounds = Rect().also(task::getBoundsInScreen)
        val iconBounds = Rect().also(icon::getBoundsInScreen)
        if (taskBounds.width() <= 0) return false
        val x = if (iconBounds.width() > 0) iconBounds.centerX().toFloat() else taskBounds.centerX().toFloat()
        // This AGUI launcher clips the icon node at the task card's top and
        // returns top > bottom. Its bottom still marks the true icon bottom.
        val iconSize = if (iconBounds.width() > 0) iconBounds.width() else (48 * resources.displayMetrics.density).toInt()
        val y = if (iconBounds.bottom > 0) {
            iconBounds.bottom - iconSize / 2f
        } else {
            taskBounds.top - iconSize * 2f / 3f
        }
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 1L))
            .build()
        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                val current = pendingSplit ?: return
                if (current.generation != generation || current.stage != SplitStage.OpeningRecents) return
                pendingSplit = current.copy(stage = SplitStage.OpeningMenu)
                mainHandler.postDelayed({ attemptSelectSplitScreen(generation, 0) }, 50L)
            }
        }, null)
    }

    private fun attemptSelectSplitScreen(generation: Int, attempt: Int) {
        val split = pendingSplit ?: return
        if (split.generation != generation || split.stage != SplitStage.OpeningMenu) return
        val label = rootInActiveWindow?.let { root ->
            findNode(root) { node -> node.text?.toString()?.equals("Split screen", true) == true }
        }
        val action = label?.let(::clickableAncestor)
        if (action?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
            cancelPendingSplit()
        } else if (attempt < 12) {
            mainHandler.postDelayed({ attemptSelectSplitScreen(generation, attempt + 1) }, 50L)
        } else {
            cancelPendingSplit()
        }
    }

    private fun centeredRecentsTask(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val displayCenter = resources.displayMetrics.widthPixels / 2
        val tasks = mutableListOf<Pair<Int, AccessibilityNodeInfo>>()
        fun visit(node: AccessibilityNodeInfo) {
            if (node.viewIdResourceName == "$RECENTS_PACKAGE:id/task_view_single") {
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                if (bounds.width() > 0) tasks += kotlin.math.abs(bounds.centerX() - displayCenter) to node
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        visit(root)
        return tasks.minByOrNull { it.first }?.second
    }

    private fun findNode(
        root: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (predicate(root)) return root
        for (index in 0 until root.childCount) {
            val found = root.getChild(index)?.let { findNode(it, predicate) }
            if (found != null) return found
        }
        return null
    }

    private fun clickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        repeat(5) {
            if (current?.isClickable == true) return current
            current = current?.parent
        }
        return null
    }

    private fun cancelPendingSplit() {
        pendingSplit = null
        finishAutomationMonitoring()
    }

    override fun onInterrupt() = Unit

    private fun enableAutomationMonitoring(durationMs: Long) {
        val generation = ++automationGeneration
        automationContentMonitoring = true
        updateContentMonitoring()
        mainHandler.postDelayed({
            if (automationGeneration == generation) {
                automationContentMonitoring = false
                updateContentMonitoring()
            }
        }, durationMs)
    }

    private fun finishAutomationMonitoring() {
        automationGeneration++
        automationContentMonitoring = false
        updateContentMonitoring()
    }

    internal fun setKeyControlContentMonitoring(enabled: Boolean) {
        keyControlContentMonitoring = enabled
        updateContentMonitoring()
    }

    private fun updateContentMonitoring() {
        val current = serviceInfo ?: return
        val enabled = automationContentMonitoring || keyControlContentMonitoring
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

    private fun tryPrepareMessengerShare(root: AccessibilityNodeInfo?) {
        val pending = MessengerShareSearchBridge.pending(this) ?: return
        if (root == null || pending.ready) return
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        fun collect(node: AccessibilityNodeInfo) {
            nodes += node
            for (index in 0 until node.childCount) node.getChild(index)?.let(::collect)
        }
        collect(root)

        fun normalized(value: CharSequence?): String = value?.toString().orEmpty()
            .trim().trimEnd('.')
            .lowercase()
            .replace(Regex("\\s+"), " ")
        val target = normalized(pending.target)
        val matchingSendButtons = nodes.filter { node ->
            if (!node.isClickable || !node.isEnabled) return@filter false
            val description = normalized(node.contentDescription)
            description == "send, button, send to $target" || description == "send to $target"
        }
        if (matchingSendButtons.size == 1) {
            val button = matchingSendButtons.single()
            button.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            button.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
            MessengerShareSearchBridge.markReady(this)
            android.widget.Toast.makeText(
                this,
                "Check ${pending.target}, then press Send",
                android.widget.Toast.LENGTH_SHORT,
            ).show()
            return
        }
        if (matchingSendButtons.size > 1) {
            MessengerShareSearchBridge.markReady(this)
            android.widget.Toast.makeText(
                this,
                "Multiple Messenger matches · choose manually",
                android.widget.Toast.LENGTH_SHORT,
            ).show()
            return
        }

        if (pending.stage == 0) {
            val search = nodes.firstOrNull { node ->
                node.isClickable && node.isEnabled && (
                    normalized(node.contentDescription) == "search" || normalized(node.text) == "search"
                )
            }
            if (search?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                MessengerShareSearchBridge.setStage(this, 1)
            }
            return
        }

        if (pending.stage == 1) {
            val editor = nodes.firstOrNull { it.isEditable && it.isEnabled } ?: return
            val arguments = android.os.Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pending.target)
            }
            if (editor.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                editor.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                MessengerShareSearchBridge.setStage(this, 2)
            }
        }
    }


    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (HardwareShortcutCapture.onKeyEvent(this, event)) return true
        if (holdSwitcherPrimaryKey != KeyEvent.KEYCODE_UNKNOWN) {
            when {
                event.keyCode == holdSwitcherStepKey && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 -> {
                    HoldSwitcherBridge.advance()
                    return true
                }
                event.keyCode == holdSwitcherStepKey -> {
                    hardwareShortcutKeysDown.remove(event.keyCode)
                    return true
                }
                event.keyCode == holdSwitcherReverseKey && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 -> {
                    HoldSwitcherBridge.reverse()
                    return true
                }
                event.keyCode == holdSwitcherReverseKey -> return true
                event.keyCode == holdSwitcherDismissKey && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 -> {
                    HoldSwitcherBridge.dismiss()
                    return true
                }
                event.keyCode == holdSwitcherDismissKey -> return true
                event.keyCode == holdSwitcherPrimaryKey && event.action == KeyEvent.ACTION_UP -> {
                    hardwareShortcutKeysDown.remove(event.keyCode)
                    HoldSwitcherBridge.commit()
                    holdSwitcherPrimaryKey = KeyEvent.KEYCODE_UNKNOWN
                    holdSwitcherStepKey = KeyEvent.KEYCODE_UNKNOWN
                    holdSwitcherReverseKey = KeyEvent.KEYCODE_UNKNOWN
                    holdSwitcherDismissKey = KeyEvent.KEYCODE_UNKNOWN
                    return true
                }
                event.keyCode == holdSwitcherPrimaryKey -> return true
            }
        }
        // Commander's editor already handles the arrows produced by Pastiera.
        // Do not claim TAB/SYM as the start of a global hardware shortcut while
        // that editor owns the active window, otherwise Pastiera sees only the
        // following I/J/K/L key and inserts it as text. An active hold-switcher
        // session is handled above and deliberately remains global.
        // During a home launch, keep the short startup handoff in the
        // accessibility service even after Commander has become the focused
        // window. This lets the IME appear immediately without allowing its
        // reconnect to swallow the first typing burst.
        if (OverlayActivity.isInputActive() && !HomeTypingHandoff.isCollecting()) {
            hardwareShortcutKeysDown.clear()
            pendingHardwareChord = null
            return false
        }
        if (HardwareShortcutSettings.enabled.value && !HardwareShortcutSettings.allowWhileTyping.value &&
            hardwareShortcutKeysDown.isEmpty() && pendingHardwareChord == null && isTypingContext()
        ) {
            hardwareShortcutKeysDown.clear()
            pendingHardwareChord = null
            return false
        }
        if (HardwareShortcutSettings.enabled.value && event.action == KeyEvent.ACTION_UP) {
            val chord = pendingHardwareChord
            if (chord != null && event.keyCode in chord.keys) {
                pendingHardwareChord = null
                consumedHardwareChordKeys += chord.keys - event.keyCode
                hardwareShortcutKeysDown.remove(event.keyCode)
                val pressDuration = event.eventTime - chord.newestDownTime
                if (pressDuration >= MIN_HARDWARE_SHORTCUT_PRESS_MS) performHardwareShortcut(chord.action)
                return true
            }
            if (consumedHardwareChordKeys.remove(event.keyCode)) {
                hardwareShortcutKeysDown.remove(event.keyCode)
                return true
            }
            val downTime = hardwareShortcutKeysDown.remove(event.keyCode)
            if (downTime != null) {
                val single = HardwareShortcutAction.entries.firstOrNull { action ->
                    if (action.isSwitcherAuxiliary()) return@firstOrNull false
                    HardwareShortcutSettings.binding(this, action)?.let { binding ->
                        binding.secondKeyCode == KeyEvent.KEYCODE_UNKNOWN && binding.keyCode == event.keyCode
                    } == true
                }
                if (single != null && event.eventTime - downTime >= MIN_HARDWARE_SHORTCUT_PRESS_MS) {
                    performHardwareShortcut(single)
                }
                return true
            }
        }
        if (HardwareShortcutSettings.enabled.value && event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode in hardwareShortcutKeysDown) return true
            if (event.repeatCount > 0) return false
            hardwareShortcutKeysDown[event.keyCode] = event.downTime
            val chordAction = HardwareShortcutAction.entries.firstOrNull { action ->
                if (action.isSwitcherAuxiliary()) return@firstOrNull false
                HardwareShortcutSettings.binding(this, action)?.let { binding ->
                    binding.secondKeyCode != KeyEvent.KEYCODE_UNKNOWN &&
                        binding.keyCode in hardwareShortcutKeysDown &&
                        binding.secondKeyCode in hardwareShortcutKeysDown
                } == true
            }
            if (chordAction != null) {
                val binding = HardwareShortcutSettings.binding(this, chordAction)!!
                if (chordAction == HardwareShortcutAction.Switcher && HardwareShortcutSettings.holdSwitcher.value) {
                    holdSwitcherPrimaryKey = binding.keyCode
                    holdSwitcherStepKey = binding.secondKeyCode
                    holdSwitcherReverseKey = HardwareShortcutSettings.binding(this, HardwareShortcutAction.SwitcherReverse)
                        ?.takeIf { it.keyCode == binding.keyCode }?.secondKeyCode ?: KeyEvent.KEYCODE_UNKNOWN
                    holdSwitcherDismissKey = HardwareShortcutSettings.binding(this, HardwareShortcutAction.SwitcherDismiss)
                        ?.takeIf { it.keyCode == binding.keyCode }?.secondKeyCode ?: KeyEvent.KEYCODE_UNKNOWN
                    hardwareShortcutKeysDown.remove(binding.secondKeyCode)
                    pendingHardwareChord = null
                    val activePackage = rootInActiveWindow?.packageName?.toString()
                        ?.takeUnless { it == packageName }
                        ?: foregroundPackage
                    HoldSwitcherBridge.start(activePackage)
                    launchOverlay(
                        Intent(this, OverlayActivity::class.java)
                            .setAction(OverlayActivity.ACTION_OPEN_OVERLAY)
                            .putExtra(OverlayActivity.EXTRA_START_SWITCHER, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                    )
                    return true
                }
                pendingHardwareChord = PendingHardwareChord(
                    chordAction,
                    setOf(binding.keyCode, binding.secondKeyCode),
                    maxOf(hardwareShortcutKeysDown[binding.keyCode] ?: 0L, hardwareShortcutKeysDown[binding.secondKeyCode] ?: 0L),
                )
                return true
            }
            val startsBinding = HardwareShortcutAction.entries.any { action ->
                HardwareShortcutSettings.binding(this, action)?.let { binding ->
                    // Modifier-like physical keys such as SYM update their own
                    // meta-state on key-down. The captured physical key code is
                    // the stable identity for both single and chord bindings.
                    binding.keyCode == event.keyCode
                } == true
            }
            if (startsBinding) return true
            hardwareShortcutKeysDown.remove(event.keyCode)
        }
        if (keyControl.active) return keyControl.onKeyEvent(event)
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
        launchOverlay(
            Intent(this, OverlayActivity::class.java)
                .setAction(OverlayActivity.ACTION_OPEN_OVERLAY)
                .putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, initial)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
        return true
    }

    private fun isTypingContext(): Boolean {
        val imeVisible = windows.any { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
        val focused = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        return imeVisible || focused?.isEditable == true
    }

    private fun performHardwareShortcut(action: HardwareShortcutAction) {
        when (action) {
            HardwareShortcutAction.Bar -> launchOverlay(
                Intent(this, OverlayActivity::class.java)
                    .setAction(OverlayActivity.ACTION_OPEN_OVERLAY)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            )
            HardwareShortcutAction.Hub -> startActivity(
                Intent(this, HubActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            HardwareShortcutAction.Switcher -> startActivity(
                Intent(this, SwitcherActivity::class.java)
                    .setAction("com.astroboii47.commander.OPEN_SWITCHER")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            HardwareShortcutAction.SwitcherBrowse -> startActivity(
                Intent(this, SwitcherActivity::class.java)
                    .setAction("com.astroboii47.commander.OPEN_SWITCHER")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            HardwareShortcutAction.KeyControl -> {
                if (keyControl.active) keyControl.stop() else keyControl.start()
            }
            HardwareShortcutAction.SwitcherReverse,
            HardwareShortcutAction.SwitcherDismiss -> Unit
        }
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
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED
        private const val HOLD_ALT_DELAY_MS = 430L
        private const val MIN_HARDWARE_SHORTCUT_PRESS_MS = 8L
        private var activeService: WeakReference<HomeTypingAccessibilityService>? = null

        fun monitorAutomation(durationMs: Long) {
            activeService?.get()?.enableAutomationMonitoring(durationMs)
        }

        fun automationFinished() {
            activeService?.get()?.finishAutomationMonitoring()
        }

        fun isConnected(): Boolean = activeService?.get() != null

        fun toggleKeyControl(delayMs: Long = 0L): Boolean {
            val service = activeService?.get() ?: return false
            val toggle = Runnable {
                if (service.keyControl.active) {
                    service.keyControl.stop()
                } else {
                    service.keyControl.start()
                }
            }
            if (delayMs > 0L) service.mainHandler.postDelayed(toggle, delayMs)
            else service.mainHandler.post(toggle)
            return true
        }

        fun splitWhenForeground(taskId: Int, packageName: String): Boolean {
            val service = activeService?.get() ?: return false
            val generation = ++service.splitGeneration
            // The activity behind Command is already resumed, so several ROMs
            // do not emit another foreground accessibility event when the
            // translucent overlay closes. Recents itself serializes the close
            // transition and presents the correct active task card.
            service.pendingSplit = PendingSplit(
                taskId,
                packageName,
                generation,
                SplitStage.OpeningRecents,
            )
            service.enableAutomationMonitoring(5_000L)
            service.mainHandler.postDelayed({
                val pending = service.pendingSplit
                if (pending?.generation == generation && pending.stage == SplitStage.OpeningRecents) {
                    service.performGlobalAction(GLOBAL_ACTION_RECENTS)
                }
            }, 180L)
            service.mainHandler.postDelayed({
                if (service.pendingSplit?.generation == generation) service.cancelPendingSplit()
            }, 5_000L)
            return true
        }

        private const val RECENTS_PACKAGE = "com.android.launcher3"

    }
}
