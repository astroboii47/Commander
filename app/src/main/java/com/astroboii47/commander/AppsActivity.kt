package com.astroboii47.commander

import android.content.Context
import android.content.Intent
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AppsActivity : ComponentActivity() {
    private val homeSwipe by lazy { SurfaceSwipe(this, 1) }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        homeSwipe.observe(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHOW_SURFACE
            putExtra(MainActivity.EXTRA_SURFACE, MainActivity.SURFACE_APPS)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        finish()
    }
}

private enum class AppsLayout { Grid, List }
private enum class AppsSort(val label: String) { Alphabetical("A–Z"), Recent("recent"), Manual("manual") }
private val AppsMono = FontFamily(Font(R.font.ibm_plex_mono_regular))
private val AppsSans = FontFamily(Font(R.font.dm_sans_regular))

private fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(AppOpsManager::class.java)
    return appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
    ) == AppOpsManager.MODE_ALLOWED
}

private fun loadDeviceRecents(context: Context): Map<String, Long> {
    if (!hasUsageAccess(context)) return emptyMap()
    val usage = context.getSystemService(UsageStatsManager::class.java)
    val end = System.currentTimeMillis()
    return usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, end - 30L * 24 * 60 * 60 * 1_000, end)
        .groupBy { it.packageName }
        .mapValues { (_, records) -> records.maxOfOrNull { it.lastTimeUsed } ?: 0L }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommanderApps(
    sharedWallpaper: Boolean = false,
    onBeforeAppLaunch: () -> Unit = {},
) {
    val context = LocalContext.current
    val accent = AccentSettings.color.value
    var query by remember { mutableStateOf("") }
    val prefs = remember { context.getSharedPreferences("commander_apps", Context.MODE_PRIVATE) }
    var layout by remember { mutableStateOf(AppsLayout.entries.firstOrNull { it.name == prefs.getString("layout", null) } ?: AppsLayout.Grid) }
    var sort by remember { mutableStateOf(AppsSort.entries.firstOrNull { it.name == prefs.getString("sort", null) } ?: AppsSort.Alphabetical) }
    var sortMenu by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<AppCatalogEntry?>(null) }
    var activeRailLetter by remember { mutableStateOf<Char?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    // Package enumeration is comparatively expensive and must not run again while
    // the parent pager is animating or its shared background is crossfading.
    val all = remember { AppCatalog.all(context) }
    var deviceHistory by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    LaunchedEffect(revision, sort) {
        if (sort == AppsSort.Recent) {
            deviceHistory = withContext(Dispatchers.IO) { loadDeviceRecents(context) }
        }
    }
    val commanderHistory = remember(revision) {
        prefs.all.filterKeys { it.startsWith("recent_") }
            .mapKeys { (key, _) -> key.removePrefix("recent_") }
            .mapValues { (_, value) -> value as? Long ?: 0L }
    }
    val recent = remember(commanderHistory, deviceHistory) {
        (commanderHistory.keys + deviceHistory.keys).associateWith { packageName ->
            maxOf(commanderHistory[packageName] ?: 0L, deviceHistory[packageName] ?: 0L)
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) revision++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val manual = remember(revision, all) {
        val saved = prefs.getString("manual_order", "").orEmpty().split('|').filter(String::isNotBlank)
        (saved + all.map { it.packageName }).distinct()
    }
    val visible = remember(all, query, sort, recent, manual) {
        val filtered = all.filter { query.isBlank() || it.label.contains(query.trim(), true) || it.packageName.contains(query.trim(), true) }
        when (sort) {
            AppsSort.Alphabetical -> filtered.sortedBy { it.label.lowercase(Locale.ROOT) }
            AppsSort.Recent -> filtered.sortedWith(
                compareByDescending<AppCatalogEntry> { recent[it.packageName] ?: 0L }
                    .thenBy { it.label.lowercase(Locale.ROOT) },
            )
            AppsSort.Manual -> filtered.sortedBy { manual.indexOf(it.packageName).let { index -> if (index < 0) Int.MAX_VALUE else index } }
        }
    }
    val letters = remember(visible, sort) {
        if (sort == AppsSort.Alphabetical) visible.mapNotNull { it.label.firstOrNull()?.uppercaseChar()?.takeIf(Char::isLetter) }.distinct()
        else emptyList()
    }
    val gridLetterIndices = remember(visible, sort) {
        if (sort != AppsSort.Alphabetical) emptyMap() else buildMap {
            var gridIndex = 0
            var previous: Char? = null
            visible.forEach { app ->
                val letter = app.label.firstOrNull()?.uppercaseChar()?.takeIf(Char::isLetter) ?: '#'
                if (letter != previous) {
                    put(letter, gridIndex)
                    gridIndex++
                    previous = letter
                }
                gridIndex++
            }
        }
    }
    fun jump(letterIndex: Int) {
        val letter = letters.getOrNull(letterIndex) ?: return
        activeRailLetter = letter
        val target = visible.indexOfFirst { it.label.startsWith(letter, true) }.coerceAtLeast(0)
        scope.launch {
            if (layout == AppsLayout.Grid) gridState.scrollToItem(gridLetterIndices[letter] ?: target)
            else listState.scrollToItem(target)
        }
    }
    fun launch(app: AppCatalogEntry) {
        prefs.edit().putLong("recent_${app.packageName}", System.currentTimeMillis()).apply()
        revision++
        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName) ?: return
        // Put the launcher back on Home before Android captures the window for
        // the app-opening transition. Otherwise returning Home briefly reveals
        // the Apps page that launched the external activity.
        onBeforeAppLaunch()
        scope.launch {
            withFrameNanos { }
            context.startActivity(intent)
        }
    }
    fun move(app: AppCatalogEntry, delta: Int) {
        val order = manual.toMutableList()
        val from = order.indexOf(app.packageName)
        val to = (from + delta).coerceIn(0, order.lastIndex)
        if (from >= 0 && from != to) {
            order.removeAt(from); order.add(to, app.packageName)
            prefs.edit().putString("manual_order", order.joinToString("|")).apply()
            sort = AppsSort.Manual
            prefs.edit().putString("sort", sort.name).apply()
            revision++
        }
    }

    MaterialTheme {
        Box(Modifier.fillMaxSize().background(if (sharedWallpaper) Color.Transparent else Color(0xFF050607))) {
            if (!sharedWallpaper) SignalWallpaper(context, Modifier.fillMaxSize())
            if (!sharedWallpaper) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)))
            Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    Box {
                        Text(sort.label, color = accent, fontFamily = AppsMono, fontSize = 13.sp, modifier = Modifier.clickable { sortMenu = true }.padding(8.dp))
                        DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                            AppsSort.entries.forEach { choice -> DropdownMenuItem(text = { Text(choice.label) }, onClick = {
                                sort = choice
                                prefs.edit().putString("sort", choice.name).apply()
                                sortMenu = false
                                if (choice == AppsSort.Recent && !hasUsageAccess(context)) {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:${context.packageName}")))
                                }
                            }) }
                        }
                    }
                    Row(Modifier.clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = .20f))
                        .border(1.dp, Color.White.copy(alpha = .22f), RoundedCornerShape(10.dp))) {
                        IconButton(onClick = { layout = AppsLayout.Grid; prefs.edit().putString("layout", layout.name).apply() }) {
                            Icon(Icons.Outlined.GridView, "Grid", tint = if (layout == AppsLayout.Grid) accent else Color.White.copy(alpha = .60f))
                        }
                        IconButton(onClick = { layout = AppsLayout.List; prefs.edit().putString("layout", layout.name).apply() }) {
                            Icon(Icons.Outlined.ViewAgenda, "List", tint = if (layout == AppsLayout.List) accent else Color.White.copy(alpha = .60f))
                        }
                    }
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontFamily = AppsMono, fontSize = 16.sp),
                    cursorBrush = SolidColor(accent),
                    decorationBox = { inner -> Box(Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = .24f))
                        .border(1.dp, Color.White.copy(alpha = .24f), RoundedCornerShape(12.dp))
                        .padding(15.dp)) {
                        if (query.isEmpty()) Text("search apps", color = Color.White.copy(alpha = .58f), fontFamily = AppsMono)
                        inner()
                    } },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxSize()) {
                    if (layout == AppsLayout.Grid) {
                        LazyVerticalGrid(columns = GridCells.Adaptive(92.dp), state = gridState,
                            modifier = Modifier.fillMaxSize().padding(end = if (letters.isEmpty()) 0.dp else 22.dp),
                            contentPadding = PaddingValues(bottom = 30.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            var previous: Char? = null
                            visible.forEach { app ->
                                val letter = app.label.firstOrNull()?.uppercaseChar()?.takeIf(Char::isLetter) ?: '#'
                                if (sort == AppsSort.Alphabetical && letter != previous) {
                                    item(key = "letter-$letter", span = { GridItemSpan(maxLineSpan) }) {
                                        Text(letter.toString(), color = Color.White.copy(alpha = .74f),
                                            fontFamily = AppsMono, fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
                                    }
                                    previous = letter
                                }
                                item(key = app.packageName) { AppTile(app, true, { launch(app) }, { selected = app }) }
                            }
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize().padding(end = 25.dp), state = listState, contentPadding = PaddingValues(bottom = 30.dp)) {
                            itemsIndexed(visible, key = { _, app -> app.packageName }) { index, app ->
                                val letter = app.label.firstOrNull()?.uppercaseChar()?.takeIf(Char::isLetter)?.toString() ?: "#"
                                val previous = visible.getOrNull(index - 1)?.label?.firstOrNull()?.uppercaseChar()?.takeIf(Char::isLetter)?.toString() ?: "#"
                                if (sort == AppsSort.Alphabetical && (index == 0 || letter != previous)) {
                                    Text(letter, color = Color.White, fontFamily = AppsMono, fontSize = 12.sp, modifier = Modifier.padding(top = if (index == 0) 2.dp else 15.dp, bottom = 4.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = .15f))
                                }
                                AppTile(app, false, { launch(app) }, { selected = app })
                            }
                        }
                    }
                    if (letters.isNotEmpty()) {
                        fun railIndex(y: Float, height: Float) =
                            ((y / height) * letters.size).toInt().coerceIn(0, (letters.size - 1).coerceAtLeast(0))
                        Column(
                            Modifier.align(Alignment.CenterEnd).fillMaxHeight(.82f)
                                .pointerInput(letters) {
                                    detectVerticalDragGestures(
                                        onDragStart = { start -> jump(railIndex(start.y, size.height.toFloat())) },
                                        onDragEnd = { activeRailLetter = null },
                                        onDragCancel = { activeRailLetter = null },
                                        onVerticalDrag = { change, _ ->
                                            change.consume()
                                            jump(railIndex(change.position.y, size.height.toFloat()))
                                        },
                                    )
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            letters.forEachIndexed { index, letter ->
                                Text(
                                    letter.toString(),
                                    color = Color.White,
                                    fontFamily = AppsMono,
                                    fontSize = 9.sp,
                                    modifier = Modifier.clickable {
                                        jump(index)
                                        scope.launch { delay(420); activeRailLetter = null }
                                    }.padding(horizontal = 4.dp),
                                )
                            }
                        }
                    }
                    activeRailLetter?.let { letter ->
                            Box(
                                Modifier.align(Alignment.CenterEnd).padding(end = 40.dp).size(64.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.Black.copy(alpha = .68f))
                                    .border(1.dp, accent.copy(alpha = .72f), RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(letter.toString(), color = Color.White, fontFamily = AppsMono, fontSize = 32.sp)
                            }
                    }
                }
            }
        }
    }
    selected?.let { app ->
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            containerColor = Color(0xF21A1C1F),
            contentColor = Color.White,
            dragHandle = { Box(Modifier.padding(top = 10.dp).size(34.dp, 3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = .28f))) },
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app, 50)
                Column(Modifier.padding(start = 15.dp)) {
                    Text(app.label, fontFamily = AppsSans, fontSize = 20.sp)
                    Text(app.packageName, color = Color.White.copy(alpha = .45f), fontFamily = AppsMono, fontSize = 10.sp, maxLines = 1)
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = .10f))
            LauncherAction("app info") { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))); selected = null }
            LauncherAction(if (FavoriteApps.contains(context, app.packageName)) "remove from favourites dock" else "pin to favourites dock") {
                FavoriteApps.toggle(context, app.packageName)
                revision++
                selected = null
            }
            LauncherAction("move earlier") { move(app, -1); selected = null }
            LauncherAction("move later") { move(app, 1); selected = null }
            LauncherAction("uninstall", destructive = true) { context.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))); selected = null }
            Spacer(Modifier.navigationBarsPadding().height(12.dp))
        }
    }
}

@Composable
private fun LauncherAction(label: String, destructive: Boolean = false, action: () -> Unit) {
    Text(label, color = if (destructive) Color(0xFFFF776D) else Color.White, fontFamily = AppsSans, fontSize = 17.sp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = action).padding(horizontal = 24.dp, vertical = 17.dp))
}

@Composable
private fun AppIcon(app: AppCatalogEntry, size: Int) {
    val context = LocalContext.current
    val painter by produceState<BitmapPainter?>(initialValue = null, app.packageName) {
        value = withContext(Dispatchers.IO) {
            runCatching { BitmapPainter(context.packageManager.getApplicationIcon(app.packageName).toBitmap(size * 2, size * 2).asImageBitmap()) }.getOrNull()
        }
    }
    painter?.let { Image(it, app.label, Modifier.size(size.dp).clip(RoundedCornerShape((size * .24f).dp))) }
}

@Composable
private fun AppTile(app: AppCatalogEntry, grid: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    if (grid) {
        Column(modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AppIcon(app, 52)
            Spacer(Modifier.height(7.dp))
            Text(app.label, color = Color.White.copy(alpha = .90f), fontFamily = AppsMono,
                fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    } else {
        Row(modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(app, 42)
            Text(app.label, color = Color.White, fontFamily = AppsMono, fontSize = 15.sp,
                modifier = Modifier.padding(start = 14.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
