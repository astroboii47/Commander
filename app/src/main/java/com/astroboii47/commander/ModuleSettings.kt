package com.astroboii47.commander

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.app.role.RoleManager
import androidx.compose.runtime.mutableStateOf

object ModuleSettings {
    private const val PREFS = "commander_modules"
    val homeEnabled = mutableStateOf(false)
    val hubEnabled = mutableStateOf(true)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        homeEnabled.value = homeAvailable(context) && prefs.getBoolean("home", false)
        hubEnabled.value = prefs.getBoolean("hub", true)
    }

    fun isCurrentHome(context: Context): Boolean {
        // Package visibility can make resolveActivity report our own HOME
        // candidate even while Nothing Launcher holds the actual Home role.
        return context.getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_HOME) == true
    }

    fun setHome(context: Context, enabled: Boolean): Boolean {
        if (enabled && !homeAvailable(context)) return false
        if (!enabled && isCurrentHome(context)) return false
        homeEnabled.value = enabled
        if (enabled) HomeSettings.initialize(context)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("home", enabled).apply()
        syncComponents(context)
        return true
    }

    fun setHub(context: Context, enabled: Boolean) {
        hubEnabled.value = enabled
        if (enabled) {
            HubSettings.initialize(context)
            HubIgnoreSettings.initialize(context)
            HubImportantSettings.initialize(context)
            HubHistoryStore.initialize(context)
            HubRepository.requestRefresh()
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("hub", enabled).apply()
        syncComponents(context)
    }

    fun syncComponents(context: Context) {
        val flags = PackageManager.DONT_KILL_APP
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, "${context.packageName}.CommanderHomeLauncherAlias"),
            if (homeEnabled.value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            flags,
        )
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, "${context.packageName}.CommanderHubLauncherAlias"),
            if (hubEnabled.value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            flags,
        )
        listOf(
            "CommanderHomeShortcutActivity" to homeEnabled.value,
            "CommanderHubShortcutActivity" to hubEnabled.value,
        ).forEach { (name, enabled) ->
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, "${context.packageName}.$name"),
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                flags,
            )
        }
        MinimalCommandApplication.refreshShortcutsIfChanged(context)
    }

    fun homeAvailable(context: Context): Boolean =
        context.resources.getBoolean(R.bool.commander_home_launcher_enabled)
}
