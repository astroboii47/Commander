package com.astroboii47.commander

import android.content.Context
import android.content.Intent

data class AssistantAppChoice(val packageName: String, val label: String)

/** The system keeps Commander as the sole default assistant. This optional
 * power-button route opens a selected app normally instead of pretending it
 * can start a second app's privileged voice session. */
object AssistantPowerRouting {
    private const val PREFS = "assistant_power_routing"
    private const val TARGET_PACKAGE = "target_package"

    fun selectedPackage(context: Context): String? = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(TARGET_PACKAGE, null)

    fun select(context: Context, packageName: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TARGET_PACKAGE, packageName)
            .apply()
    }

    fun choices(context: Context): List<AssistantAppChoice> {
        val pm = context.packageManager
        return pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .mapNotNull { info ->
                val packageName = info.activityInfo.packageName
                if (pm.getLaunchIntentForPackage(packageName) == null) null else AssistantAppChoice(
                    packageName,
                    info.loadLabel(pm).toString(),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Returns false if the selected app disappeared or could not be opened;
     * the caller then keeps the normal Commander Bar path available. */
    fun openSelected(context: Context): Boolean {
        val packageName = selectedPackage(context)?.takeIf { it != context.packageName } ?: return false
        val launch = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        return runCatching {
            context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            true
        }.getOrDefault(false)
    }
}
