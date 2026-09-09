package com.astroboii47.commander

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Toast
import android.os.Handler
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.util.Log

internal class KeyControlSession(private val service: HomeTypingAccessibilityService) {
    private data class Target(
        val code: String,
        val bounds: Rect,
        val node: AccessibilityNodeInfo,
        var searchText: String? = null,
    )

    private val windowManager = service.getSystemService(WindowManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: KeyControlView? = null
    private var targets = emptyList<Target>()
    private var prefix = ""
    private var searchQuery = ""
    private var searchActive = false
    private var searchSelection = 0
    private var pendingShiftSearch = false
    private var shownAt = 0L
    private var lastVisitedNodeCount = 0
    private var lastVisibleNodeCount = 0
    private var lastClickableNodeCount = 0
    private var lastFocusableNodeCount = 0
    private var lastTextNodeCount = 0
    private var lastActionNodeCount = 0
    private var scrollTarget: AccessibilityNodeInfo? = null
    private val windowRefresh = Runnable { refreshAfterActivation(0) }

    val active: Boolean get() = overlay != null

    fun start() {
        val startedAt = SystemClock.uptimeMillis()
        stop()
        val root = activeApplicationRoot()

        if (root == null) {
            Log.w(LOG_TAG, "start failed: no application root")
            Toast.makeText(service, "No screen controls are available", Toast.LENGTH_SHORT).show()
            return
        }

        val rootReadyAt = SystemClock.uptimeMillis()
        // Always read the current hierarchy when Key Control opens. Settings
        // pages often populate nested rows after keeping the same package and
        // window id, which made the short-lived cache return incomplete hints.
        val nodes = collectTargets(root)
        if (nodes.isEmpty()) {
            Log.w(
                LOG_TAG,
                "start failed: no targets rootMs=${rootReadyAt - startedAt} " +
                    "package=${root.packageName} class=${root.className} visited=$lastVisitedNodeCount " +
                    "visible=$lastVisibleNodeCount clickable=$lastClickableNodeCount " +
                    "focusable=$lastFocusableNodeCount text=$lastTextNodeCount actions=$lastActionNodeCount",
            )
            Toast.makeText(service, "This screen exposes no selectable controls", Toast.LENGTH_SHORT).show()
            return
        }
        val targetsReadyAt = SystemClock.uptimeMillis()
        val codes = targetCodes(nodes.size)
        targets = nodes.mapIndexed { index, pair -> Target(codes[index], pair.first, pair.second) }
        prefix = ""
        searchQuery = ""
        searchActive = false
        searchSelection = 0
        pendingShiftSearch = false
        overlay = KeyControlView(service).also { view ->
            windowManager.addView(
                view,
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT,
                ).apply { gravity = Gravity.TOP or Gravity.START },
            )
        }
        service.setKeyControlContentMonitoring(true)
        shownAt = SystemClock.uptimeMillis()
        Log.i(
            LOG_TAG,
            "shown targets=${targets.size} rootMs=${rootReadyAt - startedAt} " +
                "collectMs=${targetsReadyAt - rootReadyAt} nodes=$lastVisitedNodeCount " +
                "cached=false totalMs=${SystemClock.uptimeMillis() - startedAt}",
        )
        SoundFeedback.play(service, CommandSound.Open)
    }

    fun stop() {
        handler.removeCallbacks(windowRefresh)
        service.setKeyControlContentMonitoring(false)
        overlay?.let { runCatching { windowManager.removeViewImmediate(it) } }
        overlay = null
        targets = emptyList()
        prefix = ""
        searchQuery = ""
        searchActive = false
        searchSelection = 0
        shownAt = 0L
    }

    fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        if (event == null) return
        val eventPackage = event.packageName?.toString()
        if (eventPackage != service.packageName &&
            event.eventType == android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
        ) invalidateCache()
        if (!active || SystemClock.uptimeMillis() - shownAt < 120L) return
        if (event.eventType != android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != android.view.accessibility.AccessibilityEvent.TYPE_WINDOWS_CHANGED &&
            event.eventType != android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return
        val destination = "${event.packageName} ${event.className}".lowercase()
        val recentsVisible = service.windows.any { window ->
            window.type == AccessibilityWindowInfo.TYPE_APPLICATION &&
                window.root?.packageName?.toString() == "com.android.launcher3"
        }
        if (eventPackage == "com.android.launcher3" || recentsVisible ||
            "recents" in destination || "overview" in destination
        ) {
            invalidateCache()
            stop()
        } else if (eventPackage != service.packageName) {
            // Settings and many Compose screens keep one activity while
            // replacing the visible hierarchy. Refresh after the destination
            // has populated instead of retaining hints from the prior page.
            handler.removeCallbacks(windowRefresh)
            handler.postDelayed(
                windowRefresh,
                if (event.eventType == android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) 220L else 180L,
            )
        }
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        if (!active) return false
        if (event.action == KeyEvent.ACTION_UP) {
            if (!searchActive && pendingShiftSearch &&
                (event.keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || event.keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
            ) {
                pendingShiftSearch = false
                enterSearch()
            }
            return true
        }
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) return true
        if (!searchActive &&
            (event.keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || event.keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
        ) {
            // Wait for release so Shift+Space can page upward instead of
            // opening search before the Space event arrives.
            pendingShiftSearch = true
            return true
        }
        // Any non-Shift key means Shift was used as a modifier, not as the
        // standalone search shortcut.
        pendingShiftSearch = false
        // Pastiera navigation mode emits Ctrl+K/J rather than DPAD events.
        // Interpret those chords only while Key Control owns the keyboard.
        if (event.isCtrlPressed && event.keyCode == KeyEvent.KEYCODE_I) {
            if (searchActive) moveSearchSelection(-1) else scroll(forward = false, page = false)
            return true
        }
        if (event.isCtrlPressed && event.keyCode == KeyEvent.KEYCODE_K) {
            if (searchActive) moveSearchSelection(1) else scroll(forward = true, page = false)
            return true
        }
        if (event.isCtrlPressed && event.keyCode == KeyEvent.KEYCODE_J) {
            if (searchActive) moveSearchSelection(1) else scroll(forward = true, page = false)
            return true
        }
        // Keep Space as a familiar, low-effort page navigation key while
        // Key Control owns input. Shift reverses the direction, matching
        // desktop browser behaviour. It is intentionally inactive while the
        // control-search field is open so spaces can remain part of a query.
        if (!searchActive && event.keyCode == KeyEvent.KEYCODE_SPACE) {
            scroll(forward = !event.isShiftPressed, page = true)
            return true
        }
        when (event.keyCode) {
            KeyEvent.KEYCODE_ESCAPE -> {
                if (searchActive) exitSearch() else stop()
                return true
            }
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_DEL -> {
                if (searchActive) {
                    if (searchQuery.isEmpty()) exitSearch() else {
                        searchQuery = searchQuery.dropLast(1)
                        searchSelection = 0
                        overlay?.invalidate()
                        SoundFeedback.play(service, CommandSound.Step)
                    }
                } else if (prefix.isEmpty()) stop() else {
                    prefix = prefix.dropLast(1)
                    overlay?.invalidate()
                    SoundFeedback.play(service, CommandSound.Step)
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (searchActive) moveSearchSelection(1) else scroll(forward = true, page = false)
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (searchActive) moveSearchSelection(-1) else scroll(forward = false, page = false)
                return true
            }
            KeyEvent.KEYCODE_PAGE_DOWN -> {
                if (!searchActive) scroll(forward = true, page = true)
                return true
            }
            KeyEvent.KEYCODE_PAGE_UP -> {
                if (!searchActive) scroll(forward = false, page = true)
                return true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (searchActive) selectedSearchTarget()?.let(::activate)
                else filteredTargets().singleOrNull()?.let(::activate)
                return true
            }
        }
        val unicode = event.getUnicodeChar(event.metaState)
        if (unicode == 0 || Character.isISOControl(unicode)) return true
        // On Titan 2, Pastiera produces slash as Alt Right + M. Resolve the
        // character before rejecting other modified input.
        if (!searchActive && unicode == '/'.code) {
            enterSearch()
            return true
        }
        if (event.isCtrlPressed || event.isMetaPressed || event.isAltPressed) return true
        if (searchActive) {
            val character = String(Character.toChars(unicode))
            if (character.isNotBlank()) {
                searchQuery += character
                searchSelection = 0
                overlay?.invalidate()
                SoundFeedback.play(service, CommandSound.Step)
            }
            return true
        }
        val character = String(Character.toChars(unicode)).uppercase()
        if (character.length != 1 || character[0] !in KEY_ALPHABET) return true
        val candidate = prefix + character
        if (targets.none { it.code.startsWith(candidate) }) {
            SoundFeedback.play(service, CommandSound.Error)
            return true
        }
        prefix = candidate
        overlay?.invalidate()
        SoundFeedback.play(service, CommandSound.Step)
        targets.singleOrNull { it.code == prefix }?.let(::activate)
        return true
    }

    private fun filteredTargets(): List<Target> = if (searchActive) searchTargets() else {
        targets.filter { it.code.startsWith(prefix) }
    }

    private fun searchTargets(): List<Target> {
        val terms = searchQuery.trim().lowercase().split(Regex("\\s+")).filter(String::isNotEmpty)
        if (terms.isEmpty()) return targets
        return targets.filter { target ->
            val searchable = target.searchText ?: searchableText(target.node).also { target.searchText = it }
            terms.all { it in searchable }
        }
    }

    private fun selectedSearchTarget(): Target? {
        val matches = searchTargets()
        if (matches.isEmpty()) return null
        searchSelection = searchSelection.coerceIn(0, matches.lastIndex)
        return matches[searchSelection]
    }

    private fun moveSearchSelection(delta: Int) {
        val matches = searchTargets()
        if (matches.isEmpty()) {
            SoundFeedback.play(service, CommandSound.Error)
            return
        }
        searchSelection = (searchSelection + delta).coerceIn(0, matches.lastIndex)
        overlay?.invalidate()
        SoundFeedback.play(service, CommandSound.Step)
    }

    private fun exitSearch() {
        searchActive = false
        searchQuery = ""
        searchSelection = 0
        overlay?.invalidate()
        SoundFeedback.play(service, CommandSound.Step)
    }

    private fun enterSearch() {
        searchActive = true
        searchQuery = ""
        searchSelection = 0
        prefix = ""
        overlay?.invalidate()
        SoundFeedback.play(service, CommandSound.Step)
    }

    private fun searchableText(node: AccessibilityNodeInfo): String {
        val values = linkedSetOf<String>()
        fun add(value: CharSequence?) {
            value?.toString()?.trim()?.takeIf(String::isNotEmpty)?.let(values::add)
        }
        add(node.text)
        add(node.contentDescription)
        add(node.viewIdResourceName?.substringAfterLast('/'))
        node.actionList.forEach { add(it.label) }
        fun descendants(current: AccessibilityNodeInfo, depth: Int) {
            if (depth > 3 || values.size >= 16) return
            for (index in 0 until current.childCount) current.getChild(index)?.let { child ->
                add(child.text)
                add(child.contentDescription)
                add(child.viewIdResourceName?.substringAfterLast('/'))
                descendants(child, depth + 1)
            }
        }
        descendants(node, 0)
        return values.joinToString(" ").lowercase()
    }

    private fun scroll(forward: Boolean, page: Boolean) {
        // Recycler/Compose pages frequently replace their accessibility nodes
        // after a scroll while leaving the old node technically refreshable.
        // Resolve the current visible container per explicit navigation press
        // rather than relying on that stale handle.
        val target = activeApplicationRoot()?.let(::findScrollableTarget)
        scrollTarget = target
        if (target == null) {
            SoundFeedback.play(service, CommandSound.Error)
            return
        }
        val pageAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (forward) AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_DOWN.id
            else AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_UP.id
        } else 0
        val directionalAction = if (forward) {
            AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id
        } else {
            AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id
        }
        val regularAction = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        val availableActions = target.actionList.map { it.id }.toSet()
        val preferredAction = when {
            page && pageAction != 0 && pageAction in availableActions -> pageAction
            directionalAction in availableActions -> directionalAction
            else -> regularAction
        }
        // Some views advertise PAGE_UP/DOWN but return false at runtime.
        // In that case a normal scroll remains the correct reliable fallback.
        val performed = runCatching { target.performAction(preferredAction) }.getOrDefault(false) ||
            (preferredAction != directionalAction && runCatching {
                target.performAction(if (directionalAction in availableActions) directionalAction else regularAction)
            }.getOrDefault(false))
        if (performed) {
            SoundFeedback.play(service, CommandSound.Step)
            handler.postDelayed(::refreshVisibleTargets, 140L)
        } else SoundFeedback.play(service, CommandSound.Error)
    }

    private fun refreshVisibleTargets() {
        val root = activeApplicationRoot() ?: return
        val nodes = collectTargets(root)
        val codes = targetCodes(nodes.size)
        targets = nodes.mapIndexed { index, pair ->
            Target(codes[index], pair.first, pair.second)
        }
        prefix = ""
        searchSelection = searchSelection.coerceAtMost((searchTargets().size - 1).coerceAtLeast(0))
        invalidateCache()
        overlay?.invalidate()
    }

    private fun activate(target: Target) {
        overlay?.flash(target)
        handler.postDelayed({
            val clicked = clickTarget(target)
            Log.i(LOG_TAG, "activate code=${target.code} bounds=${target.bounds} clicked=$clicked")
            if (clicked) SoundFeedback.play(service, CommandSound.Confirm)
            else SoundFeedback.play(service, CommandSound.Error)
            invalidateCache()
            if (KeyControlSettings.closeAfterActivation.value) {
                stop()
            } else {
                prefix = ""
                searchQuery = ""
                searchActive = false
                searchSelection = 0
                handler.postDelayed({
                    overlay?.clearFlash()
                }, 240L)
            }
        }, 90L)
    }

    private fun refreshAfterActivation(attempt: Int) {
        if (!active) return
        val root = activeApplicationRoot()
        val nodes = root?.let(::collectTargets).orEmpty()
        if (nodes.isEmpty() && attempt < 8) {
            handler.postDelayed({ refreshAfterActivation(attempt + 1) }, 180L)
            return
        }
        if (nodes.isEmpty()) {
            Log.i(LOG_TAG, "closing after destination exposed no controls")
            stop()
            return
        }
        val codes = targetCodes(nodes.size)
        targets = nodes.mapIndexed { index, pair -> Target(codes[index], pair.first, pair.second) }
        overlay?.invalidate()
    }

    private fun clickTarget(target: Target): Boolean {
        val refreshed = runCatching { target.node.refresh() }.getOrDefault(false)
        if (refreshed && runCatching {
                target.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }.getOrDefault(false)
        ) return true

        val freshTarget = activeApplicationRoot()?.let(::collectTargets)
            ?.firstOrNull { (bounds, _) -> bounds == target.bounds }
            ?.second
            ?: return false
        return runCatching {
            freshTarget.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }.getOrDefault(false)
    }

    private fun activeApplicationRoot(): AccessibilityNodeInfo? = service.windows.asSequence()
        .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
        .sortedByDescending { it.layer }
        .mapNotNull { it.root }
        .firstOrNull { it.packageName?.toString() != service.packageName }
        ?: service.rootInActiveWindow?.takeIf { it.packageName?.toString() != service.packageName }

    private fun invalidateCache() {
        scrollTarget = null
    }

    private fun collectTargets(root: AccessibilityNodeInfo): List<Pair<Rect, AccessibilityNodeInfo>> {
        val display = service.resources.displayMetrics
        val screen = Rect(0, 0, display.widthPixels, display.heightPixels)
        val found = LinkedHashMap<String, Pair<Rect, AccessibilityNodeInfo>>()
        var bestScrollable: Pair<Long, AccessibilityNodeInfo>? = null
        lastVisitedNodeCount = 0
        lastVisibleNodeCount = 0
        lastClickableNodeCount = 0
        lastFocusableNodeCount = 0
        lastTextNodeCount = 0
        lastActionNodeCount = 0
        fun visit(node: AccessibilityNodeInfo, depth: Int) {
            lastVisitedNodeCount++
            if (!node.isVisibleToUser || !node.isEnabled || depth > 80) return
            lastVisibleNodeCount++
            if (node.isClickable) lastClickableNodeCount++
            if (node.isFocusable) lastFocusableNodeCount++
            if (!node.text.isNullOrBlank() || !node.contentDescription.isNullOrBlank()) lastTextNodeCount++
            if (node.actionList.isNotEmpty()) lastActionNodeCount++
            val bounds = Rect().also(node::getBoundsInScreen)
            if (bounds.isEmpty || !Rect.intersects(bounds, screen)) return
            bounds.intersect(screen)
            val actionIds = node.actionList.map { it.id }.toSet()
            if (node.isScrollable || AccessibilityNodeInfo.ACTION_SCROLL_FORWARD in actionIds ||
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD in actionIds
            ) {
                val area = bounds.width().toLong() * bounds.height()
                if (area > (bestScrollable?.first ?: -1L)) bestScrollable = area to node
            }
            val actionable = node.isClickable || node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_CLICK }
            if (actionable) {
                if (bounds.width() >= 18 && bounds.height() >= 18) {
                    val key = "${bounds.left}:${bounds.top}:${bounds.right}:${bounds.bottom}"
                    found[key] = bounds to node
                }
            }
            // A clickable row can contain an independently clickable trailing
            // action, such as the per-device settings buttons on Android's
            // Bluetooth page. Continue through actionable parents and let the
            // bounds key collapse true duplicates.
            if (found.size >= MAX_TARGETS) return
            for (index in 0 until node.childCount) childAt(node, index, depth)?.let { visit(it, depth + 1) }
        }
        visit(root, 0)
        scrollTarget = bestScrollable?.second
        return found.values.sortedWith(compareBy({ it.first.top }, { it.first.left })).take(MAX_TARGETS)
    }

    private fun findScrollableTarget(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val screen = Rect(0, 0, service.resources.displayMetrics.widthPixels, service.resources.displayMetrics.heightPixels)
        var best: Pair<Long, AccessibilityNodeInfo>? = null
        fun visit(node: AccessibilityNodeInfo) {
            if (!node.isVisibleToUser) return
            val bounds = Rect().also(node::getBoundsInScreen)
            if (!bounds.intersect(screen)) return
            val actionIds = node.actionList.map { it.id }.toSet()
            if (node.isScrollable || AccessibilityNodeInfo.ACTION_SCROLL_FORWARD in actionIds ||
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD in actionIds
            ) {
                val area = bounds.width().toLong() * bounds.height()
                if (area > (best?.first ?: -1L)) best = area to node
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        visit(root)
        return best?.second
    }

    private fun childAt(node: AccessibilityNodeInfo, index: Int, depth: Int): AccessibilityNodeInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && depth == 0 && index == 0) {
            node.getChild(
                index,
                AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_HYBRID or
                    AccessibilityNodeInfo.FLAG_PREFETCH_SIBLINGS,
            )
        } else {
            node.getChild(index)
        }

    private fun targetCodes(count: Int): List<String> {
        val width = if (count <= KEY_ALPHABET.length) 1 else 2
        return buildList {
            if (width == 1) KEY_ALPHABET.take(count).forEach { add(it.toString()) }
            else for (first in KEY_ALPHABET) for (second in KEY_ALPHABET) {
                add("$first$second")
                if (size == count) return@buildList
            }
        }
    }

    private inner class KeyControlView(context: android.content.Context) : View(context) {
        private val density = resources.displayMetrics.density
        private val accent = Color.parseColor(AccentSettings.currentHex(context))
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 12f * density
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(205, 20, 20, 20)
            setShadowLayer(7f * density, 0f, 2f * density, Color.argb(110, 0, 0, 0))
        }
        private val labelOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(95, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f * density
        }
        private val selectedOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
            setShadowLayer(8f * density, 0f, 0f, accent)
        }
        private val searchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(238, 238, 238)
            textSize = 14f * density
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        private var flashBounds: Rect? = null
        private var touchDownX = 0f
        private var touchDownY = 0f
        private var touchMoved = false

        init {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.argb(24, 0, 0, 0))
            val visibleTargets = filteredTargets()
            visibleTargets.forEachIndexed { index, target ->
                val label = labelBounds(target, canvas.width, canvas.height)
                canvas.drawRoundRect(label, 7f * density, 7f * density, fillPaint)
                canvas.drawRoundRect(label, 7f * density, 7f * density, labelOutlinePaint)
                if (searchActive && searchQuery.isNotBlank() && index == searchSelection) {
                    canvas.drawRoundRect(label, 7f * density, 7f * density, selectedOutlinePaint)
                }
                val baseline = label.centerY() - (labelPaint.ascent() + labelPaint.descent()) / 2f
                val visibleCode = if (searchActive) target.code
                else target.code.removePrefix(prefix).ifEmpty { target.code }
                canvas.drawText(visibleCode, label.centerX(), baseline, labelPaint)
            }
            if (searchActive) drawSearchPill(canvas, visibleTargets.size)
            else drawControlHint(canvas)
            flashBounds?.let {
                val flash = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 4f * density
                    color = accent
                    setShadowLayer(12f * density, 0f, 0f, accent)
                }
                canvas.drawRoundRect(RectF(it), 10f * density, 10f * density, flash)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownX = event.x
                    touchDownY = event.y
                    touchMoved = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - touchDownX
                    val dy = event.y - touchDownY
                    if (!touchMoved && dx * dx + dy * dy > (18f * density) * (18f * density)) {
                        touchMoved = true
                        stop()
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (touchMoved) return true
                    val target = filteredTargets().lastOrNull {
                        labelBounds(it, width, height).contains(event.x, event.y)
                    }
                    if (target != null) activate(target) else stop()
                    return true
                }
                MotionEvent.ACTION_CANCEL -> return true
            }
            return true
        }

        fun flash(target: Target) {
            flashBounds = Rect().also { labelBounds(target, width, height).roundOut(it) }
            invalidate()
        }

        fun clearFlash() {
            flashBounds = null
            invalidate()
        }

        private fun drawSearchPill(canvas: Canvas, matchCount: Int) {
            val margin = 18f * density
            val height = 48f * density
            val pill = RectF(margin, canvas.height - height - margin, canvas.width - margin, canvas.height - margin)
            canvas.drawRoundRect(pill, 16f * density, 16f * density, fillPaint)
            canvas.drawRoundRect(pill, 16f * density, 16f * density, labelOutlinePaint)
            val text = if (searchQuery.isEmpty()) "search controls" else searchQuery
            val baseline = pill.centerY() - (searchPaint.ascent() + searchPaint.descent()) / 2f
            canvas.drawText(text, pill.left + 16f * density, baseline, searchPaint)
            val count = "$matchCount"
            canvas.drawText(count, pill.right - 16f * density - searchPaint.measureText(count), baseline, searchPaint)
        }

        private fun drawControlHint(canvas: Canvas) {
            val text = "SHIFT search   SPACE scroll   ESC close"
            val horizontalPadding = 13f * density
            val height = 30f * density
            val width = searchPaint.measureText(text) + horizontalPadding * 2f
            val bottom = canvas.height - 12f * density
            val pill = RectF(
                (canvas.width - width) / 2f,
                bottom - height,
                (canvas.width + width) / 2f,
                bottom,
            )
            canvas.drawRoundRect(pill, 12f * density, 12f * density, fillPaint)
            canvas.drawRoundRect(pill, 12f * density, 12f * density, labelOutlinePaint)
            val baseline = pill.centerY() - (searchPaint.ascent() + searchPaint.descent()) / 2f
            canvas.drawText(text, pill.left + horizontalPadding, baseline, searchPaint)
        }

        private fun labelBounds(target: Target, viewWidth: Int, viewHeight: Int): RectF {
            val textWidth = labelPaint.measureText(target.code)
            val labelWidth = maxOf(25f * density, textWidth + 12f * density)
            val labelHeight = 24f * density
            val left = (target.bounds.left + 5f * density).coerceAtMost(viewWidth - labelWidth - 3f * density)
            val top = (target.bounds.top + 5f * density).coerceIn(3f * density, viewHeight - labelHeight - 3f * density)
            return RectF(left, top, left + labelWidth, top + labelHeight)
        }
    }

    private companion object {
        const val KEY_ALPHABET = "ASDFGHJKLQWERTYUIOPZXCVBNM"
        const val MAX_TARGETS = 300
        const val LOG_TAG = "CommanderKeyControl"
    }
}
