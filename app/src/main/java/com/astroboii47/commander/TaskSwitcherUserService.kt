package com.astroboii47.commander

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.hardware.HardwareBuffer
import android.content.Intent
import android.content.ComponentName
import android.util.Log

class TaskSwitcherUserService(@Suppress("UNUSED_PARAMETER") context: Context) : ITaskSwitcherService.Stub() {
    constructor() : this(throw UnsupportedOperationException("Context constructor required"))

    override fun getRecentTasks(maxTasks: Int): Array<String> = runCatching {
        val service = activityTaskManager()
        val method = service.javaClass.methods.first {
            it.name == "getRecentTasks" && it.parameterTypes.size == 3
        }
        val slice = method.invoke(service, maxTasks.coerceIn(1, 64), 0, 0)
        val tasks = slice.javaClass.getMethod("getList").invoke(slice) as? List<*> ?: emptyList<Any>()
        tasks.mapNotNull { task ->
            task ?: return@mapNotNull null
            val taskId = intField(task, "taskId", "id", "persistentId") ?: return@mapNotNull null
            val rootTaskId = intField(task, "rootTaskId") ?: taskId
            val baseIntent = field(task, "baseIntent") as? Intent
            val component = (field(task, "topActivity") as? ComponentName)
                ?: (field(task, "baseActivity") as? ComponentName)
                ?: baseIntent?.component
                ?: return@mapNotNull null
            val flags = baseIntent?.flags ?: 0
            val hasLiveActivities = field(task, "topActivity") != null
            listOf(
                taskId.toString(),
                rootTaskId.toString(),
                component.packageName,
                component.flattenToShortString(),
                flags.toString(),
                if (hasLiveActivities) "1" else "0",
            ).joinToString("\u001f")
        }.toTypedArray()
    }.getOrDefault(emptyArray())

    override fun getTaskSnapshot(taskId: Int, width: Int, height: Int, captureNow: Boolean): Bitmap? = runCatching {
        val service = activityTaskManager()
        val get = service.javaClass.methods.firstOrNull {
            it.name == "getTaskSnapshot" && it.parameterTypes.size in 2..3
        } ?: return@runCatching null
        val fresh = if (captureNow) service.javaClass.methods.firstOrNull {
            it.name == "takeTaskSnapshot" && it.parameterTypes.size in 1..2
        }?.let { if (it.parameterTypes.size == 2) it.invoke(service, taskId, true) else it.invoke(service, taskId) } else null
        fun read(lowResolution: Boolean): Any? = when (get.parameterTypes.size) {
            2 -> get.invoke(service, taskId, lowResolution)
            3 -> get.invoke(service, taskId, lowResolution, true)
            else -> null
        }
        decode(fresh, width, height) ?: decode(read(false), width, height)
            ?: decode(read(true), width, height)
    }.onFailure { Log.e(TAG, "Snapshot failed for task $taskId", it) }.getOrNull()

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

    private fun field(instance: Any, name: String): Any? {
        var type: Class<*>? = instance.javaClass
        while (type != null) {
            runCatching {
                return type.getDeclaredField(name).apply { isAccessible = true }.get(instance)
            }
            type = type.superclass
        }
        return null
    }

    private fun intField(instance: Any, vararg names: String): Int? = names.firstNotNullOfOrNull { name ->
        (field(instance, name) as? Number)?.toInt()?.takeIf { it > 0 }
    }

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

    private companion object {
        const val TAG = "CommanderSwitcher"
    }
}
