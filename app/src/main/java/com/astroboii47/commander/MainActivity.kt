package com.astroboii47.commander

import android.annotation.SuppressLint
import android.content.Intent
import android.content.ComponentName
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.graphics.drawable.ColorDrawable
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import android.view.KeyEvent
import android.util.Log
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private data class SurfaceRequest(val page: Int, val nonce: Int)
    private var surfaceRequest by mutableStateOf(SurfaceRequest(SURFACE_HOME, 0))
    internal var settledSurfacePage = SURFACE_HOME
    private val hubConsumedKeys = mutableSetOf<Int>()
    private var returningFromBackground = false
    private val homeButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS &&
                intent.getStringExtra("reason") == "homekey" &&
                launcherResumed && settledSurfacePage == SURFACE_HOME
            ) HomeCarouselKeyBridge.moveFromHomeButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // A launcher root must always reconstruct from its launch intent.
        // Restoring PagerState here can resurrect Hub or Apps after Android
        // has explicitly invoked HOME. Configuration changes are handled by
        // this activity, so discarding the stale page bundle is intentional.
        super.onCreate(null)
        if (!ModuleSettings.homeEnabled.value) {
            startActivity(Intent(this, OverlayActivity::class.java))
            finish()
            return
        }
        configureLauncherWindowBackground()
        enableEdgeToEdge()
        val engine = CommandEngine(this)
        lifecycleScope.launch(Dispatchers.IO) { engine.warmAppIndex() }
        val requested = intent.requestedCommanderSurface()
        setIntent(homeTaskIntent())
        surfaceRequest = SurfaceRequest(requested, surfaceRequest.nonce + 1)
        settledSurfacePage = requested
        activeSurfacePage = requested
        ContextCompat.registerReceiver(
            this,
            homeButtonReceiver,
            IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (settledSurfacePage == SURFACE_HOME) {
                    HomeCarouselKeyBridge.move(-1)
                } else {
                    surfaceRequest = SurfaceRequest(SURFACE_HOME, surfaceRequest.nonce + 1)
                    settledSurfacePage = SURFACE_HOME
                    activeSurfacePage = SURFACE_HOME
                }
            }
        })
        setContent { AccentSelectionProvider { CommanderSurfacePager(engine, surfaceRequest.page, surfaceRequest.nonce) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val repeatedHome = intent.action == Intent.ACTION_MAIN &&
            intent.hasCategory(Intent.CATEGORY_HOME) && launcherResumed &&
            settledSurfacePage == SURFACE_HOME
        if (repeatedHome) {
            HomeCarouselKeyBridge.moveFromHomeButton()
            setIntent(homeTaskIntent())
            return
        }
        val requested = intent.requestedCommanderSurface()
        Log.d("CommanderPager", "external request page=$requested action=${intent.action}")
        // Never retain a page-specific request as the launcher's task intent.
        // Android may reuse that intent the next time Home is pressed.
        setIntent(homeTaskIntent())
        // Every external surface request is authoritative. Comparing only
        // against the last settled page loses requests that arrive while the
        // pager is moving (for example Apps -> Home during a launch/return).
        settledSurfacePage = SURFACE_NONE
        activeSurfacePage = SURFACE_NONE
        surfaceRequest = SurfaceRequest(requested, surfaceRequest.nonce + 1)
    }

    override fun onResume() {
        super.onResume()
        if (returningFromBackground) {
            returningFromBackground = false
            HomeEntranceBridge.trigger()
        }
        launcherResumed = true
        configureLauncherWindowBackground()
    }

    override fun onPause() {
        returningFromBackground = true
        launcherResumed = false
        super.onPause()
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(homeButtonReceiver) }
        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val enter = event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
        val physicalHomeKey = event.keyCode == KeyEvent.KEYCODE_HOME || event.keyCode == KeyEvent.KEYCODE_MOVE_HOME
        val nextCarouselKey = event.keyCode == KeyEvent.KEYCODE_SPACE || physicalHomeKey
        val homeCarouselKey = nextCarouselKey || event.keyCode == KeyEvent.KEYCODE_DEL || event.keyCode == KeyEvent.KEYCODE_BACK
        val homeStackKey = event.keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
        if (settledSurfacePage == SURFACE_HOME && homeStackKey) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                HomeCarouselKeyBridge.moveStack(if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) 1 else -1)
            }
            return true
        }
        if (settledSurfacePage == SURFACE_HOME && enter) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) HomeCarouselKeyBridge.activate()
            return true
        }
        if (settledSurfacePage == SURFACE_HOME && homeCarouselKey) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                if (event.keyCode == KeyEvent.KEYCODE_SPACE) HomeCarouselKeyBridge.space()
                else if (physicalHomeKey) HomeCarouselKeyBridge.moveFromHomeButton()
                else HomeCarouselKeyBridge.move(if (nextCarouselKey) 1 else -1)
            }
            return true
        }
        // Hub's handler remains registered for the lifetime of the retained
        // pager page. The settled foreground page is the sole routing switch;
        // recomposition can no longer temporarily remove keyboard ownership.
        val hubOwnsInput = settledSurfacePage == SURFACE_HUB
        if (hubOwnsInput) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val handled = if (enter) HubKeyBridge.handleEnter() else HubKeyBridge.handleKey(event.keyCode)
                if (handled) {
                    hubConsumedKeys += event.keyCode
                    return true
                }
            }
            if (event.action == KeyEvent.ACTION_UP && hubConsumedKeys.remove(event.keyCode)) return true
        } else {
            hubConsumedKeys.clear()
        }
        // Plain typing launches Commander Bar only from Home. Hub owns its
        // printable shortcuts, and an unsettled pager must never guess that
        // a key was intended for Home.
        if (!hubOwnsInput && settledSurfacePage == SURFACE_HOME &&
            event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 &&
            event.keyCode !in listOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER)
        ) {
            val unicode = event.getUnicodeChar(event.metaState)
            if (unicode > 0 && !Character.isISOControl(unicode)) {
                val initial = String(Character.toChars(unicode))
                // Keep receiving the rest of a fast typing burst while the
                // overlay window is being created and acquiring key focus.
                HomeTypingHandoff.begin(initial, event.keyCode)
                startActivity(Intent(this, OverlayActivity::class.java).apply {
                    action = OverlayActivity.ACTION_OPEN_OVERLAY
                    putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, initial)
                })
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    companion object {
        const val ACTION_SHOW_SURFACE = "com.astroboii47.commander.action.SHOW_SURFACE"
        const val EXTRA_SURFACE = "commander_surface"
        const val SURFACE_HUB = 0
        const val SURFACE_HOME = 1
        const val SURFACE_APPS = 2
        const val SURFACE_NONE = -1
        @Volatile var activeSurfacePage: Int = SURFACE_NONE
        @Volatile var launcherResumed: Boolean = false
    }

    private fun Intent.requestedCommanderSurface(): Int =
        if (action == ACTION_SHOW_SURFACE) getIntExtra(EXTRA_SURFACE, SURFACE_HOME)
            .coerceIn(if (ModuleSettings.hubEnabled.value) SURFACE_HUB else SURFACE_HOME, SURFACE_APPS)
        else SURFACE_HOME

    private fun homeTaskIntent() = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_HOME)
        .setClass(this, MainActivity::class.java)

    internal fun configureLauncherWindowBackground() {
        if (HomeSettings.wallpaperMode.value == HomeWallpaperMode.System) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            val alpha = (HomeSettings.wallpaperDarkness.floatValue.coerceIn(0f, 1f) * 255).toInt()
            // This is drawn by the window manager before Compose's first frame,
            // so the raw system wallpaper is never exposed at full brightness.
            window.setBackgroundDrawable(ColorDrawable(AndroidColor.argb(alpha, 0, 0, 0)))
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            // Custom wallpaper is owned by the launcher renderer. Keep the
            // underlying window opaque so the unrelated system wallpaper can
            // never leak through while that bitmap is prepared.
            window.setBackgroundDrawable(ColorDrawable(AndroidColor.BLACK))
        }
    }
}

object HomeEntranceBridge {
    val sequence = mutableIntStateOf(0)
    fun trigger() { sequence.intValue++ }
}

@Composable
private fun MainActivity.CommanderSurfacePager(engine: CommandEngine, requestedPage: Int, requestNonce: Int) {
    val scope = rememberCoroutineScope()
    var position by remember { mutableFloatStateOf(requestedPage.toFloat()) }
    var activePage by remember { mutableIntStateOf(requestedPage) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    var restoreHomeOnResume by remember { mutableStateOf(false) }
    var notificationAccess by remember { mutableStateOf(hasNotificationAccess()) }
    // Wallpaper mode can change while the translucent picker is closing. Keep
    // the window's SHOW_WALLPAPER flag/background in lockstep with Compose;
    // onResume alone is too early when the system write completes afterward.
    LaunchedEffect(HomeSettings.wallpaperMode.value) {
        configureLauncherWindowBackground()
    }
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationAccess = hasNotificationAccess()
                if (restoreHomeOnResume) {
                    restoreHomeOnResume = false
                    settleJob?.cancel()
                    position = MainActivity.SURFACE_HOME.toFloat()
                    activePage = MainActivity.SURFACE_HOME
                    settledSurfacePage = MainActivity.SURFACE_HOME
                    MainActivity.activeSurfacePage = MainActivity.SURFACE_HOME
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(requestedPage, requestNonce) {
        settleJob?.cancel()
        position = requestedPage.toFloat()
        activePage = requestedPage
        settledSurfacePage = requestedPage
        MainActivity.activeSurfacePage = requestedPage
        Log.d("CommanderPager", "request settled=$requestedPage")
    }
    val baseDarkness = when (HomeSettings.wallpaperMode.value) {
        HomeWallpaperMode.Black -> 1f
        // The window background is the persistent darkness layer for the
        // native system wallpaper. Applying it again in Compose would double
        // dim the image and change brightness during system transitions.
        HomeWallpaperMode.System -> 0f
        HomeWallpaperMode.Custom -> HomeSettings.wallpaperDarkness.floatValue
    }
    // The visible system wallpaper is now rendered inside this retained layer,
    // so its user-selected dim must be applied here as well. The matching
    // window-level scrim remains only as a seamless pre-frame fallback.
    BoxWithConstraints(
        Modifier.fillMaxSize().background(
            if (HomeSettings.wallpaperMode.value == HomeWallpaperMode.System) Color.Transparent else Color.Black,
        ),
    ) {
        val pageWidth = maxWidth
        val pageWidthPx = with(LocalDensity.current) { pageWidth.toPx() }
        val pageHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        fun settleTo(targetPage: Int, source: String) {
            val target = targetPage.coerceIn(if (ModuleSettings.hubEnabled.value) MainActivity.SURFACE_HUB else MainActivity.SURFACE_HOME, MainActivity.SURFACE_APPS)
            Log.d("CommanderPager", "command source=$source target=$target from=$position")
            settleJob?.cancel()
            settleJob = scope.launch {
                val remaining = kotlin.math.abs(target - position).coerceIn(0f, 1f)
                val duration = (105 + remaining * 135).toInt()
                Animatable(position).animateTo(target.toFloat(), tween(duration, easing = FastOutSlowInEasing)) {
                    position = value
                }
                position = target.toFloat()
                activePage = target
                settledSurfacePage = target
                MainActivity.activeSurfacePage = target
                Log.d("CommanderPager", "settled=$target")
            }
        }
        // In Device Home mode Android owns the one authoritative wallpaper
        // surface. Drawing another decoded copy here causes overview/Recents
        // to reveal two differently timed wallpaper layers. Custom mode still
        // renders its private bitmap normally.
        SignalWallpaper(this@CommanderSurfacePager, Modifier.fillMaxSize(), drawSystemCopy = false)
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = baseDarkness)))
        Box(Modifier.fillMaxSize().clipToBounds().pointerInput(pageWidthPx, pageHeightPx) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    settleJob?.cancel()
                    val startPage = kotlin.math.round(position).toInt().coerceIn(0, 2)
                    val homeSidePx = kotlin.math.min(pageWidthPx, pageHeightPx)
                    val homeTopPx = (pageHeightPx - homeSidePx) / 2f
                    val carouselOwnsGesture = startPage == MainActivity.SURFACE_HOME &&
                        down.position.y in (homeTopPx + homeSidePx * .29f)..(homeTopPx + homeSidePx * .80f)
                    var totalX = 0f
                    var totalY = 0f
                    var horizontal = false
                    var vertical = false
                    var cancelled = false
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        val delta = change.positionChange()
                        totalX += delta.x
                        totalY += delta.y
                        if (!horizontal && !vertical && kotlin.math.hypot(totalX, totalY) > viewConfiguration.touchSlop) {
                            if (carouselOwnsGesture) { cancelled = true; break }
                            horizontal = kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 1.15f
                            vertical = kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.35f
                            if (!horizontal && !vertical) cancelled = true
                        }
                        if (cancelled || !change.pressed) break
                        if (horizontal) {
                            if (change.isConsumed) { cancelled = true; break }
                            position = (startPage - totalX / pageWidthPx)
                                .coerceIn(startPage - .92f, startPage + .92f).coerceIn(0f, 2f)
                            change.consume()
                        }
                    } while (change.pressed)
                    if (horizontal && !cancelled) {
                        val elapsed = (android.os.SystemClock.uptimeMillis() - down.uptimeMillis).coerceAtLeast(1L)
                        val velocity = totalX * 1000f / elapsed
                        val advance = kotlin.math.abs(totalX) > pageWidthPx * .22f || kotlin.math.abs(velocity) > 650f
                        val target = if (advance) {
                            (startPage + if (totalX < 0f) 1 else -1).coerceIn(if (ModuleSettings.hubEnabled.value) 0 else 1, 2)
                        } else startPage
                        Log.d("CommanderPager", "gesture from=$startPage target=$target dx=$totalX")
                        settleTo(target, "gesture")
                    } else if (vertical && !cancelled && startPage == MainActivity.SURFACE_HOME &&
                        totalY < -pageHeightPx * .105f && kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.5f
                    ) {
                        settleTo(startPage, "vertical-command")
                        this@CommanderSurfacePager.startActivity(Intent(this@CommanderSurfacePager, OverlayActivity::class.java).apply {
                            action = OverlayActivity.ACTION_OPEN_OVERLAY
                            putExtra(OverlayActivity.EXTRA_INITIAL_QUERY, "")
                        })
                    }
                }
            }) {
            for (page in 0..2) {
                // An edge page has no visible portion while the opposite edge
                // is settled. Avoid composing its notification list/app grid.
                if (page == MainActivity.SURFACE_HUB && !ModuleSettings.hubEnabled.value) continue
                if (kotlin.math.abs(page - activePage) > 1) continue
                Box(Modifier.fillMaxSize().graphicsLayer { translationX = (page - position) * pageWidthPx }.clipToBounds()) {
                when (page) {
                    0 -> MinimalHubApp(
                        hasAccess = notificationAccess,
                        forceWallpaperGlass = true,
                        // This page is retained beside Home. Keep its handler
                        // registered; MainActivity routes to it only while Hub
                        // is the settled foreground page.
                        isActiveSurface = true,
                        openAccessSettings = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                        onDismiss = { settleTo(MainActivity.SURFACE_HOME, "hub-dismiss") },
                    )
                    1 -> SignalHome(
                        engine,
                        sharedWallpaper = true,
                        onOpenHub = if (ModuleSettings.hubEnabled.value) ({ settleTo(MainActivity.SURFACE_HUB, "home-open-hub") }) else null,
                        onOpenApps = { settleTo(MainActivity.SURFACE_APPS, "home-open-apps") },
                        isActiveSurface = activePage == MainActivity.SURFACE_HOME,
                        onBeforeExternalLaunch = {
                            restoreHomeOnResume = true
                            settleJob?.cancel()
                            position = MainActivity.SURFACE_HOME.toFloat()
                            activePage = MainActivity.SURFACE_HOME
                            settledSurfacePage = MainActivity.SURFACE_HOME
                            MainActivity.activeSurfacePage = MainActivity.SURFACE_HOME
                        },
                    )
                    else -> CommanderApps(
                        sharedWallpaper = true,
                        onBeforeAppLaunch = {
                            restoreHomeOnResume = true
                            settleJob?.cancel()
                            position = MainActivity.SURFACE_HOME.toFloat()
                            activePage = MainActivity.SURFACE_HOME
                            settledSurfacePage = MainActivity.SURFACE_HOME
                            MainActivity.activeSurfacePage = MainActivity.SURFACE_HOME
                        },
                    )
                }
                }
            }
    }
}
}

private fun MainActivity.hasNotificationAccess(): Boolean {
    val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners").orEmpty()
    return enabled.split(':').mapNotNull(ComponentName::unflattenFromString).any { it.packageName == packageName }
}
