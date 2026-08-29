package com.astroboii47.commander

import rikka.shizuku.Shizuku
import android.util.Log

object ShizukuBridge {
    private const val REQUEST_CODE = 7401

    fun run(command: Array<String>): String? = runCatching {
        if (!Shizuku.pingBinder()) return "Start Shizuku to use this Titan control"
        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (Shizuku.shouldShowRequestPermissionRationale()) return "Allow Commander in Shizuku"
            Shizuku.requestPermission(REQUEST_CODE)
            return "Allow Shizuku, then trigger the control again"
        }
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java,
        ).apply { isAccessible = true }
        val process = method.invoke(null, command, null, null)
        val processClass = process.javaClass
        val errorStream = processClass.getMethod("getErrorStream").invoke(process) as java.io.InputStream
        val error = errorStream.bufferedReader().use { it.readText().trim() }
        val exit = processClass.getMethod("waitFor").invoke(process) as Int
        processClass.getMethod("destroy").invoke(process)
        if (exit == 0) null else error.ifBlank { "Titan control failed" }
    }.getOrElse {
        Log.e("CommanderShizuku", "Titan control failed", it)
        "Shizuku control unavailable"
    }

    fun capture(command: Array<String>): Result<String> = runCatching {
        check(Shizuku.pingBinder()) { "Start Shizuku" }
        check(Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) { "Allow Commander in Shizuku" }
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java,
        ).apply { isAccessible = true }
        val process = method.invoke(null, command, null, null)
        val processClass = process.javaClass
        val output = (processClass.getMethod("getInputStream").invoke(process) as java.io.InputStream)
            .bufferedReader().use { it.readText() }
        val error = (processClass.getMethod("getErrorStream").invoke(process) as java.io.InputStream)
            .bufferedReader().use { it.readText().trim() }
        val exit = processClass.getMethod("waitFor").invoke(process) as Int
        processClass.getMethod("destroy").invoke(process)
        check(exit == 0) { error.ifBlank { "Command failed" } }
        output
    }
}
