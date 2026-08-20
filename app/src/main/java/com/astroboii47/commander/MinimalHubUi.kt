package com.astroboii47.commander

import android.app.Notification
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
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
    All("all"), Messages("messages"), Calls("calls"), Email("email"), Finance("finance"), Tasks("tasks"), Apps("apps"), Flagged("flagged"), Summaries("summaries")
}

@Composable
fun MinimalHubApp(hasAccess: Boolean, openAccessSettings: () -> Unit, onDismiss: () -> Unit) {
    val nightMode = LocalConfiguration.current.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
    val dark = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    val palette = if (dark) {
        HubPalette(Color.Black, Color(0xFF070707), Color(0xFF101010), Color(0xFFF3F1EA), Color(0xFF8C8A84), Color(0xFF303030))
    } else {
        HubPalette(Color(0xFFE8E6DF), Color(0xFFF1EFE8), Color(0xFFE4E0D8), Color(0xFF171717), Color(0xFF6F6D67), Color(0xFFCAC6BD))
    }
    val context = LocalContext.current
    val flagPrefs = remember { context.getSharedPreferences("hub_flags", 0) }
    val readPrefs = remember { context.getSharedPreferences("hub_read", 0) }
    val categoryPrefs = remember { context.getSharedPreferences("hub_categories", 0) }
    var flaggedKeys by remember { mutableStateOf(flagPrefs.getStringSet("keys", emptySet())?.toSet().orEmpty()) }
    var readTimes by remember {
        mutableStateOf(readPrefs.all.mapNotNull { (key, value) -> (value as? Long)?.let { key to it } }.toMap())
    }
    var categoryOverrides by remember {
        mutableStateOf(categoryPrefs.all.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap())
    }
    var filter by remember { mutableStateOf(HubFilter.All) }
    var managing by remember { mutableStateOf(false) }
    var replyKey by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }
    var replyStatus by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedIndex by remember { mutableStateOf(0) }
    var keyboardBrowsing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val notificationScroll = rememberScrollState()
    val allItems = HubRepository.items
    val newCount = remember(allItems, readTimes) {
        allItems.count { !it.isSummary && (readTimes[it.key] ?: 0L) < it.time }
    }
    val visible = remember(allItems, filter, flaggedKeys, categoryOverrides) {
        allItems.filter { it.matches(filter, flaggedKeys, categoryOverrides) }
    }
    val filters = HubFilter.entries.filter { entry ->
        if (entry == HubFilter.All) true else when (HubSettings.visibility(entry.label)) {
            HubTabVisibility.Always -> true
            HubTabVisibility.Hidden -> false
            HubTabVisibility.Auto -> allItems.any { it.matches(entry, flaggedKeys, categoryOverrides) }
        }
    }

    LaunchedEffect(visible.size, filter) {
        selectedIndex = selectedIndex.coerceIn(0, (visible.size - 1).coerceAtLeast(0))
    }
    LaunchedEffect(filters) {
        if (filter !in filters) filter = HubFilter.All
    }
    fun toggleFlag(key: String) {
        flaggedKeys = if (key in flaggedKeys) flaggedKeys - key else flaggedKeys + key
        flagPrefs.edit().putStringSet("keys", flaggedKeys).apply()
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
        val order = listOf("auto", "messages", "calls", "email", "finance", "tasks", "apps")
        val current = categoryOverrides[packageName] ?: "auto"
        val next = order[(order.indexOf(current) + 1) % order.size]
        categoryOverrides = if (next == "auto") categoryOverrides - packageName else categoryOverrides + (packageName to next)
        categoryPrefs.edit().apply {
            if (next == "auto") remove(packageName) else putString(packageName, next)
        }.apply()
    }

    DisposableEffect(visible, filter, replyKey, HubSettings.quickKeyboardNavigation.value) {
        HubKeyBridge.navigate = navigate@{ keyCode ->
            if (replyKey != null || managing) return@navigate false
            val quick = HubSettings.quickKeyboardNavigation.value
            when (keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_UP,
                android.view.KeyEvent.KEYCODE_I.takeIf { quick } -> {
                    keyboardBrowsing = true
                    if (visible.isNotEmpty()) selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                android.view.KeyEvent.KEYCODE_K.takeIf { quick } -> {
                    keyboardBrowsing = true
                    if (visible.isNotEmpty()) selectedIndex = (selectedIndex + 1).coerceAtMost(visible.lastIndex)
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                android.view.KeyEvent.KEYCODE_J.takeIf { quick } -> {
                    keyboardBrowsing = true
                    val index = filters.indexOf(filter).coerceAtLeast(0)
                    filter = filters[(index - 1).coerceAtLeast(0)]
                    selectedIndex = 0
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                android.view.KeyEvent.KEYCODE_L.takeIf { quick } -> {
                    keyboardBrowsing = true
                    val index = filters.indexOf(filter).coerceAtLeast(0)
                    filter = filters[(index + 1).coerceAtMost(filters.lastIndex)]
                    selectedIndex = 0
                    true
                }
                android.view.KeyEvent.KEYCODE_ENTER, android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                android.view.KeyEvent.KEYCODE_O.takeIf { quick } -> {
                    visible.getOrNull(selectedIndex)?.let { item ->
                        if (item.replyAction != null) {
                            markRead(item)
                            replyKey = item.key
                            replyText = ""
                            replyStatus = null
                        } else {
                            activateHubItem(context, item, { markRead(item) }) { }
                        }
                    }
                    visible.isNotEmpty()
                }
                android.view.KeyEvent.KEYCODE_U.takeIf { quick } -> {
                    visible.getOrNull(selectedIndex)?.let(HubRepository::dismiss)
                    visible.isNotEmpty()
                }
                else -> false
            }
        }
        onDispose { HubKeyBridge.navigate = null }
    }

    Box(Modifier.fillMaxSize().background(palette.background).statusBarsPadding().padding(8.dp), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(palette.panel)
                .border(1.dp, palette.border, RoundedCornerShape(24.dp)).padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(
                    if (managing) "done" else "manage",
                    color = if (managing) HubAccent else palette.secondary,
                    fontFamily = HubMono,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { managing = !managing }.padding(end = 16.dp),
                )
                Box(
                    Modifier.size(32.dp).clip(CircleShape).clickable {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }.padding(7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Open Commander Settings",
                        tint = palette.secondary,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text("close →", color = palette.secondary, fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.clickable { onDismiss() })
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 14.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                filters.forEach { entry ->
                    Text(
                        entry.label,
                        color = if (filter == entry) Color.White else palette.secondary,
                        fontFamily = HubMono,
                        fontSize = 12.sp,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (filter == entry) HubAccent else Color.Transparent)
                            .border(1.dp, if (filter == entry) HubAccent else palette.border, RoundedCornerShape(20.dp))
                            .clickable { filter = entry }.padding(horizontal = 11.dp, vertical = 5.dp),
                    )
                }
            }

            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(notificationScroll),
            ) {
                if (managing) {
                    HubCategoryManager(allItems, categoryOverrides, palette, ::cycleCategory)
                } else if (!hasAccess) {
                    HubAccessCard(palette, openAccessSettings)
                } else if (visible.isEmpty()) {
                    Text("nothing new", color = palette.secondary, fontFamily = HubMono, modifier = Modifier.padding(vertical = 28.dp).align(Alignment.CenterHorizontally))
                } else {
                    visible.take(50).forEachIndexed { index, item ->
                        key(item.key) {
                            val bringIntoViewRequester = remember { BringIntoViewRequester() }
                            val dismissState = rememberSwipeToDismissBoxState()
                            LaunchedEffect(index == selectedIndex) {
                                if (index == selectedIndex) bringIntoViewRequester.bringIntoView()
                            }
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    HubRepository.dismiss(item)
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
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    Box(
                                        Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(HubAccent.copy(alpha = .16f)).padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) { Text("dismiss", color = HubAccent, fontFamily = HubMono, fontSize = 12.sp) }
                                },
                            ) {
                                HubNotificationCard(
                                    item = item,
                                    effectiveCategory = item.effectiveCategory(categoryOverrides),
                                    palette = palette,
                                    selected = index == selectedIndex,
                                    flagged = item.key in flaggedKeys,
                                    unread = !item.isSummary && (readTimes[item.key] ?: 0L) < item.time,
                                    replying = replyKey == item.key,
                                    replyText = replyText,
                                    replyStatus = replyStatus?.takeIf { it.first == item.key }?.second,
                                    onToggleFlag = { toggleFlag(item.key) },
                                    onMarkRead = { markRead(item) },
                                    onToggleReply = {
                                        markRead(item)
                                        replyKey = if (replyKey == item.key) null else item.key
                                        replyText = ""
                                        replyStatus = null
                                    },
                                    onReplyTextChange = { replyText = it },
                                    onSendReply = {
                                        val sent = item.sendReply(context, replyText.trim())
                                        replyStatus = item.key to if (sent) "reply sent" else "couldn't send"
                                        if (sent) {
                                            replyText = ""
                                            scope.launch { delay(1_250); replyKey = null; replyStatus = null }
                                        }
                                    },
                                )
                            }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
        val popupItem = allItems.firstOrNull { it.key == replyKey }
        if (popupItem != null) {
            HubReplyPopup(
                item = popupItem,
                palette = palette,
                replyText = replyText,
                replyStatus = replyStatus?.takeIf { it.first == popupItem.key }?.second,
                onReplyTextChange = { replyText = it },
                onDismiss = { replyKey = null; replyText = ""; replyStatus = null },
                onSendReply = {
                    val sent = popupItem.sendReply(context, replyText.trim())
                    replyStatus = popupItem.key to if (sent) "reply sent" else "couldn't send"
                    if (sent) {
                        replyText = ""
                        scope.launch { delay(850); replyKey = null; replyStatus = null }
                    }
                },
            )
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
            Text(item.title, color = HubAccent, fontFamily = HubSerif, fontSize = 28.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    palette: HubPalette,
    cycle: (String) -> Unit,
) {
    val apps = remember(items) { items.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() } }
    if (apps.isEmpty()) {
        Text("apps appear here after they post a notification", color = palette.secondary, fontFamily = HubMono, fontSize = 12.sp, modifier = Modifier.padding(vertical = 24.dp))
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
    unread: Boolean,
    replying: Boolean,
    replyText: String,
    replyStatus: String?,
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
            .clickable {
                activateHubItem(context, item, onMarkRead, onToggleReply)
            },
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = hubIcon(effectiveCategory),
                contentDescription = effectiveCategory,
                tint = if (item.isLowPriority()) palette.secondary else HubAccent,
                modifier = Modifier.size(27.dp),
            )
            Column(Modifier.padding(start = 13.dp).weight(1f)) {
                Text(primaryTitle, color = palette.text, fontFamily = HubSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                val recentMessages = if (item.messages.isNotEmpty()) item.messages.takeLast(if (replying) 5 else 3) else listOf(HubMessage(item.text, item.title, false))
                recentMessages.filter { it.text.isNotBlank() }.forEach { message ->
                    val preview = if (item.conversationTitle != null && !message.sender.isNullOrBlank() &&
                        !message.sender.equals(primaryTitle, ignoreCase = true)
                    ) "${message.sender}: ${message.text}" else message.text
                    Text(preview, color = palette.secondary, fontFamily = HubSans, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(
                Modifier.padding(start = 10.dp).widthIn(max = 105.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(time, color = palette.secondary, fontFamily = HubMono, fontSize = 11.sp)
                    if (unread) Text("  ●", color = HubAccent, fontSize = 10.sp)
                }
                if (!item.appName.equals(primaryTitle, ignoreCase = true)) {
                    Text(
                        item.appName,
                        color = palette.secondary,
                        fontFamily = HubMono,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                if (flagged) " ★" else " ☆",
                color = if (flagged) HubAccent else palette.secondary,
                fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = onToggleFlag).padding(start = 7.dp),
            )
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

private fun activateHubItem(context: android.content.Context, item: HubItem, onMarkRead: () -> Unit, onReply: () -> Unit) {
    if (item.replyAction != null) {
        onReply()
        return
    }
    onMarkRead()
    val openedNotification = item.contentIntent?.let { pending ->
        runCatching {
            val options = ActivityOptions.makeBasic().apply {
                if (Build.VERSION.SDK_INT >= 34) {
                    pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
            }
            pending.send(context, 0, null, null, null, null, options.toBundle())
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

private fun HubItem.matches(filter: HubFilter, flaggedKeys: Set<String>, overrides: Map<String, String>): Boolean = when (filter) {
    HubFilter.All -> !isSummary
    HubFilter.Messages -> !isSummary && effectiveCategory(overrides) == "messages"
    HubFilter.Calls -> !isSummary && effectiveCategory(overrides) == "calls"
    HubFilter.Email -> !isSummary && effectiveCategory(overrides) == "email"
    HubFilter.Finance -> !isSummary && effectiveCategory(overrides) == "finance"
    HubFilter.Tasks -> !isSummary && effectiveCategory(overrides) == "tasks"
    HubFilter.Apps -> !isSummary && effectiveCategory(overrides) == "apps"
    HubFilter.Flagged -> !isSummary && key in flaggedKeys
    HubFilter.Summaries -> isSummary
}

private fun HubItem.effectiveCategory(overrides: Map<String, String>): String {
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

private fun HubItem.primaryHubTitle(): String = when {
    isMessage() -> conversationTitle ?: title
    isCall() || isEmail() -> title
    else -> appName
}
