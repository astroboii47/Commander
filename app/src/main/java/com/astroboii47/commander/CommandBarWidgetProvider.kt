package com.astroboii47.commander

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** A direct, single-tap Bar entry point for launchers that support widgets. */
class CommandBarWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_LAUNCH_BAR) {
            // A widget's direct activity PendingIntent makes some launchers
            // scale our full-screen translucent dim layer out of the tile.
            // Launching from the click broadcast avoids that source animation.
            context.startActivity(
                Intent(OverlayActivity.ACTION_OPEN_OVERLAY)
                    .setClass(context, OverlayActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION),
            )
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val intent = Intent(ACTION_LAUNCH_BAR)
                .setClass(context, CommandBarWidgetProvider::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val views = RemoteViews(context.packageName, R.layout.widget_command_bar)
            views.setOnClickPendingIntent(R.id.command_bar_widget, pendingIntent)
            manager.updateAppWidget(id, views)
        }
    }

    private companion object {
        const val ACTION_LAUNCH_BAR = "com.astroboii47.commander.widget.OPEN_BAR"
    }
}
