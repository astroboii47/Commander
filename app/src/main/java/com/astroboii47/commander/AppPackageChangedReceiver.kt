package com.astroboii47.commander

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppPackageChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED,
                Intent.ACTION_PACKAGE_CHANGED,
            ) || intent.data?.scheme != "package"
        ) return
        AppCatalog.refreshAsync(context)
    }
}
