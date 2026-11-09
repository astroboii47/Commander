package com.astroboii47.commander

import android.app.Notification
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.OutlinedFlag
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HubAccent: Color get() = AccentSettings.color.value
private val HubSerif = FontFamily(Font(R.font.instrument_serif_regular))
private val HubSans = FontFamily(Font(R.font.inter_variable))
private val HubMono = FontFamily(Font(R.font.courier_prime_regular))

private data class HubPalette(
    val background: Color,
    val panel: Color,
    val tile: Color,
    val text: Color,
    val secondary: Color,
    val border: Color,
)

private enum class HubFilter(val label: String) {
    All("all"), Messages("messages"), Calls("calls"), Email("email"), Finance("finance"), Tasks("tasks"), Apps("apps"), Important("important"), Flagged("flagged"), Summaries("summaries")
}

private sealed interface HubActionEntry {
    val label: String
    data object Open : HubActionEntry { override val label = "open" }
    data object Reply : HubActionEntry { override val label = "reply" }
    data class Flag(val flagged: Boolean) : HubActionEntry { override val label = if (flagged) "unflag" else "flag" }
    data class Important(val important: Boolean) : HubActionEntry { override val label = if (important) "remove importance" else "mark important" }
    data class ImportantSource(val rule: HubImportantRule, val active: Boolean) : HubActionEntry {
        override val label = when (rule.kind) {
            "conversation" -> if (active) "stop prioritising ${rule.label}" else "always important from ${rule.label}"
            "type" -> if (active) "stop prioritising this alert type" else "always prioritise this alert type"
            else -> if (active) "stop prioritising ${rule.appName}" else "always important from ${rule.appName}"
        }
    }
    data class NotificationAction(val action: HubNotificationAction) : HubActionEntry { override val label = action.title }
    data object Snooze : HubActionEntry { override val label = "snooze for 1 hour" }
    data object Dismiss : HubActionEntry { override val label = "dismiss" }
    data object IgnoreScope : HubActionEntry { override val label = "ignore this type or conversation" }
    data object IgnoreApp : HubActionEntry { override val label = "ignore everything from this app" }
}

private data class HubRowConfirmation(
    val item: HubItem,
    val index: Int,
    val success: Boolean,
    val text: String,
)

private fun HubActionEntry.displayLabel(item: HubItem): String = when (this) {
    is HubActionEntry.NotificationAction -> {
        val raw = action.title.trim()
        val knownAction = Regex("archive|delete|trash|mark as read|reply|snooze|schedule|reschedule|complete|done|mute", RegexOption.IGNORE_CASE)
            .containsMatchIn(raw)
        if (item.hasAttachment && !knownAction) "open attachment · $raw" else raw.lowercase()
    }
    else -> label.lowercase()
}

private fun HubActionEntry.icon(item: HubItem): ImageVector = when (this) {
    HubActionEntry.Open -> Icons.AutoMirrored.Outlined.OpenInNew
    HubActionEntry.Reply -> Icons.AutoMirrored.Outlined.Reply
    is HubActionEntry.Flag -> if (flagged) Icons.Outlined.Star else Icons.Outlined.StarOutline
    is HubActionEntry.Important -> if (important) Icons.Outlined.NotificationImportant else Icons.Outlined.PriorityHigh
    is HubActionEntry.ImportantSource -> Icons.Outlined.NotificationImportant
    HubActionEntry.Snooze -> Icons.Outlined.Schedule
    HubActionEntry.Dismiss -> Icons.Outlined.Close
    HubActionEntry.IgnoreScope -> Icons.Outlined.NotificationsOff
    HubActionEntry.IgnoreApp -> Icons.Outlined.Block
    is HubActionEntry.NotificationAction -> {
        val actionLabel = action.title.lowercase()
        when {
            item.hasAttachment && !Regex("archive|delete|trash|mark as read|reply|snooze|schedule|reschedule|complete|done|mute")
                .containsMatchIn(actionLabel) -> Icons.Outlined.Attachment
            "archive" in actionLabel -> Icons.Outlined.Archive
            "delete" in actionLabel || "trash" in actionLabel || "remove" in actionLabel -> Icons.Outlined.DeleteOutline
            "unread" in actionLabel -> Icons.Outlined.MarkEmailUnread
            "read" in actionLabel -> Icons.Outlined.MarkEmailRead
            "reply" in actionLabel -> Icons.AutoMirrored.Outlined.Reply
            "schedule" in actionLabel || "reschedule" in actionLabel -> Icons.Outlined.CalendarMonth
            "snooze" in actionLabel || "later" in actionLabel -> Icons.Outlined.Schedule
            "complete" in actionLabel || "done" in actionLabel -> Icons.Outlined.TaskAlt
            "call" in actionLabel -> Icons.Outlined.Call
            else -> Icons.Outlined.NotificationsNone
        }
    }
}

private fun HubItem.actionEntries(flagged: Boolean, important: Boolean): List<HubActionEntry> = buildList {
    add(HubActionEntry.Open)
    if (replyAction != null) add(HubActionEntry.Reply)
    add(HubActionEntry.Flag(flagged))
    add(HubActionEntry.Important(important))
    HubImportantSettings.candidates(this@actionEntries).forEach { rule ->
        add(HubActionEntry.ImportantSource(rule, HubImportantSettings.contains(rule)))
    }
    notificationActions.forEach { add(HubActionEntry.NotificationAction(it)) }
    if (!detached && notificationActions.none { action ->
            action.title.contains("snooze", ignoreCase = true) || action.title.contains("later", ignoreCase = true)
        }) {
        add(HubActionEntry.Snooze)
    }
    add(HubActionEntry.Dismiss)
    if (!shortcutId.isNullOrBlank() || !conversationTitle.isNullOrBlank() || !channelId.isNullOrBlank()) add(HubActionEntry.IgnoreScope)
    add(HubActionEntry.IgnoreApp)
}

@Composable
fun MinimalHubApp(
    hasAccess: Boolean,
    openAccessSettings: () -> Unit,
    onDismiss: () -> Unit,
    forceWallpaperGlass: Boolean = false,
    isActiveSurface: Boolean = true,
) {
    val nightMode = LocalConfiguration.current.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
    val dark = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    val glass = dark && (forceWallpaperGlass || (HubSettings.wallpaperGlass.value && HomeSettings.wallpaperMode.value != HomeWallpaperMode.Black))
    val palette = if (dark) {
        HubPalette(if (glass) Color.Transparent else Color.Black, Color(0xFF070707).copy(alpha = if (glass) .74f else 1f), Color(0xFF101010).copy(alpha = if (glass) .72f else 1f), Color(0xFFF3F1EA), Color(0xFF8C8A84), Color(0xFF303030))
    } else {
        HubPalette(Color(0xFFE8E6DF), Color(0xFFF1EFE8), Color(0xFFE4E0D8), Color(0xFF171717), Color(0xFF6F6D67), Color(0xFFCAC6BD))
    }
    val context = LocalContext.current
    val flagPrefs = remember { context.getSharedPreferences("hub_flags", 0) }
    val importantPrefs = remember { context.getSharedPreferences("hub_important", 0) }
    val readPrefs = remember { context.getSharedPreferences("hub_read", 0) }
    val categoryPrefs = remember { context.getSharedPreferences("hub_categories", 0) }
    var flaggedKeys by remember { mutableStateOf(flagPrefs.getStringSet("keys", emptySet())?.toSet().orEmpty()) }
    var importantKeys by remember { mutableStateOf(importantPrefs.getStringSet("keys", emptySet())?.toSet().orEmpty()) }
    var readTimes by remember {
        mutableStateOf(readPrefs.all.mapNotNull { (key, value) -> (value as? Long)?.let { key to it } }.toMap())
    }
    var categoryOverrides by remember {
        mutableStateOf(categoryPrefs.all.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap())
    }
    var customCategories by remember { mutableStateOf(HubCustomCategories.load(context)) }
    var filter by remember { mutableStateOf(HubFilter.All) }
    var customFilterId by remember { mutableStateOf<String?>(null) }
    var managing by remember { mutableStateOf(false) }
    var replyKey by remember { mutableStateOf<String?>(null) }
    var replyItemSnapshot by remember { mutableStateOf<HubItem?>(null) }
    var replyText by remember { mutableStateOf("") }
    var replyStatus by remember { mutableStateOf<Pair<String, String>?>(null) }
    var actionPrompt by remember { mutableStateOf<HubItem?>(null) }
    var actionMenuIndex by remember { mutableStateOf(0) }
    var actionConfirmation by remember { mutableStateOf<HubNotificationAction?>(null) }
    var actionResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var actionExecuting by remember { mutableStateOf<String?>(null) }
    var rowConfirmation by remember { mutableStateOf<HubRowConfirmation?>(null) }
    var swipeActionsKey by remember { mutableStateOf<String?>(null) }
    var selectedIndex by remember { mutableStateOf(0) }
    var expandedKey by remember { mutableStateOf<String?>(null) }
    var keyboardBrowsing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val notificationScroll = rememberScrollState()
    val allItems = HubRepository.items
    val importantRules = HubImportantSettings.rules.value
    val newCount = remember(allItems, readTimes) {
        allItems.count { !it.isSummary && (readTimes[it.key] ?: 0L) < it.time }
    }
    val visible = remember(allItems, filter, customFilterId, flaggedKeys, importantKeys, importantRules, categoryOverrides) {
        if (customFilterId != null) allItems.filter {
            !it.isSummary && it.effectiveCategory(categoryOverrides) == customFilterId
        } else allItems.filter { it.matches(filter, flaggedKeys, importantKeys, categoryOverrides) }
    }
    val displayedVisible = remember(visible, rowConfirmation) {
        val confirmation = rowConfirmation
        if (confirmation == null || visible.any { it.key == confirmation.item.key }) visible
        else visible.toMutableList().apply {
            add(confirmation.index.coerceIn(0, size), confirmation.item)
        }
    }
    val filters = HubFilter.entries.filter { entry ->
        // Home exposes Important as a first-class status count, so its Hub
        // destination must never disappear because the list happens to be empty
        // or an older optional-tab preference was stored.
        if (entry == HubFilter.All || entry == HubFilter.Important) true else when (HubSettings.visibility(entry.label)) {
            HubTabVisibility.Always -> true
            HubTabVisibility.Hidden -> false
            HubTabVisibility.Auto -> allItems.any { it.matches(entry, flaggedKeys, importantKeys, categoryOverrides) }
        }
    }

    LaunchedEffect(displayedVisible.size, filter, customFilterId) {
        selectedIndex = selectedIndex.coerceIn(0, (displayedVisible.size - 1).coerceAtLeast(0))
    }
    LaunchedEffect(filters) {
        if (filter !in filters) filter = HubFilter.All
    }
    fun toggleFlag(key: String) {
        flaggedKeys = if (key in flaggedKeys) flaggedKeys - key else flaggedKeys + key
        flagPrefs.edit().putStringSet("keys", flaggedKeys).apply()
    }
    fun toggleImportant(key: String) {
        importantKeys = if (key in importantKeys) importantKeys - key else importantKeys + key
        importantPrefs.edit().putStringSet("keys", importantKeys).apply()
    }
    fun markRead(item: HubItem) {
        readTimes = readTimes + (item.key to item.time)
        readPrefs.edit().putLong(item.key, item.time).apply()
    }
    LaunchedEffect(keyboardBrowsing, selectedIndex, visible.getOrNull(selectedIndex)?.key) {
        if (keyboardBrowsing) {
            val selected = visible.getOrNull(selectedIndex) ?: return@LaunchedEffect
            delay(300)
            markRead(selected)
        }
    }
    fun cycleCategory(packageName: String) {
        val order = listOf("auto", "messages", "calls", "email", "finance", "tasks", "apps") +
            customCategories.map(HubCustomCategory::id)
        val current = categoryOverrides[packageName] ?: "auto"
        val next = order[(order.indexOf(current) + 1) % order.size]
        categoryOverrides = if (next == "auto") categoryOverrides - packageName else categoryOverrides + (packageName to next)
        categoryPrefs.edit().apply {
            if (next == "auto") remove(packageName) else putString(packageName, next)
        }.apply()
    }

    fun openActions(item: HubItem) {
        markRead(item)
        SoundFeedback.play(context, CommandSound.Open)
        displayedVisible.indexOfFirst { it.key == item.key }.takeIf { it >= 0 }?.let { selectedIndex = it }
        actionPrompt = item
        actionMenuIndex = 0
        actionConfirmation = null
        actionResult = null
        actionExecuting = null
    }
    fun performAction(item: HubItem, entry: HubActionEntry) {
        when (entry) {
            HubActionEntry.Open -> {
                actionPrompt = null
                openHubItem(context, item) { markRead(item) }
            }
            HubActionEntry.Reply -> {
                actionExecuting = entry.label
                scope.launch {
                    delay(360)
                    actionExecuting = null
                    actionPrompt = null
                    SoundFeedback.play(context, CommandSound.Open)
                    replyKey = item.key
                    replyItemSnapshot = item
                    replyText = ""
                    replyStatus = null
                }
            }
            is HubActionEntry.Flag -> {
                toggleFlag(item.key)
                SoundFeedback.play(context, CommandSound.Confirm)
                actionPrompt = null
            }
            is HubActionEntry.Important -> {
                toggleImportant(item.key)
                actionPrompt = null
                SoundFeedback.play(context, CommandSound.Open)
            }
            is HubActionEntry.ImportantSource -> {
                HubImportantSettings.toggle(context, entry.rule)
                actionPrompt = null
                SoundFeedback.play(context, CommandSound.Confirm)
            }
            is HubActionEntry.NotificationAction -> {
                if (entry.action.destructive && actionConfirmation != entry.action) {
                    actionConfirmation = entry.action
                } else {
                    actionConfirmation = null
                    actionExecuting = entry.label
                    val originalIndex = displayedVisible.indexOfFirst { it.key == item.key }
                        .takeIf { it >= 0 } ?: selectedIndex
                    scope.launch {
                        // Draw the selected-action pulse before invoking the
                        // originating app, which may update/remove its row in
                        // the same frame as PendingIntent.send().
                        delay(110)
                        HubRepository.prepareExternalMutation(item)
                        val sent = entry.action.send(context)
                        if (sent) {
                            HubRepository.recordNotificationAction(item.packageName, entry.action.title)
                            SoundFeedback.play(context, CommandSound.Confirm)
                            delay(310)
                            actionExecuting = null
                            actionPrompt = null
                            rowConfirmation = HubRowConfirmation(
                                item = item,
                                index = originalIndex,
                                success = true,
                                text = entry.action.successLabel(),
                            )
                            delay(850)
                            rowConfirmation = null
                        } else {
                            actionExecuting = null
                            actionResult = false to "couldn't ${entry.action.title.lowercase()}"
                            SoundFeedback.play(context, CommandSound.Error)
                        }
                    }
                }
            }
            HubActionEntry.Snooze -> {
                actionExecuting = entry.label
                val originalIndex = displayedVisible.indexOfFirst { it.key == item.key }
                    .takeIf { it >= 0 } ?: selectedIndex
                scope.launch {
                    delay(360)
                    HubRepository.snooze(item, 60L * 60L * 1000L)
                    SoundFeedback.play(context, CommandSound.Confirm)
                    actionPrompt = null
                    actionExecuting = null
                    rowConfirmation = HubRowConfirmation(item, originalIndex, true, "notification snoozed")
                    delay(850)
                    rowConfirmation = null
                }
            }
            HubActionEntry.Dismiss -> {
                actionExecuting = entry.label
                scope.launch {
                    delay(360); HubRepository.dismiss(item); SoundFeedback.play(context, CommandSound.Confirm)
                    actionPrompt = null; actionExecuting = null
                }
            }
            HubActionEntry.IgnoreScope -> {
                actionExecuting = entry.label
                scope.launch {
                    delay(360)
                    if (HubIgnoreSettings.ignoreScope(context, item)) {
                        HubRepository.applyIgnoreRules(); SoundFeedback.play(context, CommandSound.Confirm)
                    }
                    actionPrompt = null; actionExecuting = null
                }
            }
            HubActionEntry.IgnoreApp -> {
                actionExecuting = entry.label
                scope.launch {
                    delay(360)
                    HubIgnoreSettings.ignoreApp(context, item); HubRepository.applyIgnoreRules()
                    SoundFeedback.play(context, CommandSound.Confirm)
                    actionPrompt = null; actionExecuting = null
                }
            }
        }
    }

    val keyBridgeOwner = remember { Any() }
    DisposableEffect(visible, filter, replyKey, actionPrompt, actionMenuIndex, rowConfirmation, HubSettings.quickKeyboardNavigation.value, isActiveSurface) {
        val navigationHandler: (Int) -> Boolean = navigate@{ keyCode ->
            if (rowConfirmation != null) return@navigate true
            actionPrompt?.let { item ->
                if (keyCode == android.view.KeyEvent.KEYCODE_U || keyCode == android.view.KeyEvent.KEYCODE_P) {
                    actionPrompt = null
                    actionConfirmation = null
                    actionResult = null
                    actionExecuting = null
                    return@navigate true
                }
                if (actionResult != null || actionExecuting != null) return@navigate true
                val entries = item.actionEntries(item.key in flaggedKeys, item.key in importantKeys)
                return@navigate when (keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_UP, android.view.KeyEvent.KEYCODE_I -> {
                        val next = (actionMenuIndex - 1).coerceAtLeast(0)
                        if (next != actionMenuIndex) SoundFeedback.play(context, CommandSound.Step)
                        actionMenuIndex = next; true
                    }
                    android.view.KeyEvent.KEYCODE_DPAD_DOWN, android.view.KeyEvent.KEYCODE_K -> {
                        val next = (actionMenuIndex + 1).coerceAtMost(entries.lastIndex)
                        if (next != actionMenuIndex) SoundFeedback.play(context, CommandSound.Step)
                        actionMenuIndex = next; true
                    }
                    android.view.KeyEvent.KEYCODE_ENTER, android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                    android.view.KeyEvent.KEYCODE_O -> {
                        entries.getOrNull(actionMenuIndex)?.let { performAction(item, it) }; true
                    }
                    else -> false
                }
            }
            if (replyKey != null || managing) return@navigate false
            when (keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_UP,
                android.view.KeyEvent.KEYCODE_I -> {
                    keyboardBrowsing = true
                    if (visible.isNotEmpty()) {
                        val next = (selectedIndex - 1).coerceAtLeast(0)
                        if (next != selectedIndex) SoundFeedback.play(context, CommandSound.Step)
                        selectedIndex = next
                    }
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                android.view.KeyEvent.KEYCODE_K -> {
                    keyboardBrowsing = true
                    if (visible.isNotEmpty()) {
                        val next = (selectedIndex + 1).coerceAtMost(visible.lastIndex)
                        if (next != selectedIndex) SoundFeedback.play(context, CommandSound.Step)
                        selectedIndex = next
                    }
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                android.view.KeyEvent.KEYCODE_J -> {
                    keyboardBrowsing = true
                    val index = filters.indexOf(filter).coerceAtLeast(0)
                    val next = (index - 1).coerceAtLeast(0)
                    if (next != index) SoundFeedback.play(context, CommandSound.Step)
                    filter = filters[next]
                    customFilterId = null
                    selectedIndex = 0
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                android.view.KeyEvent.KEYCODE_L -> {
                    keyboardBrowsing = true
                    val index = filters.indexOf(filter).coerceAtLeast(0)
                    val next = (index + 1).coerceAtMost(filters.lastIndex)
                    if (next != index) SoundFeedback.play(context, CommandSound.Step)
                    filter = filters[next]
                    customFilterId = null
                    selectedIndex = 0
                    true
                }
                android.view.KeyEvent.KEYCODE_ENTER, android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                android.view.KeyEvent.KEYCODE_O -> {
                    visible.getOrNull(selectedIndex)?.let { item ->
                        if (item.replyAction != null) {
                            markRead(item)
                            replyKey = item.key
                            replyItemSnapshot = item
                            replyText = ""
                            replyStatus = null
                        } else {
                            activateHubItem(context, item, { markRead(item) }) { }
                        }
                    }
                    visible.isNotEmpty()
                }
                android.view.KeyEvent.KEYCODE_U -> {
                    visible.getOrNull(selectedIndex)?.let {
                        HubRepository.dismiss(it)
                        SoundFeedback.play(context, CommandSound.Confirm)
                    }
                    visible.isNotEmpty()
                }
                android.view.KeyEvent.KEYCODE_F -> {
                    visible.getOrNull(selectedIndex)?.let {
                        toggleFlag(it.key)
                        SoundFeedback.play(context, CommandSound.Step)
                    }
                    visible.isNotEmpty()
                }
                android.view.KeyEvent.KEYCODE_E -> {
                    visible.getOrNull(selectedIndex)?.takeIf(HubItem::isExpandable)?.let {
                        expandedKey = if (expandedKey == it.key) null else it.key
                        SoundFeedback.play(context, CommandSound.Step)
                    }
                    visible.isNotEmpty()
                }
                android.view.KeyEvent.KEYCODE_P -> {
                    visible.getOrNull(selectedIndex)?.let(::openActions)
                    visible.isNotEmpty()
                }
                else -> false
            }
        }
        if (isActiveSurface) HubKeyBridge.registerNavigate(keyBridgeOwner, navigationHandler)
        onDispose { HubKeyBridge.unregisterNavigate(keyBridgeOwner) }
    }

    Box(Modifier.fillMaxSize().background(if (glass) Color.Transparent else Color.Black), contentAlignment = Alignment.TopCenter) {
    Box(Modifier.fillMaxSize().background(palette.background).padding(8.dp), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(palette.panel)
                .border(1.dp, palette.border, RoundedCornerShape(24.dp)).padding(20.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("hub", color = palette.text, fontFamily = HubSerif, fontSize = 38.sp, modifier = Modifier.alignByBaseline())
                Text(
                    "$newCount new",
                    color = HubAccent,
                    fontFamily = HubMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.alignByBaseline().padding(start = 8.dp),
                )
                Spacer(Modifier.weight(1f))
                Row(Modifier.height(32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.height(32.dp).clickable { managing = !managing }.padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (managing) "done" else "manage",
                            color = if (managing) HubAccent else palette.secondary,
                            fontFamily = HubMono,
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        )
                    }
                    Box(
                        Modifier.size(32.dp).clip(CircleShape).clickable {
                            context.startActivity(Intent(context, HubHistoryActivity::class.java))
                        }.padding(7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Open notification history",
                            tint = palette.secondary,
                            modifier = Modifier.size(18.dp).offset(y = 1.dp),
                        )
                    }
                    Box(
                        Modifier.height(32.dp).clip(RoundedCornerShape(10.dp)).clickable {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }.padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "settings",
                            color = palette.secondary,
                            fontFamily = HubMono,
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        )
                    }
                    Box(
                        Modifier.height(32.dp).clickable { onDismiss() }.padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "close →",
                            color = palette.secondary,
                            fontFamily = HubMono,
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        )
                    }
                }
            }
            val filterScroll = rememberScrollState()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp).horizontalScroll(filterScroll)
                    .padding(top = 14.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                filters.forEach { entry ->
                    val filterBringIntoView = remember(entry) { BringIntoViewRequester() }
                    val selected = customFilterId == null && filter == entry
                    LaunchedEffect(selected, filters) {
                        if (selected) {
                            delay(45)
                            filterBringIntoView.bringIntoView()
                        }
                    }
                    Text(
                        entry.label,
                        color = if (selected) Color.White else palette.secondary,
                        fontFamily = HubMono,
                        fontSize = 12.sp,
                        modifier = Modifier.bringIntoViewRequester(filterBringIntoView)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) HubAccent else Color.Transparent)
                            .border(1.dp, if (selected) HubAccent else palette.border, RoundedCornerShape(20.dp))
                            .clickable { filter = entry; customFilterId = null }.padding(horizontal = 11.dp, vertical = 5.dp),
                    )
                }
                customCategories.forEach { category ->
                    val selected = customFilterId == category.id
                    val filterBringIntoView = remember(category.id) { BringIntoViewRequester() }
                    LaunchedEffect(selected, customCategories) {
                        if (selected) { delay(45); filterBringIntoView.bringIntoView() }
                    }
                    Text(
                        category.label,
                        color = if (selected) Color.White else palette.secondary,
                        fontFamily = HubMono,
                        fontSize = 12.sp,
                        modifier = Modifier.bringIntoViewRequester(filterBringIntoView)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) HubAccent else Color.Transparent)
                            .border(1.dp, if (selected) HubAccent else palette.border, RoundedCornerShape(20.dp))
                            .clickable { customFilterId = category.id }
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier.fillMaxSize()
                    .padding(end = if (notificationScroll.maxValue > 0) 9.dp else 0.dp)
                    .verticalScroll(notificationScroll),
            ) {
                if (managing) {
                    HubCategoryManager(
                        allItems, categoryOverrides, customCategories, palette, ::cycleCategory,
                        onCreate = { label ->
                            val category = HubCustomCategories.create(label)
                            customCategories = customCategories + category
                            HubCustomCategories.save(context, customCategories)
                        },
                        onRename = { id, label ->
                            customCategories = customCategories.map {
                                if (it.id == id) it.copy(label = label.trim()) else it
                            }
                            HubCustomCategories.save(context, customCategories)
                        },
                        onDelete = { category ->
                            customCategories = customCategories.filterNot { it.id == category.id }
                            HubCustomCategories.save(context, customCategories)
                            val affected = categoryOverrides.filterValues { it == category.id }.keys
                            categoryOverrides = categoryOverrides - affected
                            categoryPrefs.edit().apply { affected.forEach(::remove) }.apply()
                            if (customFilterId == category.id) customFilterId = null
                        },
                    )
                    Spacer(Modifier.height(24.dp))
                    HubIgnoredManager(palette) { rule ->
                        HubIgnoreSettings.remove(context, rule.id)
                        HubRepository.requestRefresh()
                    }
                } else if (!hasAccess) {
                    HubAccessCard(palette, openAccessSettings)
                } else if (displayedVisible.isEmpty()) {
                    Text("nothing new", color = palette.secondary, fontFamily = HubMono, modifier = Modifier.padding(vertical = 28.dp).align(Alignment.CenterHorizontally))
                } else {
                    displayedVisible.take(50).forEachIndexed { index, item ->
                        key(item.key) {
                            val bringIntoViewRequester = remember { BringIntoViewRequester() }
                            LaunchedEffect(isActiveSurface, index == selectedIndex, expandedKey == item.key) {
                                if (isActiveSurface && (index == selectedIndex || expandedKey == item.key)) {
                                    delay(40)
                                    bringIntoViewRequester.bringIntoView()
                                    if (index == displayedVisible.take(50).lastIndex) {
                                        notificationScroll.scrollTo(notificationScroll.maxValue)
                                    }
                                }
                            }
                            Box(
                                Modifier.fillMaxWidth()
                                    .bringIntoViewRequester(bringIntoViewRequester)
                                    .padding(horizontal = 6.dp, vertical = 5.dp),
                            ) {
                            if (index == selectedIndex) {
                                Box(
                                    Modifier.matchParentSize()
                                        .blur(11.dp, BlurredEdgeTreatment.Unbounded)
                                        .border(4.dp, HubAccent.copy(alpha = .9f), RoundedCornerShape(16.dp)),
                                )
                            }
                            AnimatedContent(
                                targetState = swipeActionsKey == item.key,
                                transitionSpec = {
                                    if (targetState) {
                                        (slideInHorizontally(tween(210)) { -it / 3 } + fadeIn(tween(170))) togetherWith
                                            (slideOutHorizontally(tween(180)) { it / 4 } + fadeOut(tween(130)))
                                    } else {
                                        (slideInHorizontally(tween(210)) { it / 3 } + fadeIn(tween(170))) togetherWith
                                            (slideOutHorizontally(tween(180)) { -it / 4 } + fadeOut(tween(130)))
                                    }
                                },
                                label = "Hub swipe actions",
                            ) { actionsOpen ->
                            if (actionsOpen) {
                                HubSwipeActions(
                                    flagged = item.key in flaggedKeys,
                                    palette = palette,
                                    onFlag = { toggleFlag(item.key); swipeActionsKey = null },
                                    onDismiss = { HubRepository.dismiss(item); swipeActionsKey = null },
                                    onCancel = {
                                        scope.launch {
                                            delay(120)
                                            swipeActionsKey = null
                                        }
                                    },
                                )
                            } else if (rowConfirmation?.item?.key == item.key) {
                                HubRowConfirmationCard(rowConfirmation!!, palette)
                            } else Box(
                                Modifier.wrapContentHeight(unbounded = true).pointerInput(item.key) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        var claimed = false
                                        var distance = 0f
                                        val slop = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                                            if (over > 0f) {
                                                claimed = true
                                                distance = over
                                                change.consume()
                                            }
                                        }
                                        if (slop != null && claimed) {
                                            horizontalDrag(slop.id) { change ->
                                                distance += change.positionChange().x
                                                change.consume()
                                            }
                                            if (distance > size.width * .12f) {
                                                swipeActionsKey = item.key
                                                SoundFeedback.play(context, CommandSound.Step)
                                            }
                                        }
                                    }
                                },
                            ) {
                                HubNotificationCard(
                                    item = item,
                                    effectiveCategory = item.effectiveCategory(categoryOverrides),
                                    palette = palette,
                                    selected = index == selectedIndex,
                                    flagged = item.key in flaggedKeys,
                                    expanded = expandedKey == item.key,
                                    showNotificationImage = HubSettings.showNotificationImages.value,
                                    tintNotificationImage = HubSettings.tintNotificationImages.value,
                                    notificationImageOnRight = HubSettings.notificationImagesOnRight.value,
                                    unread = !item.detached && !item.isSummary && (readTimes[item.key] ?: 0L) < item.time,
                                    replying = replyKey == item.key,
                                    replyText = replyText,
                                    replyStatus = replyStatus?.takeIf { it.first == item.key }?.second,
                                    onToggleExpanded = {
                                        expandedKey = if (expandedKey == item.key) null else item.key
                                    },
                                    onLongPress = { openActions(item) },
                                    onToggleFlag = {
                                        toggleFlag(item.key)
                                        SoundFeedback.play(context, CommandSound.Step)
                                    },
                                    onMarkRead = { markRead(item) },
                                    onToggleReply = {
                                        markRead(item)
                                        if (replyKey == item.key) {
                                            replyKey = null
                                            replyItemSnapshot = null
                                        } else {
                                            replyKey = item.key
                                            replyItemSnapshot = item
                                        }
                                        replyText = ""
                                        replyStatus = null
                                    },
                                    onReplyTextChange = { replyText = it },
                                    onSendReply = {
                                        HubRepository.requestRefresh()
                                        val replyTarget = HubRepository.items.firstOrNull {
                                            it.key == item.key && it.replyAction != null
                                        } ?: item
                                        val sent = replyTarget.sendReply(context, replyText.trim())
                                        replyStatus = item.key to if (sent) "reply sent" else "couldn't send"
                                        if (sent) {
                                            replyText = ""
                                            replyItemSnapshot = replyTarget
                                            scope.launch {
                                                delay(180)
                                                HubRepository.requestRefresh()
                                            }
                                            if (HubSettings.closeReplyAfterSending.value) {
                                                scope.launch {
                                                    delay(1_250)
                                                    replyKey = null
                                                    replyItemSnapshot = null
                                                    replyStatus = null
                                                }
                                            }
                                        }
                                    },
                                )
                            }
                            }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
            if (notificationScroll.maxValue > 0) {
                BoxWithConstraints(
                    Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(3.dp)
                        .clip(CircleShape).background(palette.border.copy(alpha = .62f)),
                ) {
                    val thumbHeight = 36.dp.coerceAtMost(maxHeight)
                    val travel = (maxHeight - thumbHeight).coerceAtLeast(0.dp)
                    val progress = notificationScroll.value.toFloat() / notificationScroll.maxValue.toFloat()
                    Box(
                        Modifier.offset(y = travel * progress).fillMaxWidth().height(thumbHeight)
                            .clip(CircleShape).background(HubAccent),
                    )
                }
            }
            }
        }
        val livePopupItem = allItems.firstOrNull { it.key == replyKey }
        LaunchedEffect(livePopupItem) {
            if (livePopupItem != null) replyItemSnapshot = livePopupItem
        }
        val popupItem = livePopupItem ?: replyItemSnapshot?.takeIf { it.key == replyKey }
        if (popupItem != null) {
            HubReplyPopup(
                item = popupItem,
                palette = palette,
                replyText = replyText,
                replyStatus = replyStatus?.takeIf { it.first == popupItem.key }?.second,
                onReplyTextChange = { replyText = it },
                onDismiss = {
                    replyKey = null
                    replyItemSnapshot = null
                    replyText = ""
                    replyStatus = null
                },
                onSendReply = {
                    HubRepository.requestRefresh()
                    val replyTarget = HubRepository.items.firstOrNull {
                        it.key == popupItem.key && it.replyAction != null
                    } ?: popupItem
                    val sent = replyTarget.sendReply(context, replyText.trim())
                    replyStatus = popupItem.key to if (sent) "reply sent" else "couldn't send"
                    if (sent) {
                        replyText = ""
                        replyItemSnapshot = replyTarget
                        scope.launch {
                            delay(180)
                            HubRepository.requestRefresh()
                        }
                        if (HubSettings.closeReplyAfterSending.value) {
                            scope.launch {
                                delay(850)
                                replyKey = null
                                replyItemSnapshot = null
                                replyStatus = null
                            }
                        }
                    }
                },
            )
        }
        actionPrompt?.let { item ->
            HubActionsPrompt(
                item = item,
                palette = palette,
                entries = item.actionEntries(item.key in flaggedKeys, item.key in importantKeys),
                selectedIndex = actionMenuIndex,
                confirmation = actionConfirmation,
                result = actionResult,
                executing = actionExecuting,
                onDismiss = { actionPrompt = null; actionConfirmation = null; actionExecuting = null; actionResult = null },
                onSelect = { actionMenuIndex = it; actionConfirmation = null },
                onActivate = { performAction(item, it) },
            )
        }
    }
    }
}

@Composable
private fun HubReplyPopup(
    item: HubItem,
    palette: HubPalette,
    replyText: String,
    replyStatus: String?,
    onReplyTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSendReply: () -> Unit,
) {
    val context = LocalContext.current
    val focusRequester = remember(item.key) { FocusRequester() }
    BackHandler(onBack = onDismiss)
    LaunchedEffect(item.key) { delay(60); focusRequester.requestFocus() }
    DisposableEffect(item.key, replyText, onSendReply) {
        HubKeyBridge.canSend = { replyText.isNotBlank() }
        HubKeyBridge.sendReply = onSendReply
        onDispose {
            HubKeyBridge.canSend = null
            HubKeyBridge.sendReply = null
        }
    }
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 560.dp).padding(18.dp)
                .clip(RoundedCornerShape(22.dp)).background(palette.panel)
                .border(1.dp, HubAccent.copy(alpha = .75f), RoundedCornerShape(22.dp))
                .onPreviewKeyEvent { event ->
                    val enter = event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                        event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
                    if (enter) {
                        if (event.type == KeyEventType.KeyDown && replyText.isNotBlank()) onSendReply()
                        replyText.isNotBlank()
                    } else false
                }
                .clickable(enabled = false) {}.padding(18.dp),
        ) {
            Text("reply", color = palette.secondary, fontFamily = HubMono, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val displayImage = HubImageCache.get(item.displayImageKey)
                if (HubSettings.showNotificationImages.value && displayImage != null) {
                    Image(
                        bitmap = displayImage.asImageBitmap(),
                        contentDescription = null,
                        colorFilter = when {
                            item.displayImageIsMonochrome -> ColorFilter.tint(HubAccent, BlendMode.SrcIn)
                            HubSettings.tintNotificationImages.value -> ColorFilter.tint(HubAccent, BlendMode.Color)
                            else -> null
                        },
                        modifier = Modifier.padding(end = 10.dp).size(32.dp).clip(CircleShape),
                    )
                }
                Text(
                    item.primaryHubTitle(),
                    color = HubAccent,
                    fontFamily = HubSerif,
                    fontSize = 28.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val history = if (item.messages.isNotEmpty()) item.messages.takeLast(5) else listOf(HubMessage(item.text, item.title, false))
            Spacer(Modifier.height(18.dp))
            Column(Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
                history.filter { it.text.isNotBlank() }.forEach { message ->
                    val incoming = !message.outgoing
                    Box(
                        Modifier.align(if (incoming) Alignment.Start else Alignment.End)
                            .fillMaxWidth(if (incoming) .88f else .78f)
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (incoming) palette.tile else HubAccent.copy(alpha = .18f))
                            .border(1.dp, if (incoming) palette.border else HubAccent.copy(alpha = .42f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 13.dp, vertical = 10.dp),
                    ) {
                        Column {
                            if (item.conversationTitle != null && !message.outgoing && !message.sender.isNullOrBlank()) {
                                Text(
                                    message.sender,
                                    color = HubAccent,
                                    fontFamily = HubSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                )
                                Spacer(Modifier.height(3.dp))
                            }
                            Text(message.text, color = palette.text, fontFamily = HubSans, fontSize = 14.sp)
                        }
                    }
                }
            }
            Text("▸ reply → ${item.title}", color = palette.secondary, fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.padding(top = 13.dp, bottom = 7.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.tile)
                    .border(1.dp, HubAccent.copy(alpha = .75f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (replyText.isEmpty()) Text(replyStatus ?: "type reply…", color = palette.secondary, fontFamily = HubMono, fontSize = 14.sp)
                    BasicTextField(
                        value = replyText,
                        onValueChange = onReplyTextChange,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                val enter = event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                                    event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
                                if (enter && event.type == KeyEventType.KeyDown) {
                                    if (replyText.isNotBlank()) onSendReply()
                                    replyText.isNotBlank()
                                } else if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL && replyText.isEmpty()) {
                                    if (event.type == KeyEventType.KeyDown) onDismiss()
                                    true
                                } else false
                            },
                        textStyle = TextStyle(color = palette.text, fontFamily = HubMono, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        cursorBrush = SolidColor(HubAccent),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (replyText.isNotBlank()) onSendReply() }),
                    )
                }
                Text(
                    "↵", color = if (replyText.isNotBlank()) HubAccent else palette.secondary,
                    fontFamily = HubMono, fontWeight = FontWeight.Bold, fontSize = 32.sp,
                    modifier = Modifier.padding(start = 10.dp).clickable(enabled = replyText.isNotBlank(), onClick = onSendReply),
                )
            }
            Text("tap outside to close", color = palette.secondary, fontFamily = HubMono, fontSize = 10.sp, modifier = Modifier.align(Alignment.End).padding(top = 9.dp))
        }
    }
}

@Composable
private fun HubCategoryManager(
    items: List<HubItem>,
    overrides: Map<String, String>,
    customCategories: List<HubCustomCategory>,
    palette: HubPalette,
    cycle: (String) -> Unit,
    onCreate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (HubCustomCategory) -> Unit,
) {
    var categoryDraft by remember { mutableStateOf("") }
    var renamingId by remember { mutableStateOf<String?>(null) }
    var renameDraft by remember { mutableStateOf("") }
    val apps = remember(items) { items.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() } }
    Text("custom categories", color = palette.text, fontFamily = HubSans,
        fontWeight = FontWeight.Bold, fontSize = 17.sp)
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = categoryDraft,
            onValueChange = { categoryDraft = it.take(28) },
            singleLine = true,
            textStyle = TextStyle(color = palette.text, fontFamily = HubMono, fontSize = 13.sp),
            cursorBrush = SolidColor(HubAccent),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(palette.tile)
                    .border(1.dp, palette.border, RoundedCornerShape(11.dp)).padding(11.dp)) {
                    if (categoryDraft.isBlank()) Text("new category name", color = palette.secondary,
                        fontFamily = HubMono, fontSize = 12.sp)
                    inner()
                }
            },
            modifier = Modifier.weight(1f),
        )
        Text("ADD", color = if (categoryDraft.isBlank()) palette.secondary else HubAccent,
            fontFamily = HubMono, fontSize = 12.sp,
            modifier = Modifier.padding(start = 10.dp).clickable(enabled = categoryDraft.isNotBlank()) {
                onCreate(categoryDraft)
                categoryDraft = ""
            }.padding(8.dp))
    }
    customCategories.forEach { category ->
        Row(
            Modifier.fillMaxWidth().padding(bottom = 7.dp).clip(RoundedCornerShape(12.dp))
                .background(palette.tile).border(1.dp, palette.border, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (renamingId == category.id) {
                BasicTextField(renameDraft, { renameDraft = it.take(28) }, singleLine = true,
                    textStyle = TextStyle(color = palette.text, fontFamily = HubMono, fontSize = 13.sp),
                    cursorBrush = SolidColor(HubAccent), modifier = Modifier.weight(1f))
                Text("SAVE", color = HubAccent, fontFamily = HubMono, fontSize = 11.sp,
                    modifier = Modifier.clickable(enabled = renameDraft.isNotBlank()) {
                        onRename(category.id, renameDraft)
                        renamingId = null
                    }.padding(6.dp))
            } else {
                Text(category.label, color = palette.text, fontFamily = HubMono, fontSize = 13.sp,
                    modifier = Modifier.weight(1f))
                Text("rename", color = palette.secondary, fontFamily = HubMono, fontSize = 10.sp,
                    modifier = Modifier.clickable {
                        renamingId = category.id
                        renameDraft = category.label
                    }.padding(6.dp))
                Text("×", color = HubAccent, fontFamily = HubMono, fontSize = 16.sp,
                    modifier = Modifier.clickable { onDelete(category) }.padding(start = 6.dp))
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    if (apps.isEmpty()) {
        Text("apps appear here after they post a notification", color = palette.secondary,
            fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.padding(vertical = 24.dp))
        return
    }
    Text("tap an app to change its category", color = palette.secondary, fontFamily = HubMono, fontSize = 11.sp, modifier = Modifier.padding(bottom = 10.dp))
    apps.take(14).forEachIndexed { index, item ->
        if (index > 0) Spacer(Modifier.size(7.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.tile)
                .border(1.dp, palette.border, RoundedCornerShape(14.dp)).clickable { cycle(item.packageName) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(HubAccent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Text(item.appName.take(1).uppercase(), color = HubAccent, fontFamily = HubSans, fontWeight = FontWeight.Bold)
            }
            Text(item.appName, Modifier.padding(start = 12.dp).weight(1f), color = palette.text, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(overrides[item.packageName] ?: "auto", color = if (item.packageName in overrides) HubAccent else palette.secondary, fontFamily = HubMono, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HubIgnoredManager(
    palette: HubPalette,
    onRestore: (HubIgnoreRule) -> Unit,
) {
    Text("ignored notifications", color = palette.text, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    Text("tap a rule to restore it", color = palette.secondary, fontFamily = HubMono, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
    if (HubIgnoreSettings.rules.value.isEmpty()) {
        Text("nothing ignored", color = palette.secondary, fontFamily = HubMono, fontSize = 12.sp)
        return
    }
    HubIgnoreSettings.rules.value.forEachIndexed { index, rule ->
        if (index > 0) Spacer(Modifier.height(7.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.tile)
                .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                .clickable { onRestore(rule) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(rule.appName, color = palette.text, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(rule.scopeLabel ?: "all notifications", color = palette.secondary, fontFamily = HubMono, fontSize = 11.sp)
            }
            Text("restore", color = HubAccent, fontFamily = HubMono, fontSize = 11.sp)
        }
    }
}

@Composable
private fun HubActionsPrompt(
    item: HubItem,
    palette: HubPalette,
    entries: List<HubActionEntry>,
    selectedIndex: Int,
    confirmation: HubNotificationAction?,
    result: Pair<Boolean, String>?,
    executing: String?,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onActivate: (HubActionEntry) -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val actionListMaxHeight = (screenHeight - 190.dp).coerceIn(150.dp, 430.dp)
    val actionScrollState = rememberScrollState()
    BackHandler(onBack = onDismiss)
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 480.dp).padding(22.dp)
                .clip(RoundedCornerShape(22.dp)).background(palette.panel)
                .border(1.dp, HubAccent.copy(alpha = .72f), RoundedCornerShape(22.dp))
                .clickable(enabled = false) {}.padding(18.dp),
        ) {
            if (result != null) {
                Box(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                        .background(HubAccent.copy(alpha = if (result.first) .18f else .08f))
                        .border(1.dp, HubAccent.copy(alpha = .82f), RoundedCornerShape(17.dp))
                        .padding(start = 15.dp, end = 44.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(31.dp).clip(CircleShape).background(HubAccent), contentAlignment = Alignment.Center) {
                        Text(if (result.first) "✓" else "!", color = Color.White, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Text(result.second, Modifier.padding(start = 12.dp), color = palette.text, fontFamily = HubSerif, fontSize = 25.sp)
                }
                Box(
                    Modifier.align(Alignment.TopEnd).padding(5.dp).size(34.dp).clip(CircleShape).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Close, "Close actions", tint = palette.secondary, modifier = Modifier.size(20.dp))
                }
                }
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("notification actions", color = palette.secondary, fontFamily = HubMono, fontSize = 11.sp)
                        Text(item.primaryHubTitle(), color = palette.text, fontFamily = HubSerif, fontSize = 27.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Close, "Close actions", tint = palette.secondary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().heightIn(max = actionListMaxHeight)) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(actionScrollState),
                ) {
                entries.forEachIndexed { index, entry ->
                    val bringIntoViewRequester = remember(item.key, index) { BringIntoViewRequester() }
                    LaunchedEffect(index == selectedIndex) {
                        if (index == selectedIndex) bringIntoViewRequester.bringIntoView()
                    }
                    if (index > 0) Spacer(Modifier.height(8.dp))
                    val subtitle = when (entry) {
                        HubActionEntry.Open -> "open ${item.appName}"
                        HubActionEntry.Reply -> "reply without leaving Hub"
                        is HubActionEntry.Flag -> if (entry.flagged) "remove from favourites" else "save to favourites"
                        is HubActionEntry.Important -> if (entry.important) "return to normal priority" else "show in Important"
                        is HubActionEntry.ImportantSource -> if (entry.active) {
                            "future matching notifications return to normal priority"
                        } else when (entry.rule.kind) {
                            "conversation" -> "prioritise this person or conversation in future"
                            "type" -> "prioritise this notification type in future"
                            else -> "prioritise every notification from this app"
                        }
                        is HubActionEntry.NotificationAction -> if (entry.action.destructive && confirmation == entry.action) "press again to confirm" else "action provided by ${item.appName}"
                        HubActionEntry.Snooze -> "hide this notification for 1 hour"
                        HubActionEntry.Dismiss -> "remove this notification"
                        HubActionEntry.IgnoreScope -> "keep other ${item.appName} notifications"
                        HubActionEntry.IgnoreApp -> "hide the entire app from Hub"
                    }
                    HubActionChoice(entry.displayLabel(item), subtitle, entry.icon(item), palette, index == selectedIndex,
                        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
                        confirm = entry is HubActionEntry.NotificationAction && entry.action.destructive && confirmation == entry.action,
                        active = entry.label == executing) {
                        onSelect(index)
                        onActivate(entry)
                    }
                }
                }
            }
            }
            Text("tap outside to cancel", color = palette.secondary, fontFamily = HubMono, fontSize = 10.sp, modifier = Modifier.align(Alignment.End).padding(top = 12.dp))
        }
    }
}

@Composable
private fun HubActionChoice(
    title: String,
    subtitle: String,
    icon: ImageVector,
    palette: HubPalette,
    selected: Boolean,
    modifier: Modifier = Modifier,
    confirm: Boolean,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(modifier.fillMaxWidth()) {
        if (active) {
            Box(
                Modifier.matchParentSize().blur(13.dp, BlurredEdgeTreatment.Unbounded)
                    .clip(RoundedCornerShape(14.dp)).background(HubAccent.copy(alpha = .48f)),
            )
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(if (active) HubAccent.copy(alpha = .32f).compositeOver(palette.tile) else palette.tile)
                .border(if (selected || active) 2.dp else 1.dp, if (selected || active) HubAccent else palette.border, RoundedCornerShape(14.dp))
                .clickable(enabled = !active, onClick = onClick).padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) Color.White else if (selected || confirm) HubAccent else palette.secondary,
                modifier = Modifier.size(22.dp),
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, color = if (confirm) HubAccent else palette.text, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = if (confirm) HubAccent else palette.secondary, fontFamily = HubMono, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun HubRowConfirmationCard(confirmation: HubRowConfirmation, palette: HubPalette) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier.matchParentSize().blur(13.dp, BlurredEdgeTreatment.Unbounded)
                .clip(RoundedCornerShape(16.dp)).background(HubAccent.copy(alpha = .38f)),
        )
        Row(
            Modifier.fillMaxWidth().heightIn(min = 74.dp).clip(RoundedCornerShape(16.dp))
                .background(HubAccent.copy(alpha = .18f).compositeOver(palette.tile))
                .border(2.dp, HubAccent, RoundedCornerShape(16.dp))
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(31.dp).clip(CircleShape).background(HubAccent), contentAlignment = Alignment.Center) {
                Text(if (confirmation.success) "✓" else "!", color = Color.White, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Text(
                confirmation.text,
                modifier = Modifier.padding(start = 12.dp),
                color = palette.text,
                fontFamily = HubSerif,
                fontSize = 25.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HubSwipeActions(
    flagged: Boolean,
    palette: HubPalette,
    onFlag: () -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(78.dp).clip(RoundedCornerShape(16.dp))
            .background(palette.tile).border(1.dp, HubAccent.copy(alpha = .46f), RoundedCornerShape(16.dp))
            .padding(7.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                .background(HubAccent.copy(alpha = .16f)).clickable(onClick = onFlag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(if (flagged) Icons.Outlined.Star else Icons.Outlined.StarOutline, null, tint = HubAccent, modifier = Modifier.size(20.dp))
            Text(if (flagged) "unflag" else "flag", color = palette.text, fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Row(
            Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFB3261E).copy(alpha = .20f)).clickable(onClick = onDismiss),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Outlined.DeleteOutline, null, tint = Color(0xFFFF766C), modifier = Modifier.size(20.dp))
            Text("dismiss", color = palette.text, fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Row(
            Modifier.weight(.72f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                .background(palette.border.copy(alpha = .34f)).clickable(onClick = onCancel),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Outlined.Close, null, tint = palette.secondary, modifier = Modifier.size(20.dp))
            Text("cancel", color = palette.text, fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.padding(start = 7.dp))
        }
    }
}

@Composable
private fun HubAccessCard(palette: HubPalette, openSettings: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.tile)
            .border(1.dp, HubAccent.copy(alpha = .5f), RoundedCornerShape(16.dp)).clickable(onClick = openSettings).padding(17.dp),
    ) {
        Text("notification access required", color = palette.text, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text("tap to allow Commander Hub to show your notifications", color = palette.secondary, fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun HubNotificationCard(
    item: HubItem,
    effectiveCategory: String,
    palette: HubPalette,
    selected: Boolean,
    flagged: Boolean,
    expanded: Boolean,
    showNotificationImage: Boolean,
    tintNotificationImage: Boolean,
    notificationImageOnRight: Boolean,
    unread: Boolean,
    replying: Boolean,
    replyText: String,
    replyStatus: String?,
    onToggleExpanded: () -> Unit,
    onLongPress: () -> Unit,
    onToggleFlag: () -> Unit,
    onMarkRead: () -> Unit,
    onToggleReply: () -> Unit,
    onReplyTextChange: (String) -> Unit,
    onSendReply: () -> Unit,
) {
    val context = LocalContext.current
    val time = remember(item.time) {
        Instant.ofEpochMilli(item.time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    val primaryTitle = remember(item) { item.primaryHubTitle() }
    val appNameColor = remember(item.packageName, HubSettings.appNameIconColor.value) {
        if (HubSettings.appNameIconColor.value) context.hubAppNameColor(item.packageName) else null
    }
    val focusRequester = remember(item.key) { FocusRequester() }
    LaunchedEffect(replying) { if (replying) focusRequester.requestFocus() }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            // The swipe action sits behind this card. Composite the unread
            // tint over the tile first so "dismiss" cannot show through and
            // overlap the timestamp/app label.
            .background(
                if (selected) HubAccent.copy(alpha = if (palette.background == Color.Black) .19f else .13f)
                    .compositeOver(palette.tile)
                else if (unread) HubAccent.copy(alpha = if (palette.background == Color.Black) .09f else .07f)
                    .compositeOver(palette.tile)
                else palette.tile,
            )
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) HubAccent else if (unread || flagged) HubAccent.copy(alpha = .6f) else palette.border,
                RoundedCornerShape(16.dp),
            )
            .combinedClickable(
                onClick = { activateHubItem(context, item, onMarkRead, onToggleReply) },
                onLongClick = onLongPress,
            ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(15.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(Modifier.width(27.dp).heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = hubIcon(effectiveCategory),
                    contentDescription = effectiveCategory,
                    tint = if (item.isLowPriority()) palette.secondary else HubAccent,
                    modifier = Modifier.size(27.dp),
                )
            }
            Column(Modifier.padding(start = 15.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        primaryTitle,
                        color = palette.text,
                        fontFamily = HubSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    val displayImage = HubImageCache.get(item.displayImageKey)
                    if (showNotificationImage && !notificationImageOnRight && displayImage != null) {
                        Image(
                            bitmap = displayImage.asImageBitmap(),
                            contentDescription = null,
                            colorFilter = when {
                                item.displayImageIsMonochrome -> ColorFilter.tint(HubAccent, BlendMode.SrcIn)
                                tintNotificationImage -> ColorFilter.tint(HubAccent, BlendMode.Color)
                                else -> null
                            },
                            modifier = Modifier.padding(start = 10.dp).size(17.dp).clip(CircleShape),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                val recentMessages = when {
                    item.messages.isNotEmpty() -> if (expanded) item.messages else item.messages.takeLast(if (replying) 5 else 3)
                    expanded && item.textLines.isNotEmpty() -> item.textLines.map { HubMessage(it, item.title, false) }
                    else -> listOf(
                        HubMessage(
                            if (expanded) item.expandedText.ifBlank { item.text }.cleanExpandedNotificationText() else item.text,
                            item.title,
                            false,
                        ),
                    )
                }
                recentMessages.filter { it.text.isNotBlank() }.forEach { message ->
                    val preview = if (item.conversationTitle != null && !message.sender.isNullOrBlank() &&
                        !message.sender.equals(primaryTitle, ignoreCase = true)
                    ) "${message.sender}: ${message.text}" else message.text
                    Text(
                        preview,
                        color = palette.secondary,
                        fontFamily = HubSans,
                        fontSize = 13.sp,
                        maxLines = if (expanded) {
                            if (item.messages.isNotEmpty()) 4 else 14
                        } else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (expanded) Modifier.padding(bottom = 3.dp) else Modifier,
                    )
                }
            }
            Box(
                Modifier.padding(start = 10.dp).width(112.dp).heightIn(min = 48.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(27.dp).clip(CircleShape).clickable(onClick = onToggleFlag),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (flagged) Icons.Outlined.Flag else Icons.Outlined.OutlinedFlag,
                            contentDescription = if (flagged) "Unflag" else "Flag",
                            tint = if (flagged) HubAccent else palette.secondary,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Box(Modifier.padding(end = 5.dp).size(6.dp), contentAlignment = Alignment.Center) {
                        if (unread) Box(Modifier.size(5.dp).clip(CircleShape).background(HubAccent))
                    }
                    val displayImage = HubImageCache.get(item.displayImageKey)
                    if (showNotificationImage && notificationImageOnRight && displayImage != null) {
                        Image(
                            bitmap = displayImage.asImageBitmap(),
                            contentDescription = null,
                            colorFilter = when {
                                item.displayImageIsMonochrome -> ColorFilter.tint(HubAccent, BlendMode.SrcIn)
                                tintNotificationImage -> ColorFilter.tint(HubAccent, BlendMode.Color)
                                else -> null
                            },
                            modifier = Modifier.padding(end = 6.dp).size(17.dp).clip(CircleShape),
                        )
                    }
                    Text(time, color = palette.secondary, fontFamily = HubMono, fontSize = 11.sp)
                }
                if (!item.appName.equals(primaryTitle, ignoreCase = true) || item.isExpandable()) {
                    Row(Modifier.height(25.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (item.isExpandable()) {
                            Box(
                                Modifier.size(25.dp).clip(CircleShape).clickable(onClick = onToggleExpanded),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = if (expanded) "Collapse notification" else "Expand notification",
                                    tint = if (expanded) HubAccent else palette.secondary,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                        if (!item.appName.equals(primaryTitle, ignoreCase = true)) {
                            Text(
                                item.appName,
                                color = appNameColor ?: palette.secondary,
                                fontFamily = HubMono,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            }
        }
        if (false && replying) {
            Text(
                "▸ reply → ${item.title}",
                color = palette.secondary,
                fontFamily = HubMono,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 3.dp),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp)
                    .clip(RoundedCornerShape(14.dp)).background(palette.panel)
                    .border(1.dp, HubAccent.copy(alpha = .7f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (replyText.isEmpty()) Text(replyStatus ?: "type reply…", color = palette.secondary, fontFamily = HubMono, fontSize = 14.sp)
                    BasicTextField(
                        value = replyText,
                        onValueChange = onReplyTextChange,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                                    if (event.type == KeyEventType.KeyDown && replyText.isNotBlank()) onSendReply()
                                    true
                                } else false
                            },
                        textStyle = TextStyle(color = palette.text, fontFamily = HubMono, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        cursorBrush = SolidColor(HubAccent),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (replyText.isNotBlank()) onSendReply() }),
                    )
                }
                Text(
                    "↵",
                    color = if (replyText.isNotBlank()) HubAccent else palette.secondary,
                    fontFamily = HubMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 27.sp,
                    modifier = Modifier.padding(start = 10.dp).clickable(enabled = replyText.isNotBlank(), onClick = onSendReply),
                )
            }
        }
    }
}

private val hubAppNameColorCache = mutableMapOf<String, Color>()

private fun android.content.Context.hubAppNameColor(packageName: String): Color? {
    synchronized(hubAppNameColorCache) { hubAppNameColorCache[packageName]?.let { return it } }
    val color = runCatching {
        val bitmap = packageManager.getApplicationIcon(packageName).toBitmap(32, 32)
        val hsv = FloatArray(3)
        var bestScore = -1f
        var best = android.graphics.Color.WHITE
        for (y in 0 until bitmap.height step 2) for (x in 0 until bitmap.width step 2) {
            val pixel = bitmap.getPixel(x, y)
            if (android.graphics.Color.alpha(pixel) < 150) continue
            android.graphics.Color.colorToHSV(pixel, hsv)
            val score = hsv[1] * (.45f + hsv[2])
            if (score > bestScore && hsv[2] > .22f) {
                bestScore = score
                best = android.graphics.Color.HSVToColor(
                    floatArrayOf(hsv[0], hsv[1].coerceIn(.42f, .78f), hsv[2].coerceIn(.72f, .96f)),
                )
            }
        }
        Color(best)
    }.getOrNull() ?: return null
    synchronized(hubAppNameColorCache) { hubAppNameColorCache[packageName] = color }
    return color
}

private fun activateHubItem(context: android.content.Context, item: HubItem, onMarkRead: () -> Unit, onReply: () -> Unit) {
    if (item.replyAction != null) {
        SoundFeedback.play(context, CommandSound.Open)
        onReply()
        return
    }
    openHubItem(context, item, onMarkRead)
}

private fun openHubItem(context: android.content.Context, item: HubItem, onMarkRead: () -> Unit) {
    onMarkRead()
    SoundFeedback.play(context, CommandSound.Open)
    HubRepository.prepareExternalMutation(item)
    val openedNotification = item.contentIntent?.let { pending ->
        runCatching {
            val options = ActivityOptions.makeBasic().apply {
                if (Build.VERSION.SDK_INT >= 34) {
                    pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
            }
            val activity = context as? android.app.Activity
            if (activity != null) {
                activity.startIntentSenderForResult(pending.intentSender, HUB_OPEN_REQUEST_CODE, null, 0, 0, 0, options.toBundle())
            } else {
                pending.send(context, 0, null, null, null, null, options.toBundle())
            }
            true
        }.getOrDefault(false)
    } ?: false
    if (!openedNotification) {
        runCatching {
            context.packageManager.getLaunchIntentForPackage(item.packageName)?.apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }?.let(context::startActivity)
        }
    }
}

private fun HubNotificationAction.send(context: android.content.Context): Boolean = runCatching {
    val options = ActivityOptions.makeBasic().apply {
        if (Build.VERSION.SDK_INT >= 34) {
            pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
        }
    }
    pendingIntent.send(context, 0, null, null, null, null, options.toBundle())
    true
}.getOrDefault(false)

private const val HUB_OPEN_REQUEST_CODE = 4701

private fun HubNotificationAction.successLabel(): String {
    val action = title.trim().lowercase()
    return when {
        "archive" in action -> "email archived"
        "delete" in action || "trash" in action -> "notification deleted"
        "mark" in action && "read" in action -> "marked as read"
        "complete" in action || "done" in action -> "task completed"
        "snooze" in action -> "notification snoozed"
        "schedule" in action || "reschedule" in action -> "schedule opened"
        else -> "$action complete"
    }
}

private fun HubItem.matches(filter: HubFilter, flaggedKeys: Set<String>, importantKeys: Set<String>, overrides: Map<String, String>): Boolean = when (filter) {
    HubFilter.All -> !isSummary
    HubFilter.Messages -> !isSummary && effectiveCategory(overrides) == "messages"
    HubFilter.Calls -> !isSummary && effectiveCategory(overrides) == "calls"
    HubFilter.Email -> !isSummary && effectiveCategory(overrides) == "email"
    HubFilter.Finance -> !isSummary && effectiveCategory(overrides) == "finance"
    HubFilter.Tasks -> !isSummary && effectiveCategory(overrides) == "tasks"
    HubFilter.Apps -> !isSummary && effectiveCategory(overrides) == "apps"
    HubFilter.Important -> !isSummary && (
        key in importantKeys || sourceKeys.any(importantKeys::contains) || HubImportantSettings.matches(this)
    )
    HubFilter.Flagged -> !isSummary && key in flaggedKeys
    HubFilter.Summaries -> isSummary
}

internal fun HubItem.effectiveCategory(overrides: Map<String, String>): String {
    overrides[packageName]?.let { return it }
    return when {
        isFinance() -> "finance"
        isTask() -> "tasks"
        isMessage() -> "messages"
        isCall() -> "calls"
        isEmail() -> "email"
        else -> "apps"
    }
}

private fun HubItem.isMessage(): Boolean = category == Notification.CATEGORY_MESSAGE ||
    packageName.contains("messag", true) || packageName.contains("whatsapp", true) || packageName.contains("orca", true) || packageName.contains("telegram", true) || packageName.contains("signal", true)

private fun HubItem.isCall(): Boolean = category == Notification.CATEGORY_CALL || title.contains("missed", true) || text.contains("missed call", true)

private fun HubItem.isEmail(): Boolean = category == Notification.CATEGORY_EMAIL ||
    listOf("gmail", "email", "outlook", "protonmail", "fairmail", "k9", "yahoo").any { packageName.contains(it, true) }

private fun HubItem.isFinance(): Boolean = listOf(
    "paypal", "afterpay", "klarna", "commbank", "westpac", "nab.", "anz.", "up.money",
    "revolut", "wise", "bank", "stripe", "squareup", "coinbase", "binance",
).any { packageName.contains(it, true) }

private fun HubItem.isTask(): Boolean = listOf(
    "todoist", "ticktick", "tasks", "taskito", "anydo", "rememberthemilk", "microsoft.todos",
).any { packageName.contains(it, true) } ||
    category == Notification.CATEGORY_REMINDER && listOf("todo", "task", "reminder").any { packageName.contains(it, true) }

private fun hubIcon(category: String): ImageVector = when (category) {
    "calls" -> Icons.Outlined.Call
    "email" -> Icons.Outlined.Email
    "messages" -> Icons.Outlined.ChatBubbleOutline
    "finance" -> Icons.Outlined.AttachMoney
    "tasks" -> Icons.Outlined.CheckBox
    else -> Icons.Outlined.NotificationsNone
}

private fun HubItem.isLowPriority(): Boolean = category == Notification.CATEGORY_STATUS ||
    category == Notification.CATEGORY_SERVICE || category == Notification.CATEGORY_PROGRESS

private fun HubItem.isExpandable(): Boolean {
    val compactText = text.trim()
    val fullText = expandedText.trim()
    return messages.size > 3 || textLines.size > 1 ||
        (fullText.isNotBlank() && (fullText != compactText || fullText.length > 90)) ||
        compactText.length > 90
}

private fun String.cleanExpandedNotificationText(): String =
    replace("\r\n", "\n")
        .replace('\r', '\n')
        .lines()
        .map(String::trimEnd)
        .dropWhile(String::isBlank)
        .dropLastWhile(String::isBlank)
        .joinToString("\n")
        .replace(Regex("\n[\\t ]*\n(?:[\\t ]*\n)+"), "\n\n")

private fun HubItem.primaryHubTitle(): String = when {
    isMessage() -> conversationTitle ?: title
    isCall() || isEmail() || isTask() -> title
    else -> title.takeIf(String::isNotBlank) ?: appName
}
