package com.astroboii47.commander

import android.graphics.Bitmap

data class SwitcherTask(
    val taskId: Int,
    val rootTaskId: Int,
    val packageName: String,
    val component: String,
    val label: String,
    val hasLiveActivities: Boolean = true,
    val preview: Bitmap? = null,
)
