package com.astroboii47.commander

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import kotlin.math.PI
import kotlin.math.sin

object SoundSettings {
    private const val PREFS = "sound_feedback"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_CONFIRMATION_STYLE = "confirmation_style"
    private const val KEY_VOLUME = "volume"
    private const val KEY_PULSE_DEFAULT_APPLIED = "pulse_default_applied"
    val enabled = mutableStateOf(true)
    val confirmationStyle = mutableStateOf(ConfirmationSoundStyle.Pulse)
    val volume = mutableStateOf(1f)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        enabled.value = prefs.getBoolean(KEY_ENABLED, true)
        if (!prefs.getBoolean(KEY_PULSE_DEFAULT_APPLIED, false)) {
            prefs.edit()
                .putString(KEY_CONFIRMATION_STYLE, ConfirmationSoundStyle.Pulse.name)
                .putBoolean(KEY_PULSE_DEFAULT_APPLIED, true)
                .apply()
        }
        confirmationStyle.value = runCatching {
            ConfirmationSoundStyle.valueOf(
                prefs.getString(KEY_CONFIRMATION_STYLE, null) ?: ConfirmationSoundStyle.Pulse.name,
            )
        }.getOrDefault(ConfirmationSoundStyle.Pulse)
        volume.value = prefs.getFloat(KEY_VOLUME, 1f).coerceIn(0f, 2f)
    }

    fun save(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, value).apply()
        enabled.value = value
    }

    fun saveConfirmationStyle(context: Context, style: ConfirmationSoundStyle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_CONFIRMATION_STYLE, style.name).apply()
        confirmationStyle.value = style
    }

    fun saveVolume(context: Context, value: Float) {
        val safe = value.coerceIn(0f, 2f)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(KEY_VOLUME, safe).apply()
        volume.value = safe
    }
}

enum class CommandSound { Open, Step, Confirm, Error }
enum class ConfirmationSoundStyle(val label: String) {
    Pulse("Pulse"),
    QuickGlass("Quick glass"),
    GlassResolve("Glass resolve"),
    SoftResolve("Soft resolve"),
    CrystalComplete("Crystal complete"),
    LuminousSuccess("Luminous success"),
    SingleGlassChord("Single glass chord"),
}

object SoundFeedback {
    private const val SAMPLE_RATE = 24_000
    private val mainHandler = Handler(Looper.getMainLooper())
    private var soundPool: SoundPool? = null
    private var pulseLaunchId = 0
    private var pulseConfirmId = 0
    private val loadedSounds = mutableSetOf<Int>()
    private val samples by lazy {
        mapOf(
            CommandSound.Open to tone(52, 760.0, 800.0, .060, 2.0),
            CommandSound.Step to tone(28, 920.0, 960.0, .038, 2.0),
            CommandSound.Error to chime(
                260,
                listOf(Strike(0, 470.0, .052, 20.0), Strike(44, 392.0, .044, 18.0)),
                echoDelayMs = 46,
                echoLevel = .08,
            ),
        )
    }

    fun initialize(context: Context) {
        if (soundPool != null) return
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) synchronized(loadedSounds) { loadedSounds += sampleId }
        }
        pulseLaunchId = pool.load(context.applicationContext, R.raw.pulse_launch, 1)
        pulseConfirmId = pool.load(context.applicationContext, R.raw.pulse_confirm, 1)
        soundPool = pool
    }
    private val confirmations by lazy {
        mapOf(
            ConfirmationSoundStyle.Pulse to chime(125, listOf(
                Strike(0, 587.33, .046, 23.0), Strike(38, 880.0, .056, 18.0),
            ), 31, .04),
            ConfirmationSoundStyle.QuickGlass to chime(95, listOf(
                Strike(0, 760.0, .058, 30.0),
            ), 0, 0.0),
            ConfirmationSoundStyle.GlassResolve to chime(125, listOf(
                Strike(0, 659.25, .046, 25.0), Strike(0, 987.77, .025, 25.0),
            ), 0, 0.0),
            ConfirmationSoundStyle.SoftResolve to chime(380, listOf(
                Strike(0, 587.33, .050, 22.0), Strike(52, 880.0, .060, 17.0),
            ), 42, .10),
            ConfirmationSoundStyle.CrystalComplete to chime(360, listOf(
                Strike(0, 783.99, .041, 24.0), Strike(45, 1174.66, .052, 18.0),
            ), 35, .085),
            ConfirmationSoundStyle.LuminousSuccess to chime(420, listOf(
                Strike(0, 523.25, .050, 18.0), Strike(60, 783.99, .058, 14.0),
            ), 55, .12),
            ConfirmationSoundStyle.SingleGlassChord to chime(340, listOf(
                Strike(0, 659.25, .042, 16.0), Strike(0, 987.77, .023, 16.0),
                Strike(0, 1318.51, .009, 16.0),
            ), 48, .11),
        )
    }

    fun play(context: Context, sound: CommandSound) {
        if (!SoundSettings.enabled.value) return
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        initialize(context.applicationContext)
        val renderedId = when {
            sound == CommandSound.Open -> pulseLaunchId
            sound == CommandSound.Confirm && SoundSettings.confirmationStyle.value == ConfirmationSoundStyle.Pulse -> pulseConfirmId
            else -> 0
        }
        if (renderedId != 0 && synchronized(loadedSounds) { renderedId in loadedSounds }) {
            // Pulse assets are stored at 8x their original amplitude. A cubic
            // control curve keeps 100% at the original loudness while letting
            // compact/quiet speakers reach +18 dB at 200%.
            val normalized = (SoundSettings.volume.value / 2f).coerceIn(0f, 1f)
            val level = normalized * normalized * normalized
            soundPool?.play(renderedId, level, level, 1, 0, 1f)
            return
        }
        val source = if (sound == CommandSound.Confirm) confirmations.getValue(SoundSettings.confirmationStyle.value)
            else samples.getValue(sound)
        val level = SoundSettings.volume.value.let { it * it * it }
        val pcm = if (kotlin.math.abs(level - 1f) < .001f) source else ShortArray(source.size) { index ->
            (source[index] * level).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        runCatching {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.size * 2)
                .build()
            track.write(pcm, 0, pcm.size)
            track.play()
            mainHandler.postDelayed({ runCatching { track.stop(); track.release() } }, pcm.size * 1_000L / SAMPLE_RATE + 60L)
        }
    }

    private fun tone(durationMs: Int, startHz: Double, endHz: Double, volume: Double, harmonic: Double = 0.0): ShortArray {
        val count = SAMPLE_RATE * durationMs / 1_000
        var phase = 0.0
        return ShortArray(count) { index ->
            val progress = index.toDouble() / count.coerceAtLeast(1)
            val frequency = startHz + (endHz - startHz) * progress
            phase += 2.0 * PI * frequency / SAMPLE_RATE
            val attack = (progress / .12).coerceAtMost(1.0)
            val release = ((1.0 - progress) / .72).coerceIn(0.0, 1.0)
            val envelope = attack * release * release
            val wave = sin(phase) + if (harmonic > 0.0) sin(phase * harmonic) * .24 else 0.0
            (wave * envelope * volume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private data class Strike(val atMs: Int, val frequency: Double, val volume: Double, val decay: Double)

    private fun chime(durationMs: Int, strikes: List<Strike>, echoDelayMs: Int, echoLevel: Double): ShortArray {
        val count = SAMPLE_RATE * durationMs / 1_000
        val dry = DoubleArray(count)
        for (index in 0 until count) {
            val time = index.toDouble() / SAMPLE_RATE
            var sample = 0.0
            strikes.forEach { strike ->
                val local = time - strike.atMs / 1_000.0
                if (local >= 0.0) {
                    val attack = (local / .0045).coerceAtMost(1.0)
                    val envelope = attack * kotlin.math.exp(-strike.decay * local)
                    val phase = 2.0 * PI * strike.frequency * local
                    val glass = sin(phase) + .30 * sin(phase * 2.01) + .10 * sin(phase * 3.90)
                    sample += glass * envelope * strike.volume
                }
            }
            dry[index] = sample
        }
        val delaySamples = SAMPLE_RATE * echoDelayMs / 1_000
        return ShortArray(count) { index ->
            val echoed = dry[index] + if (index >= delaySamples) dry[index - delaySamples] * echoLevel else 0.0
            (echoed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }
}
