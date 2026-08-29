package com.astroboii47.commander

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import android.util.LruCache
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TaskSwitcherClient(context: Context, private val connected: () -> Unit) {
    private data class CachedPreview(val bitmap: Bitmap, val loadedAt: Long)

    private companion object {
        private const val PREVIEW_MAX_AGE_MS = 120_000L
        private val previews = object : LruCache<Int, CachedPreview>(8 * 1024 * 1024) {
            override fun sizeOf(key: Int, value: CachedPreview): Int = value.bitmap.allocationByteCount
        }
    }

    private val appContext = context.applicationContext
    @Volatile private var service: ITaskSwitcherService? = null
    @Volatile private var latch = CountDownLatch(1)
    private val args = Shizuku.UserServiceArgs(ComponentName(context, TaskSwitcherUserService::class.java))
        .daemon(false).processNameSuffix("switcher").debuggable(BuildConfig.DEBUG).version(3)
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = ITaskSwitcherService.Stub.asInterface(binder); latch.countDown(); connected()
        }
        override fun onServiceDisconnected(name: ComponentName?) { service = null; latch = CountDownLatch(1) }
    }
    fun bind() { runCatching { Shizuku.bindUserService(args, connection) } }
    fun preview(taskId: Int, width: Int, height: Int) = runCatching {
        synchronized(previews) {
            previews.get(taskId)?.takeIf {
                !it.bitmap.isRecycled && System.currentTimeMillis() - it.loadedAt <= PREVIEW_MAX_AGE_MS
            }?.bitmap
        }?.let { return@runCatching it }
        if (service == null) latch.await(1200, TimeUnit.MILLISECONDS)
        service?.getTaskSnapshot(taskId, width, height, false)?.also { bitmap ->
            synchronized(previews) { previews.put(taskId, CachedPreview(bitmap, System.currentTimeMillis())) }
        }
    }.getOrNull()
    fun switch(task: SwitcherTask): Boolean = runCatching {
        if (!task.hasLiveActivities) return@runCatching launchNormally(task)
        if (service == null) latch.await(1200, TimeUnit.MILLISECONDS)
        if (service?.switchToTask(task.taskId, task.rootTaskId) == true) return@runCatching true
        val shellError = ShizukuBridge.run(arrayOf(
            "am", "stack", "move-task", task.taskId.toString(), task.rootTaskId.toString(), "true",
        ))
        shellError == null || launchNormally(task)
    }.getOrDefault(false)
    fun remove(task: SwitcherTask): Boolean = runCatching {
        if (service == null) latch.await(1200, TimeUnit.MILLISECONDS)
        service?.removeTask(task.taskId) == true
    }.getOrDefault(false)
    private fun launchNormally(task: SwitcherTask): Boolean = runCatching {
        val intent = if (task.packageName == "com.android.settings") {
            android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
        } else appContext.packageManager.getLaunchIntentForPackage(task.packageName)
            ?: return@runCatching false
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        appContext.startActivity(intent)
        true
    }.getOrDefault(false)
    fun unbind() { runCatching { Shizuku.unbindUserService(args, connection, true) }; service = null }

}
