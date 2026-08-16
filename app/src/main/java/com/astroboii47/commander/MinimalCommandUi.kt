package com.astroboii47.commander

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val Accent: Color get() = AccentSettings.color.value
private val InstrumentSerif = FontFamily(Font(R.font.instrument_serif_regular))
private val Inter = FontFamily(Font(R.font.inter_variable))
private val CourierPrime = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal),
    Font(R.font.courier_prime_bold, FontWeight.Bold),
)
private val LightCanvas = Color(0xFFE8E6DF)
private val LightPanel = Color(0xFFF1EFE8)
private val LightTile = Color(0xFFEAE8E1)
private val DarkCanvas = Color(0xFF000000)
private val DarkPanel = Color(0xFF050505)
private val DarkTile = Color(0xFF080808)

private data class Palette(
    val canvas: Color,
    val panel: Color,
    val tile: Color,
    val text: Color,
    val secondary: Color,
    val line: Color,
)

@Composable
fun MinimalCommandApp(engine: CommandEngine, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val palette = if (dark) {
        Palette(DarkCanvas, DarkPanel, DarkTile, Color(0xFFF3F1EA), Color(0xFF8D8C87), Color(0xFF292929))
    } else {
        Palette(LightCanvas, LightPanel, LightTile, Color(0xFF151515), Color(0xFF706F6A), Color(0xFFCFCDC5))
    }
    MaterialTheme {
        CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = Inter, color = palette.text)) {
            Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.canvas)
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
                CommandPanel(engine, palette, onDismiss) {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            }
        }
    }
}

@Composable
private fun CommandPanel(engine: CommandEngine, palette: Palette, onDismiss: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp
    var query by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val parsed = remember(query) { parseCommand(query) }
    val baseAppResults = remember(query) {
        if (parsed.kind == CommandKind.Apps) engine.searchApps(parsed.text) else emptyList()
    }
    var appResults by remember { mutableStateOf<List<AppResult>>(emptyList()) }
    // Grow into square/tall displays, but let width be the limiting dimension on
    // compact phones so the four-column action grid never gets squeezed.
    val homeScale = minOf(screenHeight / 600f, screenWidth / 420f).coerceIn(.82f, 1.22f)
    fun execute() {
        scope.launch {
            notice = if (parsed.kind == CommandKind.Todo && TodoistSettings.directEnabled(context)) "adding to todoist…" else null
            val result = engine.execute(parsed, appResults.firstOrNull())
            notice = result ?: if (parsed.kind == CommandKind.Todo && TodoistSettings.directEnabled(context)) "task added · todoist" else null
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(baseAppResults) {
        appResults = baseAppResults
        if (baseAppResults.isNotEmpty()) {
            appResults = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                engine.loadVisibleAppIcons(baseAppResults)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().widthIn(max = 470.dp),
        shape = RoundedCornerShape(20.dp),
        color = palette.panel,
        contentColor = palette.text,
        shadowElevation = if (palette.canvas == DarkCanvas) 0.dp else 10.dp,
        border = if (palette.canvas == DarkCanvas) BorderStroke(1.dp, palette.line) else null,
    ) {
        Column(modifier = Modifier.padding(if (query.isBlank()) (20 * homeScale).dp else 20.dp)) {
            if (query.isBlank()) Spacer(Modifier.height((18 * homeScale).dp))
            Header(palette, onSettings, if (query.isBlank()) homeScale else 1f)
            if (query.isBlank() && notice == null) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().height(1.dp).focusRequester(focusRequester),
                    textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
                    singleLine = true,
                )
                Spacer(Modifier.height((9 * homeScale).dp))
                HomeContent(palette, homeScale) { symbol -> query = symbol.toString() }
            } else {
                Spacer(Modifier.height(16.dp))
                CommandInput(
                    query = query,
                    onQueryChange = { query = it; notice = null },
                    palette = palette,
                    focusRequester = focusRequester,
                    onExecute = ::execute,
                    onDismiss = onDismiss,
                )
                Spacer(Modifier.height(13.dp))
                CommandLegend(active = parsed.kind, palette = palette)
                Spacer(Modifier.height(16.dp))
                when {
                    notice != null -> NoticeCard(notice!!, palette)
                    parsed.kind == CommandKind.Apps -> AppResults(appResults, parsed.text, palette) { app ->
                        scope.launch { notice = engine.execute(ParsedCommand(CommandKind.Apps, app.label), app) }
                    }
                    else -> CommandPreview(parsed, palette, onExecute = ::execute)
                }
            }
        }
    }
}

@Composable
private fun Header(palette: Palette, onSettings: () -> Unit, scale: Float) {
    val today = remember { LocalDate.now() }
    val weekday = today.format(DateTimeFormatter.ofPattern("EEEE,", Locale.getDefault())).lowercase()
    val date = today.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())).lowercase()
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(weekday, fontFamily = InstrumentSerif, fontSize = (29 * scale).sp, lineHeight = (29 * scale).sp, color = palette.text)
                Text(date, fontFamily = InstrumentSerif, fontSize = (29 * scale).sp, lineHeight = (29 * scale).sp, color = palette.text)
            }
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = palette.secondary,
                modifier = Modifier.size((25 * scale).dp).clickable(onClick = onSettings),
            )
        }
        Spacer(Modifier.height((7 * scale).dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("☼", color = Accent, fontSize = (21 * scale).sp)
            Text("  sunny, 68°", color = palette.secondary, fontSize = (13 * scale).sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CommandInput(
    query: String,
    onQueryChange: (String) -> Unit,
    palette: Palette,
    focusRequester: FocusRequester,
    onExecute: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.tile)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        if (query.isEmpty()) {
            Text("type to search · symbol to command", color = palette.secondary, fontFamily = CourierPrime, fontSize = 14.sp)
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            textStyle = TextStyle(color = palette.text, fontFamily = CourierPrime, fontWeight = FontWeight.Bold, fontSize = 17.sp),
            cursorBrush = SolidColor(Accent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onExecute() }),
        )
    }
}

@Composable
private fun CommandLegend(active: CommandKind, palette: Palette) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        CommandKind.entries.filter { commandSymbol(it) != null }.forEach { kind ->
            Text(
                text = if (commandSymbol(kind) == ' ') "␠" else commandSymbol(kind).toString(),
                color = if (kind == active) Accent else palette.secondary,
                fontFamily = CourierPrime,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun HomeContent(palette: Palette, scale: Float, choose: (Char) -> Unit) {
    val dots = "● ● ● ● ● ● ● ● ● ● ● ● ● ● ● ● ● ● ● ●"
    Text(dots, maxLines = 1, overflow = TextOverflow.Clip, color = palette.secondary.copy(alpha = .55f), fontSize = (8 * scale).sp)
    Spacer(Modifier.height((13 * scale).dp))
    SlimCard("▣", "team meeting · 1:00", "3", palette, scale) { choose('*') }
    Spacer(Modifier.height((8 * scale).dp))
    SlimCard("☷", "renew my passport", "17", palette, scale) { choose('-') }
    Spacer(Modifier.height((14 * scale).dp))
    val actions = listOf(
        Triple("Note", Icons.Outlined.NoteAlt, '!'),
        Triple("Event", Icons.Outlined.CalendarMonth, '*'),
        Triple("Timer", Icons.Outlined.Timer, '+'),
        Triple("To Do", Icons.Outlined.Checklist, '-'),
        Triple("Call", Icons.Outlined.Call, '#'),
        Triple("Message", Icons.Outlined.ChatBubbleOutline, '@'),
        Triple("Camera", Icons.Outlined.PhotoCamera, null),
        Triple("Memo", Icons.Outlined.MicNone, null),
    )
    actions.chunked(4).forEachIndexed { index, row ->
        if (index > 0) Spacer(Modifier.height((8 * scale).dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
            row.forEach { (label, icon, symbol) ->
                ActionTile(label, icon, palette, Modifier.weight(1f), scale) { if (symbol != null) choose(symbol) }
            }
        }
    }
}

@Composable
private fun SlimCard(icon: String, title: String, badge: String, palette: Palette, scale: Float, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(palette.tile)
            .border(1.dp, palette.line, RoundedCornerShape(13.dp)).clickable(onClick = onClick).padding((13 * scale).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, color = Accent, fontSize = (18 * scale).sp)
        Text(title, Modifier.padding(start = (13 * scale).dp).weight(1f), fontWeight = FontWeight.Bold, fontSize = (14 * scale).sp)
        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Accent).padding(horizontal = (10 * scale).dp, vertical = (2 * scale).dp)) {
            Text(badge, color = Color.White, fontFamily = CourierPrime, fontWeight = FontWeight.Bold, fontSize = (12 * scale).sp)
        }
    }
}

@Composable
private fun ActionTile(label: String, icon: ImageVector, palette: Palette, modifier: Modifier, scale: Float, onClick: () -> Unit) {
    Column(
        modifier.height((76 * scale).dp).clip(RoundedCornerShape(12.dp)).background(palette.tile)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, Modifier.size((23 * scale).dp), tint = palette.text)
        Spacer(Modifier.height((7 * scale).dp))
        Text(label, color = palette.text, fontWeight = FontWeight.Bold, fontSize = (12 * scale).sp)
    }
}

@Composable
private fun AppResults(results: List<AppResult>, query: String, palette: Palette, onClick: (AppResult) -> Unit) {
    if (results.isEmpty()) {
        NoticeCard(if (query.isBlank()) "Start typing an app name" else "No apps found", palette)
        return
    }
    results.forEachIndexed { index, app ->
        if (index > 0) Spacer(Modifier.height(7.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(palette.tile)
                .border(1.dp, if (index == 0) Accent else palette.line, RoundedCornerShape(12.dp))
                .clickable { onClick(app) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconPainter = remember(app.icon) { app.icon?.let { BitmapPainter(it.toBitmap(96, 96).asImageBitmap()) } }
            if (iconPainter != null) {
                Image(iconPainter, app.label, Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)))
            } else {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(if (index == 0) Accent else palette.line), contentAlignment = Alignment.Center) {
                    Text(app.label.take(1).uppercase(), color = if (index == 0) Color.White else palette.text, fontWeight = FontWeight.Bold)
                }
            }
            Text(app.label, Modifier.padding(start = 12.dp).weight(1f), fontWeight = FontWeight.Bold)
            if (index == 0) Text("enter ↵", color = palette.secondary, fontFamily = CourierPrime, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CommandPreview(command: ParsedCommand, palette: Palette, onExecute: () -> Unit) {
    val explanation = when (command.kind) {
        CommandKind.Message -> "open message composer"
        CommandKind.Files -> "open file"
        CommandKind.Call -> "open phone"
        CommandKind.Todo -> "add with Todoist"
        CommandKind.Note -> "keep in notes inbox"
        CommandKind.Event -> "create calendar event"
        CommandKind.Timer -> "set timer"
        CommandKind.Calculator -> "calculate"
        CommandKind.Web -> "search in browser"
        CommandKind.Ask -> "ask ChatGPT"
        CommandKind.Apps -> "open app"
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(palette.tile)
            .border(1.dp, Accent.copy(alpha = .65f), RoundedCornerShape(13.dp)).clickable(onClick = onExecute).padding(16.dp),
    ) {
        Text(
            "${commandSymbol(command.kind) ?: ""}${command.text}",
            color = palette.text,
            fontFamily = CourierPrime,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Spacer(Modifier.height(9.dp))
        Text("▸ $explanation", color = palette.secondary, fontFamily = CourierPrime, fontSize = 12.sp)
        Spacer(Modifier.height(5.dp))
        Text("press enter", color = Accent, fontFamily = CourierPrime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NoticeCard(text: String, palette: Palette) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(palette.tile)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp)).padding(16.dp),
    ) {
        Text(text, color = palette.secondary, fontFamily = CourierPrime, fontSize = 13.sp)
    }
}
