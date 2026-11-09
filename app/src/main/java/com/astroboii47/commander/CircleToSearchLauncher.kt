package com.astroboii47.commander

import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass

/** Requests the active assistant's screen-search session without root or Shizuku. */
internal object CircleToSearchLauncher {
    fun trigger(): Boolean = try {
        val serviceManager = Class.forName("android.os.ServiceManager")
        val binder = HiddenApiBypass.invoke(serviceManager, null, "getService", "voiceinteraction") as? IBinder
            ?: return false
        val stub = Class.forName("com.android.internal.app.IVoiceInteractionManagerService\$Stub")
        val service = HiddenApiBypass.invoke(stub, null, "asInterface", binder) ?: return false
        val args = Bundle().apply {
            putLong("invocation_time_ms", SystemClock.elapsedRealtime())
            putInt("omni.entry_point", 1)
            putBoolean("micts_trigger", true)
        }
        val interfaceClass = Class.forName("com.android.internal.app.IVoiceInteractionManagerService")
        if (Build.VERSION.SDK_INT >= 34) {
            HiddenApiBypass.invoke(interfaceClass, service, "showSessionFromSession", null, args, 7, "hyperOS_home")
        } else {
            HiddenApiBypass.invoke(interfaceClass, service, "showSessionFromSession", null, args, 7)
        } as? Boolean ?: false
    } catch (error: Throwable) {
        Log.w("CircleToSearch", "Screen search request failed", error)
        false
    }
}
