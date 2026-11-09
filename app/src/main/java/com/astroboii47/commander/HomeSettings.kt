package com.astroboii47.commander

import android.content.Context
import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.io.FileOutputStream

enum class HomeWallpaperMode(val label: String) {
    Black("Black"), System("Existing Home"), Custom("Custom")
}

enum class HomeGlyphStyle(val label: String) {
    MaterialOutlined("Material outline"), MaterialFilled("Material filled"),
    Tabler("Tabler"), Remix("Remix"), Lucide("Lucide"), Phosphor("Phosphor"),
    Iconoir("Iconoir"), Hugeicons("Hugeicons")
}

enum class HomeFontStyle(val label: String) {
    CommanderMono("IBM Plex Mono"), Ryoku("Space Grotesk"), JetBrainsMono("JetBrains Mono"),
    GeistMono("Geist Mono"), AzeretMono("Azeret Mono"), SpaceMono("Space Mono"),
    CommitMono("Commit Mono"), Oxanium("Oxanium"), ChakraPetch("Chakra Petch"),
    Michroma("Michroma"), Syncopate("Syncopate"), Orbitron("Orbitron"),
    Aldrich("Aldrich"), Audiowide("Audiowide"), Rajdhani("Rajdhani"),
    Inter("Inter"), DmSans("DM Sans"), Courier("Courier Prime")
}

enum class CarouselViewMode(val label: String, val description: String) {
    Stable("Stable ring", "Flat, balanced original geometry"),
    Angled("Angled ring", "Deeper orbit without camera elevation"),
    Elevated("Elevated ring", "Camera-above perspective for more depth"),
}

object HomeSettings {
    @Volatile private var applyingCommanderWallpaper = false
    private const val Prefs = "commander_home_settings"
    private const val Mode = "wallpaper_mode"
    private const val UriKey = "wallpaper_uri"
    private const val FolderUriKey = "wallpaper_folder_uri"
    private const val PickerSetsSystemHome = "wallpaper_picker_sets_system_home"
    private const val LastSystemWallpaperId = "last_system_wallpaper_id"
    private const val Darkness = "wallpaper_darkness"
    private const val Blur = "wallpaper_blur"
    private const val TimelineGlow = "timeline_glow"
    private const val CardGlow = "card_glow"
    private const val Remaining = "show_remaining"
    private const val WallpaperAccent = "wallpaper_accent"
    private const val WallpaperPanX = "wallpaper_pan_x"
    private const val WallpaperPanY = "wallpaper_pan_y"
    private const val WallpaperZoom = "wallpaper_zoom"
    private const val Timeline12Hour = "timeline_12_hour"
    private const val TimelineHourlyLabels = "timeline_hourly_labels"
    private const val GlassWallpaperV2 = "glass_wallpaper_v2"
    private const val CommandDeck = "command_deck"
    private const val ReferenceHome = "reference_home"
    private const val GlyphStyle = "carousel_glyph_style"
    private const val FontStyle = "home_font_style"
    private const val BoldCardTitles = "bold_card_titles"
    private const val CarouselView = "carousel_view_mode"
    private const val CarouselFloating = "carousel_floating"
    private const val CarouselAutoRotate = "carousel_auto_rotate"
    private const val CarouselPitch = "carousel_pitch"
    private const val CarouselRearOpacity = "carousel_rear_opacity"
    private const val TimelineUpcoming = "timeline_upcoming_labels"
    private const val TasksDotCount = "tasks_dot_count"
    private const val StatusIslands = "status_islands"
    private const val WeatherIsland = "weather_island"
    private const val GlassEnabled = "wallpaper_glass_enabled"
    private const val GlassBlur = "wallpaper_glass_blur"
    private const val GlassReed = "wallpaper_glass_reed"
    private const val GlassDistortion = "wallpaper_glass_distortion"
    private const val GlassLighting = "wallpaper_glass_lighting"

    val wallpaperMode = mutableStateOf(HomeWallpaperMode.Black)
    val customWallpaperUri = mutableStateOf<String?>(null)
    val wallpaperFolderUri = mutableStateOf<String?>(null)
    val wallpaperPickerSetsSystemHome = mutableStateOf(true)
    val wallpaperDarkness = mutableFloatStateOf(.44f)
    val wallpaperBlur = mutableFloatStateOf(2f)
    val timelineGlow = mutableFloatStateOf(1f)
    val cardGlow = mutableFloatStateOf(1f)
    val showRemaining = mutableStateOf(true)
    val wallpaperAccent = mutableStateOf(false)
    val wallpaperPanX = mutableFloatStateOf(0f)
    val wallpaperPanY = mutableFloatStateOf(0f)
    val wallpaperZoom = mutableFloatStateOf(1f)
    val timeline12Hour = mutableStateOf(false)
    val timelineHourlyLabels = mutableStateOf(false)
    val commandDeck = mutableStateOf(true)
    val referenceHome = mutableStateOf(true)
    val glyphStyle = mutableStateOf(HomeGlyphStyle.Tabler)
    val fontStyle = mutableStateOf(HomeFontStyle.CommanderMono)
    val boldCardTitles = mutableStateOf(false)
    val carouselView = mutableStateOf(CarouselViewMode.Elevated)
    val carouselFloating = mutableStateOf(true)
    val carouselAutoRotate = mutableStateOf(false)
    val carouselPitch = mutableFloatStateOf(7.5f)
    val carouselRearOpacity = mutableFloatStateOf(.50f)
    val timelineUpcomingLabels = mutableStateOf(true)
    val tasksDotCount = mutableStateOf(true)
    val statusIslands = mutableStateOf(true)
    val weatherIsland = mutableStateOf(true)
    val wallpaperGlassEnabled = mutableStateOf(false)
    val wallpaperGlassBlur = mutableFloatStateOf(2f)
    val wallpaperGlassReed = mutableFloatStateOf(40f)
    val wallpaperGlassDistortion = mutableFloatStateOf(26f)
    val wallpaperGlassLighting = mutableFloatStateOf(15f)
    val wallpaperGlassRevision = mutableIntStateOf(0)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(GlassWallpaperV2, false)) {
            val migratedDarkness = if (prefs.contains(Darkness)) (prefs.getFloat(Darkness, .44f) * .62f).coerceIn(.22f, .52f) else .44f
            val migratedBlur = if (prefs.contains(Blur)) (prefs.getFloat(Blur, 2f) * .25f).coerceIn(0f, 3f) else 2f
            prefs.edit().putFloat(Darkness, migratedDarkness).putFloat(Blur, migratedBlur).putBoolean(GlassWallpaperV2, true).apply()
        }
        wallpaperMode.value = runCatching { HomeWallpaperMode.valueOf(prefs.getString(Mode, HomeWallpaperMode.Black.name)!!) }.getOrDefault(HomeWallpaperMode.Black)
        customWallpaperUri.value = prefs.getString(UriKey, null)
        wallpaperFolderUri.value = prefs.getString(FolderUriKey, null)
        wallpaperPickerSetsSystemHome.value = prefs.getBoolean(PickerSetsSystemHome, true)
        wallpaperDarkness.floatValue = prefs.getFloat(Darkness, .44f).coerceIn(.15f, .85f)
        wallpaperBlur.floatValue = prefs.getFloat(Blur, 2f).coerceIn(0f, 12f)
        timelineGlow.floatValue = prefs.getFloat(TimelineGlow, 1f).coerceIn(.45f, 1.65f)
        cardGlow.floatValue = prefs.getFloat(CardGlow, 1f).coerceIn(.45f, 1.65f)
        showRemaining.value = prefs.getBoolean(Remaining, true)
        wallpaperAccent.value = prefs.getBoolean(WallpaperAccent, false)
        wallpaperPanX.floatValue = prefs.getFloat(WallpaperPanX, 0f).coerceIn(-1f, 1f)
        wallpaperPanY.floatValue = prefs.getFloat(WallpaperPanY, 0f).coerceIn(-1f, 1f)
        wallpaperZoom.floatValue = prefs.getFloat(WallpaperZoom, 1f).coerceIn(1f, 3f)
        timeline12Hour.value = prefs.getBoolean(Timeline12Hour, false)
        timelineHourlyLabels.value = prefs.getBoolean(TimelineHourlyLabels, false)
        commandDeck.value = prefs.getBoolean(CommandDeck, true)
        referenceHome.value = prefs.getBoolean(ReferenceHome, true)
        glyphStyle.value = runCatching {
            HomeGlyphStyle.valueOf(prefs.getString(GlyphStyle, HomeGlyphStyle.Tabler.name)!!)
        }.getOrDefault(HomeGlyphStyle.Tabler)
        fontStyle.value = runCatching {
            HomeFontStyle.valueOf(prefs.getString(FontStyle, HomeFontStyle.CommanderMono.name)!!)
        }.getOrDefault(HomeFontStyle.CommanderMono)
        boldCardTitles.value = prefs.getBoolean(BoldCardTitles, false)
        carouselView.value = runCatching {
            CarouselViewMode.valueOf(prefs.getString(CarouselView, CarouselViewMode.Elevated.name)!!)
        }.getOrDefault(CarouselViewMode.Elevated)
        carouselFloating.value = prefs.getBoolean(CarouselFloating, true)
        carouselAutoRotate.value = prefs.getBoolean(CarouselAutoRotate, false)
        carouselPitch.floatValue = prefs.getFloat(CarouselPitch, 7.5f).coerceIn(5f, 11f)
        carouselRearOpacity.floatValue = prefs.getFloat(CarouselRearOpacity, .50f).coerceIn(.30f, 1f)
        timelineUpcomingLabels.value = prefs.getBoolean(TimelineUpcoming, true)
        tasksDotCount.value = prefs.getBoolean(TasksDotCount, true)
        statusIslands.value = prefs.getBoolean(StatusIslands, true)
        weatherIsland.value = prefs.getBoolean(WeatherIsland, true)
        wallpaperGlassEnabled.value = prefs.getBoolean(GlassEnabled, false)
        wallpaperGlassBlur.floatValue = prefs.getFloat(GlassBlur, 2f).coerceIn(0f, 12f)
        wallpaperGlassReed.floatValue = prefs.getFloat(GlassReed, 40f).coerceIn(10f, 100f)
        wallpaperGlassDistortion.floatValue = prefs.getFloat(GlassDistortion, 26f).coerceIn(0f, 70f)
        wallpaperGlassLighting.floatValue = prefs.getFloat(GlassLighting, 15f).coerceIn(0f, 40f)
    }

    fun save(context: Context, mode: HomeWallpaperMode, darkness: Float, blur: Float, timeline: Float, cards: Float, showTime: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putString(Mode, mode.name)
            .putFloat(Darkness, darkness.coerceIn(.15f, .85f))
            .putFloat(Blur, blur.coerceIn(0f, 12f))
            .putFloat(TimelineGlow, timeline.coerceIn(.45f, 1.65f))
            .putFloat(CardGlow, cards.coerceIn(.45f, 1.65f))
            .putBoolean(Remaining, showTime)
            .apply()
        initialize(context)
        if (wallpaperAccent.value) applyWallpaperAccent(context)
    }

    fun saveCustomWallpaper(context: Context, uri: Uri) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putString(UriKey, uri.toString())
            .putString(Mode, HomeWallpaperMode.Custom.name)
            .apply()
        initialize(context)
        if (wallpaperAccent.value) applyWallpaperAccent(context)
    }

    fun setWallpaperMode(context: Context, mode: HomeWallpaperMode) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putString(Mode, mode.name).apply()
        wallpaperMode.value = mode
        if (wallpaperAccent.value) applyWallpaperAccent(context)
    }

    fun saveWallpaperFolder(context: Context, uri: Uri) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putString(FolderUriKey, uri.toString())
            .apply()
        wallpaperFolderUri.value = uri.toString()
    }

    fun setWallpaperPickerSetsSystemHome(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putBoolean(PickerSetsSystemHome, enabled).apply()
        wallpaperPickerSetsSystemHome.value = enabled
    }

    fun saveWallpaperPosition(context: Context, panX: Float, panY: Float, zoom: Float) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putFloat(WallpaperPanX, panX.coerceIn(-1f, 1f))
            .putFloat(WallpaperPanY, panY.coerceIn(-1f, 1f))
            .putFloat(WallpaperZoom, zoom.coerceIn(1f, 3f)).apply()
        initialize(context)
    }

    fun setTimeline12Hour(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(Timeline12Hour, enabled).apply()
        timeline12Hour.value = enabled
    }

    fun setFontStyle(context: Context, style: HomeFontStyle) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putString(FontStyle, style.name).apply()
        fontStyle.value = style
    }

    fun setBoldCardTitles(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putBoolean(BoldCardTitles, enabled).apply()
        boldCardTitles.value = enabled
    }

    fun setTimelineHourlyLabels(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(TimelineHourlyLabels, enabled).apply()
        timelineHourlyLabels.value = enabled
    }

    fun setCommandDeck(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(CommandDeck, enabled).apply()
        commandDeck.value = enabled
    }

    fun setReferenceHome(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(ReferenceHome, enabled).apply()
        referenceHome.value = enabled
    }

    fun setGlyphStyle(context: Context, style: HomeGlyphStyle) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putString(GlyphStyle, style.name).apply()
        glyphStyle.value = style
    }

    fun setCarouselView(context: Context, mode: CarouselViewMode) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putString(CarouselView, mode.name).apply()
        carouselView.value = mode
    }

    fun setCarouselFloating(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(CarouselFloating, enabled).apply()
        carouselFloating.value = enabled
    }

    fun setCarouselAutoRotate(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(CarouselAutoRotate, enabled).apply()
        carouselAutoRotate.value = enabled
    }

    fun setCarouselPitch(context: Context, degrees: Float) {
        val value = degrees.coerceIn(5f, 11f)
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putFloat(CarouselPitch, value).apply()
        carouselPitch.floatValue = value
    }

    fun setCarouselRearOpacity(context: Context, opacity: Float) {
        val value = opacity.coerceIn(.30f, 1f)
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putFloat(CarouselRearOpacity, value).apply()
        carouselRearOpacity.floatValue = value
    }


    fun setTimelineUpcomingLabels(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(TimelineUpcoming, enabled).apply()
        timelineUpcomingLabels.value = enabled
    }

    fun setTasksDotCount(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putBoolean(TasksDotCount, enabled).apply()
        tasksDotCount.value = enabled
    }

    fun setWeatherIsland(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putBoolean(WeatherIsland, enabled).apply()
        weatherIsland.value = enabled
    }

    fun setWallpaperAppearance(context: Context, darkness: Float, blur: Float) {
        val dark = darkness.coerceIn(.15f, .85f)
        val blurValue = blur.coerceIn(0f, 12f)
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putFloat(Darkness, dark).putFloat(Blur, blurValue).apply()
        wallpaperDarkness.floatValue = dark
        wallpaperBlur.floatValue = blurValue
    }

    fun setWallpaperGlass(
        context: Context,
        enabled: Boolean,
        blur: Float,
        reed: Float,
        distortion: Float,
        lighting: Float,
    ) {
        wallpaperGlassEnabled.value = enabled
        wallpaperGlassBlur.floatValue = blur.coerceIn(0f, 12f)
        wallpaperGlassReed.floatValue = reed.coerceIn(10f, 100f)
        wallpaperGlassDistortion.floatValue = distortion.coerceIn(0f, 70f)
        wallpaperGlassLighting.floatValue = lighting.coerceIn(0f, 40f)
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putBoolean(GlassEnabled, enabled)
            .putFloat(GlassBlur, wallpaperGlassBlur.floatValue)
            .putFloat(GlassReed, wallpaperGlassReed.floatValue)
            .putFloat(GlassDistortion, wallpaperGlassDistortion.floatValue)
            .putFloat(GlassLighting, wallpaperGlassLighting.floatValue)
            .apply()
    }

    fun glassWallpaperFile(context: Context): File = File(context.filesDir, "commander-nothing-glass.jpg")

    fun renderGlassWallpaper(context: Context): Boolean = runCatching {
        if (!wallpaperGlassEnabled.value) {
            glassWallpaperFile(context).delete()
            wallpaperGlassRevision.intValue++
            return true
        }
        val uri = customWallpaperUri.value?.let(Uri::parse) ?: return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2400) sample *= 2
        val source = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("Could not read wallpaper")
        val rendered = NothingGlassWallpaper.render(
            source,
            wallpaperGlassBlur.floatValue,
            wallpaperGlassReed.floatValue,
            wallpaperGlassDistortion.floatValue,
            wallpaperGlassLighting.floatValue,
        )
        FileOutputStream(glassWallpaperFile(context)).use {
            rendered.compress(Bitmap.CompressFormat.JPEG, 96, it)
        }
        source.recycle()
        rendered.recycle()
        wallpaperGlassRevision.intValue++
        true
    }.getOrDefault(false)

    fun setStatusIslands(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(StatusIslands, enabled).apply()
        statusIslands.value = enabled
    }

    fun applyCustomToSystemHome(context: Context): Boolean {
        val uri = customWallpaperUri.value?.let(Uri::parse) ?: return false
        return applyWallpaperUriToSystemHome(context, uri)
    }

    fun applyWallpaperUriToSystemHome(context: Context, uri: Uri): Boolean = runCatching {
        if (wallpaperGlassEnabled.value && !glassWallpaperFile(context).exists()) renderGlassWallpaper(context)
        val source = if (wallpaperGlassEnabled.value && glassWallpaperFile(context).exists()) {
            BitmapFactory.decodeFile(glassWallpaperFile(context).absolutePath)
        } else context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        source ?: error("Could not read wallpaper")
        val outputSize = 2048
        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val baseScale = maxOf(outputSize.toFloat() / source.width, outputSize.toFloat() / source.height) * wallpaperZoom.floatValue
        val matrix = Matrix().apply {
            postScale(baseScale, baseScale)
            val drawnW = source.width * baseScale
            val drawnH = source.height * baseScale
            postTranslate(
                (outputSize - drawnW) / 2f + wallpaperPanX.floatValue * (drawnW - outputSize) * .5f,
                (outputSize - drawnH) / 2f + wallpaperPanY.floatValue * (drawnH - outputSize) * .5f,
            )
        }
        Canvas(output).drawBitmap(source, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        applyingCommanderWallpaper = true
        try {
            WallpaperManager.getInstance(context).setBitmap(output, null, true, WallpaperManager.FLAG_SYSTEM)
            rememberCurrentSystemWallpaper(context)
        } finally {
            applyingCommanderWallpaper = false
        }
        source.recycle(); output.recycle(); true
    }.getOrDefault(false)

    /** Returns true only when another app changed Android's Home wallpaper. */
    fun consumeExternalSystemWallpaperChange(context: Context): Boolean {
        if (applyingCommanderWallpaper) return false
        val manager = WallpaperManager.getInstance(context)
        val current = runCatching { manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM) }.getOrDefault(-1)
        if (current < 0) return false
        val prefs = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
        val previous = prefs.getInt(LastSystemWallpaperId, -1)
        prefs.edit().putInt(LastSystemWallpaperId, current).apply()
        return previous >= 0 && previous != current
    }

    private fun rememberCurrentSystemWallpaper(context: Context) {
        val current = runCatching {
            WallpaperManager.getInstance(context).getWallpaperId(WallpaperManager.FLAG_SYSTEM)
        }.getOrDefault(-1)
        if (current >= 0) context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
            .putInt(LastSystemWallpaperId, current).apply()
    }

    fun setWallpaperAccent(context: Context, enabled: Boolean) {
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putBoolean(WallpaperAccent, enabled).apply()
        wallpaperAccent.value = enabled
        if (enabled) applyWallpaperAccent(context)
    }

    fun applyWallpaperAccent(context: Context): Boolean {
        val raw = when (wallpaperMode.value) {
            HomeWallpaperMode.System -> runCatching {
                WallpaperManager.getInstance(context).drawable?.toBitmap()?.let(::vibrantWallpaperColor)
            }.getOrNull()
            HomeWallpaperMode.Custom -> customWallpaperUri.value?.let { value ->
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(value))?.use { input ->
                        BitmapFactory.decodeStream(input)?.let(::vibrantWallpaperColor)
                    }
                }.getOrNull()
            }
            HomeWallpaperMode.Black -> null
        } ?: return false
        val hsv = FloatArray(3).also { AndroidColor.colorToHSV(raw, it) }
        hsv[1] = hsv[1].coerceIn(.48f, .82f)
        hsv[2] = hsv[2].coerceIn(.74f, .96f)
        return AccentSettings.save(context, String.format("#%06X", 0xFFFFFF and AndroidColor.HSVToColor(hsv)))
    }

    private fun vibrantWallpaperColor(bitmap: Bitmap): Int? {
        val sample = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        var red = 0.0; var green = 0.0; var blue = 0.0; var weight = 0.0
        val hsv = FloatArray(3)
        for (y in 0 until sample.height step 2) for (x in 0 until sample.width step 2) {
            val pixel = sample.getPixel(x, y)
            AndroidColor.colorToHSV(pixel, hsv)
            if (hsv[1] >= .18f && hsv[2] in .18f..96f) {
                // Prefer the wallpaper's colourful highlights instead of the
                // often dark/neutral system "primary" swatch.
                val w = (.25f + hsv[1] * hsv[1] * 1.8f + hsv[2] * .25f).toDouble()
                red += AndroidColor.red(pixel) * w
                green += AndroidColor.green(pixel) * w
                blue += AndroidColor.blue(pixel) * w
                weight += w
            }
        }
        if (sample !== bitmap) sample.recycle()
        return if (weight == 0.0) null else AndroidColor.rgb(
            (red / weight).toInt().coerceIn(0, 255),
            (green / weight).toInt().coerceIn(0, 255),
            (blue / weight).toInt().coerceIn(0, 255),
        )
    }
}
