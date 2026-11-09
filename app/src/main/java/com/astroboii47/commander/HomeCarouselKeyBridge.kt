package com.astroboii47.commander

import androidx.compose.runtime.mutableIntStateOf

object HomeCarouselKeyBridge {
    val sequence = mutableIntStateOf(0)
    val direction = mutableIntStateOf(0)
    val actionSequence = mutableIntStateOf(0)
    val stackSequence = mutableIntStateOf(0)
    val stackDirection = mutableIntStateOf(0)
    val spaceSequence = mutableIntStateOf(0)
    private var lastHomeSignalAt = 0L

    fun move(step: Int) {
        direction.intValue = if (step < 0) -1 else 1
        sequence.intValue++
    }

    fun moveFromHomeButton() {
        val now = android.os.SystemClock.elapsedRealtime()
        // Android can report one Home press through both the launcher intent
        // and CLOSE_SYSTEM_DIALOGS. Only collapse that near-simultaneous pair;
        // a longer window made legitimate quick presses feel intermittent.
        if (now - lastHomeSignalAt < 100L) return
        lastHomeSignalAt = now
        move(1)
    }


    fun activate() {
        actionSequence.intValue++
    }

    fun moveStack(step: Int) {
        stackDirection.intValue = if (step < 0) -1 else 1
        stackSequence.intValue++
    }

    fun space() {
        spaceSequence.intValue++
    }
}
