package com.astroboii47.commander

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.hardware.HardwareBuffer

class TaskSwitcherUserService(@Suppress("UNUSED_PARAMETER") context: Context) : ITaskSwitcherService.Stub() {
    constructor() : this(throw UnsupportedOperationException("Context constructor required"))

    override fun getTaskSnapshot(taskId: Int, width: Int, height: Int, captureNow: Boolean): Bitmap? = runCatching {
        val service = activityTaskManager()
        val get = service.javaClass.methods.first { it.name == "getTaskSnapshot" && it.parameterTypes.size == 2 }
        val fresh = if (captureNow) service.javaClass.methods.firstOrNull {
            it.name == "takeTaskSnapshot" && it.parameterTypes.size in 1..2
        }?.let { if (it.parameterTypes.size == 2) it.invoke(service, taskId, true) else it.invoke(service, taskId) } else null
        decode(fresh, width, height) ?: decode(get.invoke(service, taskId, false), width, height)
            ?: decode(get.invoke(service, taskId, true), width, height)
    }.getOrNull()

    override fun switchToTask(taskId: Int, rootTaskId: Int): Boolean = runCatching {
        val service = activityTaskManager()
        val candidates = listOf(rootTaskId, taskId).distinct().filter { it > 0 }
        val move = service.javaClass.methods.firstOrNull { it.name == "moveTaskToFront" && it.parameterTypes.size == 5 }
        if (move != null && candidates.any { id ->
                runCatching { move.invoke(service, null, "com.android.shell", id, 0, null); true }.getOrDefault(false)
            }) return@runCatching true
        val start = service.javaClass.methods.firstOrNull { it.name == "startActivityFromRecents" && it.parameterTypes.size == 2 }
            ?: return@runCatching false
        candidates.any { id ->
            runCatching { (start.invoke(service, id, null) as Int) >= 0 }.getOrDefault(false)
        }
    }.getOrDefault(false)

    override fun removeTask(taskId: Int): Boolean = runCatching {
        val service = activityTaskManager()
        val remove = service.javaClass.methods.firstOrNull {
            it.name == "removeTask" && it.parameterTypes.size == 1
        } ?: return@runCatching false
        remove.invoke(service, taskId) as? Boolean ?: true
    }.getOrDefault(false)

    private fun activityTaskManager(): Any = Class.forName("android.app.ActivityTaskManager")
        .getDeclaredMethod("getService").invoke(null)

    private fun decode(snapshot: Any?, width: Int, height: Int): Bitmap? {
        snapshot ?: return null
        val buffer = snapshot.javaClass.getMethod("getHardwareBuffer").invoke(snapshot) as HardwareBuffer
        val color = snapshot.javaClass.getMethod("getColorSpace").invoke(snapshot) as? ColorSpace
        val hardware = Bitmap.wrapHardwareBuffer(buffer, color) ?: run { buffer.close(); return null }
        val software = hardware.copy(Bitmap.Config.ARGB_8888, false)
        val scale = minOf(width.toFloat() / software.width, height.toFloat() / software.height).coerceAtMost(1f)
        val scaled = if (scale < 1f) Bitmap.createScaledBitmap(software, (software.width * scale).toInt(), (software.height * scale).toInt(), true) else software
        if (scaled !== software) software.recycle()
        hardware.recycle()
        buffer.close()
        return scaled
    }
}
