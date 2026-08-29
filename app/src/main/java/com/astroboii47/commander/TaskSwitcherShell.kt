package com.astroboii47.commander

object TaskSwitcherShell {
    fun load(context: android.content.Context): List<SwitcherTask> {
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
