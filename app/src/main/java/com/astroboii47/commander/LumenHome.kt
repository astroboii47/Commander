package com.astroboii47.commander

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.provider.ContactsContract
import android.view.KeyEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

private val LumenSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
)
private val LumenMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
)
private val LumenWhite = Color(0xFFF0F5F4)
private val LumenMuted = Color(0xFF9CAFB9)

@Composable
fun LumenHome(engine: CommandEngine) {
    val context = LocalContext.current
    val accent = AccentSettings.color.value
    val hubItems = HubRepository.items
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var tasks by remember { mutableStateOf(TodoistTasks.cached()) }
    var selected by remember { mutableIntStateOf(0) }
    val today = now.toLocalDate()
    val agenda = remember(tasks, today) {
        tasks.filter { task ->
            task.dueDate?.let { due -> runCatching { LocalDate.parse(due) <= today.plusDays(1) }.getOrDefault(false) } == true
        }.sortedWith(compareBy<TodoistTask> { it.dueDateTime ?: "${it.dueDate}T23:59" }.thenByDescending { it.priority })
    }
    val messageCount = remember(hubItems) { hubItems.count { it.lumenIsMessage() && !it.isSummary } }
    val profileName = remember { context.lumenProfileFirstName() }
    val battery = remember(now.minute) { context.lumenBatteryPercentage() }

    LaunchedEffect(Unit) {
        HubRepository.requestRefresh()
        withContext(Dispatchers.IO) { TodoistTasks.load(context) }.onSuccess { tasks = it }
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }
    LaunchedEffect(agenda.size) { selected = selected.coerceIn(0, agenda.lastIndex.coerceAtLeast(0)) }

    MaterialTheme {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .38f))
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP -> { selected = (selected - 1).coerceAtLeast(0); true }
                        KeyEvent.KEYCODE_DPAD_DOWN -> { selected = (selected + 1).coerceAtMost(agenda.lastIndex.coerceAtLeast(0)); true }
                        KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> { agenda.getOrNull(selected)?.let { TodoistTasks.open(context, it) }; true }
                        else -> false
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val side = if (maxWidth < maxHeight) maxWidth else maxHeight
            LumenFrame(side, accent, now, battery, profileName, agenda, selected, messageCount, { selected = it }) {
                agenda.getOrNull(selected)?.let { TodoistTasks.open(context, it) }
            }
        }
    }
}

@Composable
private fun LumenFrame(
    side: Dp,
    accent: Color,
    now: LocalDateTime,
    battery: Int?,
    profileName: String?,
    tasks: List<TodoistTask>,
    selected: Int,
    messageCount: Int,
    onSelect: (Int) -> Unit,
    onOpen: () -> Unit,
) {
    Box(Modifier.size(side)) {
        LumenAtmosphere(accent)
        LumenGlobe(accent, Modifier.fillMaxSize())
        LumenSymbol(accent, side)
        LumenContent(side, accent, now, battery, profileName, tasks, selected, messageCount, onSelect, onOpen)
    }
}

@Composable
private fun LumenAtmosphere(accent: Color) {
    Canvas(Modifier.fillMaxSize()) {
        val accentAtmosphere = lerp(Color(0xFF25373E), accent, .62f)
        val accentMiddle = lerp(Color(0xFF141D23), accent, .24f)
        drawRect(Color(0xD90D1015))
        drawRect(Brush.radialGradient(listOf(accentAtmosphere.copy(alpha = .80f), accentMiddle.copy(alpha = .88f), Color(0xE80D1015)), Offset(size.width * 1.05f, size.height * .30f), size.maxDimension * .88f))
        drawRect(Brush.radialGradient(listOf(accent.copy(alpha = .26f), Color.Transparent), Offset(size.width * .72f, size.height * .48f), size.minDimension * .60f))
    }
}

@Composable
private fun LumenGlobe(accent: Color, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val diameter = maxWidth * .64f
        Canvas(Modifier.fillMaxSize().blur(28.dp)) {
            drawCircle(
                Brush.radialGradient(listOf(accent.copy(alpha = .34f), accent.copy(alpha = .10f), Color.Transparent)),
                radius = size.minDimension * .40f,
                center = Offset(size.width * .86f, size.height * .36f),
            )
        }
        Canvas(
            Modifier.offset(x = maxWidth * .54f, y = maxHeight * .04f).size(diameter)
                .graphicsLayer { rotationZ = -23f }.clip(CircleShape),
        ) {
        val d = size.minDimension
        val globeCenter = center
        val globeLight = lerp(accent, Color.White, .62f).copy(alpha = .78f)
        val globeMiddle = lerp(Color(0xFF2C434C), accent, .58f).copy(alpha = .58f)
        drawCircle(Color(0xFF071016), d / 2f, globeCenter)
        drawCircle(
            Brush.radialGradient(listOf(globeLight, globeMiddle, Color.Transparent), Offset(globeCenter.x - d * .18f, globeCenter.y - d * .28f), d * .72f),
            d / 2f, globeCenter, .72f,
        )
        drawCircle(Brush.radialGradient(listOf(lerp(accent, Color.White, .48f).copy(alpha = .38f), Color.Transparent)), d * .38f, Offset(globeCenter.x - d * .24f, globeCenter.y + d * .28f))
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = .16f), Color.Transparent)), d * .28f, Offset(globeCenter.x + d * .20f, globeCenter.y - d * .17f))
        val grid = lerp(accent, Color.White, .72f).copy(alpha = .22f)
        val stroke = Stroke((size.minDimension * .0017f).coerceAtLeast(1f))
        listOf(.16f, .43f, .70f, .93f).forEach { ratio ->
            val rect = Rect(globeCenter.x - d * ratio / 2f, 0f, globeCenter.x + d * ratio / 2f, d)
            drawOval(grid, rect.topLeft, rect.size, style = stroke)
        }
        listOf(.13f, .39f, .68f, .89f).forEach { ratio ->
            val rect = Rect(0f, globeCenter.y - d * ratio / 2f, d, globeCenter.y + d * ratio / 2f)
            drawOval(grid, rect.topLeft, rect.size, style = stroke)
        }
        val point = Offset(d * .19f, d * .67f)
        drawCircle(accent.copy(alpha = .30f), d * .075f, point)
        drawCircle(lerp(accent, Color.White, .60f), d * .035f, point)
        drawCircle(Color(0xFFF1FCFF), d * .035f, point, style = Stroke(d * .006f))
        }
    }
}

@Composable
private fun LumenSymbol(accent: Color, side: Dp) {
    var glyph by remember { mutableIntStateOf(0) }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(3_200)
            glyph = (glyph + 1) % 5
            rotation.snapTo(-25f)
            rotation.animateTo(0f, tween(280, easing = CubicBezierEasing(.2f, .8f, .2f, 1f)))
        }
    }
    Box(
        Modifier.offset(x = side * .63f, y = side * .09f).size(side * .36f)
            .graphicsLayer { rotationZ = rotation.value }.clickable { glyph = (glyph + 1) % 5 },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(side * .238f).blur(side * .006f)) { drawLumenGlyph(glyph, accent.copy(alpha = .34f), size.minDimension * .095f) }
        Canvas(Modifier.size(side * .238f)) { drawLumenGlyph(glyph, LumenWhite, size.minDimension * .055f) }
    }
}

private fun DrawScope.drawLumenGlyph(glyph: Int, color: Color, strokeWidth: Float) {
    val c = center
    val radius = size.minDimension * .39f
    fun line(angleDegrees: Float) {
        val radians = Math.toRadians(angleDegrees.toDouble())
        val dx = kotlin.math.cos(radians).toFloat() * radius
        val dy = kotlin.math.sin(radians).toFloat() * radius
        drawLine(color, Offset(c.x - dx, c.y - dy), Offset(c.x + dx, c.y + dy), strokeWidth, StrokeCap.Round)
    }
    when (glyph) {
        0 -> listOf(0f, 45f, 90f, 135f).forEach(::line)
        1 -> line(0f)
        2 -> listOf(0f, 90f).forEach(::line)
        3 -> listOf(45f, 135f).forEach(::line)
        else -> {
            val outer = radius
            val inner = radius * .22f
            val path = Path().apply {
                moveTo(c.x, c.y - outer); lineTo(c.x + inner, c.y - inner); lineTo(c.x + outer, c.y)
                lineTo(c.x + inner, c.y + inner); lineTo(c.x, c.y + outer); lineTo(c.x - inner, c.y + inner)
                lineTo(c.x - outer, c.y); lineTo(c.x - inner, c.y - inner); close()
            }
            drawPath(path, color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun LumenContent(
    side: Dp,
    accent: Color,
    now: LocalDateTime,
    battery: Int?,
    profileName: String?,
    tasks: List<TodoistTask>,
    selected: Int,
    messageCount: Int,
    onSelect: (Int) -> Unit,
    onOpen: () -> Unit,
) {
    val pad = side * .05f
    val primary = tasks.getOrNull(selected)
    val secondary = tasks.getOrNull((selected + 1).coerceAtMost(tasks.lastIndex)).takeIf { it != primary }
    val todayTaskCount = tasks.count { it.dueDate == now.toLocalDate().toString() }
    Box(Modifier.fillMaxSize()) {
        Row(Modifier.offset(x = pad, y = side * .05f).fillMaxWidth(.90f), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(now.format(DateTimeFormatter.ofPattern("EEEE / d MMMM", Locale.getDefault())).uppercase(), color = Color(0xFFBFCCD1), fontFamily = LumenMono, fontSize = (side.value * .0225f).coerceAtLeast(11f).sp, maxLines = 1)
            Text(battery?.let { "$it%" }.orEmpty(), color = Color(0xFFBFCCD1), fontFamily = LumenMono, fontSize = (side.value * .0225f).coerceAtLeast(11f).sp)
        }
        Row(Modifier.offset(x = pad, y = side * .105f), verticalAlignment = Alignment.CenterVertically) {
            Text(now.format(DateTimeFormatter.ofPattern("HH:mm")), color = LumenWhite, fontFamily = LumenSans, fontSize = (side.value * .10f).sp, letterSpacing = (-side.value * .007f).sp, lineHeight = (side.value * .10f).sp)
            Spacer(Modifier.width(side * .03f))
            Text("${ZoneId.systemDefault().id.substringAfterLast('/').replace('_', ' ').uppercase()}\nYOUR TIME", color = Color(0xFFA6BAC5), fontFamily = LumenMono, fontSize = (side.value * .022f).coerceAtLeast(11f).sp, lineHeight = (side.value * .035f).sp)
        }
        Box(Modifier.offset(x = pad, y = side * .235f).fillMaxWidth(.90f)) {
            LumenBriefing(profileName, todayTaskCount, messageCount, primary, side)
        }
        Box(Modifier.offset(x = pad, y = side * .575f).fillMaxWidth(.90f)) { LumenDayProgress(now, accent, side) }
        LumenAgenda(primary, secondary, side, accent, selected, tasks.size, onSelect, onOpen, Modifier.offset(x = pad, y = side * .665f).fillMaxWidth(.90f).height(side * .24f))
        Row(Modifier.offset(x = pad, y = side * .915f).fillMaxWidth(.90f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(side * .009f).background(accent, CircleShape)); Spacer(Modifier.width(side * .014f))
                Text(if (todayTaskCount == 0) "A clear day ahead." else "$todayTaskCount ${if (todayTaskCount == 1) "thing" else "things"} left today.", color = Color(0xFF9EB3BD), fontFamily = LumenSans, fontSize = (side.value * .0225f).coerceAtLeast(11f).sp, maxLines = 1)
            }
            Text("↵ OPEN NEXT", color = Color(0xFF9EB3BD), fontFamily = LumenMono, fontSize = (side.value * .021f).coerceAtLeast(11f).sp, modifier = Modifier.clickable(enabled = primary != null, onClick = onOpen))
        }
    }
}

@Composable
private fun LumenBriefing(profileName: String?, taskCount: Int, messageCount: Int, nextTask: TodoistTask?, side: Dp) {
    val greeting = when (LocalDateTime.now().hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    val greetingLine = if (profileName.isNullOrBlank()) "$greeting." else "$greeting, $profileName."
    val firstLine = when { taskCount == 0 && messageCount == 0 -> "Your day is clear"; taskCount == 1 -> "You have one task"; taskCount > 1 -> "You have $taskCount tasks"; else -> "You have $messageCount messages" }
    val secondLine = when { taskCount > 0 && messageCount > 0 -> "and $messageCount ${if (messageCount == 1) "new message." else "new messages."}"; taskCount > 0 -> "to finish today."; taskCount == 0 && messageCount > 0 -> "waiting in Commander."; else -> "with room to breathe." }
    val lastLine = nextTask?.dueDateTime?.let { "Next timed item at ${it.lumenTime()}." } ?: "Type whenever you’re ready."
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = LumenMuted)) { append(greetingLine) }; append('\n')
            withStyle(SpanStyle(color = LumenWhite)) { append(firstLine) }; append('\n')
            withStyle(SpanStyle(color = LumenWhite)) { append(secondLine) }; append('\n')
            withStyle(SpanStyle(color = LumenMuted)) { append(lastLine) }
        },
        fontFamily = LumenSans, fontSize = (side.value * .058f).sp, lineHeight = (side.value * .0754f).sp,
        letterSpacing = (-side.value * .0012f).sp, maxLines = 4,
    )
}

@Composable
private fun LumenDayProgress(now: LocalDateTime, accent: Color, side: Dp) {
    val fraction = ((now.hour * 60 + now.minute) / 1440f).coerceIn(.02f, .98f)
    Column {
        Box(Modifier.fillMaxWidth().height(side * .003f).background(Color(0x337D9194))) {
            Box(Modifier.fillMaxWidth(fraction).height(side * .003f).background(accent.copy(alpha = .82f)))
            Box(Modifier.offset(x = side * .90f * fraction - side * .006f, y = -side * .004f).size(side * .012f).shadow(side * .012f, CircleShape, ambientColor = accent, spotColor = accent).background(Color(0xFFE5F3F7), CircleShape))
        }
        Spacer(Modifier.height(side * .017f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("NOW", "${now.format(DateTimeFormatter.ofPattern("HH:mm"))} · YOUR TIME", "24").forEach { Text(it, color = Color(0xFF9DABB5), fontFamily = LumenMono, fontSize = (side.value * .021f).coerceAtLeast(11f).sp) }
        }
    }
}

@Composable
private fun LumenAgenda(
    primary: TodoistTask?, secondary: TodoistTask?, side: Dp, accent: Color, selected: Int, count: Int,
    onSelect: (Int) -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier,
) {
    val cardStart = lerp(Color(0xFF293A42), accent, .32f)
    val cardEnd = lerp(Color(0xFF202D34), accent, .20f)
    val underCard = lerp(Color(0xFF18242B), accent, .14f)
    Box(modifier) {
        if (secondary != null) {
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth(.94f).height(side * .0696f)
                    .clip(RoundedCornerShape(side * .019f)).background(underCard)
                    .border(side * .0015f, Color(0x16A4C2CB), RoundedCornerShape(side * .019f))
                    .clickable { onSelect((selected + 1).coerceAtMost(count - 1)) }.padding(horizontal = side * .04f),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(secondary.content, color = Color(0xFFBACBD2), fontFamily = LumenSans, fontSize = (side.value * .03f).coerceAtLeast(12f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(side * .025f)); Text(secondary.lumenDueLabel(), color = Color(0xFF98B0BC), fontFamily = LumenSans, fontSize = (side.value * .027f).coerceAtLeast(11f).sp, maxLines = 1)
            }
        }
        Box(
            Modifier.align(Alignment.TopCenter).fillMaxWidth(.96f).height(side * .1608f)
                .blur(side * .026f).background(accent.copy(alpha = .20f), RoundedCornerShape(side * .035f)),
        )
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().height(side * .1608f).zIndex(2f)
                .shadow(side * .018f, RoundedCornerShape(side * .029f), ambientColor = Color.Black.copy(alpha = .45f))
                .clip(RoundedCornerShape(side * .029f)).background(Brush.linearGradient(listOf(cardStart, cardEnd, Color(0xE6202529))))
                .border(side * .0012f, accent.copy(alpha = .34f), RoundedCornerShape(side * .029f))
                .clickable(enabled = primary != null, onClick = onOpen).padding(horizontal = side * .05f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(side * .01f)) {
                Text(primary?.content ?: "Nothing urgent", color = Color(0xFFE2EDF1), fontFamily = LumenSans, fontSize = (side.value * .042f).coerceAtLeast(15f).sp, lineHeight = (side.value * .05f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(primary?.project?.takeIf(String::isNotBlank) ?: if (primary == null) "Your agenda is clear" else "Todoist", color = Color(0xFFA5BAC5), fontFamily = LumenSans, fontSize = (side.value * .026f).coerceAtLeast(11f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(side * .03f))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(side * .01f)) {
                Text(primary?.lumenTimeOrDate() ?: "—", color = Color(0xFFE2EDF1), fontFamily = LumenSans, fontSize = (side.value * .042f).coerceAtLeast(15f).sp, maxLines = 1, textAlign = TextAlign.End)
                Text(primary?.lumenRelativeDue() ?: "", color = Color(0xFFA5BAC5), fontFamily = LumenSans, fontSize = (side.value * .026f).coerceAtLeast(11f).sp, maxLines = 1, textAlign = TextAlign.End)
            }
        }
    }
}

private fun HubItem.lumenIsMessage(): Boolean = category == Notification.CATEGORY_MESSAGE || listOf("messag", "whatsapp", "orca", "telegram", "signal", "instagram").any { packageName.contains(it, true) }

private fun Context.lumenBatteryPercentage(): Int? {
    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return null
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level >= 0 && scale > 0) level * 100 / scale else null
}

private fun Context.lumenProfileFirstName(): String? {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null
    return runCatching {
        contentResolver.query(ContactsContract.Profile.CONTENT_URI, arrayOf(ContactsContract.Profile.DISPLAY_NAME_PRIMARY), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) null else cursor.getString(0)?.trim()?.substringBefore(' ')?.takeIf(String::isNotBlank)
        }
    }.getOrNull()
}

private fun TodoistTask.lumenTimeOrDate(): String = dueDateTime?.lumenTime() ?: dueDate?.let { runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("d MMM")) }.getOrDefault(it) } ?: "—"
private fun TodoistTask.lumenDueLabel(): String = dueDateTime?.let { "By ${it.lumenTime()}" } ?: dueDate?.let { runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("EEE d MMM")) }.getOrDefault(it) } ?: ""

private fun TodoistTask.lumenRelativeDue(): String {
    val due = dueDateTime ?: return dueDate?.let { when (runCatching { LocalDate.parse(it) }.getOrNull()) { LocalDate.now() -> "Today"; LocalDate.now().plusDays(1) -> "Tomorrow"; else -> "" } }.orEmpty()
    val target = due.lumenLocalDateTime() ?: return ""
    val minutes = java.time.Duration.between(LocalDateTime.now(), target).toMinutes()
    return when { minutes in 0..59 -> "In $minutes min"; minutes in 60..1439 -> "In ${minutes / 60} hr"; minutes < 0 -> "Overdue"; else -> target.format(DateTimeFormatter.ofPattern("EEE")) }
}

private fun String.lumenLocalDateTime(): LocalDateTime? = runCatching { Instant.parse(this).atZone(ZoneId.systemDefault()).toLocalDateTime() }
    .recoverCatching { OffsetDateTime.parse(this).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }
    .recoverCatching { LocalDateTime.parse(this) }.getOrNull()

private fun String.lumenTime(): String = lumenLocalDateTime()?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: takeLast(5)
