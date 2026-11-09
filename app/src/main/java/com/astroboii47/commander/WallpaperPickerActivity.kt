package com.astroboii47.commander

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.launch

internal data class WallpaperLibraryItem(val uri: Uri, val name: String, val modified: Long)

class WallpaperPickerActivity : ComponentActivity() {
    private var library by mutableStateOf<List<WallpaperLibraryItem>>(emptyList())
    private var loading by mutableStateOf(false)

    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@registerForActivityResult
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }
        HomeSettings.saveWallpaperFolder(this, uri)
        loadFolder(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideStatusBarNow()
        HomeSettings.initialize(this)
        setContent {
            AccentSelectionProvider {
                MaterialTheme {
                    WallpaperLibrary(
                        items = library,
                        loading = loading,
                        selected = HomeSettings.customWallpaperUri.value,
                        setsSystemHome = HomeSettings.wallpaperPickerSetsSystemHome.value,
                        onToggleTarget = {
                            HomeSettings.setWallpaperPickerSetsSystemHome(
                                this,
                                !HomeSettings.wallpaperPickerSetsSystemHome.value,
                            )
                        },
                        onChooseFolder = { folderPicker.launch(HomeSettings.wallpaperFolderUri.value?.let(Uri::parse)) },
                        onSelect = ::selectWallpaper,
                        onCommitSelection = ::commitSelectionAndClose,
                        onClose = ::finish,
                    )
                }
            }
        }
        HomeSettings.wallpaperFolderUri.value?.let(Uri::parse)?.let(::loadFolder)
            ?: folderPicker.launch(null)
    }

    override fun onResume() {
        super.onResume()
        hideStatusBarNow()
        window.decorView.postDelayed(::hideStatusBarNow, 100)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideStatusBarNow()
    }

    private fun hideStatusBarNow() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    private fun loadFolder(treeUri: Uri) {
        loading = true
        lifecycleScope.launch {
            library = withContext(Dispatchers.IO) { queryImages(treeUri) }
            loading = false
        }
    }

    private fun selectWallpaper(item: WallpaperLibraryItem) {
        // Selection is staged in Commander immediately. The optional Android
        // Home write happens once, when the user dismisses the popup, so rapid
        // browsing can never race multiple wallpaper writes.
        HomeSettings.saveCustomWallpaper(this, item.uri)
        if (HomeSettings.wallpaperGlassEnabled.value) {
            lifecycleScope.launch(Dispatchers.IO) { HomeSettings.renderGlassWallpaper(this@WallpaperPickerActivity) }
        }
    }

    private fun commitSelectionAndClose() {
        val selectedUri = HomeSettings.customWallpaperUri.value?.let(Uri::parse)
        if (selectedUri == null || !HomeSettings.wallpaperPickerSetsSystemHome.value) {
            finish()
            return
        }
        val appContext = applicationContext
        finish()
        Thread({
            // Android Home receives the selected image, but Commander keeps
            // rendering the already-selected URI in Custom mode. Switching
            // the live launcher window to System mode while ImageWallpaper is
            // rebinding can leave its wallpaper surface absent (a black Home).
            if (HomeSettings.wallpaperGlassEnabled.value) HomeSettings.renderGlassWallpaper(appContext)
            HomeSettings.applyWallpaperUriToSystemHome(appContext, selectedUri)
        }, "commander-wallpaper-apply").start()
    }

    private fun queryImages(treeUri: Uri): List<WallpaperLibraryItem> = runCatching {
        val parentId = DocumentsContract.getTreeDocumentId(treeUri)
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
        val columns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
        contentResolver.query(children, columns, null, null, null)?.use { cursor ->
            buildList {
                val idIndex = cursor.getColumnIndexOrThrow(columns[0])
                val nameIndex = cursor.getColumnIndexOrThrow(columns[1])
                val mimeIndex = cursor.getColumnIndexOrThrow(columns[2])
                val modifiedIndex = cursor.getColumnIndexOrThrow(columns[3])
                while (cursor.moveToNext()) {
                    val mime = cursor.getString(mimeIndex).orEmpty()
                    if (!mime.startsWith("image/")) continue
                    val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(idIndex))
                    add(WallpaperLibraryItem(uri, cursor.getString(nameIndex).orEmpty(), cursor.getLong(modifiedIndex)))
                }
            }.sortedByDescending(WallpaperLibraryItem::modified)
        }.orEmpty()
    }.getOrDefault(emptyList())
}

@Composable
private fun WallpaperLibrary(
    items: List<WallpaperLibraryItem>,
    loading: Boolean,
    selected: String?,
    setsSystemHome: Boolean,
    onToggleTarget: () -> Unit,
    onChooseFolder: () -> Unit,
    onSelect: (WallpaperLibraryItem) -> Unit,
    onCommitSelection: () -> Unit,
    onClose: () -> Unit,
) {
    val accent = AccentSettings.color.value
    val context = LocalContext.current
    var compact by remember { mutableStateOf(true) }
    var adjustments by remember { mutableStateOf(false) }
    var darkness by remember { mutableStateOf(HomeSettings.wallpaperDarkness.floatValue) }
    var blur by remember { mutableStateOf(HomeSettings.wallpaperBlur.floatValue) }
    var glassEnabled by remember { mutableStateOf(HomeSettings.wallpaperGlassEnabled.value) }
    var glassBlur by remember { mutableStateOf(HomeSettings.wallpaperGlassBlur.floatValue) }
    var glassReed by remember { mutableStateOf(HomeSettings.wallpaperGlassReed.floatValue) }
    var glassDistortion by remember { mutableStateOf(HomeSettings.wallpaperGlassDistortion.floatValue) }
    var glassLighting by remember { mutableStateOf(HomeSettings.wallpaperGlassLighting.floatValue) }
    var outsideTapEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(compact) {
        outsideTapEnabled = false
        delay(650)
        outsideTapEnabled = compact
    }
    LaunchedEffect(glassEnabled, glassBlur, glassReed, glassDistortion, glassLighting, selected) {
        HomeSettings.setWallpaperGlass(
            context, glassEnabled, glassBlur, glassReed, glassDistortion, glassLighting,
        )
        delay(220)
        if (selected != null) withContext(Dispatchers.IO) {
            HomeSettings.renderGlassWallpaper(context)
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Transparent)) {
        Box(
            Modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = if (compact) .12f else .25f))
                .clickable(enabled = outsideTapEnabled, onClick = onCommitSelection),
        )
        val panelShape = if (compact) WallpaperRailShape else RoundedCornerShape(18.dp)
        Column(
            Modifier
                .then(if (compact) Modifier.align(Alignment.Center).fillMaxWidth().height(if (adjustments) 590.dp else 304.dp) else Modifier.fillMaxSize())
                .padding(horizontal = if (compact) 18.dp else 18.dp, vertical = if (compact) 0.dp else 22.dp)
                .clip(panelShape)
                .background(Color(0xFF080A0C).copy(alpha = if (compact) .70f else .76f))
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .075f), Color.Transparent, Color.White.copy(alpha = .018f))))
                .border(1.dp, Color.White.copy(alpha = .34f), panelShape)
                .pointerInput(compact) { detectTapGestures(onTap = { }) }
                // The rail's diagonal corner cut is ~36dp at this height, so
                // compact content must begin beyond it rather than underneath it.
                .padding(horizontal = if (compact) 42.dp else 16.dp, vertical = if (compact) 24.dp else 18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("WALLPAPER // LIBRARY", color = Color.White, fontFamily = SignalMono, fontWeight = FontWeight.Medium, fontSize = 17.sp)
                    Text("tap to select · tap outside to apply", color = Color.White.copy(alpha = .48f), fontFamily = SignalMono, fontSize = 10.sp)
                }
                Text(
                    if (setsSystemHome) "DEVICE HOME" else "COMMANDER ONLY",
                    color = if (setsSystemHome) accent else Color.White.copy(alpha = .58f),
                    fontFamily = SignalMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    letterSpacing = .4.sp,
                    modifier = Modifier.clickable(onClick = onToggleTarget).padding(horizontal = 7.dp, vertical = 8.dp),
                )
                Icon(
                    if (compact) Icons.Outlined.GridView else Icons.Outlined.ViewCarousel,
                    if (compact) "Show full library" else "Show compact strip",
                    tint = accent.copy(alpha = .92f),
                    modifier = Modifier.size(34.dp).clickable { compact = !compact }.padding(6.dp),
                )
                Spacer(Modifier.size(8.dp))
                Icon(
                    Icons.Outlined.Tune,
                    "Wallpaper appearance",
                    tint = if (adjustments) accent else Color.White.copy(alpha = .78f),
                    modifier = Modifier.size(34.dp).clickable { adjustments = !adjustments }.padding(6.dp),
                )
                Spacer(Modifier.size(8.dp))
                Icon(Icons.Outlined.FolderOpen, "Choose folder", tint = Color.White.copy(alpha = .78f), modifier = Modifier.size(34.dp).clickable(onClick = onChooseFolder).padding(6.dp))
                Spacer(Modifier.size(8.dp))
                Icon(Icons.Outlined.Close, "Close", tint = Color.White.copy(alpha = .78f), modifier = Modifier.size(34.dp).clickable(onClick = onClose).padding(6.dp))
            }
            if (adjustments) {
                Spacer(Modifier.height(10.dp))
                WallpaperSliderRow("DARKNESS", (darkness * 100).toInt().toString(), darkness, .15f..85f / 100f) {
                    darkness = it; HomeSettings.setWallpaperAppearance(context, darkness, blur)
                }
                WallpaperSliderRow("UI BLUR", blur.toInt().toString(), blur, 0f..12f) {
                    blur = it; HomeSettings.setWallpaperAppearance(context, darkness, blur)
                }
                Row(Modifier.fillMaxWidth().height(38.dp).clickable { glassEnabled = !glassEnabled },
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("NOTHING GLASS", color = if (glassEnabled) accent else Color.White.copy(alpha = .70f),
                        fontFamily = SignalMono, fontSize = 10.sp, modifier = Modifier.width(108.dp))
                    Text(if (glassEnabled) "ON" else "OFF", color = if (glassEnabled) accent else Color.White.copy(alpha = .42f),
                        fontFamily = SignalMono, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }
                if (glassEnabled) {
                    WallpaperSliderRow("GLASS BLUR", glassBlur.toInt().toString(), glassBlur, 0f..12f) { glassBlur = it }
                    WallpaperSliderRow("REED", glassReed.toInt().toString(), glassReed, 10f..100f) { glassReed = it }
                    WallpaperSliderRow("DISTORT", glassDistortion.toInt().toString(), glassDistortion, 0f..70f) { glassDistortion = it }
                    WallpaperSliderRow("LIGHT", glassLighting.toInt().toString(), glassLighting, 0f..40f) { glassLighting = it }
                }
            }
            Spacer(Modifier.height(15.dp))
            if (items.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (loading) "SCANNING…" else "NO IMAGES IN THIS FOLDER", color = Color.White.copy(alpha = .45f), fontFamily = SignalMono, fontSize = 12.sp)
                }
            } else if (compact) {
                // Each thumbnail has a 15% diagonal cut (about 23dp at
                // 156dp wide). Overlap by that amount so the adjoining image
                // fills the transparent wedge and the strip is continuous.
                LazyRow(horizontalArrangement = Arrangement.spacedBy((-23).dp)) {
                    rowItems(items, key = { it.uri.toString() }) { item ->
                        WallpaperTile(
                            item,
                            selected == item.uri.toString(),
                            accent,
                            Modifier.width(156.dp),
                            WallpaperThumbShape,
                        ) { onSelect(item) }
                    }
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(0.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    items(items, key = { it.uri.toString() }) { item ->
                        WallpaperTile(item, selected == item.uri.toString(), accent, shape = RoundedCornerShape(1.dp)) { onSelect(item) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperSliderRow(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Row(Modifier.fillMaxWidth().height(42.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$label  $valueLabel", color = Color.White.copy(alpha = .70f),
            fontFamily = SignalMono, fontSize = 10.sp, modifier = Modifier.width(108.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = range,
            modifier = Modifier.weight(1f))
    }
}

@Composable
private fun WallpaperTile(
    item: WallpaperLibraryItem,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(null, item.uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(item.uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (bounds.outWidth / sample > 420 || bounds.outHeight / sample > 420) sample *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sample.coerceAtLeast(1) }
                context.contentResolver.openInputStream(item.uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            }.getOrNull()
        }
    }
    Box(
        modifier.aspectRatio(1f).clip(shape).background(Color(0xFF15181B))
            .border(if (selected) 2.dp else 1.dp, if (selected) accent else Color.White.copy(alpha = .14f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let { Image(it.asImageBitmap(), item.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            ?: Icon(Icons.Outlined.Image, null, tint = Color.White.copy(alpha = .22f), modifier = Modifier.size(28.dp))
        if (selected) Box(Modifier.fillMaxSize().border(1.dp, Color.White.copy(alpha = .72f), shape))
    }
}

private val WallpaperRailShape = GenericShape { size, _ ->
    val cut = size.height * .12f
    moveTo(cut, 0f)
    lineTo(size.width - cut, 0f)
    lineTo(size.width, cut)
    lineTo(size.width - cut, size.height)
    lineTo(cut, size.height)
    lineTo(0f, size.height - cut)
    close()
}

private val WallpaperThumbShape = GenericShape { size, _ ->
    val cut = size.width * .15f
    moveTo(cut, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width - cut, size.height)
    lineTo(0f, size.height)
    close()
}
