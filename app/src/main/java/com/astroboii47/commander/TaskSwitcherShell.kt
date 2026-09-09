package com.astroboii47.commander

object TaskSwitcherShell {
    data class SplitTarget(val taskId: Int, val packageName: String)
    private const val CACHE_MS = 3_000L
    @Volatile private var cachedAt = 0L
    @Volatile private var cached = emptyList<SwitcherTask>()

    fun load(context: android.content.Context, client: TaskSwitcherClient? = null, forceRefresh: Boolean = false): List<SwitcherTask> {
        val now = android.os.SystemClock.elapsedRealtime()
        cached.takeIf { !forceRefresh && it.isNotEmpty() && now - cachedAt <= CACHE_MS }?.let { return it }
        val direct = client?.recentTasks(32).orEmpty()
        val loaded = if (direct.isNotEmpty()) direct else loadFromShell(context)
        if (loaded.isNotEmpty()) {
            cached = loaded
            cachedAt = now
        }
        return loaded
    }

    fun underlyingSplitTarget(context: android.content.Context): Result<SplitTarget> = runCatching {
        val output = ShizukuBridge.capture(arrayOf("dumpsys", "activity", "recents")).getOrThrow()
        val blocks = output.split(Regex("(?=\\* Recent #\\d+:)"))
        val commanderIndex = blocks.indexOfFirst { "mActivityComponent=${context.packageName}/" in it }
        val target = if (commanderIndex >= 0) {
            blocks.drop(commanderIndex + 1).firstOrNull { "type=standard" in it }
        } else {
            blocks.firstOrNull { block ->
                "type=standard" in block && "mActivityComponent=${context.packageName}/" !in block
            }
        } ?: error("No app is available to split")
        val taskId = Regex("""taskId=(\d+)""").find(target)?.groupValues?.get(1)?.toIntOrNull()
            ?: error("Could not identify the active app")
        val component = Regex("""mActivityComponent=([^\s}]+)""").find(target)?.groupValues?.get(1)
            ?: error("Could not identify the active app")
        SplitTarget(taskId, component.substringBefore('/'))
    }

    private fun loadFromShell(context: android.content.Context): List<SwitcherTask> {
        val output = ShizukuBridge.capture(arrayOf("dumpsys", "activity", "recents")).getOrNull() ?: return emptyList()
        val pm = context.packageManager
        val blocks = output.split(Regex("(?=\\* Recent #\\d+:)"))
        val commanderIndex = blocks.indexOfFirst { "mActivityComponent=${context.packageName}/" in it }
        val underlyingBlock = blocks.getOrNull(commanderIndex + 1).orEmpty()
        val invokedOverApp = commanderIndex >= 0 && "type=standard" in underlyingBlock
        val underlyingTaskId = if (invokedOverApp) {
            Regex("""taskId=(\d+)""").find(underlyingBlock)?.groupValues?.get(1)?.toIntOrNull()
        } else null
        return blocks.mapNotNull { block ->
                val ids = Regex("""taskId=(\d+)\s+rootTaskId=(\d+)""").find(block) ?: return@mapNotNull null
                val component = Regex("""mActivityComponent=([^\s}]+)""").find(block)?.groupValues?.get(1)
                    ?: return@mapNotNull null
                val packageName = component.substringBefore('/')
                val taskId = ids.groupValues[1].toInt()
                val flags = Regex("""intent=\{[^\n]*\bflg=0x([0-9a-fA-F]+)""").find(block)
                    ?.groupValues?.get(1)?.toLongOrNull(16) ?: 0L
                val excludedFromRecents = flags and 0x00800000L != 0L
                if ("type=standard" !in block || excludedFromRecents || taskId == underlyingTaskId ||
                    packageName in setOf(context.packageName, "com.android.launcher3", "com.ss.launcher2")) {
                    return@mapNotNull null
                }
                val label = runCatching { pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString() }
                    .getOrDefault(packageName)
                SwitcherTask(
                    taskId,
                    ids.groupValues[2].toInt(),
                    packageName,
                    component,
                    label,
                    hasLiveActivities = "Activities=[]" !in block,
                )
            }
            .distinctBy { it.taskId }
    }
}
