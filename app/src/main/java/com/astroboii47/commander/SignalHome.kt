package com.astroboii47.commander

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.app.WallpaperManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.provider.CalendarContract
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.KeyEvent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.window.Dialog
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import compose.icons.LucideIcons
import compose.icons.lucideicons.*
import com.woowla.compose.icon.collections.remix.Remix
import com.woowla.compose.icon.collections.remix.remix.*
import com.woowla.compose.icon.collections.remix.remix.business.*
import com.woowla.compose.icon.collections.remix.remix.communication.*
import com.woowla.compose.icon.collections.remix.remix.device.*
import com.woowla.compose.icon.collections.remix.remix.document.*
import com.woowla.compose.icon.collections.remix.remix.editor.*
import com.woowla.compose.icon.collections.remix.remix.media.*
import com.woowla.compose.icon.collections.remix.remix.system.*
import com.woowla.compose.icon.collections.remix.remix.weather.*
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.*
import com.adamglin.phosphoricons.duotone.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private val SignalSans = FontFamily(Font(R.font.inter_variable))
private val SignalDeckSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
)
private val SignalCardSans = SignalSans
internal val SignalMono = FontFamily(Font(R.font.ibm_plex_mono_regular))
private val SignalSpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
)
private val SignalJetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)
private val SignalGeistMono = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
    Font(R.font.geist_mono_medium, FontWeight.Medium),
)
private val SignalAzeretMono = FontFamily(
    Font(R.font.azeret_mono_regular, FontWeight.Normal),
    Font(R.font.azeret_mono_medium, FontWeight.Medium),
)
private val SignalSpaceMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)
private val SignalCommitMono = FontFamily(
    Font(R.font.commit_mono_regular, FontWeight.Normal),
    Font(R.font.commit_mono_medium, FontWeight.Medium),
)
private val SignalOxanium = FontFamily(Font(R.font.oxanium_variable))
private val SignalChakraPetch = FontFamily(
    Font(R.font.chakra_petch_regular, FontWeight.Normal),
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
)
private val SignalMichroma = FontFamily(Font(R.font.michroma_regular))
private val SignalSyncopate = FontFamily(
    Font(R.font.syncopate_regular, FontWeight.Normal),
    Font(R.font.syncopate_bold, FontWeight.Bold),
)
private val SignalOrbitron = FontFamily(Font(R.font.orbitron_variable))
private val SignalAldrich = FontFamily(Font(R.font.aldrich_regular))
private val SignalAudiowide = FontFamily(Font(R.font.audiowide_regular))
private val SignalRajdhani = FontFamily(
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_semibold, FontWeight.SemiBold),
)
private val SignalDmSans = FontFamily(Font(R.font.dm_sans_regular), Font(R.font.dm_sans_medium, FontWeight.Medium))
private val SignalCourier = FontFamily(Font(R.font.courier_prime_regular), Font(R.font.courier_prime_bold, FontWeight.Bold))
private val SignalDot = FontFamily(Font(R.font.ndot_47))
private val SignalSerif = FontFamily(Font(R.font.instrument_serif_regular))
private val SignalWhite = Color(0xFFF2F3F2)
private val SignalGrey = Color(0xFFADB2B7)

private fun commanderHomeFont(style: HomeFontStyle): FontFamily = when (style) {
    HomeFontStyle.CommanderMono -> SignalMono
    HomeFontStyle.Ryoku -> SignalSpaceGrotesk
    HomeFontStyle.JetBrainsMono -> SignalJetBrainsMono
    HomeFontStyle.GeistMono -> SignalGeistMono
    HomeFontStyle.AzeretMono -> SignalAzeretMono
    HomeFontStyle.SpaceMono -> SignalSpaceMono
    HomeFontStyle.CommitMono -> SignalCommitMono
    HomeFontStyle.Oxanium -> SignalOxanium
    HomeFontStyle.ChakraPetch -> SignalChakraPetch
    HomeFontStyle.Michroma -> SignalMichroma
    HomeFontStyle.Syncopate -> SignalSyncopate
    HomeFontStyle.Orbitron -> SignalOrbitron
    HomeFontStyle.Aldrich -> SignalAldrich
    HomeFontStyle.Audiowide -> SignalAudiowide
    HomeFontStyle.Rajdhani -> SignalRajdhani
    HomeFontStyle.Inter -> SignalSans
    HomeFontStyle.DmSans -> SignalDmSans
    HomeFontStyle.Courier -> SignalCourier
}

private data class NowPlaying(val title: String, val artist: String, val artwork: Bitmap?, val packageName: String)

private fun Context.activeNowPlaying(): NowPlaying? = runCatching {
    val manager = getSystemService(MediaSessionManager::class.java) ?: return@runCatching null
    val listener = ComponentName(this, HubNotificationService::class.java)
    val session = manager.getActiveSessions(listener).firstOrNull {
        it.playbackState?.state == PlaybackState.STATE_PLAYING
    } ?: return@runCatching null
    val metadata = session.metadata
    val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        ?: metadata?.description?.title?.toString()
        ?: "Now playing"
    val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        ?: metadata?.description?.subtitle?.toString()
        ?: ""
    val sourceArtwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        ?: metadata?.description?.iconBitmap
    // The artwork is displayed at roughly 100dp. Keeping a 1000–3000px album
    // cover alive wastes both native heap and a large GPU texture.
    val artwork = sourceArtwork?.let { source ->
        val longest = maxOf(source.width, source.height)
        if (longest <= 320) source else {
            val scale = 320f / longest
            Bitmap.createScaledBitmap(
                source,
                (source.width * scale).toInt().coerceAtLeast(1),
                (source.height * scale).toInt().coerceAtLeast(1),
                true,
            )
        }
    }
    NowPlaying(title, artist, artwork, session.packageName)
}.getOrNull()

// Carousel geometry reference points:
// CHECKPOINT 1 — original stable view: no camera pitch, orbitDepth=.033,
//                rear height scale loss=.12.
// CHECKPOINT 2 — pitched-path view approved as "not bad": no camera pitch,
//                orbitDepth=.068 + .007 idle wave, height loss=.06.
// CHECKPOINT 3 — refined elevated-camera view: +11deg camera pitch with card
//                centres and corners locked to the same projected ring.
// Projects a card's four corners as one panel on the shared carousel orbit.
// This keeps the neighbouring panels broad and avoids a per-card Y-axis spin.
private fun Modifier.orbitPanelProjection(
    angleDegrees: Float,
    ringRadiusPx: Float,
    cameraPitchDegrees: Float = 16f,
): Modifier = orbitPanelProjection({ angleDegrees }, ringRadiusPx, cameraPitchDegrees)

private fun Modifier.orbitPanelProjection(
    angleDegrees: () -> Float,
    ringRadiusPx: Float,
    cameraPitchDegrees: Float = 16f,
): Modifier =
    drawWithContent {
        val halfWidth = size.width * .5f
        val halfHeight = size.height * .5f
        val orbitRadians = Math.toRadians(angleDegrees().toDouble()).toFloat()
        val radius = ringRadiusPx.coerceAtLeast(1f)
        val camera = radius * 5f
        val frontPlane = camera - radius
        val sin = kotlin.math.sin(orbitRadians)
        val cos = kotlin.math.cos(orbitRadians)
        val pitch = Math.toRadians(cameraPitchDegrees.toDouble()).toFloat()
        val pitchSin = kotlin.math.sin(pitch)
        val pitchCos = kotlin.math.cos(pitch)
        val centreDepth = radius * cos * pitchCos
        val centreProjection = frontPlane / (camera - centreDepth)
        val centreProjectedX = radius * sin * centreProjection
        val centreProjectedY = radius * cos * pitchSin * centreProjection
        fun projectedCorner(localX: Float, localY: Float): Offset {
            val worldX = radius * sin + localX * cos
            val worldZ = radius * cos - localX * sin
            val cameraDepth = worldZ * pitchCos - localY * pitchSin
            val perspective = frontPlane / (camera - cameraDepth)
            val screenY = (localY * pitchCos + worldZ * pitchSin) * perspective
            return Offset(
                halfWidth + worldX * perspective - centreProjectedX,
                halfHeight + screenY - centreProjectedY,
            )
        }
        val topLeft = projectedCorner(-halfWidth, -halfHeight)
        val topRight = projectedCorner(halfWidth, -halfHeight)
        val bottomRight = projectedCorner(halfWidth, halfHeight)
        val bottomLeft = projectedCorner(-halfWidth, halfHeight)
        val source = floatArrayOf(
            0f, 0f, size.width, 0f,
            size.width, size.height, 0f, size.height,
        )
        val target = floatArrayOf(
            topLeft.x, topLeft.y,
            topRight.x, topRight.y,
            bottomRight.x, bottomRight.y,
            bottomLeft.x, bottomLeft.y,
        )
        val projection = Matrix().apply { setPolyToPoly(source, 0, target, 0, 4) }
        val canvas = drawContext.canvas.nativeCanvas
        val checkpoint = canvas.save()
        canvas.concat(projection)
        drawContent()
        canvas.restoreToCount(checkpoint)
    }

private class OrbitFrameGeometry {
    var phase = Float.NaN
    var ringPosition = Float.NaN
    var idleStrength = Float.NaN
    var entranceSpread = Float.NaN
    var baseX = Float.NaN
    var baseY = Float.NaN
    var angle = 0f
    var translationX = 0f
    var translationY = 0f
    var lightPulse = 1f
    var focusAmount = 0f
    var visibility = 1f
    var heightScale = 1f
}

private class RearFaceLayerState {
    var key: List<Any?>? = null
    var size = IntSize.Zero
}

// Rear cards are deliberately painted by a single lightweight node per
// physical panel. Their content is static while unfocused, so composing a
// Column/Rows/Text/Icon tree for every visible rear position only makes the
// UI and render threads repeat work that cannot be seen changing. The focused
// card below remains the full live Compose implementation.
@Composable
private fun RearCarouselCard(
    modifier: Modifier,
    side: Dp,
    index: Int,
    title: String,
    detail: String,
    leftHeader: String,
    rightHeader: String,
    icon: ImageVector,
    artwork: androidx.compose.ui.graphics.ImageBitmap?,
    taskCount: Int,
    showTaskCounter: Boolean,
    singleLineDetail: Boolean,
    font: FontFamily,
    accent: Color,
    cacheScale: Float = .65f,
) {
    val iconPainter = rememberVectorPainter(icon)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val boldTitles = HomeSettings.boldCardTitles.value
    // Rear panels are already perspective-reduced. A 65% face cache remains
    // visually sharp at their displayed size while avoiding full-resolution
    // textures for every physical slot around the ring.
    // Rear panels are perspective-reduced; a smaller cached face avoids a
    // large texture upload on focus handoff. Alignment comes from shared
    // measurements below, not from allocating full-size rear bitmaps.
    val cacheDensity = Density(density.density * cacheScale, density.fontScale)
    // TextMeasurer retains the composition's full density, unlike the
    // CanvasDrawScope below. Scale every typographic dimension explicitly so
    // the cached bitmap is a true reduced copy of the live card rather than a
    // reduced card with full-resolution (and therefore oversized) type.
    fun cachedSp(value: Float) = (value * cacheScale).sp
    val bitmapWidth = with(cacheDensity) { (side * .37f).roundToPx().coerceAtLeast(1) }
    val bitmapHeight = with(cacheDensity) { (side * .325f).roundToPx().coerceAtLeast(1) }
    val cachedFace = remember(
        side, index, title, detail, leftHeader, rightHeader, icon, artwork,
        taskCount, showTaskCounter, singleLineDetail, font, accent, boldTitles,
    ) {
        ImageBitmap(bitmapWidth, bitmapHeight).also { target ->
            CanvasDrawScope().draw(
                density = cacheDensity,
                layoutDirection = layoutDirection,
                canvas = ComposeCanvas(target),
                size = Size(bitmapWidth.toFloat(), bitmapHeight.toFloat()),
            ) {
        val sidePx = side.toPx()
        val radius = sidePx * .012f
        val horizontalPadding = sidePx * .025f
        drawRoundRect(
            color = Color(0xFF080A0D).copy(alpha = .51f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        )
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = .075f), Color.Transparent, accent.copy(alpha = .045f)),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color.White.copy(alpha = .10f), Color.Transparent, Color.Black.copy(alpha = .18f)),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = SignalWhite.copy(alpha = .42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = Stroke(width = sidePx * .00135f),
        )

        fun layout(
            value: String,
            style: TextStyle,
            width: Int,
            maxLines: Int = 1,
        ) = textMeasurer.measure(
            text = value,
            style = style,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines,
            constraints = Constraints(maxWidth = width.coerceAtLeast(1)),
        )

        val numberStyle = TextStyle(
            color = SignalWhite.copy(alpha = .72f),
            fontFamily = SignalMono,
            fontWeight = FontWeight.Medium,
            fontSize = cachedSp(side.value * .0125f),
            letterSpacing = cachedSp(.75f),
        )
        val headerStyle = numberStyle.copy(fontWeight = FontWeight.Normal, letterSpacing = 0.sp)
        val number = layout("${(index + 1).toString().padStart(2, '0')}  /  ", numberStyle, (sidePx * .09f).toInt())
        val headerY = sidePx * .022f
        drawText(number, topLeft = Offset(horizontalPadding, headerY))
        val leftX = horizontalPadding + number.size.width
        val left = layout(leftHeader, headerStyle, (size.width * .42f).toInt())
        drawText(left, topLeft = Offset(leftX, headerY))
        val right = layout(rightHeader, headerStyle, (size.width * .28f).toInt())
        drawText(right, topLeft = Offset(size.width - horizontalPadding - right.size.width, headerY))

        val iconSize = sidePx * .064f
        val iconLeft = (size.width - iconSize) * .5f
        val headerHeight = maxOf(number.size.height, left.size.height, right.size.height).toFloat()
        // Native Canvas text uses tighter font metrics than Compose Text. Keep
        // the established compensation so the cached icon/title/detail block
        // lands on the same visual baselines as the live focused card.
        val iconBoxTop = headerY + headerHeight + sidePx * .052f
        val iconTop = iconBoxTop + (sidePx * .100f - iconSize) * .5f
        if (artwork != null) {
            val artworkSize = sidePx * .090f
            drawImage(
                image = artwork,
                srcOffset = androidx.compose.ui.unit.IntOffset.Zero,
                srcSize = IntSize(artwork.width, artwork.height),
                dstOffset = androidx.compose.ui.unit.IntOffset(
                    ((size.width - artworkSize) * .5f).toInt(),
                    (iconBoxTop + (sidePx * .100f - artworkSize) * .5f).toInt(),
                ),
                dstSize = IntSize(artworkSize.toInt(), artworkSize.toInt()),
            )
        } else if (showTaskCounter) {
            val count = layout(
                taskCount.toString().padStart(2, '0'),
                TextStyle(
                    color = SignalWhite,
                    fontFamily = SignalDot,
                    fontSize = cachedSp(side.value * .061f),
                    letterSpacing = cachedSp(-1f),
                ),
                (size.width * .5f).toInt(),
            )
            drawText(count, topLeft = Offset(
                (size.width - count.size.width) * .5f,
                iconBoxTop + (sidePx * .100f - count.size.height) * .5f,
            ))
        } else {
            withTransform({ translate(iconLeft, iconTop) }) {
                with(iconPainter) {
                    draw(
                        size = Size(iconSize, iconSize),
                        colorFilter = ColorFilter.tint(SignalWhite),
                    )
                }
            }
        }

        val titleLayout = layout(
            title.uppercase(),
            TextStyle(
                color = SignalWhite,
                fontFamily = font,
                fontWeight = if (boldTitles) FontWeight.Bold else FontWeight.Medium,
                fontSize = cachedSp(side.value * .023f),
                letterSpacing = cachedSp(.8f),
            ),
            (size.width - horizontalPadding * 2f).toInt(),
        )
        val titleY = iconBoxTop + sidePx * .110f
        drawText(titleLayout, topLeft = Offset((size.width - titleLayout.size.width) * .5f, titleY))
        val detailLayout = layout(
            detail,
            TextStyle(
                color = SignalWhite.copy(alpha = .76f),
                fontFamily = font,
                fontSize = cachedSp(side.value * .016f),
                lineHeight = cachedSp(side.value * .022f),
                letterSpacing = cachedSp(-.15f),
                textAlign = TextAlign.Center,
            ),
            (size.width - horizontalPadding * 2f).toInt(),
            maxLines = if (singleLineDetail) 1 else 2,
        )
        val detailY = titleY + titleLayout.size.height + sidePx * .008f
        drawText(detailLayout, topLeft = Offset((size.width - detailLayout.size.width) * .5f, detailY))
            }
        }
    }
    Canvas(modifier) {
        drawImage(
            image = cachedFace,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
        )
    }
}

@Composable
private fun StackCompanionCard(
    modifier: Modifier,
    side: Dp,
    index: Int,
    title: String,
    detail: String,
    leftHeader: String,
    rightHeader: String,
    icon: ImageVector,
    artwork: androidx.compose.ui.graphics.ImageBitmap?,
    taskCount: Int,
    showTaskCounter: Boolean,
    font: FontFamily,
    accent: Color,
) {
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            // Frost the overlapping upper portion of each face so lettering
            // on cards farther back does not show through the next card.
            drawRect(Brush.verticalGradient(listOf(
                Color.Black.copy(alpha = .17f),
                Color.Black.copy(alpha = .08f),
                Color.Transparent,
                Color.Transparent,
            )))
            drawRect(Brush.verticalGradient(listOf(
                Color.White.copy(alpha = .095f), Color.Transparent,
                accent.copy(alpha = .075f),
            )))
            drawRect(Brush.linearGradient(listOf(
                Color.White.copy(alpha = .10f), Color.Transparent,
                Color.Black.copy(alpha = .18f),
            ), start = Offset.Zero, end = Offset(size.width, size.height)))
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = side * .025f, vertical = side * .022f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text("${(index + 1).toString().padStart(2, '0')}  /  ",
                        color = SignalWhite.copy(alpha = .72f), fontFamily = SignalMono,
                        fontWeight = FontWeight.Medium, fontSize = (side.value * .0125f).sp,
                        letterSpacing = (.75).sp, maxLines = 1)
                    Text(leftHeader, color = SignalWhite.copy(alpha = .86f),
                        fontFamily = SignalMono, fontSize = (side.value * .0125f).sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                }
                Text(rightHeader, color = SignalWhite.copy(alpha = .86f),
                    fontFamily = SignalMono, fontSize = (side.value * .0125f).sp,
                    maxLines = 1)
            }
            Spacer(Modifier.height(side * .022f))
            Box(Modifier.size(side * .100f), contentAlignment = Alignment.Center) {
                when {
                    artwork != null -> Image(bitmap = artwork, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(side * .090f)
                            .clip(RoundedCornerShape(side * .010f)))
                    showTaskCounter -> Text(taskCount.toString().padStart(2, '0'),
                        color = SignalWhite, fontFamily = SignalDot,
                        fontSize = (side.value * .061f).sp, letterSpacing = (-1).sp)
                    else -> {
                        Icon(icon, null, tint = accent.copy(alpha = .70f),
                            modifier = Modifier.size(side * .076f)
                                .blur(side * .017f, BlurredEdgeTreatment.Unbounded))
                        Icon(icon, null, tint = lerp(accent, Color.White, .55f).copy(alpha = .58f),
                            modifier = Modifier.size(side * .068f)
                                .blur(side * .005f, BlurredEdgeTreatment.Unbounded))
                        Icon(icon, null, tint = lerp(SignalWhite, accent, .26f),
                            modifier = Modifier.size(side * .064f))
                    }
                }
            }
            Spacer(Modifier.height(side * .010f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title.uppercase(), color = SignalWhite, fontFamily = font,
                    fontWeight = if (HomeSettings.boldCardTitles.value) FontWeight.Bold else FontWeight.Medium,
                    fontSize = (side.value * .023f).sp, letterSpacing = (.8).sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(side * .008f))
                Text(detail, color = SignalWhite.copy(alpha = .82f), fontFamily = font,
                    fontSize = (side.value * .016f).sp, lineHeight = (side.value * .022f).sp,
                    letterSpacing = (-.15).sp, maxLines = 1,
                    textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// The bitmap rear-face renderer saved composition work, but introduced
// visible typography differences, texture-upload stalls and discontinuities
// when focus changed or a spring was interrupted. All visible physical slots
// now use the same Compose face so content remains pixel-identical throughout
// rotation. Hidden logical cards are still culled above.
private const val UseCanvasRearCards = false

private fun Modifier.signalGlassIsland(side: Dp): Modifier {
    val shape = RoundedCornerShape(side * .011f)
    return clip(shape)
        .background(Color(0xFF080A0D).copy(alpha = .38f))
        .background(Brush.verticalGradient(listOf(
            Color.White.copy(alpha = .085f),
            Color.Transparent,
            Color.White.copy(alpha = .024f),
        )))
        .border(side * .0010f, SignalWhite.copy(alpha = .26f), shape)
}

@Composable
private fun SignalMenuMark(side: Dp, accent: Color) {
    Canvas(Modifier.size(side * .027f)) {
        val peak = Path().apply {
            moveTo(size.width * .50f, size.height * .08f)
            lineTo(size.width * .93f, size.height * .88f)
            lineTo(size.width * .67f, size.height * .73f)
            lineTo(size.width * .50f, size.height * .43f)
            lineTo(size.width * .33f, size.height * .73f)
            lineTo(size.width * .07f, size.height * .88f)
            close()
        }
        drawPath(peak, accent.copy(alpha = .16f), style = Stroke(width = size.width * .20f))
        drawPath(peak, lerp(accent, Color.White, .66f))
        drawCircle(Color(0xFF080A0D), radius = size.width * .075f, center = Offset(size.width * .50f, size.height * .70f))
    }
}

@Composable
private fun SignalStatusCounter(
    side: Dp,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    count: Int,
    accent: Color,
) {
    Row(
        Modifier.height(side * .032f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(side * .0015f),
    ) {
        Icon(icon, description, tint = SignalWhite.copy(alpha = if (count > 0) .94f else .40f),
            modifier = Modifier.size(side * .0255f))
        Text(count.coerceAtMost(99).toString(), color = SignalWhite.copy(alpha = if (count > 0) .82f else .50f),
            fontFamily = SignalMono, fontSize = (side.value * .0115f).sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.width(side * .022f))
    }
}

private data class SignalEvent(val id: Long, val title: String, val start: ZonedDateTime, val end: ZonedDateTime, val allDay: Boolean)
private data class RainPoint(val hour: String, val chance: Int)
private data class RainForecast(
    val hourly: List<RainPoint> = emptyList(),
    val weekly: List<RainPoint> = emptyList(),
    val weatherCode: Int? = null,
    val temperature: Int? = null,
)

@Composable
fun SignalHome(
    engine: CommandEngine,
    sharedWallpaper: Boolean = false,
    onOpenHub: (() -> Unit)? = null,
    onOpenApps: (() -> Unit)? = null,
    onBeforeExternalLaunch: () -> Unit = {},
    isActiveSurface: Boolean = true,
) {
    val context = LocalContext.current
    val accent = AccentSettings.color.value
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var tasks by remember { mutableStateOf(TodoistTasks.cached()) }
    var events by remember { mutableStateOf(context.signalEvents(now.toLocalDate())) }
    var recentNote by remember { mutableStateOf(RecentNoteStore.latest(context)) }
    var rainForecast by remember { mutableStateOf(context.cachedRainForecast()) }
    var page by remember { mutableIntStateOf(0) }
    var selectedQuarter by remember { mutableIntStateOf(-1) }
    var surfaceDrag by remember { mutableFloatStateOf(0f) }
    val focusRequester = remember { FocusRequester() }
    val refreshScope = rememberCoroutineScope()
    val hubItems = HubRepository.items
    val flaggedKeys = context.getSharedPreferences("hub_flags", 0)
        .getStringSet("keys", emptySet())
        .orEmpty()
    val importantKeys = context.getSharedPreferences("hub_important", 0)
        .getStringSet("keys", emptySet())
        .orEmpty()
    val importantRules = HubImportantSettings.rules.value
    val flaggedCount = hubItems.count { item -> item.key in flaggedKeys || item.sourceKeys.any(flaggedKeys::contains) }
    val importantCount = hubItems.count { item ->
        item.key in importantKeys || item.sourceKeys.any(importantKeys::contains) ||
            importantRules.isNotEmpty() && HubImportantSettings.matches(item)
    }

    LaunchedEffect(Unit) {
        HubRepository.requestRefresh()
        withContext(Dispatchers.IO) { TodoistTasks.load(context) }.onSuccess { tasks = it }
        events = withContext(Dispatchers.IO) { context.signalEvents(LocalDate.now()) }
        rainForecast = withContext(Dispatchers.IO) { signalRainForecast(context) }
        if (rainForecast.hourly.isEmpty() && rainForecast.weekly.isEmpty()) {
            delay(2_500)
            rainForecast = withContext(Dispatchers.IO) { signalRainForecast(context) }
        }
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            now = LocalDateTime.now()
            delay(60_000)
        }
    }
    DisposableEffect(context) {
        val owner = context as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                HubRepository.requestRefresh()
                now = LocalDateTime.now()
                recentNote = RecentNoteStore.latest(context)
                tasks = TodoistTasks.cached()
                refreshScope.launch {
                    events = withContext(Dispatchers.IO) { context.signalEvents(now.toLocalDate()) }
                    rainForecast = withContext(Dispatchers.IO) { signalRainForecast(context) }
                    withContext(Dispatchers.IO) { TodoistTasks.load(context) }.onSuccess { tasks = it }
                }
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    fun openTrigger(symbol: Char) {
        val kind = when (symbol) {
            '@' -> CommandKind.Message
            '#' -> CommandKind.Call
            '+' -> CommandKind.Timer
            '?' -> CommandKind.Ask
            TriggerSettings.noteSymbol() -> CommandKind.Note
            else -> null
        }
        context.startActivity(Intent(context, OverlayActivity::class.java).apply {
            action = OverlayActivity.ACTION_OPEN_OVERLAY
            putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, kind?.let(TriggerSettings::trigger) ?: symbol.toString())
        })
    }

    MaterialTheme {
        val standaloneSwipeModifier = if (onOpenHub == null && onOpenApps == null) {
            Modifier.pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { surfaceDrag = 0f },
                    onHorizontalDrag = { _, amount -> surfaceDrag += amount },
                    onDragEnd = {
                        if (kotlin.math.abs(surfaceDrag) > size.width * .16f) {
                            if (surfaceDrag < 0f || ModuleSettings.hubEnabled.value)
                                context.openCommanderSurface(if (surfaceDrag > 0f) HubActivity::class.java else AppsActivity::class.java)
                        }
                        surfaceDrag = 0f
                    },
                )
            }
        } else Modifier
        BoxWithConstraints(
            Modifier.fillMaxSize().background(if (sharedWallpaper) Color.Transparent else Color.Black).focusRequester(focusRequester).focusable()
                .then(standaloneSwipeModifier)
                .onPreviewKeyEvent { event ->
                    if (!isActiveSurface || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> { page = (page + 2) % 3; true }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> { page = (page + 1) % 3; true }
                        KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> { signalOpenCard(context, page, events); true }
                        else -> false
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val side = minOf(maxWidth, maxHeight)
            if (!sharedWallpaper) SignalWallpaper(context, Modifier.size(side))
            if (HomeSettings.referenceHome.value) ReferenceCarouselHome(
                side = side, now = now, battery = context.signalBattery(),
                charging = context.signalCharging(), accent = accent,
                isActiveSurface = isActiveSurface,
                events = events,
                deadlines = tasks.filter { it.dueDate == now.toLocalDate().toString() && it.dueDateTime != null },
                event = events.firstOrNull { !it.allDay && it.end.toLocalDateTime().isAfter(now) },
                taskCount = tasks.count { it.dueDate == now.toLocalDate().toString() },
                hubCount = hubItems.size,
                flaggedCount = flaggedCount,
                importantCount = importantCount,
                weather = rainForecast,
                sharedWallpaper = sharedWallpaper,
                onEvent = { signalOpenCard(context, 0, events) }, onTasks = { context.signalOpenTasks() },
                onTrigger = ::openTrigger, onOpenHub = onOpenHub, onOpenApps = onOpenApps,
                onBeforeExternalLaunch = onBeforeExternalLaunch,
                keySequence = HomeCarouselKeyBridge.sequence.intValue,
                keyDirection = HomeCarouselKeyBridge.direction.intValue,
                stackKeySequence = HomeCarouselKeyBridge.stackSequence.intValue,
                stackKeyDirection = HomeCarouselKeyBridge.stackDirection.intValue,
                spaceSequence = HomeCarouselKeyBridge.spaceSequence.intValue,
                actionSequence = HomeCarouselKeyBridge.actionSequence.intValue,
            ) else SignalScreen(
                side = side,
                now = now,
                battery = remember(now.minute) { context.signalBattery() },
                charging = remember(now.minute) { context.signalCharging() },
                accent = accent,
                events = events,
                tasks = tasks,
                recentNote = recentNote,
                rainForecast = rainForecast,
                hubCount = hubItems.size,
                flaggedCount = flaggedCount,
                importantCount = importantCount,
                page = page,
                selectedQuarter = selectedQuarter,
                onPage = { page = it },
                onQuarter = { selectedQuarter = it },
                onCard = { signalOpenCard(context, page, events) },
                onTask = { context.signalOpenTasks() },
                onTrigger = ::openTrigger,
                onOpenHub = onOpenHub,
                onOpenApps = onOpenApps,
                sharedWallpaper = sharedWallpaper,
            )
        }
    }
}

@Composable
private fun SignalScreen(
    side: Dp,
    now: LocalDateTime,
    battery: Int?,
    charging: Boolean,
    accent: Color,
    events: List<SignalEvent>,
    tasks: List<TodoistTask>,
    recentNote: String?,
    rainForecast: RainForecast,
    hubCount: Int,
    flaggedCount: Int,
    importantCount: Int,
    page: Int,
    selectedQuarter: Int,
    onPage: (Int) -> Unit,
    onQuarter: (Int) -> Unit,
    onCard: () -> Unit,
    onTask: () -> Unit,
    onTrigger: (Char) -> Unit,
    onOpenHub: (() -> Unit)?,
    onOpenApps: (() -> Unit)?,
    sharedWallpaper: Boolean,
) {
    val context = LocalContext.current
    val pad = side * .045f
    val contentWidth = side - pad * 2
    val todayTasks = remember(tasks, now.toLocalDate()) { tasks.filter { it.dueDate == now.toLocalDate().toString() } }
    val timedTasks = remember(todayTasks) { todayTasks.filter { it.dueDateTime != null } }
    val nextEvent = events.firstOrNull { !it.allDay && it.end.toLocalDateTime().isAfter(now) }
    val nextDeadline = timedTasks.mapNotNull { task -> task.signalDateTime()?.let { task to it } }.minByOrNull { kotlin.math.abs(Duration.between(now, it.second).toMinutes()) }
    val remaining = Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay()).coerceAtLeast(Duration.ZERO)
    val connectivity = rememberLiveConnectivity(context, true)

    val wallpaperActive = HomeSettings.wallpaperMode.value != HomeWallpaperMode.Black
    Box(Modifier.size(side).background(if (sharedWallpaper) Color.Transparent else Color.Black.copy(alpha = if (wallpaperActive) HomeSettings.wallpaperDarkness.floatValue else 1f))) {
        SignalDotLattice(wallpaperActive)
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                Brush.radialGradient(
                    listOf(accent.copy(alpha = if (wallpaperActive) .032f else .085f), accent.copy(alpha = if (wallpaperActive) .008f else .022f), Color.Transparent),
                    center = Offset(size.width * .30f, size.height * .18f),
                    radius = size.width * .42f,
                ),
                radius = size.width * .42f,
                center = Offset(size.width * .30f, size.height * .18f),
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(accent.copy(alpha = if (wallpaperActive) .022f else .060f), Color.Transparent),
                    center = Offset(size.width * .72f, size.height * .39f),
                    radius = size.width * .32f,
                ),
                radius = size.width * .32f,
                center = Offset(size.width * .72f, size.height * .39f),
            )
        }

        Row(Modifier.offset(x = pad, y = side * .035f).width(contentWidth), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                now.format(DateTimeFormatter.ofPattern("EEE dd MMM", Locale.getDefault())).uppercase(),
                color = SignalWhite,
                fontFamily = SignalMono,
                fontSize = (side.value * .0235f).sp,
                letterSpacing = (side.value * .0008f).sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(side * .007f)) {
                if (ModuleSettings.hubEnabled.value) Text("←HUB", color = SignalGrey, fontFamily = SignalMono, fontSize = (side.value * .022f).sp, modifier = Modifier.clickable { (onOpenHub ?: { context.openCommanderSurface(HubActivity::class.java) })() })
                val statusAccent = lerp(accent, Color.White, .20f)
                Row(
                    Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(side * .0035f),
                ) {
                    Icon(Icons.Outlined.Flag, contentDescription = "Flagged Hub items", tint = statusAccent.copy(alpha = if (flaggedCount > 0) 1f else .48f), modifier = Modifier.size(side * .020f))
                    Text(flaggedCount.toString(), color = statusAccent.copy(alpha = if (flaggedCount > 0) 1f else .52f), fontFamily = SignalMono, fontSize = (side.value * .018f).sp, style = androidx.compose.ui.text.TextStyle(shadow = Shadow(accent.copy(alpha = .75f), blurRadius = 7f)))
                    Spacer(Modifier.width(side * .003f))
                    Icon(Icons.Outlined.PriorityHigh, contentDescription = "Important Hub items", tint = statusAccent.copy(alpha = if (importantCount > 0) 1f else .48f), modifier = Modifier.size(side * .020f))
                    Text(importantCount.toString(), color = statusAccent.copy(alpha = if (importantCount > 0) 1f else .52f), fontFamily = SignalMono, fontSize = (side.value * .018f).sp, style = androidx.compose.ui.text.TextStyle(shadow = Shadow(accent.copy(alpha = .75f), blurRadius = 7f)))
                    Spacer(Modifier.width(side * .003f))
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = "Hub notifications", tint = statusAccent.copy(alpha = if (hubCount > 0) 1f else .48f), modifier = Modifier.size(side * .020f))
                    Text(hubCount.toString(), color = statusAccent.copy(alpha = if (hubCount > 0) 1f else .52f), fontFamily = SignalMono, fontSize = (side.value * .018f).sp, style = androidx.compose.ui.text.TextStyle(shadow = Shadow(accent.copy(alpha = .70f), blurRadius = 7f)))
                }
                Spacer(Modifier.width(side * .010f))
                Text("APPS→", color = SignalGrey, fontFamily = SignalMono, fontSize = (side.value * .022f).sp, modifier = Modifier.clickable { (onOpenApps ?: { context.openCommanderSurface(AppsActivity::class.java) })() })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Wifi, null, tint = SignalWhite.copy(alpha = if (connectivity.first) .95f else .28f), modifier = Modifier.size(side * .022f))
                Spacer(Modifier.width(side * .005f))
                Icon(Icons.Outlined.SignalCellularAlt, null, tint = SignalWhite.copy(alpha = if (connectivity.second) .95f else .28f), modifier = Modifier.size(side * .022f))
                Spacer(Modifier.width(side * .005f))
                Icon(Icons.Outlined.Bluetooth, null, tint = SignalWhite.copy(alpha = if (connectivity.third) .95f else .28f), modifier = Modifier.size(side * .021f))
                Spacer(Modifier.width(side * .009f))
                Text(battery?.let { "$it%" }.orEmpty(), color = SignalWhite, fontFamily = SignalMono, fontSize = (side.value * .019f).sp)
                Spacer(Modifier.width(side * .010f)); SignalBatteryGlyph(side * .72f, battery ?: 0, charging)
                Spacer(Modifier.width(side * .010f))
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Commander settings",
                    tint = SignalWhite.copy(alpha = .72f),
                    modifier = Modifier.size(side * .022f).clickable {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                )
            }
        }

        Row(
            Modifier.offset(x = pad, y = side * .073f).width(contentWidth),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            val clockPattern = if (HomeSettings.timeline12Hour.value) "h:mm a" else "HH:mm"
            Text(now.format(DateTimeFormatter.ofPattern(clockPattern)).uppercase(), color = SignalWhite, fontFamily = SignalMono, fontSize = (side.value * .029f).sp)
            if (HomeSettings.showRemaining.value) {
                Text("−${remaining.toHours()}h ${remaining.toMinutes() % 60}m", color = SignalWhite, fontFamily = SignalMono, fontSize = (side.value * .027f).sp)
            }
        }

        DaySignalGrid(
            side = side,
            now = now,
            events = events,
            deadlines = timedTasks,
            modifier = Modifier.offset(x = pad, y = side * .125f).width(contentWidth).height(side * .285f),
        )

        DaySignalWeatherStrip(
            side = side,
            briefing = signalDailyBriefing(now, events, todayTasks),
            modifier = Modifier.offset(x = pad, y = side * .430f).width(contentWidth).height(side * .056f),
        )
        DaySignalRainStrip(
            side = side,
            forecast = rainForecast,
            accent = accent,
            modifier = Modifier.offset(x = pad, y = side * .505f).width(contentWidth).height(side * .121f),
        )
        if (HomeSettings.commandDeck.value) {
            DaySignalCommandDeck(
                side = side, accent = accent, event = nextEvent, taskCount = todayTasks.size,
                modifier = Modifier.offset(y = side * .638f).fillMaxWidth().height(side * .230f),
                onEvent = onCard, onTasks = onTask, onTrigger = onTrigger,
            )
        } else {
            DaySignalTwoCards(
                side = side,
                accent = accent,
                event = nextEvent,
                taskCount = todayTasks.size,
                modifier = Modifier.offset(x = pad, y = side * .638f).width(contentWidth).height(side * .230f),
                onEvent = onCard,
                onTasks = onTask,
            )
        }

        Box(Modifier.offset(x = pad, y = side * .890f).width(contentWidth).height(side * .001f).background(SignalGrey.copy(alpha = .48f)))
        DaySignalShortcuts(side, Modifier.offset(x = pad, y = side * .920f).width(contentWidth).height(side * .072f), onTrigger)
    }
}

@Composable
internal fun SignalWallpaper(context: Context, modifier: Modifier = Modifier, drawSystemCopy: Boolean = true) {
    val mode = HomeSettings.wallpaperMode.value
    val customUri = HomeSettings.customWallpaperUri.value
    val glassEnabled = HomeSettings.wallpaperGlassEnabled.value
    val glassRevision = HomeSettings.wallpaperGlassRevision.intValue
    var systemWallpaperRevision by remember { mutableIntStateOf(0) }
    LaunchedEffect(mode, customUri, systemWallpaperRevision, HomeSettings.wallpaperAccent.value) {
        if (HomeSettings.wallpaperAccent.value && mode != HomeWallpaperMode.Black) {
            HomeSettings.applyWallpaperAccent(context)
        }
    }
    LaunchedEffect(context) {
        if (HomeSettings.consumeExternalSystemWallpaperChange(context)) {
            HomeSettings.setWallpaperMode(context, HomeWallpaperMode.System)
            (context as? MainActivity)?.configureLauncherWindowBackground()
        }
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                systemWallpaperRevision++
                if (HomeSettings.consumeExternalSystemWallpaperChange(context)) {
                    HomeSettings.setWallpaperMode(context, HomeWallpaperMode.System)
                    (context as? MainActivity)?.configureLauncherWindowBackground()
                }
                if (HomeSettings.wallpaperAccent.value) HomeSettings.applyWallpaperAccent(context)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED))
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    val bitmap = remember(mode, customUri, systemWallpaperRevision, drawSystemCopy, glassEnabled, glassRevision) {
        runCatching {
            when (mode) {
                HomeWallpaperMode.Black -> null
                HomeWallpaperMode.System -> if (drawSystemCopy) {
                    WallpaperManager.getInstance(context).drawable?.let { drawable ->
                        val sourceWidth = drawable.intrinsicWidth.coerceAtLeast(1)
                        val sourceHeight = drawable.intrinsicHeight.coerceAtLeast(1)
                        val displayLongEdge = maxOf(
                            context.resources.displayMetrics.widthPixels,
                            context.resources.displayMetrics.heightPixels,
                        ).coerceAtLeast(1)
                        val targetLongEdge = displayLongEdge * 1.25f
                        val scale = minOf(targetLongEdge / maxOf(sourceWidth, sourceHeight), 1f)
                        drawable.toBitmap(
                            (sourceWidth * scale).toInt().coerceAtLeast(1),
                            (sourceHeight * scale).toInt().coerceAtLeast(1),
                        )
                    }
                } else null
                HomeWallpaperMode.Custom -> customUri?.let { saved ->
                    val glassFile = HomeSettings.glassWallpaperFile(context)
                    val source = if (glassEnabled && glassFile.exists()) {
                        ImageDecoder.createSource(glassFile)
                    } else ImageDecoder.createSource(context.contentResolver, Uri.parse(saved))
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        val width = info.size.width.coerceAtLeast(1)
                        val height = info.size.height.coerceAtLeast(1)
                        // A landscape source could previously remain at 5K+ because the
                        // shorter edge controlled sampling. Size against the display's
                        // long edge instead: still sharp through a 2x zoom, without
                        // permanently retaining a huge GPU texture on the launcher.
                        val displayLongEdge = maxOf(
                            context.resources.displayMetrics.widthPixels,
                            context.resources.displayMetrics.heightPixels,
                        ).coerceAtLeast(1)
                        // 1.25x leaves headroom for the normal pan/zoom range
                        // without retaining a 4K/5K texture on a phone screen.
                        val targetLongEdge = displayLongEdge * 1.25f
                        val scale = minOf(targetLongEdge / maxOf(width, height), 1f)
                        decoder.setTargetSize((width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1))
                    }
                }
            }
        }.getOrNull()
    }
    if (bitmap != null) {
        Box(modifier.clipToBounds()) {
            val wallpaperBlur = HomeSettings.wallpaperBlur.floatValue
            Image(
                bitmap = bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = HomeSettings.wallpaperZoom.floatValue
                    scaleY = HomeSettings.wallpaperZoom.floatValue
                    translationX = HomeSettings.wallpaperPanX.floatValue * size.width * .35f
                    translationY = HomeSettings.wallpaperPanY.floatValue * size.height * .35f
                }.then(
                    if (wallpaperBlur > .1f) Modifier.blur(wallpaperBlur.dp) else Modifier,
                ),
            )
        }
    }
}

@Composable
private fun DaySignalGrid(
    side: Dp,
    now: LocalDateTime,
    events: List<SignalEvent>,
    deadlines: List<TodoistTask>,
    minuteStep: Int = 15,
    isActiveSurface: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val gridInset = side * .047f
    val gridWidthFraction = .953f
    val accent = AccentSettings.color.value
    val accentDeep = lerp(accent, Color.Black, .42f)
    val accentMid = lerp(accent, Color.White, .16f)
    val accentLight = lerp(accent, Color.White, .58f)
    val glowStrength = HomeSettings.timelineGlow.floatValue
    // Keep the timeline glow optically stable. Pulsing three full-width,
    // blurred grid passes forced their textures to be regenerated every
    // frame; the current slot and elapsed fade already provide sufficient
    // focus without that continuous cost.
    val focusPulseState = rememberUpdatedState(1f)
    val rows = 60 / minuteStep
    val totalSlots = 24 * rows
    val currentQuarter = (now.hour * rows + now.minute / minuteStep).coerceIn(0, totalSlots - 1)
    val eventQuarters = remember(events) {
        events.filterNot { it.allDay }.flatMap { event ->
            val start = (event.start.hour * rows + event.start.minute / minuteStep).coerceIn(0, totalSlots - 1)
            val end = (event.end.hour * rows + (event.end.minute + minuteStep - 1) / minuteStep)
                .coerceIn(start + 1, totalSlots)
            (start until end).toList()
        }.toSet()
    }
    val dueQuarters = remember(deadlines) {
        deadlines.mapNotNull { it.signalDateTime() }
            .map { (it.hour * rows + it.minute / minuteStep).coerceIn(0, totalSlots - 1) }.toSet()
    }
    val showUpcomingLabels = HomeSettings.timelineUpcomingLabels.value
    val upcomingDeadlines = remember(deadlines, now.toLocalDate(), showUpcomingLabels) {
        if (!showUpcomingLabels) emptyList() else deadlines.mapNotNull { it.signalDateTime() }
            .filter { due -> !due.isBefore(now) }
            .distinct()
            .sorted()
            .take(2)
    }
    BoxWithConstraints(modifier) {
        // The three grid rows are vertically centred inside the canvas below,
        // so its first visible row begins well after the canvas boundary.
        // End this label rail immediately above that real first-row position.
        BoxWithConstraints(Modifier.offset(x = gridInset, y = side * .029f).fillMaxWidth(gridWidthFraction).height(side * .034f)) {
            val twelveHour = HomeSettings.timeline12Hour.value
            val mainWidth = side * .040f
            val minorWidth = side * .025f
            fun hourX(hour: Int, width: Dp): Dp = mainWidth / 2 + (maxWidth - mainWidth) * (hour / 24f) - width / 2
            listOf(0, 6, 12, 18, 24).forEach { hour ->
                    val label = when {
                        !twelveHour -> hour.toString().padStart(2, '0')
                        hour == 0 -> "00"
                        else -> (hour % 12).let { if (it == 0) "12" else it.toString().padStart(2, '0') }
                    }
                    Box(
                        Modifier.offset(x = hourX(hour, mainWidth)).width(mainWidth).fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Text(label, color = SignalGrey, fontFamily = SignalMono,
                            fontSize = (side.value * .0095f).sp, maxLines = 1,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
            }
            if (HomeSettings.timelineHourlyLabels.value) (1 until 24).filter { it % 6 != 0 }.forEach { hour ->
                val label = when {
                    !twelveHour -> hour.toString().padStart(2, '0')
                    else -> (hour % 12).let { if (it == 0) "12" else it.toString().padStart(2, '0') }
                }
                Box(
                    Modifier.offset(x = hourX(hour, minorWidth)).width(minorWidth).fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        label,
                        color = SignalGrey.copy(alpha = .34f),
                        fontFamily = SignalMono,
                        fontSize = (side.value * .0068f).sp,
                        maxLines = 1,
                    )
                }
            }
        }
        val gridCanvasHeight = side * .155f
        val gridDpWidth = maxWidth * gridWidthFraction
        val gridGap = gridDpWidth * .0105f
        val gridCell = (gridDpWidth - gridGap * 23f) / 24f
        val gridSpan = gridCell * rows + gridGap * (rows - 1)
        val gridTop = (gridCanvasHeight - gridSpan) / 2f
        val gutterLineHeight = side * .0095f
        Column(
            Modifier.offset(y = side * .043f + gridTop - gutterLineHeight / 2f)
                .height(gridSpan + gutterLineHeight),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
        ) {
            val boundaryLabels = if (minuteStep == 20) listOf(0, 20, 40, 60)
                else (0 until 60 step minuteStep).toList()
            boundaryLabels.forEach { minute ->
                Text(
                    minute.toString().padStart(2, '0'),
                    color = SignalGrey.copy(alpha = .68f),
                    fontFamily = SignalMono,
                    fontSize = (side.value * .0095f).sp,
                    lineHeight = (side.value * .0095f).sp,
                )
            }
        }
        Canvas(Modifier.offset(x = gridInset, y = side * .043f).fillMaxWidth(gridWidthFraction).height(side * .155f)) {
            val gapX = size.width * .0105f
            val gapY = gapX
            val cell = (size.width - gapX * 23f) / 24f
            val gridTop = (size.height - (cell * rows + gapY * (rows - 1))) / 2f
            val currentCol = currentQuarter / rows
            val currentRow = currentQuarter % rows
            for (quarter in 0 until totalSlots) {
                val col = quarter / rows
                val row = quarter % rows
                val topLeft = Offset(col * (cell + gapX), gridTop + row * (cell + gapY))
                val elapsed = quarter < currentQuarter
                val dx = (col - currentCol).toFloat()
                val dy = (row - currentRow).toFloat()
                val localLight = kotlin.math.exp(-(dx * dx / 14f + dy * dy / 3.5f))
                val color = when {
                    quarter == currentQuarter -> SignalWhite
                    elapsed -> lerp(Color(0xFF303439), Color(0xFFD8D9D6), .10f + localLight * .48f)
                    quarter in dueQuarters -> accentLight
                    quarter in eventQuarters -> accentMid
                    else -> Color(0xFF171C21)
                }
                val wallpaperActive = HomeSettings.wallpaperMode.value != HomeWallpaperMode.Black
                val scheduled = quarter >= currentQuarter && (quarter in eventQuarters || quarter in dueQuarters)
                val radius = androidx.compose.ui.geometry.CornerRadius(side.value * .0015f)
                when {
                    quarter == currentQuarter -> {
                        drawRoundRect(color.copy(alpha = .96f), topLeft, Size(cell, cell), radius)
                        drawRoundRect(Color.White.copy(alpha = .92f), topLeft, Size(cell, cell), radius, style = Stroke(side.value * .0012f))
                    }
                    elapsed -> {
                        val hoursAgo = (currentQuarter - quarter).coerceAtLeast(1) / rows.toFloat()
                        val fade = kotlin.math.exp(-hoursAgo / 3.6f)
                        val breath = .82f + focusPulseState.value * .18f
                        val fill = if (wallpaperActive) .008f + .22f * fade else .018f + .30f * fade
                        val outline = if (wallpaperActive) .012f + .36f * fade else .025f + .36f * fade
                        drawRoundRect(Color.White.copy(alpha = fill * breath), topLeft, Size(cell, cell), radius)
                        drawRoundRect(Color.White.copy(alpha = outline * breath), topLeft, Size(cell, cell), radius, style = Stroke(side.value * .0008f))
                    }
                    scheduled -> {
                        drawRoundRect(color.copy(alpha = if (wallpaperActive) .17f else .25f), topLeft, Size(cell, cell), radius)
                        drawRoundRect(color.copy(alpha = .86f), topLeft, Size(cell, cell), radius, style = Stroke(side.value * .00115f))
                    }
                    else -> drawRoundRect(
                        Color.White.copy(alpha = if (wallpaperActive) .18f else .15f),
                        topLeft, Size(cell, cell), radius,
                        style = Stroke(side.value * .00075f),
                    )
                }
            }
        }
        // A separate soft pass gives elapsed time a real optical fade rather
        // than merely lowering the opacity of otherwise crisp squares.
        Canvas(Modifier.offset(x = gridInset, y = side * .043f)
            .fillMaxWidth(gridWidthFraction)
            .height(side * .155f)
            .blur(side * .0065f, BlurredEdgeTreatment.Unbounded)) {
            val gapX = size.width * .0105f
            val gapY = gapX
            val cell = (size.width - gapX * 23f) / 24f
            val gridTop = (size.height - (cell * rows + gapY * (rows - 1))) / 2f
            val wallpaperFactor = if (HomeSettings.wallpaperMode.value != HomeWallpaperMode.Black) .82f else 1f
            for (quarter in 0 until currentQuarter) {
                val col = quarter / rows
                val row = quarter % rows
                val topLeft = Offset(col * (cell + gapX), gridTop + row * (cell + gapY))
                val hoursAgo = (currentQuarter - quarter).coerceAtLeast(1) / rows.toFloat()
                val fade = kotlin.math.exp(-hoursAgo / 4.2f)
                val pulse = .76f + focusPulseState.value * .24f
                drawRoundRect(
                    color = Color.White.copy(alpha = .24f * fade * pulse * wallpaperFactor),
                    topLeft = topLeft,
                    size = Size(cell, cell),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(side.value * .003f),
                )
            }
        }
        Canvas(Modifier.offset(x = gridInset, y = side * .043f).fillMaxWidth(gridWidthFraction).height(side * .155f).blur(side * .014f, BlurredEdgeTreatment.Unbounded)) {
            val gapX = size.width * .0105f
            val gapY = gapX
            val cell = (size.width - gapX * 23f) / 24f
            val gridTop = (size.height - (cell * rows + gapY * (rows - 1))) / 2f
            val wallpaperFactor = if (HomeSettings.wallpaperMode.value != HomeWallpaperMode.Black) .72f else 1f
            val currentCol = currentQuarter / rows
            val currentRow = currentQuarter % rows
            for (quarter in 0 until totalSlots) {
                val col = quarter / rows
                val row = quarter % rows
                val topLeft = Offset(col * (cell + gapX), gridTop + row * (cell + gapY))
                val dx = (col - currentCol).toFloat()
                val dy = (row - currentRow).toFloat()
                val localLight = kotlin.math.exp(-(dx * dx / 9f + dy * dy / 2.5f))
                val scheduled = quarter >= currentQuarter && (quarter in eventQuarters || quarter in dueQuarters)
                if (localLight > .025f || scheduled) {
                    val glowColor = if (scheduled) accentLight else SignalWhite
                    val elapsed = quarter < currentQuarter
                    val elapsedBreath = if (elapsed) (.72f + focusPulseState.value * .28f) else 1f
                    val trail = if (elapsed) kotlin.math.exp(-((currentQuarter - quarter) / rows.toFloat()) / 2.8f) else 0f
                    val alpha = ((if (quarter == currentQuarter) .92f * focusPulseState.value else
                        (localLight * .46f + trail * .14f) * elapsedBreath + if (scheduled) .34f else 0f) *
                        glowStrength * wallpaperFactor).coerceAtMost(1f)
                    drawRoundRect(glowColor.copy(alpha = alpha), topLeft, Size(cell, cell), androidx.compose.ui.geometry.CornerRadius(side.value * .004f))
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val insetPx = gridInset.toPx()
            val gridPx = size.width * gridWidthFraction
            val currentX = insetPx + gridPx * ((currentQuarter + .5f) / totalSlots.toFloat())
            val gridBottom = side.value * .181f
            val markerY = side.value * .194f
            drawLine(SignalWhite.copy(alpha = .78f), Offset(currentX, gridBottom), Offset(currentX, markerY), side.value * .0015f)
            upcomingDeadlines.forEach { due ->
                    val x = insetPx + gridPx * ((due.hour + due.minute / 60f) / 24f)
                    val y = markerY
                    val isPast = due.isBefore(now)
                    val taskColor = if (isPast) Color(0xFF777A7D) else accent
                    val taskHot = if (isPast) Color(0xFFB0B2B2) else accentLight
                    drawCircle(taskColor.copy(alpha = if (isPast) .10f else .25f), side.value * .020f, Offset(x, y))
                    drawCircle(taskHot.copy(alpha = if (isPast) .52f else 1f), side.value * .0068f, Offset(x, y))
                    drawCircle(Color.White, side.value * .0032f, Offset(x, y))
                    val ray = side.value * .011f
                    drawLine(taskHot.copy(alpha = if (isPast) .28f else .74f), Offset(x - ray, y), Offset(x + ray, y), side.value * .0011f)
                    drawLine(taskHot.copy(alpha = if (isPast) .28f else .74f), Offset(x, y - ray), Offset(x, y + ray), side.value * .0011f)
            }
        }
        val trackWidth = maxWidth * gridWidthFraction
        val nowLabelWidth = side * .132f
        val currentCenterX = gridInset + trackWidth * ((currentQuarter + .5f) / totalSlots.toFloat())
        val currentLabelX = (currentCenterX - nowLabelWidth / 2).coerceIn(0.dp, maxWidth - nowLabelWidth)
        Text("NOW", color = SignalWhite, fontFamily = SignalMono, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = (side.value * .014f).sp, modifier = Modifier.offset(x = currentLabelX, y = side * .181f).width(nowLabelWidth))
        val nowPattern = if (HomeSettings.timeline12Hour.value) "h:mm a" else "HH:mm"
        Text(
            now.format(DateTimeFormatter.ofPattern(nowPattern)),
            color = SignalWhite,
            fontFamily = SignalMono,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = (side.value * .014f).sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.offset(x = currentLabelX, y = side * .201f).width(nowLabelWidth),
        )
        val eventLabelWidth = side * .068f
        val deadlineLabelXs = upcomingDeadlines.map { due ->
            val center = gridInset + trackWidth * ((due.hour * 60 + due.minute) / 1440f)
            (center - eventLabelWidth / 2).coerceIn(0.dp, maxWidth - eventLabelWidth)
        }.toMutableList().also { positions ->
            if (positions.size == 2 && positions[1] - positions[0] < eventLabelWidth * 1.05f) {
                val middle = (positions[0] + positions[1]) / 2
                positions[0] = (middle - eventLabelWidth * .525f).coerceAtLeast(0.dp)
                positions[1] = (middle + eventLabelWidth * .525f).coerceAtMost(maxWidth - eventLabelWidth)
            }
        }
        upcomingDeadlines.forEachIndexed { index, due ->
                val timePattern = if (HomeSettings.timeline12Hour.value) "h:mm" else "HH:mm"
                Text(
                    due.format(DateTimeFormatter.ofPattern(timePattern)),
                    color = SignalWhite.copy(alpha = .82f),
                    fontFamily = SignalMono,
                    fontSize = (side.value * .0115f).sp,
                    modifier = Modifier.offset(
                        x = deadlineLabelXs[index],
                        y = side * .240f,
                    ).width(eventLabelWidth),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
        }
    }
}

@Composable
private fun DaySignalWeatherStrip(side: Dp, briefing: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = side * .006f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("22°", color = SignalWhite, fontFamily = SignalMono, fontSize = (side.value * .027f).sp)
            Spacer(Modifier.width(side * .010f))
            Icon(Icons.Outlined.CloudQueue, contentDescription = "Partly cloudy skies", tint = SignalWhite.copy(alpha = .82f), modifier = Modifier.size(side * .023f))
            Spacer(Modifier.width(side * .018f))
            Text(
                briefing,
                color = SignalWhite.copy(alpha = .94f),
                fontFamily = SignalSans,
                fontWeight = FontWeight.Medium,
                fontSize = (side.value * .0165f).sp,
                lineHeight = (side.value * .020f).sp,
                letterSpacing = (-side.value * .0002f).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(side * .014f))
            Text("H 24°   L 16°", color = Color(0xFFA9C1D2), fontFamily = SignalMono, fontSize = (side.value * .0145f).sp)
        }
    }
}

@Composable
private fun DaySignalRainStrip(side: Dp, forecast: RainForecast, accent: Color, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        var weekly by remember { mutableStateOf(true) }
        val shown = if (weekly) forecast.weekly else forecast.hourly
        val labelSize = (side.value * .0095f).sp
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("RAIN CHANCE", color = SignalGrey.copy(alpha = .82f), fontFamily = SignalMono, fontSize = labelSize)
            Text(
                if (shown.isEmpty()) "FORECAST UNAVAILABLE" else if (weekly) "7 DAY FORECAST  ‹" else "NEXT 12 HOURS  ›",
                color = SignalGrey.copy(alpha = .62f), fontFamily = SignalMono, fontSize = labelSize,
                modifier = Modifier.clickable(enabled = shown.isNotEmpty()) { weekly = !weekly }
                    .padding(start = side * .018f, bottom = side * .008f),
            )
        }

        if (shown.isEmpty()) {
            Box(
                Modifier.offset(y = side * .042f).fillMaxWidth().height(side * .001f)
                    .background(SignalGrey.copy(alpha = .22f)),
            )
            return@BoxWithConstraints
        }

        // One continuous luminous trace reads cleanly over wallpaper. Text has
        // dedicated lanes above and below it, so labels can never collide.
        Canvas(Modifier.offset(y = side * .025f).fillMaxWidth().height(side * .058f)) {
            val inset = size.width * .018f
            val usableWidth = size.width - inset * 2
            val top = size.height * .16f
            val bottom = size.height * .88f
            fun pointAt(index: Int): Offset {
                val chance = shown[index].chance.coerceIn(0, 100) / 100f
                return Offset(
                    inset + usableWidth * index / (shown.size - 1).coerceAtLeast(1),
                    bottom - (bottom - top) * chance,
                )
            }
            val path = Path().apply {
                shown.indices.forEach { index ->
                    val p = pointAt(index)
                    if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
            }
            drawLine(SignalGrey.copy(alpha = .18f), Offset(inset, bottom), Offset(size.width - inset, bottom), 1f)
            drawPath(path, accent.copy(alpha = .12f), style = Stroke(width = size.height * .22f))
            drawPath(path, accent.copy(alpha = .28f), style = Stroke(width = size.height * .10f))
            drawPath(path, lerp(accent, Color.White, .78f).copy(alpha = .96f), style = Stroke(width = size.height * .025f))
            shown.indices.forEach { index ->
                val p = pointAt(index)
                drawCircle(accent.copy(alpha = .22f), size.height * .10f, p)
                drawCircle(lerp(accent, Color.White, .82f), size.height * .040f, p)
            }
        }

        val columnWidth = maxWidth / shown.size
        shown.forEachIndexed { index, point ->
            Box(Modifier.offset(x = columnWidth * index, y = side * .073f).width(columnWidth)) {
                Text(
                    "${point.chance}%",
                    color = SignalWhite.copy(alpha = .92f), fontFamily = SignalMono,
                    fontSize = (side.value * .0105f).sp, maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    point.hour,
                    color = SignalGrey.copy(alpha = .66f), fontFamily = SignalMono,
                    fontSize = (side.value * .0075f).sp, maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.offset(y = side * .017f).fillMaxWidth(),
                )
            }
        }
    }
}

private data class DeckEntry(
    val label: String,
    val detail: String,
    val symbol: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: () -> Unit,
)

private fun cardRole(label: String): String = when (label.lowercase()) {
    "messages" -> "MESSAGING"
    "email" -> "MAIL"
    "calendar" -> "SCHEDULE"
    "tasks" -> "TASKS"
    "notes" -> "CAPTURE"
    "calls" -> "CONTACTS"
    "timer" -> "TIME"
    "important" -> "PRIORITY"
    "media" -> "NOW PLAYING"
    "ai" -> "AGENT"
    "weather" -> "FORECAST"
    "controls" -> "QUICK CONTROL"
    else -> ""
}

private fun swipeActionTag(context: Context, action: String?): String? = when {
    action == null -> null
    action == "tap" -> "TAP"
    action == "hub" -> "HUB"
    action == "apps" -> "APPS"
    action == "settings" -> "SET"
    action == "wallpaper" -> "WALL"
    action.startsWith("trigger:") -> "BAR"
    action.startsWith("input:") -> "TEXT"
    action.startsWith("app:") -> runCatching {
        val packageName = action.removePrefix("app:")
        context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(packageName, 0),
        ).toString().uppercase()
    }.getOrDefault("APP")
    else -> null
}

@Composable
private fun ReferenceCarouselHome(
    side: Dp,
    now: LocalDateTime,
    battery: Int?,
    charging: Boolean,
    accent: Color,
    isActiveSurface: Boolean,
    events: List<SignalEvent>,
    deadlines: List<TodoistTask>,
    event: SignalEvent?,
    taskCount: Int,
    hubCount: Int,
    flaggedCount: Int,
    importantCount: Int,
    weather: RainForecast,
    sharedWallpaper: Boolean,
    onEvent: () -> Unit,
    onTasks: () -> Unit,
    onTrigger: (Char) -> Unit,
    onOpenHub: (() -> Unit)?,
    onOpenApps: (() -> Unit)?,
    onBeforeExternalLaunch: () -> Unit,
    keySequence: Int,
    keyDirection: Int,
    stackKeySequence: Int,
    stackKeyDirection: Int,
    spaceSequence: Int,
    actionSequence: Int,
) {
    val context = LocalContext.current
    // Only the selected module is live. Rear cards keep their last rendered
    // content, avoiding timers, marquees and media polling around the entire
    // ring while preserving exactly the same appearance.
    var activeCardId by remember {
        mutableStateOf(CarouselCardSettings.home(context))
    }
    var cardSettingsRevision by remember { mutableIntStateOf(0) }
    var editingCard by remember { mutableStateOf<DeckEntry?>(null) }
    var cardEditorPage by remember { mutableStateOf("menu") }
    var cardActionTarget by remember { mutableStateOf("tap") }
    var cardEditorQuery by remember { mutableStateOf("") }
    var cardActionTextDraft by remember { mutableStateOf("") }
    var addTargetStackId by remember { mutableStateOf<String?>(null) }
    var newCardName by remember { mutableStateOf("") }
    var cardTitleDraft by remember { mutableStateOf("") }
    var cardDetailDraft by remember { mutableStateOf("") }
    var cardTopDraft by remember { mutableStateOf("") }
    var nowPlaying by remember { mutableStateOf<NowPlaying?>(null) }
    LaunchedEffect(context, isActiveSurface, activeCardId) {
        if (!isActiveSurface || activeCardId != CarouselCardSettings.id("Media")) return@LaunchedEffect
        while (isActive) {
            nowPlaying = withContext(Dispatchers.IO) { context.activeNowPlaying() }
            delay(2_500)
        }
    }
    val mediaArtwork = remember(nowPlaying?.artwork) { nowPlaying?.artwork?.asImageBitmap() }
    val hubItems = HubRepository.items
    val categoryOverrides = remember(hubItems) {
        context.getSharedPreferences("hub_categories", android.content.Context.MODE_PRIVATE).all
            .mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap()
    }
    val messageItems = remember(hubItems, categoryOverrides) {
        hubItems.filter { !it.isSummary && it.effectiveCategory(categoryOverrides) == "messages" }
            .sortedByDescending(HubItem::time)
    }
    val emailItems = remember(hubItems, categoryOverrides) {
        hubItems.filter { !it.isSummary && it.effectiveCategory(categoryOverrides) == "email" }
            .sortedByDescending(HubItem::time)
    }
    var messageIndex by remember { mutableIntStateOf(0) }
    var emailIndex by remember { mutableIntStateOf(0) }
    val messageKeys = remember(messageItems) { messageItems.map(HubItem::key) }
    val emailKeys = remember(emailItems) { emailItems.map(HubItem::key) }
    LaunchedEffect(isActiveSurface, messageKeys, activeCardId) {
        messageIndex = 0
        if (!isActiveSurface || activeCardId != CarouselCardSettings.id("Messages")) return@LaunchedEffect
        while (isActive) {
            delay(6_500)
            if (messageItems.size > 1) messageIndex = (messageIndex + 1) % messageItems.size
        }
    }
    LaunchedEffect(isActiveSurface, emailKeys, activeCardId) {
        emailIndex = 0
        if (!isActiveSurface || activeCardId != CarouselCardSettings.id("Email")) return@LaunchedEffect
        // Keep independent live modules from visibly refreshing as one batch.
        delay(2_350)
        while (isActive) {
            if (emailItems.size > 1) emailIndex = (emailIndex + 1) % emailItems.size
            delay(7_900)
        }
    }
    val currentMessage = messageItems.getOrNull(messageIndex % messageItems.size.coerceAtLeast(1))
    val currentEmail = emailItems.getOrNull(emailIndex % emailItems.size.coerceAtLeast(1))
    fun preview(item: HubItem?, index: Int, count: Int, empty: String): String {
        if (item == null) return empty
        val body = listOf(item.title, item.text).filter(String::isNotBlank).joinToString(": ")
        return if (count > 1) "${index + 1}/$count · $body" else body.ifBlank { empty }
    }
    fun openNotificationOrApp(item: HubItem?, fallback: () -> Unit) {
        if (item == null) { fallback(); return }
        val opened = runCatching { item.contentIntent?.send(); item.contentIntent != null }.getOrDefault(false)
        if (!opened) {
            val intent = context.packageManager.getLaunchIntentForPackage(item.packageName)
            if (intent != null) context.startActivity(intent) else fallback()
        }
    }
    val messageSummary = preview(currentMessage, messageIndex, messageItems.size, "Open conversations")
    val emailSummary = preview(currentEmail, emailIndex, emailItems.size, "Open email")
    val latestNote = RecentNoteStore.latest(context)?.takeIf(String::isNotBlank) ?: "Quick capture"
    val missedCalls = HubRepository.items.count {
        it.category == android.app.Notification.CATEGORY_CALL ||
            it.title.contains("missed call", ignoreCase = true)
    }
    val connectivity = rememberLiveConnectivity(context, isActiveSurface)
    val ringerMode = rememberLiveRingerMode(context, isActiveSurface)
    val caffeineActive = "moe.zhs.caffeine" in HubRepository.ongoingPackages
    val remaining = remember(now.minute) {
        Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay()).coerceAtLeast(Duration.ZERO)
    }
    val glyphStyle = HomeSettings.glyphStyle.value
    fun glyph(
        materialOutlined: androidx.compose.ui.graphics.vector.ImageVector,
        materialFilled: androidx.compose.ui.graphics.vector.ImageVector,
        tabler: androidx.compose.ui.graphics.vector.ImageVector,
        remix: androidx.compose.ui.graphics.vector.ImageVector,
        lucide: androidx.compose.ui.graphics.vector.ImageVector,
        phosphor: androidx.compose.ui.graphics.vector.ImageVector,
        iconoir: androidx.compose.ui.graphics.vector.ImageVector,
        hugeicons: androidx.compose.ui.graphics.vector.ImageVector,
    ) = when (glyphStyle) {
        HomeGlyphStyle.MaterialOutlined -> materialOutlined
        HomeGlyphStyle.MaterialFilled -> materialFilled
        HomeGlyphStyle.Tabler -> tabler
        HomeGlyphStyle.Remix -> remix
        HomeGlyphStyle.Lucide -> lucide
        HomeGlyphStyle.Phosphor -> phosphor
        HomeGlyphStyle.Iconoir -> iconoir
        HomeGlyphStyle.Hugeicons -> hugeicons
    }
    val builtInEntries = listOf(
        DeckEntry("Messages", messageSummary, commandTrigger(CommandKind.Message), glyph(Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble, TablerIcons.MessageDots, Remix.Communication.Message2Line, LucideIcons.MessageSquareMore, PhosphorIcons.Duotone.ChatDots, IconoirCommanderGlyphs.Message, HugeCommanderGlyphs.Message)) { onTrigger('@') },
        DeckEntry("Email", emailSummary, "E", glyph(Icons.Outlined.Email, Icons.Filled.Email, TablerIcons.Mail, Remix.Business.MailLine, LucideIcons.Mail, PhosphorIcons.Duotone.Envelope, IconoirCommanderGlyphs.Mail, HugeCommanderGlyphs.Mail)) {
            val launch = currentEmail?.packageName?.let(context.packageManager::getLaunchIntentForPackage)
                ?: listOf("com.google.android.gm", "com.microsoft.office.outlook", "ch.protonmail.android", "eu.faircode.email")
                    .firstNotNullOfOrNull(context.packageManager::getLaunchIntentForPackage)
            if (launch != null) {
                onBeforeExternalLaunch()
                context.startActivity(launch)
            } else {
                (onOpenApps ?: { context.openCommanderSurface(AppsActivity::class.java) })()
            }
        },
        DeckEntry("Calendar", event?.title ?: "Upcoming events", "*", glyph(Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth, TablerIcons.CalendarEvent, Remix.Business.CalendarEventLine, LucideIcons.CalendarDays, PhosphorIcons.Duotone.CalendarDots, IconoirCommanderGlyphs.Calendar, HugeCommanderGlyphs.Calendar), onEvent),
        DeckEntry("Tasks", "$taskCount due today", "−", glyph(Icons.Outlined.CheckCircleOutline, Icons.Filled.TaskAlt, TablerIcons.ClipboardCheck, Remix.Document.TaskLine, LucideIcons.ClipboardCheck, PhosphorIcons.Duotone.ClipboardText, IconoirCommanderGlyphs.Tasks, HugeCommanderGlyphs.Tasks), onTasks),
        DeckEntry("Notes", latestNote, commandTrigger(CommandKind.Note), glyph(Icons.Outlined.Description, Icons.Filled.NoteAdd, TablerIcons.Notes, Remix.Document.StickyNoteLine, LucideIcons.NotebookPen, PhosphorIcons.Duotone.NotePencil, IconoirCommanderGlyphs.Notes, HugeCommanderGlyphs.Notes)) { onTrigger(TriggerSettings.noteSymbol()) },
        DeckEntry("Calls", if (missedCalls > 0) "$missedCalls missed calls" else "Call a contact", commandTrigger(CommandKind.Call), glyph(Icons.Outlined.Call, Icons.Filled.Call, TablerIcons.PhoneCall, Remix.Device.PhoneLine, LucideIcons.PhoneCall, PhosphorIcons.Duotone.PhoneCall, IconoirCommanderGlyphs.Phone, HugeCommanderGlyphs.Phone)) { onTrigger('#') },
        DeckEntry("Timer", "Start a timer or set alarm", commandTrigger(CommandKind.Timer), glyph(Icons.Outlined.Timer, Icons.Filled.Timer, TablerIcons.Clock, Remix.System.TimerLine, LucideIcons.Timer, PhosphorIcons.Duotone.Timer, IconoirCommanderGlyphs.Timer, HugeCommanderGlyphs.Timer)) { onTrigger('+') },
        DeckEntry("Important", if (importantCount > 0) "$importantCount priority alerts" else "Nothing urgent", "!", glyph(Icons.Outlined.PriorityHigh, Icons.Filled.Bolt, TablerIcons.AlertTriangle, Remix.System.AlertLine, LucideIcons.TriangleAlert, PhosphorIcons.Duotone.Warning, IconoirCommanderGlyphs.Alert, HugeCommanderGlyphs.Alert)) {
            (onOpenHub ?: { context.openCommanderSurface(HubActivity::class.java) })()
        },
        DeckEntry("Media", nowPlaying?.let { playback ->
            listOf(playback.title, playback.artist).filter(String::isNotBlank).joinToString(" · ")
        } ?: "Nothing playing", "", glyph(Icons.Outlined.MusicNote, Icons.Filled.MusicNote, TablerIcons.Music, Remix.Media.Music2Line, LucideIcons.Music2, PhosphorIcons.Duotone.MusicNotes, IconoirCommanderGlyphs.Music, HugeCommanderGlyphs.Music)) {
            val launch = nowPlaying?.let { context.packageManager.getLaunchIntentForPackage(it.packageName) }
            if (launch != null) context.startActivity(launch)
            else (onOpenApps ?: { context.openCommanderSurface(AppsActivity::class.java) })()
        },
        DeckEntry("AI", "Ask Commander or agent", commandTrigger(CommandKind.Ask), glyph(Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome, TablerIcons.Atom, Remix.Editor.Ai, LucideIcons.Bot, PhosphorIcons.Duotone.Robot, IconoirCommanderGlyphs.Ai, HugeCommanderGlyphs.Ai)) { onTrigger('?') },
        DeckEntry(
            "Controls",
            "Open Commander controls",
            "⌁",
            Icons.Outlined.Settings,
        ) {
            val target = AliasSettings.targets.first { it.id == "control" }
            val alias = AliasSettings.alias(context, target).split(',').firstOrNull()?.trim().orEmpty()
            context.startActivity(Intent(context, OverlayActivity::class.java).apply {
                action = OverlayActivity.ACTION_OPEN_OVERLAY
                putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, "${TriggerSettings.effectiveAlias(alias)} ")
            })
        },
        DeckEntry(
            "Weather",
            buildString {
                append(weather.temperature?.let { "$it°" } ?: "—")
                append(" · ").append(signalWeatherDescription(weather.weatherCode))
                if (weather.hourly.isNotEmpty()) {
                    append("\nRAIN  ")
                    append(weather.hourly.take(3).mapIndexed { index, point ->
                        val time = if (index == 0) "NOW" else "+${index}H"
                        "$time ${point.chance}%"
                    }.joinToString(" · "))
                }
            },
            "°",
            signalWeatherIcon(weather.weatherCode, glyphStyle),
        ) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather")))
            }
        },
    )
    val customEntries = remember(cardSettingsRevision) {
        val byPackage = AppCatalog.all(context).associateBy(AppCatalogEntry::packageName)
        val appCards = CarouselCardSettings.customPackages(context).mapNotNull { packageName ->
            byPackage[packageName]?.let { app ->
                DeckEntry(
                    label = app.label,
                    detail = "Open ${app.label}",
                    symbol = "APP",
                    icon = signalDockGlyph(app, FavoriteApps.glyph(context, packageName)),
                    action = {
                        context.packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
                            onBeforeExternalLaunch()
                            context.startActivity(launch)
                        }
                    },
                )
            }
        }
        val blankCards = CarouselCardSettings.customCards(context).map { label ->
            DeckEntry(label, "Open Commander Bar", "", Icons.Outlined.Apps) {
                context.startActivity(Intent(context, OverlayActivity::class.java).apply {
                    action = OverlayActivity.ACTION_OPEN_OVERLAY
                    putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, "")
                })
            }
        }
        appCards + blankCards
    }
    val customPackageByCardId = remember(customEntries, cardSettingsRevision) {
        val byPackage = AppCatalog.all(context).associateBy(AppCatalogEntry::packageName)
        CarouselCardSettings.customPackages(context).mapNotNull { packageName ->
            byPackage[packageName]?.let { app -> CarouselCardSettings.id(app.label) to packageName }
        }.toMap()
    }
    fun runConfiguredAction(encoded: String, fallback: () -> Unit) {
        when {
            encoded == "tap" -> fallback()
            encoded == "hub" -> (onOpenHub ?: { context.openCommanderSurface(HubActivity::class.java) })()
            encoded == "apps" -> (onOpenApps ?: { context.openCommanderSurface(AppsActivity::class.java) })()
            encoded == "settings" -> context.startActivity(Intent(context, SettingsActivity::class.java))
            encoded == "wallpaper" -> context.startActivity(Intent(context, WallpaperPickerActivity::class.java))
            encoded.startsWith("trigger:") -> encoded.removePrefix("trigger:").firstOrNull()?.let(onTrigger)
            encoded.startsWith("input:") -> context.startActivity(Intent(context, OverlayActivity::class.java).apply {
                action = OverlayActivity.ACTION_OPEN_OVERLAY
                putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, encoded.removePrefix("input:"))
            })
            encoded.startsWith("app:") -> {
                context.packageManager.getLaunchIntentForPackage(encoded.removePrefix("app:"))?.let { launch ->
                    onBeforeExternalLaunch()
                    context.startActivity(launch)
                }
            }
        }
    }
    val allEntries = remember(builtInEntries, customEntries, cardSettingsRevision) {
        val available = builtInEntries + customEntries
        val hidden = CarouselCardSettings.hidden(context)
        val order = CarouselCardSettings.order(context)
        val ranked = order.withIndex().associate { it.value to it.index }
        available.filter { CarouselCardSettings.id(it.label) !in hidden }
            .sortedBy { ranked[CarouselCardSettings.id(it.label)] ?: Int.MAX_VALUE }
            .map { entry ->
                val cardId = CarouselCardSettings.id(entry.label)
                val glyphOverride = CarouselCardSettings.glyph(context, cardId)
                val styledEntry = if (glyphOverride == null) entry else entry.copy(
                    icon = packedCommanderGlyph(glyphOverride, glyphStyle, entry.icon),
                )
                val override = CarouselCardSettings.appOverride(context, cardId)
                val tapAction = CarouselCardSettings.tapAction(context, cardId)
                    ?: override?.let { "app:$it" }
                if (tapAction == null) styledEntry else styledEntry.copy(action = {
                    runConfiguredAction(tapAction, styledEntry.action)
                })
            }
            .ifEmpty { builtInEntries.take(1) }
    }
    val visibleIds = allEntries.map { CarouselCardSettings.id(it.label) }.toSet()
    val entries = allEntries.filter { entry ->
        val parent = CarouselCardSettings.stackParent(context, CarouselCardSettings.id(entry.label))
        parent == null || parent !in visibleIds
    }.ifEmpty { allEntries.take(1) }
    val stackCardIndex = remember { mutableStateMapOf<String, Int>() }
    val stackMotion = remember { Animatable(0f) }
    var stackMotionRoot by remember { mutableStateOf<String?>(null) }
    var stackOutgoingCard by remember { mutableStateOf<DeckEntry?>(null) }
    var stackMotionDirection by remember { mutableIntStateOf(1) }
    var stackExpandedRootId by remember { mutableStateOf<String?>(null) }
    var visibleStackRootId by remember { mutableStateOf<String?>(null) }
    val stackReveal = remember { Animatable(0f) }
    var stackMotionJob by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(stackExpandedRootId) {
        if (stackExpandedRootId != null) {
            visibleStackRootId = stackExpandedRootId
            stackReveal.animateTo(1f, tween(430))
        } else if (visibleStackRootId != null) {
            val closingRoot = visibleStackRootId
            stackReveal.animateTo(0f, tween(290))
            // The category is the durable front face of a closed stack.
            // Only reset after the close finishes, so a child never changes
            // identity halfway through the zoom-out animation.
            if (closingRoot != null) {
                stackMotionJob?.cancel()
                stackMotionRoot = null
                stackMotion.snapTo(0f)
                stackCardIndex[closingRoot] = 0
                activeCardId = closingRoot
            }
            visibleStackRootId = null
        }
    }
    var stackPeekRoot by remember { mutableStateOf<String?>(null) }
    var stackPeekTravel by remember { mutableFloatStateOf(0f) }
    val stackScope = rememberCoroutineScope()
    var manualInteractionEpoch by remember { mutableIntStateOf(0) }
    fun stackCards(root: DeckEntry): List<DeckEntry> {
        val rootId = CarouselCardSettings.id(root.label)
        val children = allEntries.filter { child ->
            CarouselCardSettings.stackParent(context, CarouselCardSettings.id(child.label)) == rootId
        }
        // Old per-card swipe shortcuts are retained in settings, but they are
        // not actual members of a category. Turning them into synthetic
        // "TASKS UP" / "CALENDAR DOWN" faces made the new stack feel like a
        // mixture of cards and commands, often with a copied, wrong glyph.
        val members = listOf(root) + children
        val savedOrder = CarouselCardSettings.stackOrder(context, rootId)
        return members.sortedWith(compareBy { member ->
            val position = savedOrder.indexOf(CarouselCardSettings.id(member.label))
            if (position >= 0) position else savedOrder.size + members.indexOf(member)
        })
    }
    fun shownCard(root: DeckEntry): DeckEntry {
        val cards = stackCards(root)
        val rootId = CarouselCardSettings.id(root.label)
        return cards[(stackCardIndex[rootId] ?: 0).mod(cards.size)]
    }
    fun cycleStack(root: DeckEntry, direction: Int) {
        val cards = stackCards(root)
        if (cards.size < 2) return
        val rootId = CarouselCardSettings.id(root.label)
        // Finish the current step before accepting another one; cancelling it
        // midway made the whole vertical track jump back to its origin.
        if (stackMotionJob?.isActive == true) return
        stackMotionJob = stackScope.launch {
            val old = (stackCardIndex[rootId] ?: 0).mod(cards.size)
            val next = (old + direction).mod(cards.size)
            // Keep the physical travel direction tied to the gesture, even
            // in a two-card stack. Both incoming slots are rendered below.
            val visualDirection = direction
            stackMotion.snapTo(0f)
            stackMotionRoot = rootId
            stackMotionDirection = visualDirection
            manualInteractionEpoch++
            stackMotion.animateTo(1f, tween(390,
                easing = CubicBezierEasing(.22f, 1f, .36f, 1f)))
            // The new index is committed only after the physical cards reach
            // their new slots. Their screen positions are identical on both
            // sides of this handoff.
            stackCardIndex[rootId] = next
            activeCardId = CarouselCardSettings.id(cards[next].label)
            stackMotion.snapTo(0f)
            stackMotionRoot = null
        }
    }
    val configuredHomeIndex = entries.indexOfFirst {
        val homeId = CarouselCardSettings.home(context)
        CarouselCardSettings.id(it.label) == homeId ||
            stackCards(it).any { card -> CarouselCardSettings.id(card.label) == homeId }
    }.coerceAtLeast(0)
    val ringPositionState = remember { mutableFloatStateOf(configuredHomeIndex.toFloat()) }
    var composedRingPosition by remember { mutableIntStateOf(configuredHomeIndex) }
    var dragAnchor by remember { mutableFloatStateOf(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val ringInteracting = remember { mutableStateOf(false) }
    val carouselScope = rememberCoroutineScope()
    val idlePhaseState = remember { mutableFloatStateOf(0f) }
    var warmIncomingCard by remember { mutableStateOf(false) }
    var carouselWarmupComplete by remember { mutableStateOf(false) }
    val entrance = remember { Animatable(1f) }
    val entranceSequence = HomeEntranceBridge.sequence.intValue
    LaunchedEffect(entranceSequence) {
        if (isActiveSurface) {
            entrance.snapTo(0f)
            entrance.animateTo(1f, tween(380))
        } else {
            entrance.snapTo(1f)
        }
    }
    val floatingEnabled = HomeSettings.carouselFloating.value
    LaunchedEffect(isActiveSurface, carouselWarmupComplete) {
        if (!isActiveSurface || carouselWarmupComplete) return@LaunchedEffect
        // Prime exactly one incoming live face for a few real display frames.
        // This prepares text/shader/layer paths before the first user input,
        // then disposes the hidden tree so it has no steady-state memory or
        // rendering cost.
        warmIncomingCard = true
        repeat(4) { withFrameNanos { } }
        warmIncomingCard = false
        carouselWarmupComplete = true
    }
    val idleStrengthState = remember { mutableFloatStateOf(if (floatingEnabled) 1f else 0f) }
    LaunchedEffect(isActiveSurface, floatingEnabled) {
        if (!isActiveSurface || !floatingEnabled) {
            idlePhaseState.floatValue = 0f
            idleStrengthState.floatValue = 0f
            return@LaunchedEffect
        }
        val durationNanos = 8_200_000_000L
        val idleUpdateMillis = 32L // Ambient float only; interaction stays full-rate.
        var lastUpdateNanos = android.os.SystemClock.elapsedRealtimeNanos()
        idleStrengthState.floatValue = 1f
        while (isActive) {
            delay(idleUpdateMillis)
            val updateNanos = android.os.SystemClock.elapsedRealtimeNanos()
            val delta = (updateNanos - lastUpdateNanos).coerceAtMost(50_000_000L)
            lastUpdateNanos = updateNanos
            // Freeze the ambient pose during a swipe so it cannot introduce
            // a second angular motion on release.
            if (!ringInteracting.value) {
                idlePhaseState.floatValue =
                    (idlePhaseState.floatValue + delta.toFloat() / durationNanos) % 1f
            }
        }
    }
    // The approved ring was tuned around eleven physical positions. The
    // camera angle, card yaw and neighbour spacing must not change when the
    // user adds another module: extra entries travel through the hidden rear
    // of the same eleven-slot orbit instead of compressing the cylinder.
    val referenceRingSlots = 11f
    val stepRadians = (2f * Math.PI / referenceRingSlots).toFloat()
    val stepProjection = 4f / (5f - kotlin.math.cos(stepRadians))
    // Eleven built-in modules established the approved visual spacing. Keep
    // that exact projected neighbour gap invariant as cards are added or
    // removed; only the cylinder radius changes. A radius cap used here
    // previously caused every card beyond eleven to become progressively
    // cramped.
    // The original eleven-card layout targeted a .346 screen-width projected
    // neighbour step. Preserve that exact approved geometry.
    val referenceGap = .346f
    val ringRadiusFraction = referenceGap / (kotlin.math.sin(stepRadians) * stepProjection)
    val neighbourSpacingFraction = ringRadiusFraction * kotlin.math.sin(stepRadians) * stepProjection
    val cardStepPx = with(LocalDensity.current) { side.toPx() * neighbourSpacingFraction }
    val ringDrag = rememberDraggableState { delta ->
        dragOffset -= delta / cardStepPx
        // A held drag previews at most the adjacent card. Additional travel is
        // earned by release velocity, rather than by finger distance alone.
        ringPositionState.floatValue = dragAnchor + dragOffset.coerceIn(-.92f, .92f)
    }
    fun startRingDrag() {
        stackExpandedRootId = null
        settleJob?.cancel()
        composedRingPosition = kotlin.math.round(ringPositionState.floatValue).toInt()
        ringInteracting.value = true
        dragAnchor = ringPositionState.floatValue
        dragOffset = 0f
    }
    fun stopRingDrag(velocity: Float) {
        settleJob?.cancel()
        settleJob = carouselScope.launch {
            val ringPosition = ringPositionState.floatValue
            val animation = Animatable(ringPosition)
            val ringVelocity = (-velocity / cardStepPx).coerceIn(-28f, 28f)
            val origin = kotlin.math.round(dragAnchor)
            val displacement = ringPosition - origin
            val speed = kotlin.math.abs(ringVelocity)
            val direction = when {
                displacement > .22f -> 1f
                displacement < -.22f -> -1f
                speed >= 4.2f -> kotlin.math.sign(ringVelocity)
                else -> 0f
            }
            val momentumSteps = if (direction == 0f) 0f else when {
                speed >= 27f -> 4f
                speed >= 23f -> 3f
                speed >= 18.5f -> 2f
                else -> 1f
            }
            val target = origin + direction * momentumSteps
            animation.animateTo(
                target,
                spring(dampingRatio = .94f, stiffness = 2_200f),
                initialVelocity = direction * (speed * .06f).coerceAtMost(1.25f),
            ) { ringPositionState.floatValue = value }
            composedRingPosition = kotlin.math.round(target).toInt()
            activeCardId = CarouselCardSettings.id(
                shownCard(entries[kotlin.math.round(target).toInt().mod(entries.size)]).label,
            )
            manualInteractionEpoch++
            dragOffset = 0f
            ringInteracting.value = false
        }
    }
    fun wrappedPosition(value: Float): Float {
        val count = entries.size.toFloat()
        var wrapped = (value + count / 2f) % count
        if (wrapped < 0f) wrapped += count
        return wrapped - count / 2f
    }
    fun centreCard(index: Int, manual: Boolean = true) {
        if (entries[index.mod(entries.size)].let { CarouselCardSettings.id(it.label) } !=
            stackExpandedRootId) stackExpandedRootId = null
        val ringPosition = ringPositionState.floatValue
        val adjustment = wrappedPosition(index - ringPosition)
        val destination = ringPosition + adjustment
        settleJob?.cancel()
        // A new input may interrupt the previous spring between card slots.
        // Rebase composition to the nearest physical slot before continuing;
        // render translations still use the unchanged fractional position, so
        // this is visually continuous while keeping every card inside its
        // allocated transform neighbourhood during repeated fast swipes.
        composedRingPosition = kotlin.math.round(ringPosition).toInt()
        ringInteracting.value = true
        settleJob = carouselScope.launch {
            val animation = Animatable(ringPosition)
            animation.animateTo(
                destination,
                spring(dampingRatio = .82f, stiffness = Spring.StiffnessMediumLow),
            ) { ringPositionState.floatValue = value }
            composedRingPosition = kotlin.math.round(destination).toInt()
            activeCardId = CarouselCardSettings.id(shownCard(entries[index.mod(entries.size)]).label)
            if (manual) manualInteractionEpoch++
            ringInteracting.value = false
        }
    }
    fun jumpToCard(index: Int) {
        val target = index.mod(entries.size)
        settleJob?.cancel()
        stackExpandedRootId = null
        ringInteracting.value = false
        ringPositionState.floatValue = target.toFloat()
        composedRingPosition = target
        activeCardId = CarouselCardSettings.id(shownCard(entries[target]).label)
        manualInteractionEpoch++
    }
    LaunchedEffect(keySequence) {
        if (keySequence > 0 && keyDirection != 0 && isActiveSurface) {
            if (keyDirection < 0 && visibleStackRootId != null) {
                // Backspace/Back exits the vertical stack before navigating the ring.
                stackExpandedRootId = null
                manualInteractionEpoch++
            } else {
                centreCard(kotlin.math.round(ringPositionState.floatValue).toInt() + keyDirection, manual = true)
            }
        }
    }
    LaunchedEffect(stackKeySequence) {
        if (stackKeySequence > 0 && stackKeyDirection != 0 && isActiveSurface && entries.isNotEmpty()) {
            val root = entries[kotlin.math.round(ringPositionState.floatValue).toInt().mod(entries.size)]
            val rootId = CarouselCardSettings.id(root.label)
            if (stackCards(root).size > 1) {
                if (stackExpandedRootId != rootId) {
                    stackExpandedRootId = rootId
                    manualInteractionEpoch++
                } else cycleStack(root, stackKeyDirection)
            }
        }
    }
    LaunchedEffect(spaceSequence) {
        if (spaceSequence > 0 && isActiveSurface && entries.isNotEmpty()) {
            val expandedId = stackExpandedRootId
            val expandedRoot = entries.firstOrNull {
                CarouselCardSettings.id(it.label) == expandedId
            }
            if (expandedRoot != null && stackCards(expandedRoot).size > 1) {
                cycleStack(expandedRoot, 1)
            } else {
                centreCard(kotlin.math.round(ringPositionState.floatValue).toInt() + 1, manual = true)
            }
        }
    }
    LaunchedEffect(actionSequence) {
        if (actionSequence > 0 && isActiveSurface && entries.isNotEmpty()) {
            val root = entries[kotlin.math.round(ringPositionState.floatValue).toInt().mod(entries.size)]
            val rootId = CarouselCardSettings.id(root.label)
            if (stackCards(root).size > 1 && stackExpandedRootId != rootId) {
                stackExpandedRootId = rootId
                manualInteractionEpoch++
            } else {
                shownCard(root).action()
            }
        }
    }
    val autoRotate = HomeSettings.carouselAutoRotate.value
    LaunchedEffect(autoRotate, isActiveSurface, manualInteractionEpoch, stackExpandedRootId) {
        if (!autoRotate || !isActiveSurface || stackExpandedRootId != null) return@LaunchedEffect
        while (isActive) {
            delay(5_000)
            if (!ringInteracting.value) centreCard(kotlin.math.round(ringPositionState.floatValue).toInt() + 1, manual = false)
        }
    }
    val font = commanderHomeFont(HomeSettings.fontStyle.value)
    Box(Modifier.size(side).background(if (sharedWallpaper) Color.Transparent else Color.Black.copy(alpha = .42f))) {
        if (HomeSettings.statusIslands.value) Row(Modifier.offset(x = side * .040f, y = side * .027f)
            .width(side * .920f).height(side * .076f),
            horizontalArrangement = Arrangement.spacedBy(side * .007f),
            verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.fillMaxHeight()
                .weight(1.20f)
                .signalGlassIsland(side)
                .clickable { (onOpenHub ?: { context.openCommanderSurface(HubActivity::class.java) })() }
                .padding(horizontal = side * .018f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically) {
                val islandClock = now.format(DateTimeFormatter.ofPattern(if (HomeSettings.timeline12Hour.value) "h:mm a" else "HH:mm", Locale.getDefault())).uppercase()
                val islandClockWeight = if (HomeSettings.boldCardTitles.value) FontWeight.Bold else FontWeight.Medium
                Box(
                    modifier = Modifier.fillMaxHeight().padding(top = side * .005f).combinedClickable(
                        onClick = { },
                        onLongClick = { context.startActivity(Intent(context, WallpaperPickerActivity::class.java)) },
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        islandClock,
                        color = accent.copy(alpha = .46f),
                        fontFamily = font,
                        fontWeight = islandClockWeight,
                        fontSize = (side.value * .0175f).sp,
                        maxLines = 1,
                        modifier = Modifier.blur(side * .007f, BlurredEdgeTreatment.Unbounded),
                    )
                    Text(
                        islandClock,
                        color = lerp(SignalWhite, accent, .16f),
                        fontFamily = font,
                        fontWeight = islandClockWeight,
                        fontSize = (side.value * .0175f).sp,
                        maxLines = 1,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(accent.copy(alpha = .58f), blurRadius = side.value * .010f),
                        ),
                    )
                }
                Spacer(Modifier.width(side * .007f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(side * .002f)) {
                    SignalStatusCounter(side, Icons.Outlined.Flag, "Flagged", flaggedCount, accent)
                    SignalStatusCounter(side, Icons.Outlined.PriorityHigh, "Important", importantCount, accent)
                    SignalStatusCounter(side, Icons.Outlined.NotificationsNone, "Pending", hubCount, accent)
                }
            }
            Row(Modifier.fillMaxHeight().weight(1.08f).signalGlassIsland(side)
                .padding(horizontal = side * .018f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                val compactDate = "${now.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).take(3)} ${now.dayOfMonth} ${now.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).take(3)}".uppercase()
                Text(compactDate, color = SignalWhite.copy(alpha = .88f), fontFamily = font, fontSize = (side.value * .016f).sp, maxLines = 1)
                Spacer(Modifier.width(side * .008f))
                Text("·", color = SignalWhite.copy(alpha = .36f), fontFamily = font, fontSize = (side.value * .013f).sp)
                Spacer(Modifier.width(side * .008f))
                Text("−${remaining.toHours()}h ${remaining.toMinutes() % 60}m", color = SignalWhite.copy(alpha = .88f), fontFamily = font, fontSize = (side.value * .016f).sp, maxLines = 1)
            }
            if (HomeSettings.weatherIsland.value) {
                Row(Modifier.fillMaxHeight().weight(.52f).signalGlassIsland(side)
                    .padding(horizontal = side * .014f),
                    horizontalArrangement = Arrangement.spacedBy(side * .006f),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        signalWeatherIcon(weather.weatherCode),
                        signalWeatherDescription(weather.weatherCode),
                        tint = lerp(accent, Color.White, .48f),
                        modifier = Modifier.size(side * .026f),
                    )
                    Text(
                        weather.temperature?.let { "$it°" } ?: "—",
                        color = SignalWhite.copy(alpha = .90f),
                        fontFamily = font,
                        fontSize = (side.value * .016f).sp,
                        maxLines = 1,
                    )
                }
            }
            Row(Modifier.fillMaxHeight().weight(1.30f).signalGlassIsland(side)
                .combinedClickable(
                    onClick = { },
                    onLongClick = {
                        context.startActivity(
                            Intent(context, SettingsActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                )
                .padding(horizontal = side * .018f),
                horizontalArrangement = Arrangement.spacedBy(side * .012f, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Wifi, "Wi-Fi", tint = SignalWhite.copy(alpha = if (connectivity.first) .90f else .38f),
                    modifier = Modifier.size(side * .025f))
                Icon(Icons.Outlined.SignalCellularAlt, "Cellular", tint = SignalWhite.copy(alpha = if (connectivity.second) .90f else .38f),
                    modifier = Modifier.size(side * .025f))
                Icon(Icons.Outlined.Bluetooth, "Bluetooth", tint = SignalWhite.copy(alpha = if (connectivity.third) .90f else .38f),
                    modifier = Modifier.size(side * .024f))
                Icon(Icons.Outlined.Coffee, "Caffeinate", tint = SignalWhite.copy(alpha = if (caffeineActive) .90f else .38f),
                    modifier = Modifier.size(side * .024f))
                Icon(
                    when (ringerMode) {
                        AudioManager.RINGER_MODE_SILENT -> Icons.Outlined.VolumeOff
                        AudioManager.RINGER_MODE_VIBRATE -> Icons.Outlined.Vibration
                        else -> Icons.Outlined.VolumeUp
                    },
                    when (ringerMode) {
                        AudioManager.RINGER_MODE_SILENT -> "Silent"
                        AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
                        else -> "Ringer"
                    },
                    tint = SignalWhite.copy(alpha = .90f),
                    modifier = Modifier.size(side * .024f),
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(side * .004f)) {
                    Text("${battery ?: 0}%", color = SignalWhite.copy(alpha = .88f), fontFamily = font,
                        fontWeight = FontWeight.Medium, fontSize = (side.value * .0145f).sp)
                    SignalBatteryGlyph(side * .62f, battery ?: 0, charging)
                }
            }
        } else Row(
            Modifier.offset(x = side * .050f, y = side * .035f).width(side * .900f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                now.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())).uppercase(),
                color = SignalWhite.copy(alpha = .86f), fontFamily = font,
                fontSize = (side.value * .014f).sp,
            )
            Text(
                "←HUB  ⚑$flaggedCount  !$importantCount  ♧$hubCount  APPS→",
                color = SignalWhite.copy(alpha = .70f), fontFamily = font,
                fontSize = (side.value * .0125f).sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(side * .007f)) {
                Icon(Icons.Outlined.Wifi, "Wi-Fi", tint = SignalWhite.copy(alpha = if (connectivity.first) .9f else .3f), modifier = Modifier.size(side * .018f))
                Icon(Icons.Outlined.SignalCellularAlt, "Cellular", tint = SignalWhite.copy(alpha = if (connectivity.second) .9f else .3f), modifier = Modifier.size(side * .018f))
                Icon(Icons.Outlined.Bluetooth, "Bluetooth", tint = SignalWhite.copy(alpha = if (connectivity.third) .9f else .3f), modifier = Modifier.size(side * .017f))
                Icon(if (ringerMode == AudioManager.RINGER_MODE_SILENT) Icons.Outlined.VolumeOff else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) Icons.Outlined.Vibration else Icons.Outlined.VolumeUp, "Ringer status", tint = SignalWhite.copy(alpha = .78f), modifier = Modifier.size(side * .017f))
                Text("${battery ?: 0}%", color = SignalWhite, fontFamily = font, fontSize = (side.value * .013f).sp)
                SignalBatteryGlyph(side * .52f, battery ?: 0, charging)
            }
        }
        val islandHeader = HomeSettings.statusIslands.value
        if (!islandHeader) Row(
            Modifier.offset(x = side * .052f, y = side * .060f),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                now.format(DateTimeFormatter.ofPattern(if (HomeSettings.timeline12Hour.value) "h:mm" else "HH:mm")),
                color = lerp(SignalWhite, accent, .24f), fontFamily = SignalDot,
                fontSize = (side.value * .053f).sp, letterSpacing = (-1).sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(accent.copy(alpha = .58f), blurRadius = side.value * .020f),
                ),
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = { context.startActivity(Intent(context, WallpaperPickerActivity::class.java)) },
                ),
            )
            if (HomeSettings.timeline12Hour.value) {
                Text(
                    now.format(DateTimeFormatter.ofPattern("a", Locale.getDefault())).uppercase(),
                    color = lerp(SignalWhite, accent, .24f).copy(alpha = .86f),
                    fontFamily = SignalDot,
                    fontSize = (side.value * .021f).sp,
                    letterSpacing = (-.3).sp,
                    modifier = Modifier.padding(start = side * .006f, bottom = side * .005f),
                )
            }
        }
        if (!islandHeader) Row(Modifier.offset(x = side * .660f, y = side * .060f).width(side * .288f),
            horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(side * .052f), contentAlignment = Alignment.Center) {
                Icon(
                    signalWeatherIcon(weather.weatherCode),
                    null,
                    tint = accent.copy(alpha = .72f),
                    modifier = Modifier.size(side * .048f)
                        .blur(side * .012f, BlurredEdgeTreatment.Unbounded),
                )
                Icon(
                    signalWeatherIcon(weather.weatherCode),
                    signalWeatherDescription(weather.weatherCode),
                    tint = lerp(accent, Color.White, .48f),
                    modifier = Modifier.size(side * .044f),
                )
            }
            Spacer(Modifier.width(side * .020f))
            Text(weather.temperature?.let { "$it°" } ?: "—", color = SignalWhite, fontFamily = SignalDot,
                fontSize = (side.value * .052f).sp, lineHeight = (side.value * .052f).sp,
                letterSpacing = (-1).sp)
        }
        DaySignalGrid(
            side = side,
            now = now,
            events = events,
            deadlines = deadlines,
            minuteStep = 20,
            isActiveSurface = isActiveSurface,
            modifier = Modifier.offset(x = side * .035f, y = side * if (islandHeader) .070f else .095f)
                .width(side * .93f).height(side * .270f),
        )
        BoxWithConstraints(Modifier.offset(y = side * if (islandHeader) .365f else .385f).fillMaxWidth().height(side * .350f)
            .graphicsLayer {
                // Layer-only read keeps the shared vertical lift on the render
                // thread rather than invalidating the surrounding homepage.
                val floatWave = kotlin.math.sin(
                    idlePhaseState.floatValue * 2f * Math.PI,
                ).toFloat()
                translationY = floatWave * side.value * density * .0014f * idleStrengthState.floatValue +
                    (1f - entrance.value) * side.value * density * .030f +
                    side.value * density * .170f * stackReveal.value
                // Opening a stack shifts the entire full-size ring toward the
                // bottom, making room for its next card above. No ring panel
                // is shrunk or individually repositioned.
            }
            .draggable(
                state = ringDrag,
                orientation = Orientation.Horizontal,
                onDragStarted = { startRingDrag() },
                onDragStopped = { velocity -> stopRingDrag(velocity) },
            )) {
            val cardWidth = side * .37f
            val cardHeight = side * .325f
            val centreX = (maxWidth - cardWidth) / 2
            val carouselDensity = LocalDensity.current
            val cardPx = with(carouselDensity) { side.toPx() }
            val ringRadius = side * ringRadiusFraction
            val ringRadiusPx = cardPx * ringRadiusFraction
            val entranceSpread = .80f + .20f * entrance.value
            // Idle phase is read only by Canvas/graphicsLayer below. Keeping it
            // out of composition prevents a full ring rebuild every 16 ms.
            // One authoritative animated coordinate drives layout, depth,
            // identity and focus. Splitting these between a render-layer float
            // and a settled composition integer caused rear slots to show the
            // previous card before remapping, and made glow arrive late.
            val visualRingPosition = ringPositionState.floatValue
            entries.forEachIndexed { index, rootEntry ->
                val rootId = CarouselCardSettings.id(rootEntry.label)
                // The horizontal ring is the category layer. Cycling its
                // expanded vertical children must not silently replace the
                // ring face underneath and reveal a different card on close.
                val entry = if (visibleStackRootId == rootId) rootEntry
                    else shownCard(rootEntry)
                val cardId = CarouselCardSettings.id(entry.label)
                val displayTitle = CarouselCardSettings.title(context, cardId) ?: entry.label
                val customDetail = CarouselCardSettings.detail(context, cardId)
                val configuredTapPackage = CarouselCardSettings.tapAction(context, cardId)
                    ?.takeIf { it.startsWith("app:") }?.removePrefix("app:")
                    ?: CarouselCardSettings.appOverride(context, cardId)
                    ?: customPackageByCardId[cardId]
                val showAppNotifications = configuredTapPackage != null &&
                    CarouselCardSettings.showAppNotifications(
                        context, cardId, defaultValue = cardId in customPackageByCardId,
                    )
                val appNotifications = if (showAppNotifications) hubItems
                    .filter { !it.isSummary && it.packageName == configuredTapPackage }
                    .sortedByDescending(HubItem::time) else emptyList()
                val notificationDetail = appNotifications.takeIf { it.isNotEmpty() }?.let { pending ->
                    // The cached rear face must not depend on selection. The
                    // focused live face owns notification cycling; changing
                    // focus must never rebuild/upload a rear texture mid-swipe.
                    val notificationIndex = 0
                    val item = pending[notificationIndex]
                    val body = listOf(item.title, item.text).filter(String::isNotBlank).joinToString(": ")
                    if (pending.size > 1) "${notificationIndex + 1}/${pending.size} · $body" else body
                }
                val displayDetail = notificationDetail ?: customDetail ?: entry.detail
                val displayTop = CarouselCardSettings.topText(context, cardId) ?: cardRole(entry.label)
                val stackCount = stackCards(rootEntry).size
                val stackPosition = if (visibleStackRootId == rootId) 1 else
                    (stackCardIndex[rootId] ?: 0).mod(stackCount) + 1
                val leftHeader = displayTop.uppercase()
                val rightHeader = if (stackCount > 1) "$stackPosition/$stackCount" else entry.symbol.ifBlank { "·" }
                // Notification cards use the same fixed one-line detail slot
                // in both their cached rear face and live focused face. Only
                // the focused face scrolls; its layout must never reflow from
                // two lines to one as it reaches the centre.
                val singleLineDetail = true
                val position = wrappedPosition(index - visualRingPosition)
                val distance = kotlin.math.abs(position)
                // Idle drift moves the physical card, but must not dirty the
                // card's glass, text and glyph layers. Focus styling follows
                // the interaction position and therefore changes only during
                // a real swipe/settle, allowing rear-card layers to stay cached.
                val interactionDistance = distance
                // Only the nearest revolution is visible. With more than the
                // reference eleven cards, remaining modules occupy the rear
                // passage and rotate into these stable physical slots.
                if (distance > referenceRingSlots * .5f) {
                    // Hidden logical cards must not own textures. The eleven
                    // physical ring positions are the complete cache window;
                    // an entry receives a face only when it rotates into one.
                    return@forEachIndexed
                }
                // Every entry owns an equal segment of one real cylinder.
                // The segment angle is deliberately count-independent.
                val orbitAngle = position * (360f / referenceRingSlots)
                val orbitRadians = Math.toRadians(orbitAngle.toDouble()).toFloat()
                // All card positions and panel projections derive from this
                // one orbital coordinate; no individual rotation animation.
                val orbitPerspective = 4f / (5f - kotlin.math.cos(orbitRadians))
                val x = centreX + ringRadius * entranceSpread *
                    (kotlin.math.sin(orbitRadians) * orbitPerspective)
                val focus = interactionDistance < .50f
                val focusAmount = (1f - interactionDistance).coerceIn(0f, 1f)
                val focusStyle = focusAmount
                // Only the focused face remains live. Invisible neighbouring
                // Compose trees retained full render layers despite their
                // near-zero alpha; rear neighbours use their static face.
                val warmupNeighbour = warmIncomingCard &&
                    wrappedPosition(index - visualRingPosition) in .50f..1.50f
                val composeLive = focus || warmupNeighbour
                val cosine = kotlin.math.cos(orbitRadians)
                val depth = ((cosine + 1f) * .5f).coerceIn(0f, 1f)
                val rearOpacity = HomeSettings.carouselRearOpacity.floatValue
                val frontVisibility = rearOpacity + (1f - rearOpacity) * depth
                val recess = 1f - depth
                val carouselView = HomeSettings.carouselView.value
                val cameraPitch = if (carouselView == CarouselViewMode.Elevated) HomeSettings.carouselPitch.floatValue else 0f
                val rearHeightLoss = if (carouselView == CarouselViewMode.Stable) .12f else .06f
                val orbitalHeightScale = 1f - rearHeightLoss * recess
                val orbitDepth = when (carouselView) {
                    CarouselViewMode.Stable -> .033f
                    CarouselViewMode.Angled -> .068f
                    CarouselViewMode.Elevated -> 0f
                }
                val cardY = if (carouselView == CarouselViewMode.Elevated) {
                    // Use the very same camera projection as the four card
                    // corners. An independently tuned cosine here puts the
                    // card centres on a different ellipse and creates steps.
                    val pitchRadians = Math.toRadians(cameraPitch.toDouble()).toFloat()
                    side * .004f + ringRadius * cosine *
                        kotlin.math.sin(pitchRadians) * orbitPerspective
                } else {
                    side * (.004f + orbitDepth * cosine)
                }
                val frameGeometry = remember(cardId) { OrbitFrameGeometry() }
                fun currentVisualGeometry(): OrbitFrameGeometry {
                    val phase = idlePhaseState.floatValue
                    val currentRingPosition = ringPositionState.floatValue
                    val currentIdleStrength = idleStrengthState.floatValue
                    val currentEntranceSpread = entranceSpread
                    if (frameGeometry.phase == phase &&
                        frameGeometry.ringPosition == currentRingPosition &&
                        frameGeometry.idleStrength == currentIdleStrength &&
                        frameGeometry.entranceSpread == currentEntranceSpread &&
                        frameGeometry.baseX == x.value &&
                        frameGeometry.baseY == cardY.value
                    ) return frameGeometry
                    val wave = kotlin.math.sin(phase * 2f * Math.PI).toFloat()
                    val visualPosition = wrappedPosition(
                        index - currentRingPosition - wave * .026f * currentIdleStrength,
                    )
                    val visualAngle = visualPosition * (360f / referenceRingSlots)
                    val visualRadians = Math.toRadians(visualAngle.toDouble()).toFloat()
                    val visualCosine = kotlin.math.cos(visualRadians)
                    val visualPerspective = 4f / (5f - visualCosine)
                    val visualDepthAmount = ((visualCosine + 1f) * .5f).coerceIn(0f, 1f)
                    val visualXPx = with(carouselDensity) {
                        (centreX + ringRadius * currentEntranceSpread *
                            (kotlin.math.sin(visualRadians) * visualPerspective)).toPx()
                    }
                    val visualYPx = with(carouselDensity) {
                        if (carouselView == CarouselViewMode.Elevated) {
                            val pitchRadians = Math.toRadians(cameraPitch.toDouble()).toFloat()
                            (side * .004f + ringRadius * visualCosine *
                                kotlin.math.sin(pitchRadians) * visualPerspective).toPx()
                        } else {
                            val visualDepth = when (carouselView) {
                                CarouselViewMode.Stable -> .033f
                                CarouselViewMode.Angled -> .068f + .007f * wave * currentIdleStrength
                                CarouselViewMode.Elevated -> 0f
                            }
                            (side * (.004f + visualDepth * visualCosine)).toPx()
                        }
                    }
                    return frameGeometry.apply {
                        this.phase = phase
                        this.ringPosition = currentRingPosition
                        this.idleStrength = currentIdleStrength
                        this.entranceSpread = currentEntranceSpread
                        baseX = x.value
                        baseY = cardY.value
                        angle = visualAngle
                        translationX = visualXPx - with(carouselDensity) { x.toPx() }
                        translationY = visualYPx - with(carouselDensity) { cardY.toPx() }
                        lightPulse = 1f + .07f * currentIdleStrength * kotlin.math.sin(
                            phase * 2f * Math.PI + .8f,
                        ).toFloat()
                        this.focusAmount = (1f - kotlin.math.abs(visualPosition)).coerceIn(0f, 1f)
                        visibility = rearOpacity + (1f - rearOpacity) * visualDepthAmount
                        heightScale = 1f - rearHeightLoss * (1f - visualDepthAmount)
                    }
                }
                val cardShape = RoundedCornerShape(side * .012f)
                // Record the actual Compose face once while it is on the rear
                // arc. Unlike the old bitmap renderer, this is the same text,
                // glyph and glass tree used by the focused card. The orbit's
                // projection and alpha remain outside the cached display list.
                val rearFaceLayer = rememberGraphicsLayer()
                val rearFaceState = remember(cardId) { RearFaceLayerState() }
                val rearFaceKey = listOf(
                    cardId, index, displayTitle, displayDetail, leftHeader,
                    rightHeader, entry.icon, mediaArtwork, taskCount, font,
                    accent, weather, HomeSettings.tasksDotCount.value,
                    HomeSettings.boldCardTitles.value, cardSettingsRevision,
                )
                val staticRearFace = distance >= 1f
                val rearFaceModifier = Modifier.drawWithContent {
                    if (!staticRearFace) {
                        drawContent()
                    } else {
                        val faceSize = IntSize(size.width.toInt(), size.height.toInt())
                        if (rearFaceState.key != rearFaceKey || rearFaceState.size != faceSize) {
                            rearFaceLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            rearFaceState.key = rearFaceKey
                            rearFaceState.size = faceSize
                        }
                        drawLayer(rearFaceLayer)
                    }
                }
                if (composeLive) {
                    Canvas(Modifier.fillMaxSize().zIndex(1.5f - distance)) {
                        val geometry = currentVisualGeometry()
                        val lightPulse = geometry.lightPulse
                        val visualFocus = geometry.focusAmount
                        val centre = Offset(
                            x.toPx() + geometry.translationX + cardWidth.toPx() * .5f,
                            cardY.toPx() + geometry.translationY + cardHeight.toPx() * .55f,
                        )
                        val haloRadius = cardWidth.toPx() * .82f
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    accent.copy(alpha = .22f * visualFocus *
                                        HomeSettings.cardGlow.floatValue * lightPulse),
                                    accent.copy(alpha = .065f * visualFocus *
                                        HomeSettings.cardGlow.floatValue),
                                    Color.Transparent,
                                ),
                                center = centre,
                                radius = haloRadius,
                            ),
                            center = centre,
                            radius = haloRadius,
                        )
                    }
                    Box(Modifier.offset(x = x, y = cardY).size(cardWidth, cardHeight)
                        .zIndex(2.6f - distance)
                        .graphicsLayer {
                            val geometry = currentVisualGeometry()
                            translationX = geometry.translationX
                            translationY = geometry.translationY
                        }
                        .orbitPanelProjection(
                            { currentVisualGeometry().angle },
                            ringRadiusPx,
                            cameraPitchDegrees = cameraPitch,
                        )
                        // Pulse the completed glow layer. Keeping the border
                        // itself static lets HWUI reuse its blurred texture.
                        .graphicsLayer {
                            val lightPulse = currentVisualGeometry().lightPulse
                            val geometry = currentVisualGeometry()
                            alpha = (.68f * geometry.focusAmount * geometry.lightPulse).coerceIn(0f, 1f)
                        }
                        .blur(side * .011f, BlurredEdgeTreatment.Unbounded)
                        .border(side * .007f, lerp(accent, Color.White, .24f).copy(
                            alpha = 1f), cardShape))
                }
                // A stack is made of physical card faces at the same orbit
                // position, not a counter on a single face. The backing faces
                // use the exact orbital projection of their front card; their
                // stepped local offsets reveal the top and side edges without
                // changing the ring radius, yaw or neighbour spacing.
                val cardsInStack = stackCards(rootEntry)
                val stackRootId = CarouselCardSettings.id(rootEntry.label)
                val stackAnimating = focus && stackMotionRoot == stackRootId &&
                    stackOutgoingCard != null
                val stackExpanded = focus && stackExpandedRootId == stackRootId &&
                    cardsInStack.size > 1
                val stackPeeking = focus && stackPeekRoot == stackRootId &&
                    kotlin.math.abs(stackPeekTravel) > cardPx * .008f &&
                    cardsInStack.size > 1 && !stackExpanded
                if (cardsInStack.size > 1 && !stackAnimating &&
                    !stackPeeking && !stackExpanded) {
                    val backingCount = (cardsInStack.size - 1).coerceAtMost(2)
                    for (backingLayer in backingCount downTo 1) {
                        Box(
                            Modifier.offset(
                                x = x,
                                y = cardY - side * (.006f * backingLayer),
                            ).size(cardWidth, cardHeight)
                                .zIndex(2.7f - distance - backingLayer * .02f)
                                .graphicsLayer {
                                    val geometry = currentVisualGeometry()
                                    translationX = geometry.translationX
                                    translationY = geometry.translationY
                                }
                                .orbitPanelProjection(
                                    { currentVisualGeometry().angle },
                                    ringRadiusPx,
                                    cameraPitchDegrees = cameraPitch,
                                )
                                .graphicsLayer {
                                    val geometry = currentVisualGeometry()
                                    // Keep the same physical stack on the
                                    // rear arc. Fade its extra glass by orbit
                                    // depth, but never collapse it into a
                                    // single face merely because it rotated
                                    // away from the viewer.
                                    val farSide = ((distance - 2.45f) / 3f)
                                        .coerceIn(0f, 1f)
                                    alpha = geometry.visibility *
                                        (if (backingLayer == 1) .92f else .75f) *
                                        (1f - .25f * farSide)
                                    scaleY = geometry.heightScale
                                }
                                .drawWithContent {
                                    // Only the raised top lip is visible. A
                                    // full duplicate panel exposes side and
                                    // bottom outlines after perspective.
                                    clipRect(bottom = size.height * .075f) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                                .clip(cardShape)
                                .background(Brush.verticalGradient(listOf(
                                    SignalWhite.copy(alpha = .18f),
                                    Color(0xFF080A0D).copy(alpha = .88f),
                                )))
                                .border(side * .0011f,
                                    SignalWhite.copy(alpha = if (backingLayer == 1) .70f else .46f),
                                    cardShape),
                        )
                    }
                }
                if (stackPeeking) {
                    val peekDirection = if (stackPeekTravel < 0f) 1 else -1
                    val peekIndex = ((stackCardIndex[stackRootId] ?: 0) + peekDirection)
                        .mod(cardsInStack.size)
                    val peekCard = cardsInStack[peekIndex]
                    val peekProgress = (kotlin.math.abs(stackPeekTravel) /
                        (with(carouselDensity) { cardHeight.toPx() } * .50f)).coerceIn(0f, 1f)
                    RearCarouselCard(
                        modifier = Modifier.offset(x = x, y = cardY).size(cardWidth, cardHeight)
                            .zIndex(2.95f - distance)
                            .graphicsLayer {
                                val geometry = currentVisualGeometry()
                                translationX = geometry.translationX
                                translationY = geometry.translationY
                            }
                            .orbitPanelProjection(
                                { currentVisualGeometry().angle }, ringRadiusPx,
                                cameraPitchDegrees = cameraPitch,
                            )
                            .graphicsLayer {
                                val geometry = currentVisualGeometry()
                                translationY = -peekDirection * (.04f + .31f * peekProgress) *
                                    cardHeight.toPx()
                                alpha = geometry.visibility * (.45f + .50f * peekProgress)
                                scaleY = geometry.heightScale
                            }
                            .clip(cardShape)
                            .background(Color(0xFF080A0D).copy(alpha = .92f)),
                        side = side,
                        index = index,
                        title = CarouselCardSettings.title(context,
                            CarouselCardSettings.id(peekCard.label)) ?: peekCard.label,
                        detail = CarouselCardSettings.detail(context,
                            CarouselCardSettings.id(peekCard.label)) ?: peekCard.detail,
                        leftHeader = CarouselCardSettings.topText(context,
                            CarouselCardSettings.id(peekCard.label)) ?: cardRole(peekCard.label),
                        rightHeader = "${peekIndex + 1}/${cardsInStack.size}",
                        icon = peekCard.icon,
                        artwork = if (peekCard.label == "Media") mediaArtwork else null,
                        taskCount = taskCount,
                        showTaskCounter = peekCard.label == "Tasks" && HomeSettings.tasksDotCount.value,
                        singleLineDetail = true,
                        font = font,
                        accent = accent,
                    )
                }
                val cardModifier = Modifier.offset(x = x, y = cardY).size(cardWidth, cardHeight)
                    .zIndex(3f - distance)
                    .graphicsLayer {
                        val geometry = currentVisualGeometry()
                        translationX = geometry.translationX
                        translationY = geometry.translationY
                    }
                    // Project the completed card layer. Keeping graphicsLayer
                    // outside this transform clips the corners which extend
                    // beyond the card's original rectangular allocation.
                    .orbitPanelProjection(
                        { currentVisualGeometry().angle },
                        ringRadiusPx,
                        cameraPitchDegrees = cameraPitch,
                    )
                    .graphicsLayer {
                        val geometry = currentVisualGeometry()
                        val progress = if (stackAnimating) stackMotion.value else 1f
                        val stackDepthVisibility = when {
                            focus -> 1f
                            distance < 1.6f -> 1f - .12f * stackReveal.value
                            distance < 2.6f -> 1f - .28f * stackReveal.value
                            else -> 1f - .45f * stackReveal.value
                        }
                        alpha = geometry.visibility *
                            stackDepthVisibility *
                            (if (focus && visibleStackRootId == stackRootId)
                                1f - stackReveal.value else 1f) *
                            (if (stackAnimating)
                            (progress * 1.5f).coerceIn(0f, 1f) else 1f)
                        scaleY = geometry.heightScale
                        if (stackAnimating) translationY = stackMotionDirection *
                            (1f - progress) * cardHeight.toPx() * .35f
                    }
                    .then(rearFaceModifier)
                    .clip(cardShape)
                    .background(Color(0xFF080A0D).copy(alpha =
                        .51f + .09f * focusStyle))
                    .border(side * (.00135f + .00055f * focusStyle),
                        lerp(Color.White.copy(alpha = .42f), lerp(accent, Color.White, .38f), focusStyle), cardShape)
                    .then(if (focus) Modifier.pointerInput(
                        cardId, rootEntry.label, stackCount,
                    ) {
                        var verticalTravel = 0f
                        detectVerticalDragGestures(
                            onDragStart = {
                                verticalTravel = 0f
                            },
                            onVerticalDrag = { change, amount ->
                                change.consume()
                                verticalTravel += amount
                            },
                            onDragEnd = {
                                if (kotlin.math.abs(verticalTravel) >= size.height * .16f) {
                                    if (stackCount > 1) {
                                        stackExpandedRootId = stackRootId
                                        manualInteractionEpoch++
                                    }
                                }
                                verticalTravel = 0f
                            },
                            onDragCancel = {
                                verticalTravel = 0f
                            },
                        )
                    } else Modifier)
                    .then(if (frontVisibility > .08f) Modifier.combinedClickable(
                        onClick = { if (focus) entry.action() else centreCard(index) },
                        onLongClick = {
                            if (focus) {
                                editingCard = if (
                                    entry.label == "${rootEntry.label} UP" ||
                                    entry.label == "${rootEntry.label} DOWN"
                                ) rootEntry else entry
                                cardEditorPage = "menu"
                                cardEditorQuery = ""
                                cardTitleDraft = CarouselCardSettings.title(context, cardId).orEmpty()
                                cardDetailDraft = CarouselCardSettings.detail(context, cardId).orEmpty()
                                cardTopDraft = CarouselCardSettings.topText(context, cardId).orEmpty()
                            } else centreCard(index)
                        },
                    ) else Modifier)
                val outgoing = if (stackAnimating) stackOutgoingCard else null
                if (outgoing != null) RearCarouselCard(
                    modifier = Modifier.offset(x = x, y = cardY).size(cardWidth, cardHeight)
                        .zIndex(3.1f - distance)
                        .graphicsLayer {
                            val geometry = currentVisualGeometry()
                            translationX = geometry.translationX
                            translationY = geometry.translationY
                        }
                        .orbitPanelProjection(
                            { currentVisualGeometry().angle }, ringRadiusPx,
                            cameraPitchDegrees = cameraPitch,
                        )
                        .graphicsLayer {
                            val geometry = currentVisualGeometry()
                            val progress = stackMotion.value
                            translationY = -stackMotionDirection * progress * cardHeight.toPx() * .35f
                            alpha = geometry.visibility * (1f - progress * 1.5f).coerceIn(0f, 1f)
                            scaleY = geometry.heightScale
                        },
                    side = side,
                    index = index,
                    title = CarouselCardSettings.title(context, CarouselCardSettings.id(outgoing.label))
                        ?: outgoing.label,
                    detail = CarouselCardSettings.detail(context, CarouselCardSettings.id(outgoing.label))
                        ?: outgoing.detail,
                    leftHeader = CarouselCardSettings.topText(context,
                        CarouselCardSettings.id(outgoing.label)) ?: cardRole(outgoing.label),
                    rightHeader = "${((stackCardIndex[stackRootId] ?: 0) - stackMotionDirection)
                        .mod(stackCount) + 1}/$stackCount",
                    icon = outgoing.icon,
                    artwork = if (outgoing.label == "Media") mediaArtwork else null,
                    taskCount = taskCount,
                    showTaskCounter = outgoing.label == "Tasks" && HomeSettings.tasksDotCount.value,
                    singleLineDetail = true,
                    font = font,
                    accent = accent,
                )
                if (UseCanvasRearCards) key(cardId) {
                    // Keep every static face alive when it passes through the
                    // focused position. Previously the bitmap was disposed and
                    // synchronously rebuilt as focus moved on, creating an
                    // otherwise intermittent hitch during swipes.
                    RearCarouselCard(
                        modifier = cardModifier.graphicsLayer {
                            alpha = if (focus) 0f else 1f
                        },
                        side = side,
                        index = index,
                        title = displayTitle,
                        detail = displayDetail,
                        leftHeader = leftHeader,
                        rightHeader = rightHeader,
                        icon = entry.icon,
                        artwork = if (entry.label == "Media") mediaArtwork else null,
                        taskCount = taskCount,
                        showTaskCounter = entry.label == "Tasks" && HomeSettings.tasksDotCount.value,
                        singleLineDetail = singleLineDetail,
                        font = font,
                        accent = accent,
                    )
                }
                if (!UseCanvasRearCards || composeLive) Box(
                    cardModifier.then(
                        if (warmupNeighbour && !focus) Modifier.graphicsLayer { alpha = .001f }
                        else Modifier,
                    ),
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        drawRect(Brush.verticalGradient(listOf(
                            Color.White.copy(alpha = .075f + .045f * focusStyle),
                            Color.Transparent,
                            accent.copy(alpha = .045f + .155f * focusStyle),
                        )))
                        drawRect(Brush.linearGradient(listOf(
                            Color.White.copy(alpha = .10f), Color.Transparent, Color.Black.copy(alpha = .18f),
                        ), start = Offset.Zero, end = Offset(size.width, size.height)))
                        if (focusStyle > .01f) drawRect(Brush.radialGradient(
                            listOf(accent.copy(alpha = .24f * focusStyle), accent.copy(alpha = .08f * focusStyle), Color.Transparent),
                            center = Offset(size.width * .50f, size.height * 1.05f), radius = size.width * .88f,
                        ))
                    }
                    Column(Modifier.fillMaxSize().padding(horizontal = side * .025f, vertical = side * .022f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Text("${(index + 1).toString().padStart(2, '0')}  /  ",
                                    color = SignalWhite.copy(alpha = .52f + .24f * focusStyle),
                                    fontFamily = SignalMono, fontWeight = FontWeight.Medium,
                                    fontSize = (side.value * .0125f).sp, letterSpacing = (.75).sp,
                                    maxLines = 1)
                                Text(leftHeader,
                                    color = SignalWhite.copy(alpha = .72f + .18f * focusStyle),
                                    fontFamily = SignalMono, fontSize = (side.value * .0125f).sp,
                                    maxLines = 1, overflow = TextOverflow.Clip,
                                    modifier = Modifier.weight(1f).then(
                                        if (focus && isActiveSurface) Modifier.basicMarquee(
                                            iterations = 1,
                                            initialDelayMillis = 1_200,
                                            repeatDelayMillis = 700,
                                            spacing = MarqueeSpacing(10.dp),
                                        ) else Modifier,
                                    ))
                            }
                            Box(
                                modifier = Modifier.width(side * .145f),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Text(rightHeader,
                                    color = SignalWhite.copy(alpha = .72f + .18f * focusStyle),
                                    fontFamily = SignalMono, fontSize = (side.value * .0125f).sp,
                                    maxLines = 1, overflow = TextOverflow.Clip,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth().then(
                                        if (focus && isActiveSurface) Modifier.basicMarquee(
                                            iterations = 1,
                                            initialDelayMillis = 1_200,
                                            repeatDelayMillis = 700,
                                            spacing = MarqueeSpacing(10.dp),
                                        ) else Modifier,
                                    ))
                            }
                        }
                        Spacer(Modifier.height(side * .022f))
                        Box(Modifier.size(side * .100f), contentAlignment = Alignment.Center) {
                            val showTaskCounter = entry.label == "Tasks" && HomeSettings.tasksDotCount.value
                            if (focusStyle > .001f && entry.label != "Media" && !showTaskCounter) {
                                Icon(entry.icon, null,
                                    tint = accent.copy(alpha = .70f * focusStyle),
                                    modifier = Modifier.size(side * .076f)
                                        .blur(side * .017f, BlurredEdgeTreatment.Unbounded))
                                Icon(entry.icon, null,
                                    tint = lerp(accent, Color.White, .55f)
                                        .copy(alpha = .58f * focusStyle),
                                    modifier = Modifier.size(side * .068f)
                                        .blur(side * .005f, BlurredEdgeTreatment.Unbounded))
                            }
                            if (entry.label == "Media" && mediaArtwork != null) {
                                Image(
                                    bitmap = mediaArtwork,
                                    contentDescription = "Album artwork",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(side * .090f)
                                        .clip(RoundedCornerShape(side * .010f)),
                                )
                            } else if (showTaskCounter) {
                                if (focusStyle > .001f) {
                                    Text(taskCount.toString().padStart(2, '0'),
                                        color = accent.copy(alpha = .72f * focusStyle),
                                        fontFamily = SignalDot,
                                        fontSize = (side.value * .061f).sp,
                                        letterSpacing = (-1).sp,
                                        modifier = Modifier.blur(
                                            side * .012f,
                                            BlurredEdgeTreatment.Unbounded,
                                        ))
                                }
                                Text(taskCount.toString().padStart(2, '0'),
                                    color = lerp(SignalWhite, accent, .34f * focusStyle),
                                    fontFamily = SignalDot,
                                    fontSize = (side.value * .061f).sp,
                                    letterSpacing = (-1).sp)
                            } else {
                                Icon(entry.icon, entry.label,
                                    tint = lerp(SignalWhite, accent, .26f * focusStyle),
                                    modifier = Modifier.size(side * .064f))
                            }
                        }
                        Spacer(Modifier.height(side * .010f))
                        Text(displayTitle.uppercase(), color = SignalWhite, fontFamily = font,
                            fontWeight = if (HomeSettings.boldCardTitles.value) FontWeight.Bold else FontWeight.Medium,
                            fontSize = (side.value * .023f).sp,
                            maxLines = 1, letterSpacing = (.8).sp)
                        Spacer(Modifier.height(side * .008f))
                        // Every card reserves one detail line, but only text
                        // that truly overflows enters the scrolling/start-
                        // aligned marquee. Short focused details remain
                        // centred exactly like their unfocused face.
                        val marqueePreview = focus && isActiveSurface && displayDetail.length > 42
                        val rotatesNotifications = focus && (
                            (entry.label == "Messages" && messageItems.size > 1) ||
                                (entry.label == "Email" && emailItems.size > 1) ||
                                appNotifications.size > 1
                            )
                        @Composable fun CardDetail(detail: String) {
                            Text(detail, color = SignalWhite.copy(alpha = .76f + .10f * focusStyle),
                                fontFamily = font, fontSize = (side.value * .016f).sp,
                                lineHeight = (side.value * .022f).sp, maxLines = 1,
                                letterSpacing = (-.15).sp,
                                textAlign = if (marqueePreview) {
                                    androidx.compose.ui.text.style.TextAlign.Start
                                } else {
                                    androidx.compose.ui.text.style.TextAlign.Center
                                },
                                overflow = if (marqueePreview) TextOverflow.Clip else TextOverflow.Ellipsis,
                                modifier = if (marqueePreview) Modifier.fillMaxWidth().basicMarquee(
                                    iterations = 1,
                                    initialDelayMillis = 1_300,
                                    repeatDelayMillis = 650,
                                    spacing = MarqueeSpacing(12.dp),
                                ) else Modifier)
                        }
                        if (entry.label == "Weather" && customDetail == null) {
                            Text(
                                "${weather.temperature?.let { "$it°" } ?: "—"} · ${signalWeatherDescription(weather.weatherCode)}",
                                color = SignalWhite.copy(alpha = .86f), fontFamily = font,
                                fontSize = (side.value * .0145f).sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (weather.hourly.isNotEmpty()) {
                                Spacer(Modifier.height(side * .006f))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    weather.hourly.take(3).forEachIndexed { rainIndex, point ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(if (rainIndex == 0) "NOW" else "+${rainIndex}H",
                                                color = SignalGrey.copy(alpha = .76f), fontFamily = SignalMono,
                                                fontSize = (side.value * .0085f).sp)
                                            Text("${point.chance}%", color = lerp(SignalWhite, accent, .32f),
                                                fontFamily = font, fontWeight = FontWeight.Medium,
                                                fontSize = (side.value * .012f).sp)
                                        }
                                    }
                                }
                            }
                        } else if (rotatesNotifications) {
                            AnimatedContent(
                                modifier = Modifier.fillMaxWidth(),
                                targetState = displayDetail,
                                transitionSpec = {
                                    (fadeIn(tween(320)) + slideInVertically(tween(360)) { it / 2 }) togetherWith
                                        (fadeOut(tween(220)) + slideOutVertically(tween(280)) { -it / 2 })
                                },
                                label = "${entry.label.lowercase()} notification transition",
                            ) { detail -> CardDetail(detail) }
                        } else {
                            CardDetail(displayDetail)
                        }
                    }
                }
            }
        }
        // The pitched cards can visually extend below their fixed geometry
        // box. Give only that projected edge its own transparent hit strip;
        // never resize the geometry box itself, because its height defines
        // the orbit ellipse and camera projection.
        Box(
            Modifier.offset(y = side * if (islandHeader) .715f else .735f)
                .fillMaxWidth().height(side * .075f)
                .draggable(
                    state = ringDrag,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { startRingDrag() },
                    onDragStopped = { velocity -> stopRingDrag(velocity) },
                ),
        )
        val selectedRingIndex = kotlin.math.round(ringPositionState.floatValue).toInt()
        val selectedRoot = entries[selectedRingIndex.mod(entries.size)]
        val openStackRoot = selectedRoot.takeIf {
            CarouselCardSettings.id(it.label) == stackExpandedRootId
        }
        var categoryPickerOpen by remember { mutableStateOf(false) }
        if (openStackRoot == null) Canvas(
            Modifier.zIndex(10f).offset(x = side * .12f, y = side * .829f)
                .width(side * .70f).height(side * .052f)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { categoryPickerOpen = true })
                }
                .semantics { contentDescription = "Hold to choose a carousel category" },
        ) {
            val railY = size.height * .52f
            val startX = size.width * .02f
            val endX = size.width * .98f
            drawLine(SignalWhite.copy(alpha = .28f),
                Offset(startX, railY), Offset(endX, railY),
                strokeWidth = side.toPx() * .0012f)
            val fraction = ((ringPositionState.floatValue % entries.size + entries.size) %
                entries.size) / (entries.size - 1).coerceAtLeast(1)
            val markerX = startX + (endX - startX) * fraction
            drawLine(accent.copy(alpha = .70f),
                Offset(markerX - side.toPx() * .018f, railY),
                Offset(markerX + side.toPx() * .018f, railY),
                strokeWidth = side.toPx() * .0025f)
            drawCircle(accent.copy(alpha = .90f), side.toPx() * .0027f,
                Offset(markerX, railY))
        }
        val autoScrollShape = RoundedCornerShape(side * .018f)
        if (openStackRoot == null) Row(
            Modifier.graphicsLayer { alpha = 1f - .48f * stackReveal.value }
                .offset(x = side * .844f, y = side * .835f)
                .width(side * .084f).height(side * .040f)
                .clip(autoScrollShape)
                .background(Color.Black.copy(alpha = .32f))
                .border(side * .0008f, SignalWhite.copy(alpha = .35f), autoScrollShape)
                .clickable { HomeSettings.setCarouselAutoRotate(context, !autoRotate) }
                .semantics {
                    contentDescription = "Auto-scroll cards ${if (autoRotate) "on" else "off"}; tap to toggle"
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(side * .006f).clip(CircleShape)
                .background(if (autoRotate) accent else SignalGrey.copy(alpha = .65f)))
            Spacer(Modifier.width(side * .005f))
            Text(
                if (autoRotate) "AUTO" else "PAUSE",
                color = SignalWhite.copy(alpha = .88f),
                fontFamily = SignalMono,
                fontSize = (side.value * .009f).sp,
                maxLines = 1,
            )
        }
        if (categoryPickerOpen && openStackRoot == null) Box(
            Modifier.fillMaxSize().zIndex(30f)
                .background(Color.Black.copy(alpha = .34f))
                .clickable { categoryPickerOpen = false },
        ) {
            Column(
                Modifier.offset(x = side * .115f, y = side * .455f)
                    .width(side * .77f).height(side * .315f)
                    .clip(RoundedCornerShape(side * .018f))
                    .background(Brush.verticalGradient(listOf(
                        Color(0xFF20252A).copy(alpha = .985f),
                        Color(0xFF090C10).copy(alpha = .98f),
                    )))
                    .border(side * .001f, SignalWhite.copy(alpha = .38f),
                        RoundedCornerShape(side * .018f))
                    .clickable { },
            ) {
                Row(Modifier.fillMaxWidth().height(side * .050f)
                    .padding(horizontal = side * .022f),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("CATEGORIES", color = SignalWhite.copy(alpha = .86f),
                        fontFamily = SignalMono, fontSize = (side.value * .013f).sp,
                        letterSpacing = (.8).sp)
                    Spacer(Modifier.weight(1f))
                    Text("${selectedRingIndex.mod(entries.size) + 1} / ${entries.size}",
                        color = accent, fontFamily = SignalMono,
                        fontSize = (side.value * .011f).sp)
                    Spacer(Modifier.width(side * .025f))
                    Text("×", color = SignalWhite.copy(alpha = .75f),
                        fontFamily = SignalMono, fontSize = (side.value * .021f).sp,
                        modifier = Modifier.clickable { categoryPickerOpen = false })
                }
                LazyColumn(Modifier.fillMaxWidth().weight(1f)
                    .padding(horizontal = side * .012f, vertical = side * .008f)) {
                    items(entries.indices.chunked(3)) { row ->
                        Row(Modifier.fillMaxWidth().height(side * .047f)) {
                            repeat(3) { column ->
                                val index = row.getOrNull(column)
                                if (index == null) Spacer(Modifier.weight(1f))
                                else {
                                    val chosen = index == selectedRingIndex.mod(entries.size)
                                    Row(Modifier.weight(1f).fillMaxHeight()
                                        .clip(RoundedCornerShape(side * .007f))
                                        .background(if (chosen) accent.copy(alpha = .13f)
                                            else Color.Transparent)
                                        .clickable {
                                            categoryPickerOpen = false
                                            centreCard(index)
                                        }
                                        .padding(horizontal = side * .010f),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text((index + 1).toString().padStart(2, '0'),
                                            color = if (chosen) accent else SignalWhite.copy(alpha = .45f),
                                            fontFamily = SignalMono,
                                            fontSize = (side.value * .009f).sp)
                                        Spacer(Modifier.width(side * .006f))
                                        Text(entries[index].label.uppercase(),
                                            color = SignalWhite.copy(alpha = if (chosen) 1f else .80f),
                                            fontFamily = SignalMono,
                                            fontSize = (side.value * .011f).sp,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        editingCard?.let { selectedCard ->
            val selectedId = CarouselCardSettings.id(selectedCard.label)
            val selectedStackRootId = entries.firstOrNull { root ->
                stackCards(root).any { CarouselCardSettings.id(it.label) == selectedId }
            }?.let { CarouselCardSettings.id(it.label) } ?: selectedId
            val selectedCustomPackage = customEntries.firstOrNull { it.label == selectedCard.label }
                ?.let { custom -> CarouselCardSettings.customPackages(context).firstOrNull { packageName ->
                    AppCatalog.all(context).firstOrNull { it.packageName == packageName }?.label == custom.label
                } }
            val selectedCustomCard = CarouselCardSettings.customCards(context)
                .any { CarouselCardSettings.id(it) == selectedId }
            val stackMembers = allEntries.filter {
                CarouselCardSettings.stackParent(context, CarouselCardSettings.id(it.label)) == selectedId
            }
            val selectedAppPackage = CarouselCardSettings.tapAction(context, selectedId)
                ?.takeIf { it.startsWith("app:") }?.removePrefix("app:")
                ?: CarouselCardSettings.appOverride(context, selectedId)
                ?: selectedCustomPackage
            val allIds = (builtInEntries + customEntries).map { CarouselCardSettings.id(it.label) }
            val installed = remember { AppCatalog.all(context) }
            val matchingApps = remember(cardEditorQuery, installed) {
                installed.filter {
                    cardEditorQuery.isBlank() || it.label.contains(cardEditorQuery, true) ||
                        it.packageName.contains(cardEditorQuery, true)
                }.take(80)
            }
            Dialog(onDismissRequest = { editingCard = null }) {
                Column(
                    Modifier.fillMaxWidth().heightIn(max = 610.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xF2111418))
                        .border(1.dp, accent.copy(alpha = .55f), RoundedCornerShape(18.dp))
                        .padding(18.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            when (cardEditorPage) {
                                "apps" -> when (cardActionTarget) {
                                    "up" -> "SWIPE UP ACTION"
                                    "down" -> "SWIPE DOWN ACTION"
                                    else -> "TAP ACTION"
                                }
                                "action_text" -> when (cardActionTarget) {
                                    "up" -> "SWIPE UP TEXT"
                                    "down" -> "SWIPE DOWN TEXT"
                                    else -> "TAP TEXT"
                                }
                                "add" -> "ADD CARD"
                                "stack" -> "MOVE TO STACK"
                                "reorder" -> "REORDER STACK"
                                "icons" -> "CARD ICON"
                                "text" -> "CARD TEXT"
                                else -> selectedCard.label.uppercase()
                            },
                            color = SignalWhite, fontFamily = SignalMono, fontSize = 15.sp,
                            modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(if (cardEditorPage == "menu") "DONE" else "BACK", color = accent,
                            fontFamily = SignalMono, fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                if (cardEditorPage == "menu") editingCard = null
                                else { cardEditorPage = "menu"; cardEditorQuery = "" }
                            }.padding(6.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    if (cardEditorPage == "menu") {
                        val currentHome = CarouselCardSettings.home(context) == selectedId
                        val showingAppNotifications = selectedAppPackage != null &&
                            CarouselCardSettings.showAppNotifications(
                                context, selectedId, defaultValue = selectedCustomPackage != null,
                            )
                        val actions: List<Pair<String, () -> Unit>> = buildList {
                            addAll(listOf(
                            "＋  ADD CARD TO THIS STACK" to {
                                addTargetStackId = selectedStackRootId
                                newCardName = ""
                                cardEditorPage = "add"
                            },
                            "＋  ADD STANDALONE CARD" to {
                                addTargetStackId = null
                                newCardName = ""
                                cardEditorPage = "add"
                            },
                            "Aa  EDIT CARD TEXT" to { cardEditorPage = "text" },
                            "◈  CHOOSE ICON" to { cardEditorPage = "icons" },
                            "↗  CHOOSE TAP ACTION / APP" to {
                                cardActionTarget = "tap"; cardEditorPage = "apps"
                            },
                            "←  MOVE LEFT" to {
                                CarouselCardSettings.move(context, selectedId, -1, allIds); cardSettingsRevision++
                            },
                            "→  MOVE RIGHT" to {
                                CarouselCardSettings.move(context, selectedId, 1, allIds); cardSettingsRevision++
                            },
                            (if (currentHome) "●  HOME CARD" else "○  MAKE HOME CARD") to {
                                CarouselCardSettings.setHome(context, selectedId); cardSettingsRevision++
                            },
                            ))
                            if ((entries.firstOrNull {
                                    CarouselCardSettings.id(it.label) == selectedStackRootId
                                }?.let(::stackCards)?.size ?: 0) > 1) add(
                                "↕  REORDER THIS STACK" to { cardEditorPage = "reorder" },
                            )
                            if (selectedCustomPackage != null || selectedCustomCard) add(
                                "▤  MOVE CARD TO ANOTHER STACK" to { cardEditorPage = "stack" },
                            )
                            if (stackMembers.isNotEmpty()) add(
                                "◇  UNGROUP THIS STACK" to {
                                    stackMembers.forEach { member ->
                                        CarouselCardSettings.setStackParent(
                                            context, CarouselCardSettings.id(member.label), null,
                                        )
                                    }
                                    stackExpandedRootId = null
                                    cardSettingsRevision++
                                    editingCard = null
                                },
                            )
                            if (CarouselCardSettings.swipeUpAction(context, selectedId) != null) add(
                                "×  REMOVE OLD UP-ACTION CARD" to {
                                    CarouselCardSettings.setSwipeUpAction(context, selectedId, null)
                                    cardSettingsRevision++; editingCard = null
                                },
                            )
                            if (CarouselCardSettings.swipeDownAction(context, selectedId) != null) add(
                                "×  REMOVE OLD DOWN-ACTION CARD" to {
                                    CarouselCardSettings.setSwipeDownAction(context, selectedId, null)
                                    cardSettingsRevision++; editingCard = null
                                },
                            )
                            if (selectedAppPackage != null) add(
                                (if (showingAppNotifications) "◉  SHOWING APP NOTIFICATIONS" else "○  SHOW APP NOTIFICATIONS") to {
                                    CarouselCardSettings.setShowAppNotifications(
                                        context, selectedId, !showingAppNotifications,
                                    )
                                    cardSettingsRevision++
                                },
                            )
                            add(
                            (if (selectedCustomPackage != null || selectedCustomCard) "×  REMOVE CARD" else "−  HIDE CARD") to {
                                if (selectedCustomPackage != null) {
                                    CarouselCardSettings.removeCustom(context, selectedCustomPackage)
                                } else if (selectedCustomCard) {
                                    CarouselCardSettings.removeCustomCard(context, selectedId)
                                } else CarouselCardSettings.setVisible(context, selectedId, false)
                                cardSettingsRevision++
                                editingCard = null
                                Unit
                            },
                            )
                        }
                        LazyColumn {
                            items(actions.size) { index ->
                                val (label, action) = actions[index]
                                Text(label, color = if (label.startsWith("×") || label.startsWith("−")) Color(0xFFFF8D88) else SignalWhite,
                                    fontFamily = SignalMono, fontSize = 13.sp,
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                        .clickable(onClick = action).padding(vertical = 13.dp, horizontal = 10.dp))
                            }
                        }
                    } else if (cardEditorPage == "reorder") {
                        val root = entries.firstOrNull {
                            CarouselCardSettings.id(it.label) == selectedStackRootId
                        }
                        val members = root?.let(::stackCards).orEmpty()
                        Text("Set the card order. The first card is the closed stack's front face.",
                            color = SignalGrey, fontFamily = SignalMono, fontSize = 11.sp)
                        Spacer(Modifier.height(10.dp))
                        LazyColumn {
                            items(members.size) { index ->
                                val member = members[index]
                                val memberId = CarouselCardSettings.id(member.label)
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("${index + 1}  ${CarouselCardSettings.title(context, memberId) ?: member.label}",
                                        color = SignalWhite, fontFamily = SignalMono, fontSize = 12.sp,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f))
                                    Text("↑", color = if (index > 0) accent else SignalGrey,
                                        fontFamily = SignalMono, fontSize = 18.sp,
                                        modifier = Modifier.clickable(enabled = index > 0) {
                                            CarouselCardSettings.moveInStack(context, selectedStackRootId,
                                                memberId, -1, members.map { CarouselCardSettings.id(it.label) })
                                            stackCardIndex[selectedStackRootId] = 0
                                            cardSettingsRevision++
                                        }.padding(horizontal = 10.dp, vertical = 7.dp))
                                    Text("↓", color = if (index < members.lastIndex) accent else SignalGrey,
                                        fontFamily = SignalMono, fontSize = 18.sp,
                                        modifier = Modifier.clickable(enabled = index < members.lastIndex) {
                                            CarouselCardSettings.moveInStack(context, selectedStackRootId,
                                                memberId, 1, members.map { CarouselCardSettings.id(it.label) })
                                            stackCardIndex[selectedStackRootId] = 0
                                            cardSettingsRevision++
                                        }.padding(horizontal = 10.dp, vertical = 7.dp))
                                }
                            }
                        }
                    } else if (cardEditorPage == "stack") {
                        Text("Choose the ring position for this card. Vertical swipes cycle its stack.",
                            color = SignalGrey, fontFamily = SignalMono, fontSize = 11.sp)
                        Spacer(Modifier.height(10.dp))
                        LazyColumn {
                            item {
                                Text("STANDALONE RING CARD", color = accent, fontFamily = SignalMono,
                                    fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
                                        CarouselCardSettings.setStackParent(context, selectedId, null)
                                        cardSettingsRevision++; editingCard = null
                                    }.padding(vertical = 12.dp))
                            }
                            items(allEntries.filter { candidate ->
                                CarouselCardSettings.id(candidate.label) != selectedId &&
                                    CarouselCardSettings.stackParent(
                                        context, CarouselCardSettings.id(candidate.label),
                                    ) == null
                            }) { candidate ->
                                Text(candidate.label.uppercase(), color = SignalWhite, fontFamily = SignalMono,
                                    fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
                                        CarouselCardSettings.setStackParent(
                                            context, selectedId, CarouselCardSettings.id(candidate.label),
                                        )
                                        cardSettingsRevision++; editingCard = null
                                    }.padding(vertical = 12.dp))
                            }
                        }
                    } else if (cardEditorPage == "text") {
                        @Composable fun TextOverrideField(label: String, value: String, update: (String) -> Unit) {
                            Text(label, color = SignalGrey, fontFamily = SignalMono, fontSize = 10.sp)
                            Spacer(Modifier.height(5.dp))
                            BasicTextField(
                                value = value,
                                onValueChange = update,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = SignalWhite, fontFamily = SignalMono, fontSize = 13.sp,
                                ),
                                cursorBrush = SolidColor(accent),
                                decorationBox = { inner ->
                                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                        .background(Color.White.copy(alpha = .07f)).padding(11.dp)) { inner() }
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        Text("Leave a field blank to keep its live Commander content.",
                            color = SignalGrey, fontFamily = SignalMono, fontSize = 10.sp)
                        Spacer(Modifier.height(10.dp))
                        TextOverrideField("CARD TITLE", cardTitleDraft) { cardTitleDraft = it }
                        TextOverrideField("TOP LABEL", cardTopDraft) { cardTopDraft = it }
                        TextOverrideField("SUBTEXT", cardDetailDraft) { cardDetailDraft = it }
                        Text("SAVE TEXT", color = accent, fontFamily = SignalMono, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().clickable {
                                CarouselCardSettings.setText(
                                    context, selectedId, cardTitleDraft, cardDetailDraft, cardTopDraft,
                                )
                                cardSettingsRevision++
                                editingCard = null
                            }.padding(vertical = 12.dp))
                        Text("RESET TO LIVE DEFAULTS", color = SignalWhite.copy(alpha = .72f),
                            fontFamily = SignalMono, fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth().clickable {
                                CarouselCardSettings.setText(context, selectedId, null, null, null)
                                cardSettingsRevision++
                                editingCard = null
                            }.padding(vertical = 10.dp))
                    } else if (cardEditorPage == "icons") {
                        Text("${HomeSettings.glyphStyle.value.label.uppercase()}  /  choose a glyph inside this pack",
                            color = SignalGrey, fontFamily = SignalMono, fontSize = 10.sp)
                        Spacer(Modifier.height(8.dp))
                        if (HomeSettings.glyphStyle.value == HomeGlyphStyle.Phosphor) {
                            BasicTextField(
                                value = cardEditorQuery,
                                onValueChange = { cardEditorQuery = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = SignalWhite, fontFamily = SignalMono, fontSize = 13.sp,
                                ),
                                cursorBrush = SolidColor(accent),
                                decorationBox = { inner ->
                                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                        .background(Color.White.copy(alpha = .07f)).padding(11.dp)) {
                                        if (cardEditorQuery.isBlank()) Text(
                                            "search all Phosphor icons", color = SignalGrey,
                                            fontFamily = SignalMono, fontSize = 12.sp,
                                        )
                                        inner()
                                    }
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        val phosphorEntries by produceState<List<PhosphorCatalogEntry>>(
                            initialValue = emptyList(),
                            key1 = HomeSettings.glyphStyle.value,
                        ) {
                            value = if (HomeSettings.glyphStyle.value == HomeGlyphStyle.Phosphor) {
                                PhosphorIconCatalog.entries(context)
                            } else emptyList()
                        }
                        val iconChoices = remember(
                            HomeSettings.glyphStyle.value, phosphorEntries, cardEditorQuery,
                        ) {
                            if (HomeSettings.glyphStyle.value == HomeGlyphStyle.Phosphor) {
                                phosphorEntries.filter {
                                    cardEditorQuery.isBlank() || it.label.contains(cardEditorQuery, true)
                                }.map { it.key to it.label }
                            } else carouselGlyphChoices
                        }
                        LazyColumn {
                            item {
                                Text("DEFAULT CARD GLYPH", color = accent, fontFamily = SignalMono, fontSize = 11.sp,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        CarouselCardSettings.setGlyph(context, selectedId, null)
                                        cardSettingsRevision++; editingCard = null
                                    }.padding(vertical = 12.dp))
                            }
                            items(iconChoices.chunked(4)) { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    row.forEach { choice ->
                                        val selected = CarouselCardSettings.glyph(context, selectedId) == choice.first
                                        Column(Modifier.width(68.dp).clip(RoundedCornerShape(9.dp))
                                            .background(if (selected) accent.copy(alpha = .14f) else Color.Transparent)
                                            .clickable {
                                                CarouselCardSettings.setGlyph(context, selectedId, choice.first)
                                                cardSettingsRevision++
                                            }.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(packedCommanderGlyph(choice.first, HomeSettings.glyphStyle.value, selectedCard.icon),
                                                choice.second, tint = if (selected) accent else SignalWhite,
                                                modifier = Modifier.size(27.dp))
                                            Text(choice.second, color = SignalGrey, fontFamily = SignalMono,
                                                fontSize = 8.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cardEditorPage == "action_text") {
                        Text(
                            "Commander Bar will open with this text ready to run or edit.",
                            color = SignalGrey, fontFamily = SignalMono, fontSize = 10.sp,
                        )
                        Spacer(Modifier.height(10.dp))
                        BasicTextField(
                            value = cardActionTextDraft,
                            onValueChange = { cardActionTextDraft = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = SignalWhite, fontFamily = SignalMono, fontSize = 14.sp,
                            ),
                            cursorBrush = SolidColor(accent),
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                    .background(Color.White.copy(alpha = .07f)).padding(12.dp)) {
                                    if (cardActionTextDraft.isBlank()) Text(
                                        "text to put into Commander Bar", color = SignalGrey,
                                        fontFamily = SignalMono, fontSize = 12.sp,
                                    )
                                    inner()
                                }
                            },
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "SAVE ACTION", color = accent, fontFamily = SignalMono, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().clickable {
                                val encoded = cardActionTextDraft.trim().takeIf(String::isNotBlank)
                                    ?.let { "input:$it" }
                                when (cardActionTarget) {
                                    "up" -> CarouselCardSettings.setSwipeUpAction(context, selectedId, encoded)
                                    "down" -> CarouselCardSettings.setSwipeDownAction(context, selectedId, encoded)
                                    else -> {
                                        CarouselCardSettings.setTapAction(context, selectedId, encoded)
                                        CarouselCardSettings.setAppOverride(context, selectedId, null)
                                    }
                                }
                                cardSettingsRevision++
                                editingCard = null
                            }.padding(vertical = 12.dp),
                        )
                    } else {
                        if (cardEditorPage == "add") {
                            Text("NEW CARD WITHOUT AN APP", color = accent, fontFamily = SignalMono,
                                fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            BasicTextField(
                                value = newCardName,
                                onValueChange = { newCardName = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = SignalWhite, fontFamily = SignalMono, fontSize = 14.sp,
                                ),
                                cursorBrush = SolidColor(accent),
                                decorationBox = { inner ->
                                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                        .background(Color.White.copy(alpha = .07f)).padding(12.dp)) {
                                        if (newCardName.isBlank()) Text("card name", color = SignalGrey,
                                            fontFamily = SignalMono, fontSize = 13.sp)
                                        inner()
                                    }
                                },
                            )
                            Text("CREATE CARD", color = accent, fontFamily = SignalMono,
                                fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
                                    val newId = CarouselCardSettings.addCustomCard(
                                        context, newCardName, allIds.toSet(),
                                    )
                                    CarouselCardSettings.setStackParent(context, newId, addTargetStackId)
                                    cardSettingsRevision++
                                    editingCard = null
                                }.padding(vertical = 12.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("OR ADD AN APP CARD", color = SignalGrey, fontFamily = SignalMono,
                                fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                        }
                        BasicTextField(
                            value = cardEditorQuery, onValueChange = { cardEditorQuery = it }, singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = SignalWhite, fontFamily = SignalMono, fontSize = 14.sp),
                            cursorBrush = SolidColor(accent),
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                    .background(Color.White.copy(alpha = .07f)).padding(12.dp)) {
                                    if (cardEditorQuery.isBlank()) Text("search apps", color = SignalGrey, fontFamily = SignalMono, fontSize = 13.sp)
                                    inner()
                                }
                            },
                        )
                        if (cardEditorPage == "apps") {
                            val builtInActions = if (cardActionTarget == "tap") {
                                listOf("USE COMMANDER DEFAULT" to null)
                            } else listOf(
                                "NO ACTION" to null,
                                "SAME AS CARD TAP" to "tap",
                                "OPEN HUB" to "hub",
                                "OPEN APPS" to "apps",
                                "OPEN COMMANDER SETTINGS" to "settings",
                                "OPEN WALLPAPER PICKER" to "wallpaper",
                                "OPEN COMMANDER BAR" to "trigger:?",
                            )
                            builtInActions.forEach { (label, encoded) ->
                                Text(label, color = accent, fontFamily = SignalMono, fontSize = 11.sp,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        when (cardActionTarget) {
                                            "up" -> CarouselCardSettings.setSwipeUpAction(context, selectedId, encoded)
                                            "down" -> CarouselCardSettings.setSwipeDownAction(context, selectedId, encoded)
                                            else -> {
                                                CarouselCardSettings.setTapAction(context, selectedId, null)
                                                CarouselCardSettings.setAppOverride(context, selectedId, null)
                                            }
                                        }
                                        cardSettingsRevision++; editingCard = null
                                    }.padding(vertical = 10.dp))
                            }
                            Text("⌨  INPUT TEXT INTO COMMANDER BAR", color = accent,
                                fontFamily = SignalMono, fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    cardActionTextDraft = ""
                                    cardEditorPage = "action_text"
                                }.padding(vertical = 10.dp))
                        }
                        LazyColumn {
                            items(matchingApps, key = { it.packageName }) { app ->
                                Row(Modifier.fillMaxWidth().height(52.dp).clickable {
                                    if (cardEditorPage == "add") {
                                        CarouselCardSettings.addCustom(context, app.packageName)
                                        CarouselCardSettings.setStackParent(
                                            context, CarouselCardSettings.id(app.label), addTargetStackId,
                                        )
                                    }
                                    else when (cardActionTarget) {
                                        "up" -> CarouselCardSettings.setSwipeUpAction(context, selectedId, "app:${app.packageName}")
                                        "down" -> CarouselCardSettings.setSwipeDownAction(context, selectedId, "app:${app.packageName}")
                                        else -> {
                                            CarouselCardSettings.setTapAction(context, selectedId, "app:${app.packageName}")
                                            CarouselCardSettings.setAppOverride(context, selectedId, null)
                                        }
                                    }
                                    cardSettingsRevision++
                                    editingCard = null
                                }, verticalAlignment = Alignment.CenterVertically) {
                                    InstalledAppIcon(app, Modifier.size(30.dp))
                                    Text(app.label, color = SignalWhite, fontFamily = SignalMono, fontSize = 13.sp,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 12.dp).weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.graphicsLayer { alpha = 1f - .48f * stackReveal.value }
            .offset(x = side * .065f, y = side * .930f).width(side * .87f),
            horizontalArrangement = Arrangement.SpaceBetween) {
            if (ModuleSettings.hubEnabled.value) Text("← HUB", color = SignalWhite.copy(alpha = .55f), fontFamily = font,
                fontSize = (side.value * .016f).sp,
                modifier = Modifier.clickable { (onOpenHub ?: { context.openCommanderSurface(HubActivity::class.java) })() })
            Text("APPS →", color = SignalWhite.copy(alpha = .55f), fontFamily = font,
                fontSize = (side.value * .016f).sp,
                modifier = Modifier.clickable { (onOpenApps ?: { context.openCommanderSurface(AppsActivity::class.java) })() })
        }
        if (visibleStackRootId == null) FavoriteAppsDock(
            side = side,
            accent = accent,
            isActiveSurface = isActiveSurface,
            badgeCounts = remember(hubItems) {
                hubItems.asSequence().filter { !it.isSummary }
                    .groupingBy(HubItem::packageName).eachCount()
            },
            onBeforeExternalLaunch = onBeforeExternalLaunch,
        )
        // Expanding a category keeps the horizontal ring in place underneath,
        // but opens its cards into a readable vertical track. All three faces
        // share one animated stack coordinate; none is a cropped edge of the
        // focused card or a separate modal window.
        if (editingCard == null) visibleStackRootId?.let { expandedId ->
            val root = entries.firstOrNull {
                CarouselCardSettings.id(it.label) == expandedId
            }
            if (root != null && stackCards(root).size > 1) {
                val cards = stackCards(root)
                val current = (stackCardIndex[expandedId] ?: 0).mod(cards.size)
                val selectedIndex = entries.indexOf(root)
                val overlayCardWidth = side * .37f
                val overlayCardHeight = side * .325f
                val overlayX = (side - overlayCardWidth) / 2
                val stackRingRadiusPx = with(LocalDensity.current) { side.toPx() } * ringRadiusFraction
                // This face occupies the same full-size physical slot as the
                // lowered ring's focused card.
                val overlayY = side * (.469f + .170f * stackReveal.value)
                var overlayDrag by remember(expandedId) { mutableFloatStateOf(0f) }
                Box(
                    Modifier.fillMaxSize()
                        .graphicsLayer {
                            val floatWave = kotlin.math.sin(
                                idlePhaseState.floatValue * 2f * Math.PI,
                            ).toFloat()
                            translationY = floatWave * side.toPx() * .0014f *
                                idleStrengthState.floatValue
                        }
                        .drawWithContent {
                            drawRect(Color.Black.copy(alpha = .08f * stackReveal.value))
                            drawContent()
                        }
                        .clickable { stackExpandedRootId = null }
                        .pointerInput(expandedId) {
                            detectVerticalDragGestures(
                                onDragStart = { overlayDrag = 0f },
                                onVerticalDrag = { change, amount ->
                                    change.consume()
                                    overlayDrag += amount
                                },
                                onDragEnd = {
                                    if (kotlin.math.abs(overlayDrag) > size.height * .07f) {
                                        cycleStack(root, if (overlayDrag < 0f) -1 else 1)
                                    }
                                    overlayDrag = 0f
                                },
                                onDragCancel = { overlayDrag = 0f },
                            )
                        },
                ) {
                    // Render only real stack members. One additional, fully
                    // hidden copy beyond the farthest visible face is kept
                    // ready so it can enter continuously during a swipe.
                    val farthestVisible = (cards.size - 1).coerceAtMost(3)
                    (listOf(-1) + (farthestVisible + 1 downTo 0)).forEach { slot ->
                        val cardIndex = (current + slot).mod(cards.size)
                        val stacked = cards[cardIndex]
                        val stackedId = CarouselCardSettings.id(stacked.label)
                        val title = CarouselCardSettings.title(context, stackedId) ?: stacked.label
                        val detail = CarouselCardSettings.detail(context, stackedId) ?: stacked.detail
                        val stackPitch = if (HomeSettings.carouselView.value == CarouselViewMode.Elevated) {
                            HomeSettings.carouselPitch.floatValue
                        } else 0f
                        // A two- or three-card loop can contain the same card
                        // at opposite ends of the track. Distinguish those
                        // hidden copies while keeping each visible face stable.
                        if (slot in -1..1) Box(
                            Modifier.offset(x = overlayX, y = overlayY)
                                .size(overlayCardWidth, overlayCardHeight)
                                .graphicsLayer {
                                    val travel = if (stackMotionRoot == expandedId)
                                        -stackMotionDirection * stackMotion.value else 0f
                                    val position = slot + travel
                                    translationY = -position * side.toPx() * .15f *
                                        stackReveal.value
                                    val depth = position.coerceIn(0f, 3f)
                                    val recede = 1f - depth * .12f * stackReveal.value
                                    scaleX = recede
                                    scaleY = recede
                                    rotationX = 11f * stackReveal.value
                                    cameraDistance = side.toPx() * 9f
                                    alpha = stackReveal.value *
                                        (1f - kotlin.math.abs(position)).coerceIn(0f, 1f) *
                                        HomeSettings.cardGlow.floatValue
                                }
                                .orbitPanelProjection(0f, stackRingRadiusPx, stackPitch)
                                .blur(side * .011f, BlurredEdgeTreatment.Unbounded)
                                .border(side * .007f,
                                    lerp(accent, Color.White, .24f),
                                    RoundedCornerShape(side * .012f)),
                        )
                        key(stackedId, slot) { StackCompanionCard(
                            modifier = Modifier.offset(x = overlayX, y = overlayY)
                                .size(overlayCardWidth, overlayCardHeight)
                                .graphicsLayer {
                                    val travel = if (stackMotionRoot == expandedId) {
                                        -stackMotionDirection * stackMotion.value
                                    } else 0f
                                    val position = slot + travel
                                    val depth = position.coerceIn(0f, 3f)
                                    val trackPosition = if (position > farthestVisible)
                                        farthestVisible +
                                            (position - farthestVisible) * .03f
                                    else position
                                    translationY = -trackPosition * side.toPx() * .15f *
                                        stackReveal.value
                                    val recede = 1f - depth * .12f * stackReveal.value
                                    scaleX = recede
                                    scaleY = recede
                                    rotationX = 11f * stackReveal.value
                                    cameraDistance = side.toPx() * 9f
                                    val edgeFade = when {
                                        position < 0f -> (1f + position).coerceIn(0f, 1f)
                                        position > farthestVisible ->
                                            (farthestVisible + 1f - position)
                                                .coerceIn(0f, 1f) * .60f
                                        else -> 1f
                                    }
                                    alpha = stackReveal.value * edgeFade
                                }
                                .orbitPanelProjection(0f, stackRingRadiusPx, stackPitch)
                                .clip(RoundedCornerShape(side * .012f))
                                .drawWithContent {
                                    drawRect(Color(0xFF080A0D).copy(
                                        alpha = .51f))
                                    drawContent()
                                }
                                .drawWithContent {
                                    drawContent()
                                    val travel = if (stackMotionRoot == expandedId)
                                        -stackMotionDirection * stackMotion.value else 0f
                                    val focus = (1f - kotlin.math.abs(slot + travel))
                                        .coerceIn(0f, 1f)
                                    drawRoundRect(
                                        color = lerp(SignalWhite, accent, .22f + .33f * focus)
                                            .copy(alpha = .60f + .30f * focus),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            side.toPx() * .012f),
                                        style = Stroke(width = side.toPx() *
                                            (.00135f + .00055f * focus)),
                                    )
                                }
                                .combinedClickable(
                                    onClick = {
                                        if (slot == 0) shownCard(root).action()
                                        else cycleStack(root, if (slot < 0) -1 else 1)
                                    },
                                    onLongClick = {
                                        stackExpandedRootId = null
                                        editingCard = stacked
                                        cardEditorPage = "menu"
                                        cardEditorQuery = ""
                                        cardTitleDraft = CarouselCardSettings.title(context, stackedId).orEmpty()
                                        cardDetailDraft = CarouselCardSettings.detail(context, stackedId).orEmpty()
                                        cardTopDraft = CarouselCardSettings.topText(context, stackedId).orEmpty()
                                    },
                                ),
                            side = side,
                            index = selectedIndex,
                            title = title,
                            detail = detail,
                            leftHeader = title.uppercase(),
                            rightHeader = "${cardIndex + 1}/${cards.size}",
                            icon = stacked.icon,
                            artwork = if (stacked.label == "Media") mediaArtwork else null,
                            taskCount = taskCount,
                            showTaskCounter = stacked.label == "Tasks" && HomeSettings.tasksDotCount.value,
                            font = font,
                            accent = accent,
                        ) }
                    }
                    // A quiet fixed-position scale shows both the number of
                    // cards and the current position without adding another
                    // label to the moving glass faces.
                    Box(Modifier.offset(x = side * .705f, y = side * .340f)
                        .size(side * .055f, side * .180f)
                        .graphicsLayer { alpha = stackReveal.value }) {
                        Canvas(Modifier.fillMaxSize()) {
                            val railX = size.width * .16f
                            val railTop = size.height * .04f
                            val railBottom = size.height * .80f
                            drawLine(SignalWhite.copy(alpha = .20f),
                                Offset(railX, railTop), Offset(railX, railBottom),
                                strokeWidth = side.toPx() * .0012f)
                            repeat(cards.size) { item ->
                                val fraction = if (cards.size == 1) 0f
                                    else item.toFloat() / (cards.size - 1)
                                val tickY = railTop + (railBottom - railTop) * fraction
                                if (item == current) {
                                    drawCircle(
                                        Brush.radialGradient(listOf(
                                            accent.copy(alpha = .55f), Color.Transparent),
                                            center = Offset(railX + size.width * .12f, tickY),
                                            radius = size.width * .58f,
                                        ),
                                        radius = size.width * .58f,
                                        center = Offset(railX + size.width * .12f, tickY),
                                    )
                                }
                                drawLine(
                                    if (item == current) lerp(accent, SignalWhite, .32f)
                                    else SignalWhite.copy(alpha = .40f),
                                    Offset(railX, tickY),
                                    Offset(railX + size.width *
                                        if (item == current) .43f else .18f, tickY),
                                    strokeWidth = side.toPx() *
                                        if (item == current) .0027f else .0012f,
                                )
                            }
                        }
                        Text("${current + 1}/${cards.size}",
                            color = SignalWhite.copy(alpha = .76f),
                            fontFamily = SignalMono,
                            fontSize = (side.value * .010f).sp,
                            modifier = Modifier.offset(y = side * .149f))
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledAppIcon(app: AppCatalogEntry, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(app.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(app.packageName)
                .toBitmap(96, 96).asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = app.label,
            modifier = modifier.clip(RoundedCornerShape(7.dp)),
            contentScale = ContentScale.Fit,
        )
    } else {
        Icon(signalDockGlyph(app), app.label, tint = SignalWhite, modifier = modifier)
    }
}

@Composable
private fun FavoriteAppsDock(
    side: Dp,
    accent: Color,
    isActiveSurface: Boolean,
    badgeCounts: Map<String, Int>,
    onBeforeExternalLaunch: () -> Unit,
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(FavoriteApps.resolved(context)) }
    var editing by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var glyphQuery by remember { mutableStateOf("") }
    var glyphTarget by remember { mutableStateOf<AppCatalogEntry?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    LaunchedEffect(isActiveSurface) {
        if (isActiveSurface) apps = FavoriteApps.resolved(context)
    }
    val dockWidthFraction = if (apps.size > 7) .58f else .45f
    Box(
        Modifier.offset(x = side * ((1f - dockWidthFraction) / 2f), y = side * .910f)
            .width(side * dockWidthFraction).height(side * .160f)
            .clip(RoundedCornerShape(
                topStart = side * .012f,
                topEnd = side * .012f,
                bottomStart = 0.dp,
                bottomEnd = 0.dp,
            ))
            .then(if (apps.isNotEmpty()) Modifier.background(Color.Black.copy(alpha = .24f)).border(
                side * .0012f, SignalWhite.copy(alpha = .38f), RoundedCornerShape(
                    topStart = side * .012f, topEnd = side * .012f,
                    bottomStart = 0.dp, bottomEnd = 0.dp,
                ),
            ) else Modifier)
            .combinedClickable(onClick = {}, onLongClick = { editing = true }),
    ) {
        if (apps.isNotEmpty()) Canvas(Modifier.matchParentSize()) {
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = .085f), Color.White.copy(alpha = .018f), Color.Transparent),
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(accent.copy(alpha = .14f), accent.copy(alpha = .025f), Color.Transparent),
                    center = Offset(size.width * .5f, size.height * .15f),
                    radius = size.width * .55f,
                ),
            )
        }
        Row(
            Modifier.fillMaxSize().padding(horizontal = side * .014f, vertical = side * .012f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            apps.forEach { app ->
                DockAppIcon(
                    app = app,
                    icon = signalDockGlyph(app, FavoriteApps.glyph(context, app.packageName)),
                    accent = accent,
                    size = side * .040f,
                    badgeCount = badgeCounts[app.packageName] ?: 0,
                    modifier = Modifier.offset(y = side * .009f),
                    onLongClick = { editing = true },
                ) {
                    val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launch != null) {
                        onBeforeExternalLaunch()
                        context.getSharedPreferences("commander_apps", Context.MODE_PRIVATE).edit()
                            .putLong("recent_${app.packageName}", System.currentTimeMillis()).apply()
                        context.startActivity(launch)
                    }
                }
            }
        }
        if (apps.isEmpty()) Text(
            "+ DOCK",
            color = SignalWhite.copy(alpha = .28f),
            fontFamily = SignalMono,
            fontSize = (side.value * .010f).sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = side * .018f),
        )
    }
    if (editing) {
        val installed = remember { AppCatalog.all(context) }
        val chosen = remember(revision) { FavoriteApps.pinned(context) }
        val visible = remember(query, installed, chosen) {
            installed.filter { query.isBlank() || it.label.contains(query, true) || it.packageName.contains(query, true) }
                .sortedWith(compareBy<AppCatalogEntry> {
                    chosen.indexOf(it.packageName).let { position -> if (position < 0) Int.MAX_VALUE else position }
                }.thenBy { it.label.lowercase(Locale.ROOT) })
        }
        Dialog(onDismissRequest = { editing = false }) {
            Column(
                Modifier.fillMaxWidth().height(580.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF2111418))
                    .border(1.dp, accent.copy(alpha = .52f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (glyphTarget == null) "DOCK  /  ${chosen.size} OF 9" else "GLYPH  /  ${glyphTarget?.label}",
                        color = SignalWhite, fontFamily = SignalMono, fontSize = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(if (glyphTarget == null) "DONE" else "BACK", color = accent,
                        fontFamily = SignalMono, fontSize = 13.sp,
                        modifier = Modifier.clickable {
                            if (glyphTarget != null) glyphTarget = null else editing = false
                        })
                }
                if (glyphTarget != null) {
                    val target = glyphTarget!!
                    Spacer(Modifier.height(18.dp))
                    Text("Choose an icon for ${target.label}", color = SignalGrey,
                        fontFamily = SignalMono, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    if (HomeSettings.glyphStyle.value == HomeGlyphStyle.Phosphor) {
                        BasicTextField(
                            value = glyphQuery,
                            onValueChange = { glyphQuery = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = SignalWhite, fontFamily = SignalMono, fontSize = 13.sp,
                            ),
                            cursorBrush = SolidColor(accent),
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                    .background(Color.White.copy(alpha = .07f)).padding(11.dp)) {
                                    if (glyphQuery.isBlank()) Text(
                                        "search all Phosphor icons", color = SignalGrey,
                                        fontFamily = SignalMono, fontSize = 12.sp,
                                    )
                                    inner()
                                }
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    val phosphorEntries by produceState<List<PhosphorCatalogEntry>>(
                        initialValue = emptyList(),
                        key1 = HomeSettings.glyphStyle.value,
                    ) {
                        value = if (HomeSettings.glyphStyle.value == HomeGlyphStyle.Phosphor) {
                            PhosphorIconCatalog.entries(context)
                        } else emptyList()
                    }
                    val glyphChoices = remember(
                        HomeSettings.glyphStyle.value, phosphorEntries, glyphQuery,
                    ) {
                        if (HomeSettings.glyphStyle.value == HomeGlyphStyle.Phosphor) {
                            phosphorEntries.filter {
                                glyphQuery.isBlank() || it.label.contains(glyphQuery, true)
                            }.map { it.key to it.label }
                        } else dockGlyphChoices
                    }
                    LazyColumn {
                        items(glyphChoices.chunked(4)) { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                row.forEach { choice ->
                                    val active = FavoriteApps.glyph(context, target.packageName) == choice.first
                                    Column(Modifier.width(68.dp).padding(vertical = 9.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) accent.copy(alpha = .16f) else Color.White.copy(alpha = .035f))
                                        .clickable {
                                            FavoriteApps.setGlyph(context, target.packageName, choice.first)
                                            revision++
                                            apps = FavoriteApps.resolved(context)
                                            glyphQuery = ""
                                            glyphTarget = null
                                        }.padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(signalDockGlyph(target, choice.first), choice.second,
                                            tint = if (active) accent else SignalWhite, modifier = Modifier.size(27.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text(choice.second, color = SignalGrey, fontFamily = SignalMono,
                                            fontSize = 9.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                } else {
                Spacer(Modifier.height(12.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = SignalWhite, fontFamily = SignalMono, fontSize = 15.sp),
                    cursorBrush = SolidColor(accent),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                            .background(Color.White.copy(alpha = .07f)).padding(12.dp)) {
                            if (query.isBlank()) Text("search apps", color = SignalGrey, fontFamily = SignalMono, fontSize = 14.sp)
                            inner()
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
                Text("Pinned apps first · hold and drag to reorder · tap glyph for icon", color = SignalGrey,
                    fontFamily = SignalMono, fontSize = 11.sp)
                LazyColumn {
                    items(visible, key = { it.packageName }) { app ->
                        var dragDistance by remember(app.packageName) { mutableFloatStateOf(0f) }
                        val pinned = app.packageName in chosen
                        Row(
                            Modifier.fillMaxWidth().height(58.dp)
                                .then(if (pinned) Modifier.pointerInput(app.packageName, chosen) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { dragDistance = 0f },
                                        onDragCancel = { dragDistance = 0f },
                                        onDragEnd = { dragDistance = 0f },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragDistance += amount.y
                                            val threshold = 34.dp.toPx()
                                            if (abs(dragDistance) >= threshold) {
                                                val order = FavoriteApps.pinned(context)
                                                val from = order.indexOf(app.packageName)
                                                val to = (from + if (dragDistance > 0f) 1 else -1)
                                                    .coerceIn(0, order.lastIndex)
                                                if (from >= 0 && to != from) {
                                                    FavoriteApps.move(context, app.packageName, to)
                                                    revision++
                                                    apps = FavoriteApps.resolved(context)
                                                }
                                                dragDistance = 0f
                                            }
                                        },
                                    )
                                } else Modifier),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(34.dp).clickable {
                                    glyphQuery = ""
                                    glyphTarget = app
                                }, contentAlignment = Alignment.Center) {
                                InstalledAppIcon(app, Modifier.size(30.dp))
                            }
                            Text(app.label, color = SignalWhite, fontFamily = SignalMono,
                                fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).clickable {
                                    FavoriteApps.toggle(context, app.packageName)
                                    revision++
                                    apps = FavoriteApps.resolved(context)
                                }.padding(horizontal = 12.dp))
                            Text(if (app.packageName in chosen) "✓" else "+",
                                color = if (app.packageName in chosen) accent else SignalGrey,
                                fontFamily = SignalMono, fontSize = 19.sp,
                                modifier = Modifier.clickable {
                                    FavoriteApps.toggle(context, app.packageName)
                                    revision++
                                    apps = FavoriteApps.resolved(context)
                                }.padding(6.dp))
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun DockAppIcon(
    app: AppCatalogEntry,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    size: Dp,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    Box(modifier.size(size * 1.30f).combinedClickable(onClick = onClick, onLongClick = onLongClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = accent.copy(alpha = .58f),
            modifier = Modifier.size(size * 1.08f).blur(size * .22f, BlurredEdgeTreatment.Unbounded))
        Icon(icon, app.label, tint = lerp(SignalWhite, accent, .20f), modifier = Modifier.size(size * .82f))
        if (badgeCount > 0) {
            Text(
                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                color = SignalWhite,
                fontFamily = SignalMono,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * .24f).sp,
                lineHeight = (size.value * .29f).sp,
                modifier = Modifier.align(Alignment.TopEnd)
                    .offset(x = -size * .03f, y = size * .05f)
                    .background(Color.Black.copy(alpha = .66f), CircleShape)
                    .border(1.dp, SignalWhite.copy(alpha = .58f), CircleShape)
                    .padding(horizontal = size * .11f, vertical = size * .012f),
            )
        }
    }
}

private val dockGlyphChoices = listOf(
    "auto" to "Auto", "chat" to "Chat", "mail" to "Mail", "call" to "Call",
    "calendar" to "Date", "music" to "Music", "star" to "Star", "grid" to "Apps",
    "home" to "Home", "search" to "Find", "camera" to "Camera", "photo" to "Photos",
    "map" to "Map", "work" to "Work", "school" to "Study", "heart" to "Heart",
    "shop" to "Shop", "play" to "Play", "audio" to "Audio", "folder" to "Files",
    "bolt" to "Power", "car" to "Car", "flight" to "Travel", "lock" to "Lock",
    "web" to "Web", "timer" to "Timer", "note" to "Note", "task" to "Task",
)

private val carouselGlyphChoices = listOf(
    "chat" to "Chat", "mail" to "Mail", "call" to "Call", "calendar" to "Date",
    "music" to "Music", "task" to "Task", "note" to "Note", "timer" to "Timer",
    "alert" to "Alert", "ai" to "AI", "weather" to "Weather", "apps" to "Apps",
)

private fun packedCommanderGlyph(
    key: String,
    style: HomeGlyphStyle,
    fallback: androidx.compose.ui.graphics.vector.ImageVector,
): androidx.compose.ui.graphics.vector.ImageVector {
    PhosphorIconCatalog.resolve(key)?.let { return it }
    fun select(
        material: androidx.compose.ui.graphics.vector.ImageVector,
        tabler: androidx.compose.ui.graphics.vector.ImageVector,
        remix: androidx.compose.ui.graphics.vector.ImageVector,
        lucide: androidx.compose.ui.graphics.vector.ImageVector,
        phosphor: androidx.compose.ui.graphics.vector.ImageVector,
        iconoir: androidx.compose.ui.graphics.vector.ImageVector,
        huge: androidx.compose.ui.graphics.vector.ImageVector,
    ) = when (style) {
        HomeGlyphStyle.MaterialOutlined, HomeGlyphStyle.MaterialFilled -> material
        HomeGlyphStyle.Tabler -> tabler
        HomeGlyphStyle.Remix -> remix
        HomeGlyphStyle.Lucide -> lucide
        HomeGlyphStyle.Phosphor -> phosphor
        HomeGlyphStyle.Iconoir -> iconoir
        HomeGlyphStyle.Hugeicons -> huge
    }
    return when (key) {
        "chat" -> select(Icons.Outlined.ChatBubbleOutline, TablerIcons.MessageDots, Remix.Communication.Message2Line, LucideIcons.MessageSquareMore, PhosphorIcons.Duotone.ChatDots, IconoirCommanderGlyphs.Message, HugeCommanderGlyphs.Message)
        "mail" -> select(Icons.Outlined.Email, TablerIcons.Mail, Remix.Business.MailLine, LucideIcons.Mail, PhosphorIcons.Duotone.Envelope, IconoirCommanderGlyphs.Mail, HugeCommanderGlyphs.Mail)
        "call" -> select(Icons.Outlined.Call, TablerIcons.PhoneCall, Remix.Device.PhoneLine, LucideIcons.PhoneCall, PhosphorIcons.Duotone.PhoneCall, IconoirCommanderGlyphs.Phone, HugeCommanderGlyphs.Phone)
        "calendar" -> select(Icons.Outlined.CalendarMonth, TablerIcons.CalendarEvent, Remix.Business.CalendarEventLine, LucideIcons.CalendarDays, PhosphorIcons.Duotone.CalendarDots, IconoirCommanderGlyphs.Calendar, HugeCommanderGlyphs.Calendar)
        "music" -> select(Icons.Outlined.MusicNote, TablerIcons.Music, Remix.Media.Music2Line, LucideIcons.Music2, PhosphorIcons.Duotone.MusicNotes, IconoirCommanderGlyphs.Music, HugeCommanderGlyphs.Music)
        "task" -> select(Icons.Outlined.CheckCircleOutline, TablerIcons.ClipboardCheck, Remix.Document.TaskLine, LucideIcons.ClipboardCheck, PhosphorIcons.Duotone.ClipboardText, IconoirCommanderGlyphs.Tasks, HugeCommanderGlyphs.Tasks)
        "note" -> select(Icons.Outlined.Description, TablerIcons.Notes, Remix.Document.StickyNoteLine, LucideIcons.NotebookPen, PhosphorIcons.Duotone.NotePencil, IconoirCommanderGlyphs.Notes, HugeCommanderGlyphs.Notes)
        "timer" -> select(Icons.Outlined.Timer, TablerIcons.Clock, Remix.System.TimerLine, LucideIcons.Timer, PhosphorIcons.Duotone.Timer, IconoirCommanderGlyphs.Timer, HugeCommanderGlyphs.Timer)
        "alert" -> select(Icons.Outlined.PriorityHigh, TablerIcons.AlertTriangle, Remix.System.AlertLine, LucideIcons.TriangleAlert, PhosphorIcons.Duotone.Warning, IconoirCommanderGlyphs.Alert, HugeCommanderGlyphs.Alert)
        "ai" -> select(Icons.Outlined.AutoAwesome, TablerIcons.Atom, Remix.Editor.Ai, LucideIcons.Bot, PhosphorIcons.Duotone.Robot, IconoirCommanderGlyphs.Ai, HugeCommanderGlyphs.Ai)
        "weather" -> select(Icons.Outlined.WbSunny, TablerIcons.Sun, Remix.Weather.SunLine, LucideIcons.Sun, PhosphorIcons.Duotone.Sun, IconoirCommanderGlyphs.Weather, HugeCommanderGlyphs.Weather)
        else -> fallback
    }
}

private fun signalDockGlyph(
    app: AppCatalogEntry,
    override: String = "auto",
): androidx.compose.ui.graphics.vector.ImageVector {
    PhosphorIconCatalog.resolve(override)?.let { return it }
    val packedKey = when (override) {
        "chat", "mail", "call", "calendar", "music", "timer", "note", "task" -> override
        "grid" -> "apps"
        "star", "bolt" -> "alert"
        else -> null
    }
    if (packedKey != null) return packedCommanderGlyph(
        packedKey,
        HomeSettings.glyphStyle.value,
        Icons.Outlined.Apps,
    )
    when (override) {
        "home" -> return Icons.Outlined.Home
        "search" -> return Icons.Outlined.Search
        "camera" -> return Icons.Outlined.CameraAlt
        "photo" -> return Icons.Outlined.Photo
        "map" -> return Icons.Outlined.Map
        "work" -> return Icons.Outlined.WorkOutline
        "school" -> return Icons.Outlined.School
        "heart" -> return Icons.Outlined.FavoriteBorder
        "shop" -> return Icons.Outlined.ShoppingCart
        "play" -> return Icons.Outlined.PlayCircleOutline
        "audio" -> return Icons.Outlined.Headphones
        "folder" -> return Icons.Outlined.FolderOpen
        "car" -> return Icons.Outlined.DirectionsCar
        "flight" -> return Icons.Outlined.Flight
        "lock" -> return Icons.Outlined.Lock
        "web" -> return Icons.Outlined.Language
    }
    val identity = "${app.label} ${app.packageName}".lowercase(Locale.ROOT)
    return when {
        listOf("mail", "gmail", "outlook", "proton").any(identity::contains) -> packedCommanderGlyph("mail", HomeSettings.glyphStyle.value, Icons.Outlined.Email)
        listOf("message", "signal", "whatsapp", "messenger", "telegram").any(identity::contains) -> packedCommanderGlyph("chat", HomeSettings.glyphStyle.value, Icons.Outlined.ChatBubbleOutline)
        listOf("phone", "dialer", "call").any(identity::contains) -> packedCommanderGlyph("call", HomeSettings.glyphStyle.value, Icons.Outlined.Call)
        listOf("calendar", "agenda").any(identity::contains) -> packedCommanderGlyph("calendar", HomeSettings.glyphStyle.value, Icons.Outlined.CalendarMonth)
        listOf("music", "spotify", "audio").any(identity::contains) -> packedCommanderGlyph("music", HomeSettings.glyphStyle.value, Icons.Outlined.MusicNote)
        listOf("amazon", "shop", "store", "ebay").any(identity::contains) -> Icons.Outlined.ShoppingBag
        listOf("car", "tesla", "vehicle").any(identity::contains) -> Icons.Outlined.DirectionsCar
        listOf("game", "steam", "final fantasy").any(identity::contains) -> Icons.Outlined.SportsEsports
        listOf("password", "authenticator", "vault").any(identity::contains) -> Icons.Outlined.Lock
        listOf("airbnb", "travel", "flight").any(identity::contains) -> Icons.Outlined.Flight
        listOf("browser", "chrome", "firefox", "edge", "arc").any(identity::contains) -> Icons.Outlined.Language
        else -> Icons.Outlined.Apps
    }
}

@Composable
private fun DaySignalCommandDeck(
    side: Dp,
    accent: Color,
    event: SignalEvent?,
    taskCount: Int,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    onTasks: () -> Unit,
    onTrigger: (Char) -> Unit,
) {
    val entries = listOf(
        DeckEntry("Tasks", "$taskCount due", "−", Icons.Outlined.CheckCircleOutline, onTasks),
        DeckEntry("Notes", "Quick capture", TriggerSettings.noteSymbol().toString(), Icons.Outlined.Description) { onTrigger(TriggerSettings.noteSymbol()) },
        DeckEntry("Messages", "Open conversations", "@", Icons.Outlined.ChatBubbleOutline) { onTrigger('@') },
        DeckEntry("Calendar", event?.title ?: "Next event", "*", Icons.Outlined.CalendarMonth, onEvent),
        DeckEntry("AI", "Ask Commander or agent", "?", Icons.Outlined.AutoAwesome) { onTrigger('?') },
    )
    var selected by remember { mutableIntStateOf(2) }
    var drag by remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(side * .018f)
    val activeGlow = HomeSettings.cardGlow.floatValue
    BoxWithConstraints(modifier.pointerInput(entries.size) {
        detectHorizontalDragGestures(
            onHorizontalDrag = { change, amount -> drag += amount; change.consume() },
            onDragEnd = {
                if (kotlin.math.abs(drag) > size.width * .055f) {
                    selected = (selected + if (drag < 0) 1 else entries.lastIndex) % entries.size
                }
                drag = 0f
            },
            onDragCancel = { drag = 0f },
        )
    }) {
        val cardWidth = side * .31f
        val cardHeight = side * .213f
        val nearStep = side * .245f
        val farStep = side * .420f
        val centreX = (maxWidth - cardWidth) / 2
        val deckPx = with(LocalDensity.current) { side.toPx() }
        entries.forEachIndexed { index, entry ->
            val relative = ((index - selected + entries.size + entries.size / 2) % entries.size) - entries.size / 2
            if (kotlin.math.abs(relative) <= 2) {
                val settledPosition by animateFloatAsState(relative.toFloat(), animationSpec = tween(300), label = "deck position")
                val position = (settledPosition + drag / (deckPx * .245f)).coerceIn(-2.25f, 2.25f)
                val distance = kotlin.math.abs(position)
                val x = centreX + when {
                    position < -1f -> -farStep
                    position > 1f -> farStep
                    else -> nearStep * position
                }
                val active = relative == 0
                val angle = when {
                    position < 0f -> -52f - (distance - 1f).coerceAtLeast(0f) * 8f
                    position > 0f -> 52f + (distance - 1f).coerceAtLeast(0f) * 8f
                    else -> 0f
                }
                if (active) {
                    Box(Modifier.offset(x = x, y = (maxHeight - cardHeight) / 2)
                        .size(cardWidth, cardHeight).zIndex(2f)
                        .blur(side * .042f, BlurredEdgeTreatment.Unbounded)
                        .background(accent.copy(alpha = .50f * activeGlow), shape))
                }
                Box(
                    Modifier.offset(x = x, y = (maxHeight - cardHeight) / 2)
                        .size(cardWidth, cardHeight)
                        .zIndex(3f - distance)
                        .graphicsLayer {
                            rotationY = angle
                            cameraDistance = side.value * density * 1.20f
                            transformOrigin = TransformOrigin.Center
                            alpha = if (distance > 1.5f) .58f else 1f
                        }
                        .clickable { if (active) entry.action() else selected = index },
                ) {
                    if (active) {
                        Box(Modifier.matchParentSize().blur(side * .014f, BlurredEdgeTreatment.Unbounded)
                            .border(side * .008f, accent.copy(alpha = .95f * activeGlow), shape))
                    }
                    Box(Modifier.matchParentSize().clip(shape)
                        .background(Color(0xFF0B0E11).copy(alpha = if (HomeSettings.wallpaperMode.value == HomeWallpaperMode.Black) .95f else .84f))
                        .border(side * .0014f, if (active) lerp(accent, Color.White, .35f).copy(alpha = .94f) else SignalWhite.copy(alpha = .48f), shape))
                    if (active) {
                        Canvas(Modifier.matchParentSize().clip(shape)) {
                            drawRect(Brush.radialGradient(listOf(accent.copy(alpha = .26f * activeGlow), accent.copy(alpha = .08f * activeGlow), Color.Transparent),
                                center = Offset(size.width * .50f, size.height * 1.10f), radius = size.width * .70f))
                        }
                    }
                    Column(Modifier.fillMaxSize().padding(horizontal = side * .014f, vertical = side * .020f),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(entry.icon, entry.label,
                            tint = if (active) lerp(accent, Color.White, .58f) else SignalWhite.copy(alpha = .9f),
                            modifier = Modifier.size(side * .060f))
                        Spacer(Modifier.height(side * .016f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(entry.label, color = SignalWhite, fontFamily = SignalSans, fontWeight = FontWeight.Medium,
                                fontSize = (side.value * .020f).sp, maxLines = 1)
                            Spacer(Modifier.height(side * .004f))
                            Text(entry.detail, color = SignalWhite.copy(alpha = .78f), fontFamily = SignalCardSans,
                                fontSize = (side.value * .013f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        Row(Modifier.align(Alignment.BottomCenter), horizontalArrangement = Arrangement.spacedBy(side * .012f)) {
            entries.forEachIndexed { index, _ ->
                Box(Modifier.size(if (selected == index) side * .005f else side * .004f).clip(CircleShape)
                    .background(if (selected == index) accent else SignalWhite.copy(alpha = .45f)))
            }
        }
    }
}

@Composable
private fun DaySignalTwoCards(
    side: Dp,
    accent: Color,
    event: SignalEvent?,
    taskCount: Int,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    onTasks: () -> Unit,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(side * .022f)) {
        val wallpaperActive = HomeSettings.wallpaperMode.value != HomeWallpaperMode.Black
        val glowStrength = HomeSettings.cardGlow.floatValue
        val primaryShape = RoundedCornerShape(side * .018f)
        val accentShine = lerp(accent, Color.White, .68f)
        val cardPx = with(LocalDensity.current) { side.toPx() }
        Box(
            Modifier.weight(2.35f).fillMaxSize()
                .clickable(onClick = onEvent),
        ) {
            val cardPulseTransition = rememberInfiniteTransition(label = "next up luminosity")
            val cardPulse by cardPulseTransition.animateFloat(
                initialValue = .82f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(animation = tween(3_600), repeatMode = RepeatMode.Reverse),
                label = "next up luminosity pulse",
            )
            Box(
                Modifier.matchParentSize()
                    .blur(side * .010f, BlurredEdgeTreatment.Unbounded)
                    .border(
                        side * .0030f,
                        accentShine.copy(alpha = .34f * glowStrength * cardPulse),
                        primaryShape,
                    ),
            )
            Box(
                Modifier.matchParentSize().clip(primaryShape)
                    .background(Color(0xFF0B0F12).copy(alpha = if (wallpaperActive) .50f else .97f))
                    .border(
                        side * .00115f,
                        Color(0xFF9AA3AA).copy(alpha = if (wallpaperActive) .55f else .62f),
                        primaryShape,
                    ),
            )
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = .23f * glowStrength * cardPulse),
                            accent.copy(alpha = .075f * glowStrength * cardPulse),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * .08f, size.height * .92f),
                        radius = size.width * .72f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = .095f * cardPulse), Color.Transparent),
                        center = Offset(size.width * .88f, size.height * .04f),
                        radius = size.width * .36f,
                    ),
                )
                drawLine(
                    accentShine.copy(alpha = .68f * glowStrength * cardPulse),
                    Offset(0f, size.height * .14f),
                    Offset(0f, size.height * .82f),
                    strokeWidth = side.value * .0032f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
            Column(Modifier.fillMaxSize().padding(start = side * .038f, top = side * .032f, end = side * .030f, bottom = side * .032f), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("NEXT UP", color = SignalWhite.copy(alpha = .74f), fontFamily = SignalMono, fontSize = (side.value * .014f).sp, letterSpacing = (side.value * .0008f).sp)
                    Icon(Icons.Outlined.CalendarMonth, null, tint = SignalWhite.copy(alpha = .64f), modifier = Modifier.size(side * .022f))
                }
                val eventRange = event?.let {
                    if (HomeSettings.timeline12Hour.value) {
                        "${it.start.format(DateTimeFormatter.ofPattern("h:mm"))} – ${it.end.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                    } else {
                        "${it.start.format(DateTimeFormatter.ofPattern("HH:mm"))} – ${it.end.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    }
                } ?: "—"
                Text(eventRange, color = SignalWhite, fontFamily = SignalMono, fontSize = (side.value * .035f).sp, letterSpacing = (-side.value * .00065f).sp, maxLines = 1)
                Text(event?.title ?: "Nothing scheduled", color = SignalWhite.copy(alpha = .91f), fontFamily = SignalSans, fontWeight = FontWeight.Medium, fontSize = (side.value * .021f).sp, letterSpacing = 0.sp, lineHeight = (side.value * .024f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        val taskShape = RoundedCornerShape(side * .018f)
        Box(Modifier.weight(1f).fillMaxSize().clickable(onClick = onTasks)) {
            Box(
                Modifier.matchParentSize().blur(side * .008f, BlurredEdgeTreatment.Unbounded)
                    .border(side * .0024f, accent.copy(alpha = .10f * glowStrength), taskShape),
            )
            Box(
                Modifier.matchParentSize().clip(taskShape)
                    .background(Color(0xFF0B0F12).copy(alpha = if (wallpaperActive) .52f else .98f))
                    .border(side * .00115f, Color(0xFF9AA3AA).copy(alpha = if (wallpaperActive) .55f else .62f), taskShape),
            )
            Column(Modifier.fillMaxSize().padding(start = side * .035f, top = side * .032f, end = side * .020f, bottom = side * .032f), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TASKS", color = SignalWhite.copy(alpha = .74f), fontFamily = SignalMono, fontSize = (side.value * .014f).sp, letterSpacing = (side.value * .0008f).sp)
                    Icon(Icons.Outlined.CheckCircleOutline, null, tint = SignalWhite.copy(alpha = .64f), modifier = Modifier.size(side * .022f))
                }
                Text(taskCount.toString().padStart(2, '0'), color = SignalWhite, fontFamily = SignalDot, fontSize = (side.value * .049f).sp)
                Text(if (taskCount == 1) "task due  →" else "tasks due  →", color = SignalWhite.copy(alpha = .86f), fontFamily = SignalSans, fontWeight = FontWeight.Medium, fontSize = (side.value * .020f).sp, letterSpacing = 0.sp, lineHeight = (side.value * .024f).sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SignalDotLattice(wallpaperActive: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        val step = size.minDimension / 52f
        var y = step * .6f
        while (y < size.height) {
            var x = step * .6f
            while (x < size.width) {
                drawCircle(Color.White.copy(alpha = if (wallpaperActive) .024f else .055f), radius = .75f, center = Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

@Composable
private fun DaySignalTimeline(
    side: Dp,
    now: LocalDateTime,
    events: List<SignalEvent>,
    deadlines: List<TodoistTask>,
    selectedQuarter: Int,
    onQuarter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = AccentSettings.color.value
    val currentMinutes = now.hour * 60 + now.minute
    Box(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("00", "06", "12", "18", "24").forEach {
                Text(it, color = SignalGrey, fontFamily = SignalMono, fontSize = (side.value * .017f).sp)
            }
        }
        Text("EVENTS", color = SignalGrey, fontFamily = SignalMono, fontSize = (side.value * .017f).sp, modifier = Modifier.offset(y = side * .083f))
        Text("DUE", color = SignalGrey, fontFamily = SignalMono, fontSize = (side.value * .017f).sp, modifier = Modifier.offset(y = side * .158f))
        Canvas(Modifier.fillMaxSize().blur(side * .010f, BlurredEdgeTreatment.Unbounded)) {
            val xNow = size.width * currentMinutes / 1440f
            val yLine = size.height * .49f
            drawLine(
                Color(0xFFFF7440).copy(alpha = .58f),
                Offset(0f, yLine), Offset(size.width, yLine), side.value * .013f,
            )
            drawLine(
                Brush.horizontalGradient(listOf(accent.copy(alpha = .30f), Color(0xFFFF542F), Color(0xFFFFE1BD))),
                Offset(0f, yLine), Offset(xNow, yLine), side.value * .019f,
            )
            drawLine(Color(0xFFFFE0C3), Offset(xNow, yLine - side.value * .062f), Offset(xNow, yLine + side.value * .064f), side.value * .009f)
            drawCircle(Color(0xFFFFC39A), side.value * .026f, Offset(xNow, yLine))
        }
        Canvas(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { point -> onQuarter(((point.x / size.width) * 96f).toInt().coerceIn(0, 95)) }
            }.semantics { contentDescription = "Day timeline. Tap a time to inspect it." },
        ) {
            val xNow = size.width * currentMinutes / 1440f
            val yLine = size.height * .49f
            drawLine(Color(0xFF6B5148), Offset(0f, yLine), Offset(size.width, yLine), side.value * .0012f)
            drawLine(
                Brush.horizontalGradient(listOf(accent.copy(alpha = .25f), accent, Color(0xFFFFD2A9))),
                Offset(0f, yLine), Offset(xNow, yLine), side.value * .0022f,
            )
            drawLine(Color(0xFFFFF0E3), Offset(xNow, yLine - side.value * .055f), Offset(xNow, yLine + side.value * .055f), side.value * .0021f)
            drawCircle(accent.copy(alpha = .42f), side.value * .014f, Offset(xNow, yLine))
            drawCircle(SignalWhite, side.value * .0045f, Offset(xNow, yLine))
            listOf(0, 360, 720, 1080, 1440).forEach { minute ->
                val x = size.width * minute / 1440f
                drawLine(SignalGrey, Offset(x, size.height * .15f), Offset(x, size.height * .22f), side.value * .0012f)
            }
            deadlines.forEach { task ->
                task.signalDateTime()?.let { dt ->
                    val x = size.width * (dt.hour * 60 + dt.minute) / 1440f
                    drawCircle(accent.copy(alpha = .20f), side.value * .015f, Offset(x, size.height * .72f))
                    drawCircle(accent, side.value * .0065f, Offset(x, size.height * .72f))
                }
            }
            if (selectedQuarter >= 0) {
                val x = size.width * selectedQuarter / 96f
                drawCircle(SignalWhite, side.value * .0035f, Offset(x, yLine))
            }
        }
        events.filterNot { it.allDay }.take(5).forEachIndexed { index, event ->
            val minute = event.start.hour * 60 + event.start.minute
            val xFraction = (minute / 1440f).coerceIn(.05f, .88f)
            Text(
                event.title.take(7),
                color = Color(0xFF231711),
                fontFamily = SignalSans,
                fontWeight = FontWeight.Medium,
                fontSize = (side.value * .0115f).sp,
                lineHeight = (side.value * .013f).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = side * xFraction, y = side * (.050f + (index % 2) * .026f))
                    .width(side * .075f).height(side * .019f).background(Color(0xFFFFD0AE), RoundedCornerShape(side * .010f)).padding(horizontal = side * .006f),
            )
        }
        Text(now.format(DateTimeFormatter.ofPattern("HH:mm")), color = SignalGrey, fontFamily = SignalMono, fontSize = (side.value * .014f).sp, modifier = Modifier.offset(x = side * ((currentMinutes / 1440f) * .82f).coerceIn(0f, .76f), y = side * .142f))
    }
}

@Composable
private fun DaySignalCard(
    side: Dp,
    accent: Color,
    intensity: Float,
    eyebrow: String,
    leading: String,
    title: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(side * .015f)
    Box(modifier.clickable(onClick = onClick)) {
        Canvas(Modifier.fillMaxSize().blur(side * .015f, BlurredEdgeTreatment.Unbounded)) {
            // The reference does not glow around the whole outline. Light pools at
            // the lower-right edge, with only a quiet ember at the left corner.
            drawCircle(accent.copy(alpha = .48f * intensity), size.width * .13f, Offset(0f, size.height * .72f))
            drawCircle(Color(0xFFFF572F).copy(alpha = .82f * intensity), size.width * .27f, Offset(size.width, size.height * .92f))
            drawCircle(Color(0xFFFFD7A9).copy(alpha = .92f * intensity), size.width * .095f, Offset(size.width, size.height * .86f))
        }
        Box(
            Modifier.fillMaxSize()
                .shadow(
                    elevation = side * (.016f * intensity),
                    shape = shape,
                    clip = false,
                    ambientColor = accent.copy(alpha = .48f * intensity),
                    spotColor = Color(0xFFFF6B38).copy(alpha = .72f * intensity),
                )
                .clip(shape)
                .background(if (eyebrow == "PINNED NOTE") Color(0xFF070707) else Color(0xFF090808))
                .border(
                    side * .00055f,
                    Color(0xFFE0AA8C).copy(alpha = .28f + .36f * intensity),
                    shape,
                ),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFE4C3).copy(alpha = .88f * intensity),
                            Color(0xFFFF8B4C).copy(alpha = .55f * intensity),
                            accent.copy(alpha = .30f * intensity),
                            accent.copy(alpha = .06f * intensity),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 1.08f, size.height * 1.08f),
                        radius = size.width * .43f,
                    ),
                )
                drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .58f), Color.Transparent)))
                if (eyebrow == "NEXT UP") {
                    val textureStep = side.value * .016f
                    var textureY = textureStep * .6f
                    while (textureY < size.height) {
                        var textureX = textureStep * .6f
                        while (textureX < size.width) {
                            drawCircle(Color.White.copy(alpha = .026f), side.value * .00055f, Offset(textureX, textureY))
                            textureX += textureStep
                        }
                        textureY += textureStep
                    }
                    drawRect(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to accent.copy(alpha = .30f),
                                .055f to accent.copy(alpha = .16f),
                                .18f to Color.Transparent,
                                1f to Color.Transparent,
                            ),
                        ),
                    )
                    drawCircle(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = .42f), accent.copy(alpha = .16f), Color.Transparent),
                        ),
                        radius = size.width * .16f,
                        center = Offset(-size.width * .025f, size.height * .72f),
                    )
                    drawRect(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                .62f to Color.Transparent,
                                .78f to accent.copy(alpha = .10f),
                                .92f to Color(0xFFFF5F32).copy(alpha = .34f),
                                1f to Color(0xFFFFA46B).copy(alpha = .52f),
                            ),
                        ),
                    )
                    drawCircle(
                        Brush.radialGradient(listOf(Color(0xFFFFD4A9).copy(alpha = .82f), Color(0xFFFF8247).copy(alpha = .62f), accent.copy(alpha = .28f), Color.Transparent)),
                        radius = size.width * .31f,
                        center = Offset(size.width * 1.04f, size.height * 1.18f),
                    )
                }
                if (eyebrow == "WEATHER") {
                    val cloud = Color(0xFFE7C9BA)
                    listOf(
                        Triple(.76f, .83f, .11f), Triple(.82f, .65f, .15f), Triple(.88f, .72f, .18f),
                        Triple(.94f, .54f, .13f), Triple(.98f, .72f, .18f), Triple(1.04f, .58f, .16f),
                    ).forEach { (x, y, radius) ->
                        drawCircle(
                            Brush.radialGradient(listOf(cloud.copy(alpha = .58f), Color(0xFF8E5650).copy(alpha = .32f), Color.Transparent)),
                            radius = size.width * radius,
                            center = Offset(size.width * x, size.height * y),
                        )
                    }
                    drawCircle(
                        Brush.radialGradient(listOf(Color(0xFFFFC58F).copy(alpha = .48f), Color.Transparent)),
                        radius = size.width * .20f,
                        center = Offset(size.width, size.height),
                    )
                }
                if (eyebrow == "RECENT NOTE") {
                    drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .38f), Color.Transparent)), size.width * .14f, Offset(0f, size.height))
                    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFA45C).copy(alpha = .44f), Color.Transparent)), size.width * .16f, Offset(size.width, size.height))
                }
            }
            if (eyebrow == "WEATHER") {
                Image(
                    painter = painterResource(R.drawable.weather_cloud_signal),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.align(Alignment.CenterEnd).width(side * .33f).height(side * .108f)
                        .blur(side * .0025f)
                        .graphicsLayer {
                            alpha = .82f
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0f to Color.Transparent,
                                        .22f to Color.White.copy(alpha = .12f),
                                        .48f to Color.White.copy(alpha = .72f),
                                        1f to Color.White,
                                    ),
                                ),
                                blendMode = BlendMode.DstIn,
                            )
                        },
                )
            }
            Text(eyebrow, color = SignalWhite.copy(alpha = .78f), fontFamily = SignalMono, fontWeight = FontWeight.Medium, fontSize = (side.value * .0125f).sp, letterSpacing = (side.value * .0003f).sp, modifier = Modifier.offset(x = side * .034f, y = side * .014f))
            if (leading.isNotBlank()) {
                Text(leading, color = SignalWhite, fontFamily = SignalCardSans, fontSize = (side.value * .034f).sp, modifier = Modifier.offset(x = side * .034f, y = side * .041f))
                Box(Modifier.offset(x = side * .245f, y = side * .040f).width(side * .001f).height(side * .052f).background(SignalWhite.copy(alpha = .7f)))
                Text(title, color = SignalWhite.copy(alpha = .72f + .20f * intensity), fontFamily = SignalCardSans, fontSize = (side.value * .018f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.offset(x = side * .285f, y = side * .058f).width(side * .48f))
            } else {
                Text(title, color = SignalWhite.copy(alpha = .72f + .20f * intensity), fontFamily = SignalCardSans, fontSize = (side.value * .020f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.offset(x = side * .034f, y = side * .047f).width(side * .68f))
            }
            Text("→", color = SignalWhite.copy(alpha = .68f + .18f * intensity), fontFamily = SignalMono, fontSize = (side.value * .025f).sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = side * .032f))
        }
    }
}

@Composable
private fun DaySignalShortcuts(side: Dp, modifier: Modifier, onTrigger: (Char) -> Unit) {
    val shortcuts = listOf(
        Triple("CALL", "#", '#'),
        Triple("MSG", "@", '@'),
        Triple("NOTE", TriggerSettings.noteSymbol().toString(), TriggerSettings.noteSymbol()),
        Triple("TASK", "-", '-'),
        Triple("EVENT", "*", '*'),
        Triple("TIMER", "+", '+'),
        Triple("FILES", FileSearchSettings.trigger.value.toString(), FileSearchSettings.trigger.value),
        Triple("WEB", "/", '/'),
        Triple("AI", "?", '?'),
    )
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        shortcuts.forEach { (label, symbol, trigger) ->
            Column(Modifier.weight(1f).fillMaxSize().clickable { onTrigger(trigger) }.semantics { contentDescription = "$label — opens Commander Bar" }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(symbol, color = SignalWhite, fontFamily = SignalMono, fontWeight = FontWeight.Medium, fontSize = (side.value * .0215f).sp, lineHeight = (side.value * .024f).sp)
                Text(label, color = SignalGrey, fontFamily = SignalMono, fontSize = (side.value * .0115f).sp, letterSpacing = (side.value * .00025f).sp)
            }
        }
    }
}

@Composable
private fun SignalDayMap(
    side: Dp,
    now: LocalDateTime,
    events: List<SignalEvent>,
    deadlines: List<TodoistTask>,
    selectedQuarter: Int,
    onQuarter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = now.hour * 4 + now.minute / 15
    val pulseTransition = rememberInfiniteTransition(label = "current quarter")
    val pulse by pulseTransition.animateFloat(.76f, 1f, infiniteRepeatable(tween(1_600), RepeatMode.Reverse), label = "current quarter pulse")
    val eventSlots = remember(events) {
        buildSet {
            events.filterNot { it.allDay }.forEach { event ->
                val start = event.start.hour * 4 + event.start.minute / 15
                val end = event.end.hour * 4 + (event.end.minute + 14) / 15
                for (slot in start until end.coerceAtMost(96)) add(slot)
            }
        }
    }
    val deadlineSlots = remember(deadlines) { deadlines.mapNotNull { it.signalDateTime()?.let { dt -> dt.hour * 4 + dt.minute / 15 } }.toSet() }
    Canvas(
        modifier.semantics { contentDescription = "Day timeline in 15 minute intervals. Tap a time to inspect it." }
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    val column = (position.x / (size.width / 24f)).toInt().coerceIn(0, 23)
                    val row = (position.y / (size.height / 4f)).toInt().coerceIn(0, 3)
                    onQuarter(column * 4 + row)
                }
            },
    ) {
        val columnWidth = size.width / 24f
        val rowHeight = size.height / 4f
        val cellWidth = columnWidth * .82f
        val cellHeight = rowHeight * .88f
        for (column in 0 until 24) for (row in 0 until 4) {
            val slot = column * 4 + row
            val center = Offset(column * columnWidth + columnWidth / 2f, row * rowHeight + rowHeight / 2f)
            val color = when {
                slot == current -> SignalWhite.copy(alpha = if (ValueAnimator.areAnimatorsEnabled()) pulse else 1f)
                slot in deadlineSlots -> Color(0xFFFF3B36)
                slot in eventSlots -> Color(0xFFFF8B32)
                slot < current -> {
                    val acrossDay = column / 23f
                    val warm = if (acrossDay <= .50f) lerp(Color(0xFFE39226), Color(0xFFFFD84C), acrossDay * 2f)
                    else lerp(Color(0xFFFFD84C), Color(0xFFE79A2B), (acrossDay - .5f) * 2f)
                    warm.copy(alpha = .90f - row * .018f)
                }
                else -> Color(0xFF202224)
            }
            if (slot < current || slot == current || slot in eventSlots || slot in deadlineSlots) {
                drawRect(
                    Brush.radialGradient(listOf(color.copy(alpha = .38f), color.copy(alpha = .14f), Color.Transparent), center, maxOf(cellWidth, cellHeight) * 2.45f),
                    topLeft = Offset(center.x - cellWidth * 2.1f, center.y - cellHeight * 2.1f),
                    size = Size(cellWidth * 4.2f, cellHeight * 4.2f),
                )
            }
            drawRect(color, Offset(center.x - cellWidth / 2f, center.y - cellHeight / 2f), Size(cellWidth, cellHeight))
            if (slot == selectedQuarter) drawRect(Color.White.copy(alpha = .85f), Offset(center.x - cellWidth / 2f, center.y - cellHeight / 2f), Size(cellWidth, cellHeight), style = Stroke((side.value * .0025f).coerceAtLeast(1f)))
        }
    }
}

@Composable
private fun SignalFeatureCard(
    page: Int,
    side: Dp,
    accent: Color,
    event: SignalEvent?,
    modifier: Modifier,
    onClick: () -> Unit,
    onSwipe: (Int) -> Unit,
) {
    val palette = when (page) {
        0 -> listOf(Color(0xFFF00078), Color(0xFFFF3D63), Color(0xFFFFC65B))
        1 -> listOf(Color(0xFF0068E8), Color(0xFF2545FF), Color(0xFF50E8FF))
        else -> listOf(Color(0xFF007F61), Color(0xFF00B78E), Color(0xFF72F69B))
    }
    val transition = rememberInfiniteTransition(label = "signal glow")
    val motion by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(7_000), RepeatMode.Reverse), label = "glow drift")
    var drag by remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(side * .025f)
    Box(
        modifier.clip(shape).background(Color(0xFF0C090D)).border(side * .0008f, palette.first().copy(alpha = .52f), shape)
            .pointerInput(page) {
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onHorizontalDrag = { _, amount -> drag += amount },
                    onDragEnd = { if (kotlin.math.abs(drag) > size.width * .12f) onSwipe(if (drag < 0) 1 else -1) },
                )
            }.clickable(onClick = onClick),
    ) {
        Canvas(Modifier.fillMaxSize().blur(side * .012f)) {
            val animated = if (ValueAnimator.areAnimatorsEnabled()) motion else .5f
            drawRect(
                Brush.radialGradient(
                    listOf(
                        palette[2].copy(alpha = .96f),
                        palette[1].copy(alpha = .72f),
                        palette[0].copy(alpha = .46f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * (1.07f - animated * .025f), size.height * 1.27f),
                    radius = size.width * .91f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(palette[0].copy(alpha = .72f), palette[0].copy(alpha = .18f), Color.Transparent),
                    center = Offset(size.width * (-.12f + animated * .035f), size.height * 1.38f),
                    radius = size.width * 1.02f,
                ),
            )
            drawRect(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = .62f), Color.Black.copy(alpha = .10f), Color.Transparent)),
            )
        }
        Column(Modifier.fillMaxSize().padding(horizontal = side * .042f, vertical = side * .027f), verticalArrangement = Arrangement.SpaceBetween) {
            Text(when (page) { 0 -> "NEXT UP"; 1 -> "WEATHER"; else -> "PINNED NOTE" }, color = SignalWhite, fontFamily = SignalSans, fontWeight = FontWeight.Medium, fontSize = (side.value * .022f).sp, letterSpacing = (side.value * .0005f).sp)
            when (page) {
                0 -> if (event == null) {
                    Column { Text("00", color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .052f).sp); Text("Events scheduled", color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .030f).sp) }
                } else {
                    Column { Text(event.start.format(DateTimeFormatter.ofPattern("HH:mm")), color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .052f).sp); Text(event.title, color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .030f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
                1 -> Column { Text("—", color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .052f).sp); Text("Weather unavailable", color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .030f).sp, maxLines = 1) }
                else -> Column { Text("No pinned note", color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .039f).sp, maxLines = 1); Text("Use ! to add one", color = SignalWhite.copy(alpha = .82f), fontFamily = SignalSans, fontSize = (side.value * .025f).sp) }
            }
        }
    }
}

@Composable
private fun SignalTaskCard(tasks: List<TodoistTask>, next: Pair<TodoistTask, LocalDateTime>?, side: Dp, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(side * .025f)
    Box(
        modifier.clip(shape).background(Brush.linearGradient(listOf(Color(0xFF111315), Color(0xFF08090A))))
            .border(side * .0015f, Color(0xFF303437), shape).clickable(onClick = onClick).padding(horizontal = side * .042f, vertical = side * .027f),
    ) {
        Text(tasks.size.coerceAtMost(99).toString().padStart(2, '0'), color = SignalWhite, fontFamily = SignalDot, fontSize = (side.value * .064f).sp, lineHeight = (side.value * .064f).sp, modifier = Modifier.offset(y = -side * .006f))
        Text(if (tasks.size == 1) "Task to finish" else "Tasks to finish", color = SignalWhite, fontFamily = SignalSans, fontSize = (side.value * .029f).sp, maxLines = 1, modifier = Modifier.offset(y = side * .098f))
        Text(next?.second?.let { "${if (it.isBefore(LocalDateTime.now())) "Due" else "By"} ${it.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}" } ?: "All clear", color = Color(0xFFA9C1D2), fontFamily = SignalSans, fontSize = (side.value * .025f).sp, maxLines = 1, modifier = Modifier.offset(y = side * .139f))
    }
}

@Composable
private fun SignalShortcuts(side: Dp, modifier: Modifier, onTrigger: (Char) -> Unit) {
    val shortcuts = listOf(
        Triple("Call", Icons.Filled.Call, '#'), Triple("Message", Icons.Filled.ChatBubble, '@'),
        Triple("Note", Icons.Filled.NoteAdd, TriggerSettings.noteSymbol()), Triple("Task", Icons.Filled.TaskAlt, '-'),
        Triple("Timer", Icons.Filled.Timer, '+'), Triple("Files", Icons.Filled.Folder, FileSearchSettings.trigger.value),
        Triple("AI", Icons.Filled.AutoAwesome, '?'),
    )
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        shortcuts.forEach { (label, icon, trigger) ->
            Box(Modifier.weight(1f).fillMaxSize().clickable { onTrigger(trigger) }.semantics { contentDescription = "$label — opens Commander Bar" }, contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = SignalWhite, modifier = Modifier.size(side * .034f))
            }
        }
    }
}

@Composable
private fun SignalSun(size: Dp) {
    Canvas(Modifier.size(size)) {
        val c = center; val r = this.size.minDimension * .19f
        drawCircle(SignalWhite, r, c, style = Stroke(this.size.minDimension * .065f))
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            val a = Offset(c.x + kotlin.math.cos(angle).toFloat() * r * 1.55f, c.y + kotlin.math.sin(angle).toFloat() * r * 1.55f)
            val b = Offset(c.x + kotlin.math.cos(angle).toFloat() * r * 2.1f, c.y + kotlin.math.sin(angle).toFloat() * r * 2.1f)
            drawLine(SignalWhite, a, b, this.size.minDimension * .065f)
        }
    }
}

@Composable
private fun SignalBatteryGlyph(side: Dp, level: Int, charging: Boolean = false) {
    val chargeColor = lerp(AccentSettings.color.value, Color.White, .18f)
    Canvas(Modifier.width(side * .052f).height(side * .027f)) {
        val bodyWidth = size.width * .84f
        val stroke = size.height * .10f
        val radius = androidx.compose.ui.geometry.CornerRadius(size.height * .22f)
        drawRoundRect(SignalWhite.copy(alpha = .88f), size = Size(bodyWidth, size.height), cornerRadius = radius, style = Stroke(stroke))
        drawRoundRect(SignalWhite.copy(alpha = .78f), Offset(size.width * .87f, size.height * .31f), Size(size.width * .08f, size.height * .38f), androidx.compose.ui.geometry.CornerRadius(size.height * .10f))
        val inset = stroke * 2f
        val available = (bodyWidth - inset * 2f).coerceAtLeast(0f)
        val fillWidth = (available * (level.coerceIn(0, 100) / 100f)).coerceAtLeast(if (level > 0) size.height * .10f else 0f)
        drawRoundRect(
            if (charging) chargeColor.copy(alpha = .92f) else SignalWhite.copy(alpha = .84f),
            Offset(inset, inset), Size(fillWidth, (size.height - inset * 2f).coerceAtLeast(0f)),
            androidx.compose.ui.geometry.CornerRadius(size.height * .11f),
        )
        if (charging) {
            val bolt = Path().apply {
                moveTo(size.width * .50f, size.height * .10f)
                lineTo(size.width * .34f, size.height * .55f)
                lineTo(size.width * .49f, size.height * .55f)
                lineTo(size.width * .40f, size.height * .92f)
                lineTo(size.width * .67f, size.height * .43f)
                lineTo(size.width * .52f, size.height * .43f)
                close()
            }
            drawPath(bolt, Color(0xFF080A0D).copy(alpha = .90f))
        }
    }
}

private fun signalAgendaCaption(events: List<SignalEvent>, tasks: List<TodoistTask>, selected: Int): String {
    if (selected >= 0) {
        val hour = selected / 4; val minute = (selected % 4) * 15
        val matchingEvents = events.filter { !it.allDay && selected in (it.start.hour * 4 + it.start.minute / 15) until (it.end.hour * 4 + (it.end.minute + 14) / 15).coerceAtMost(96) }
        val matchingTasks = tasks.filter { it.signalDateTime()?.let { dt -> dt.hour == hour && dt.minute / 15 == minute / 15 } == true }
        return (matchingEvents.map { it.title } + matchingTasks.map { it.content }).joinToString(" · ").ifBlank { "%02d:%02d · Free".format(hour, minute) }
    }
    val labels = buildList {
        events.filterNot { it.allDay }.take(2).forEach { add("${it.title} ${it.start.format(DateTimeFormatter.ofPattern("HH:mm"))}") }
        tasks.mapNotNull { task -> task.signalDateTime()?.let { "${task.content} ${it.format(DateTimeFormatter.ofPattern("HH:mm"))}" } }.take(2 - size).forEach(::add)
    }
    return labels.joinToString("  ·  ").ifBlank { events.firstOrNull { it.allDay }?.let { "All day · ${it.title}" } ?: "No timed items today" }
}

private fun Context.signalEvents(date: LocalDate): List<SignalEvent> {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return emptyList()
    val zone = ZoneId.systemDefault(); val start = date.atStartOfDay(zone); val end = date.plusDays(1).atStartOfDay(zone)
    val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().appendPath(start.toInstant().toEpochMilli().toString()).appendPath(end.toInstant().toEpochMilli().toString()).build()
    return runCatching {
        contentResolver.query(uri, arrayOf(CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.ALL_DAY), null, null, CalendarContract.Instances.BEGIN)?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(SignalEvent(cursor.getLong(0), cursor.getString(1).orEmpty().ifBlank { "Untitled event" }, Instant.ofEpochMilli(cursor.getLong(2)).atZone(zone), Instant.ofEpochMilli(cursor.getLong(3)).atZone(zone), cursor.getInt(4) == 1))
            }
        }.orEmpty()
    }.getOrDefault(emptyList())
}

private fun Context.signalBattery(): Int? {
    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return null
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level >= 0 && scale > 0) level * 100 / scale else null
}

private fun Context.signalCharging(): Boolean {
    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
    return intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) in
        setOf(BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL)
}

@Composable
private fun rememberLiveConnectivity(context: Context, active: Boolean): Triple<Boolean, Boolean, Boolean> {
    var state by remember { mutableStateOf(context.signalConnectivity()) }
    DisposableEffect(context, active) {
        if (!active) return@DisposableEffect onDispose { }
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        fun refresh() {
            state = context.signalConnectivity()
        }
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = refresh()
            override fun onLost(network: Network) = refresh()
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = refresh()
        }
        val bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) = refresh()
        }
        runCatching {
            manager?.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                networkCallback,
            )
        }
        context.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        refresh()
        onDispose {
            runCatching { manager?.unregisterNetworkCallback(networkCallback) }
            runCatching { context.unregisterReceiver(bluetoothReceiver) }
        }
    }
    return state
}

@Composable
private fun rememberLiveRingerMode(context: Context, active: Boolean): Int {
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var mode by remember { mutableIntStateOf(audioManager.ringerMode) }
    DisposableEffect(context, active) {
        if (!active) return@DisposableEffect onDispose { }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                mode = audioManager.ringerMode
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        mode = audioManager.ringerMode
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    return mode
}

private fun Context.signalConnectivity(): Triple<Boolean, Boolean, Boolean> {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val capabilities: List<NetworkCapabilities> = manager?.let { connectivityManager ->
        connectivityManager.allNetworks.mapNotNull { network ->
            connectivityManager.getNetworkCapabilities(network)
        }
    }.orEmpty()
    val wifi = capabilities.any {
        it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    val cellularNetwork = capabilities.any {
        it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    // NetworkCapabilities reports active cellular connectivity without asking
    // for phone-state or location permissions that Commander does not need.
    val cellular = cellularNetwork
    val bluetooth = runCatching { Settings.Global.getInt(contentResolver, "bluetooth_on", 0) == 1 }.getOrDefault(false)
    return Triple(wifi, cellular, bluetooth)
}

private const val RAIN_CACHE_PREFS = "signal_rain_forecast"
private const val RAIN_CACHE_JSON = "forecast"

private fun Context.cachedRainForecast(): RainForecast = runCatching {
    val root = JSONObject(getSharedPreferences(RAIN_CACHE_PREFS, Context.MODE_PRIVATE).getString(RAIN_CACHE_JSON, null) ?: return@runCatching RainForecast())
    fun points(name: String): List<RainPoint> {
        val array = root.optJSONArray(name) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val point = array.optJSONObject(index) ?: continue
                val label = point.optString("label")
                if (label.isNotBlank()) add(RainPoint(label, point.optInt("chance", 0).coerceIn(0, 100)))
            }
        }
    }
    RainForecast(
        hourly = points("hourly"),
        weekly = points("weekly"),
        weatherCode = root.optInt("weatherCode").takeUnless { root.isNull("weatherCode") },
        temperature = root.optInt("temperature").takeUnless { root.isNull("temperature") },
    )
}.getOrDefault(RainForecast())

private fun Context.cacheRainForecast(forecast: RainForecast) {
    if (forecast.hourly.isEmpty() && forecast.weekly.isEmpty()) return
    fun encode(points: List<RainPoint>) = JSONArray().apply {
        points.forEach { put(JSONObject().put("label", it.hour).put("chance", it.chance)) }
    }
    getSharedPreferences(RAIN_CACHE_PREFS, Context.MODE_PRIVATE).edit().putString(
        RAIN_CACHE_JSON,
        JSONObject()
            .put("hourly", encode(forecast.hourly))
            .put("weekly", encode(forecast.weekly))
            .put("weatherCode", forecast.weatherCode)
            .put("temperature", forecast.temperature)
            .toString(),
    ).apply()
}

private fun signalRainForecast(context: Context): RainForecast = runCatching {
    val location = WeatherPreferences.location(context)
    val payload = URL(
        "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current=temperature_2m,weather_code&hourly=precipitation_probability&daily=precipitation_probability_max&forecast_days=7&timezone=auto",
    ).openConnection().apply {
        connectTimeout = 4_000
        readTimeout = 4_000
    }.getInputStream().bufferedReader().use { it.readText() }
    val root = JSONObject(payload)
    val current = root.optJSONObject("current")
    val weatherCode = current?.optInt("weather_code")
    val temperature = current?.optDouble("temperature_2m")?.let { kotlin.math.round(it).toInt() }
    val hourly = root.getJSONObject("hourly")
    val times = hourly.getJSONArray("time")
    val chances = hourly.getJSONArray("precipitation_probability")
    val currentHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
    val nextHours = buildList {
        for (index in 0 until times.length()) {
            val time = runCatching { LocalDateTime.parse(times.getString(index)) }.getOrNull() ?: continue
            if (!time.isBefore(currentHour) && size < 12) add(RainPoint(time.format(DateTimeFormatter.ofPattern("ha")).lowercase(), chances.optInt(index, 0)))
        }
    }
    val daily = root.getJSONObject("daily")
    val dailyTimes = daily.getJSONArray("time")
    val dailyChances = daily.getJSONArray("precipitation_probability_max")
    val nextDays = buildList {
        for (index in 0 until minOf(7, dailyTimes.length())) {
            val date = runCatching { LocalDate.parse(dailyTimes.getString(index)) }.getOrNull() ?: continue
            add(RainPoint(date.format(DateTimeFormatter.ofPattern("EEE")).uppercase(), dailyChances.optInt(index, 0)))
        }
    }
    RainForecast(nextHours, nextDays, weatherCode, temperature).also(context::cacheRainForecast)
}.getOrElse { context.cachedRainForecast() }

private fun signalWeatherIcon(
    code: Int?,
    style: HomeGlyphStyle = HomeGlyphStyle.Tabler,
): androidx.compose.ui.graphics.vector.ImageVector = when (style) {
    HomeGlyphStyle.MaterialOutlined -> when (code) {
        0 -> Icons.Outlined.WbSunny
        in 51..67, in 80..82 -> Icons.Outlined.WaterDrop
        in 71..77, 85, 86 -> Icons.Outlined.AcUnit
        in 95..99 -> Icons.Outlined.Thunderstorm
        else -> Icons.Outlined.CloudQueue
    }
    HomeGlyphStyle.MaterialFilled -> when (code) {
        0 -> Icons.Filled.WbSunny
        in 51..67, in 80..82 -> Icons.Filled.WaterDrop
        in 71..77, 85, 86 -> Icons.Filled.AcUnit
        in 95..99 -> Icons.Filled.Thunderstorm
        else -> Icons.Filled.Cloud
    }
    HomeGlyphStyle.Tabler -> when (code) {
        0 -> TablerIcons.Sun
        in 1..3 -> TablerIcons.Cloud
        45, 48 -> TablerIcons.CloudFog
        in 51..67, in 80..82 -> TablerIcons.CloudRain
        in 71..77, 85, 86 -> TablerIcons.CloudSnow
        in 95..99 -> TablerIcons.CloudStorm
        else -> TablerIcons.Cloud
    }
    HomeGlyphStyle.Remix -> when (code) {
        0 -> Remix.Weather.SunLine
        in 51..67, in 80..82 -> Remix.Weather.RainyLine
        in 71..77, 85, 86 -> Remix.Weather.SnowyLine
        in 95..99 -> Remix.Weather.ThunderstormsLine
        else -> Remix.Weather.CloudyLine
    }
    HomeGlyphStyle.Lucide -> when (code) {
        0 -> LucideIcons.Sun
        45, 48 -> LucideIcons.CloudFog
        in 51..67, in 80..82 -> LucideIcons.CloudRain
        in 71..77, 85, 86 -> LucideIcons.CloudSnow
        in 95..99 -> LucideIcons.CloudLightning
        else -> LucideIcons.Cloud
    }
    HomeGlyphStyle.Phosphor -> when (code) {
        0 -> PhosphorIcons.Duotone.Sun
        45, 48 -> PhosphorIcons.Duotone.CloudFog
        in 51..67, in 80..82 -> PhosphorIcons.Duotone.CloudRain
        in 71..77, 85, 86 -> PhosphorIcons.Duotone.CloudSnow
        in 95..99 -> PhosphorIcons.Duotone.CloudLightning
        else -> PhosphorIcons.Duotone.Cloud
    }
    HomeGlyphStyle.Iconoir -> IconoirCommanderGlyphs.Weather
    HomeGlyphStyle.Hugeicons -> HugeCommanderGlyphs.Weather
}

private fun signalWeatherDescription(code: Int?): String = when (code) {
    0 -> "Clear skies"
    1 -> "Mostly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Foggy"
    in 51..57 -> "Drizzle"
    in 61..67, in 80..82 -> "Rain"
    in 71..77, 85, 86 -> "Snow"
    in 95..99 -> "Thunderstorms"
    else -> "Weather unavailable"
}

private fun signalDailyBriefing(now: LocalDateTime, events: List<SignalEvent>, tasks: List<TodoistTask>): String {
    val upcoming = events.count { !it.allDay && it.end.toLocalDateTime().isAfter(now) }
    val hour = now.hour
    val daySummary = when {
        upcoming == 0 && tasks.isEmpty() -> if (hour >= 18) "The evening is yours." else "A clear stretch ahead."
        upcoming == 0 && tasks.size == 1 -> "One task remains."
        upcoming == 0 -> "${tasks.size} tasks remain."
        upcoming == 1 && tasks.isEmpty() -> "One event remains."
        upcoming == 1 && tasks.size == 1 -> "One event and one task remain."
        tasks.isEmpty() -> "$upcoming events remain."
        else -> "$upcoming events and ${tasks.size} tasks remain."
    }
    return "Partly cloudy. $daySummary"
}

private fun Context.openCommanderSurface(target: Class<*>) {
    startActivity(Intent(this, target))
    @Suppress("DEPRECATION")
    (this as? Activity)?.overridePendingTransition(
        if (target == HubActivity::class.java) R.anim.surface_from_left else R.anim.surface_from_right,
        if (target == HubActivity::class.java) R.anim.surface_home_to_right else R.anim.surface_home_to_left,
    )
}

private fun TodoistTask.signalDateTime(): LocalDateTime? = dueDateTime?.let { value ->
    runCatching { Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime() }.recoverCatching { LocalDateTime.parse(value) }.getOrNull()
}

private fun signalOpenCard(context: Context, page: Int, events: List<SignalEvent>) {
    when (page) {
        0 -> events.firstOrNull()?.let { event -> context.startActivity(Intent(Intent.ACTION_VIEW, CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(event.id.toString()).build())) }
        1 -> Unit
        else -> context.startActivity(Intent(context, OverlayActivity::class.java).apply { action = OverlayActivity.ACTION_OPEN_OVERLAY; putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, TriggerSettings.noteSymbol().toString()) })
    }
}

private fun Context.signalOpenTasks() {
    startActivity(Intent(this, OverlayActivity::class.java).apply {
        action = OverlayActivity.ACTION_OPEN_OVERLAY
        putExtra(OverlayActivity.EXTRA_OPEN_TASKS, true)
    })
}
