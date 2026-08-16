package com.astroboii47.commander

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppPackageChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCatalog.refreshAsync(context)
    }
}
