package com.astroboii47.commander

import android.graphics.Color
import android.os.Bundle
import android.os.Build
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwitcherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(android.content.Intent(this, OverlayActivity::class.java).apply {
            putExtra(OverlayActivity.EXTRA_START_SWITCHER, true)
            addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        })
        finish()
    }
}

@Composable
private fun SwitcherUi(close: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val dark = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val invertedBlurDarkness = AppearanceSettings.invertBlurDarkness.value
    val accent = AccentSettings.color.value
    val panel = when {
        dark && invertedBlurDarkness -> ComposeColor.Transparent
        dark -> ComposeColor(0x98070707)
        invertedBlurDarkness -> ComposeColor(0xD8D3D0C8)
        else -> ComposeColor(0xB8ECE9E1)
    }
    val tile = when {
        dark && invertedBlurDarkness -> ComposeColor(0x20F2EEE6)
        dark -> ComposeColor(0x70111111)
        invertedBlurDarkness -> ComposeColor(0xE8F6F3EC)
        else -> ComposeColor(0x9AF4F2EC)
    }
    val text = if (dark) ComposeColor(0xFFF6F3EA) else ComposeColor(0xFF161616)
    val secondary = if (dark) ComposeColor(0xFFAAA7A0) else ComposeColor(0xFF6D6B65)
    val border = when {
        dark && invertedBlurDarkness -> ComposeColor.Transparent
        dark -> ComposeColor(0x604F4F4F)
        invertedBlurDarkness -> ComposeColor(0x99BDB9AF)
        else -> ComposeColor(0x88CAC7BE)
    }
    val mono = FontFamily(Font(R.font.courier_prime_regular), Font(R.font.courier_prime_bold, FontWeight.Bold))
    val sans = FontFamily(Font(R.font.inter_variable))
    var allTasks by remember { mutableStateOf<List<SwitcherTask>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(0) }
    var ready by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    val client = remember { TaskSwitcherClient(context) { ready = true } }
    val tasks = remember(allTasks, query) { allTasks.filter { query.isBlank() || it.label.contains(query, true) } }
    val listState = rememberLazyListState()
    fun open() {
        tasks.getOrNull(selected)?.let { task ->
            scope.launch(Dispatchers.IO) { if (client.switch(task)) withContext(Dispatchers.Main) { close() } }
        }
    }
    LaunchedEffect(Unit) {
        client.bind()
        allTasks = withContext(Dispatchers.IO) { TaskSwitcherShell.load(context) }
        selected = 0
        focus.requestFocus()
    }
    LaunchedEffect(selected) { if (selected in tasks.indices) listState.animateScrollToItem(selected) }
    DisposableEffect(Unit) { onDispose { client.unbind() } }
    Box(
        Modifier.fillMaxSize()
            .background(if (invertedBlurDarkness) ComposeColor(0x6C000000) else ComposeColor.Transparent)
            .clickable { close() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 720.dp)
                .statusBarsPadding().navigationBarsPadding().imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(23.dp)).background(panel)
                .border(1.dp, border, RoundedCornerShape(23.dp))
                .clickable { }.padding(6.dp),
        ) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.History, null, tint = accent, modifier = Modifier.size(22.dp))
                Text("Switcher", color = text, fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(start = 10.dp))
                Text("${tasks.size} tasks", color = secondary, fontFamily = mono, fontSize = 11.sp, modifier = Modifier.padding(start = 10.dp))
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().height(230.dp)) {
                itemsIndexed(tasks, key = { _, it -> it.taskId }) { index, task ->
                    SwitcherCard(task, index == selected, accent, tile, text, border, client, onDismiss = {
                        allTasks = allTasks.filterNot { it.taskId == task.taskId }
                        selected = selected.coerceAtMost((tasks.size - 2).coerceAtLeast(0))
                    }) { selected = index; open() }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(17.dp)).background(tile)
                    .border(2.dp, accent, RoundedCornerShape(17.dp)).padding(horizontal = 15.dp)
                    .onPreviewKeyEvent { e ->
                        if (e.type != KeyEventType.KeyDown) false else when (e.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> { selected = (selected - 1).coerceAtLeast(0); true }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> { selected = (selected + 1).coerceAtMost(tasks.lastIndex.coerceAtLeast(0)); true }
                            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> { open(); true }
                            else -> false
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(query, { query = it; selected = 0 }, Modifier.weight(1f).focusRequester(focus), textStyle = TextStyle(color = text, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 22.sp), singleLine = true)
                Icon(Icons.AutoMirrored.Rounded.KeyboardReturn, null, tint = accent, modifier = Modifier.size(30.dp).clickable { open() })
            }
        }
    }
}

@Composable
internal fun SwitcherCard(task: SwitcherTask, selected: Boolean, accent: ComposeColor, tile: ComposeColor, text: ComposeColor, border: ComposeColor, client: TaskSwitcherClient, onDismiss: () -> Unit, open: () -> Unit) {
    val context = LocalContext.current
    var preview by remember(task.taskId) { mutableStateOf(task.preview) }
    val previewAlpha = remember(task.taskId) { Animatable(if (preview == null) 0f else 1f) }
    val swipeOffset = remember(task.taskId) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val appIcon = remember(task.packageName) {
        runCatching { context.packageManager.getApplicationIcon(task.packageName).toBitmap(36, 36).asImageBitmap() }.getOrNull()
    }
    LaunchedEffect(task.taskId) {
        if (preview == null) {
            preview = withContext(Dispatchers.IO) { client.preview(task.taskId, 360, 220) }
            if (preview != null) {
                previewAlpha.snapTo(0f)
                previewAlpha.animateTo(1f, tween(180))
            }
        }
    }
    Column(
        Modifier.width(210.dp).fillMaxHeight()
            .graphicsLayer { translationY = swipeOffset.value; alpha = (1f + swipeOffset.value / 260f).coerceIn(.35f, 1f) }
            .pointerInput(task.taskId) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, amount ->
                        if (amount < 0f || swipeOffset.value < 0f) {
                            change.consume()
                            scope.launch { swipeOffset.snapTo((swipeOffset.value + amount).coerceAtMost(0f)) }
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (swipeOffset.value < -90f) {
                                swipeOffset.animateTo(-size.height.toFloat(), tween(130))
                                if (withContext(Dispatchers.IO) { client.remove(task) }) onDismiss()
                                else swipeOffset.animateTo(0f, tween(170))
                            } else swipeOffset.animateTo(0f, tween(170))
                        }
                    },
                    onDragCancel = { scope.launch { swipeOffset.animateTo(0f, tween(170)) } },
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) accent.copy(alpha = .24f) else tile)
            .border(if (selected) 2.dp else 1.dp, if (selected) accent else border, RoundedCornerShape(16.dp))
            .clickable(onClick = open).padding(8.dp),
    ) {
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(11.dp)).background(ComposeColor(0xFF252525)), contentAlignment = Alignment.Center) {
            val bitmap = preview
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), task.label, Modifier.fillMaxSize().graphicsLayer { alpha = previewAlpha.value })
            } else {
                Icon(Icons.Outlined.History, null, tint = accent.copy(alpha = .72f), modifier = Modifier.size(30.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(task.label, color = text, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
            if (appIcon != null) Image(appIcon, null, Modifier.padding(start = 8.dp, end = 5.dp).size(18.dp))
        }
    }
}
