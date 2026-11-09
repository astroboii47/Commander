package com.astroboii47.commander

import android.content.Context
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

data class CommanderWeatherLocation(val name: String, val latitude: Double, val longitude: Double)

object WeatherPreferences {
    private const val Prefs = "commander_weather"
    fun location(context: Context): CommanderWeatherLocation {
        val prefs = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
        return CommanderWeatherLocation(
            prefs.getString("name", "Sydney").orEmpty(),
            java.lang.Double.longBitsToDouble(prefs.getLong("latitude", java.lang.Double.doubleToLongBits(-33.8688))),
            java.lang.Double.longBitsToDouble(prefs.getLong("longitude", java.lang.Double.doubleToLongBits(151.2093))),
        )
    }

    fun resolveAndSave(context: Context, query: String): CommanderWeatherLocation? = runCatching {
        val encoded = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
        val payload = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=en&format=json")
            .openConnection().apply { connectTimeout = 5_000; readTimeout = 5_000 }
            .getInputStream().bufferedReader().use { it.readText() }
        val result = JSONObject(payload).optJSONArray("results")?.optJSONObject(0) ?: return@runCatching null
        val displayName = listOf(result.optString("name"), result.optString("admin1"), result.optString("country"))
            .filter(String::isNotBlank).distinct().joinToString(", ")
        CommanderWeatherLocation(displayName, result.getDouble("latitude"), result.getDouble("longitude")).also { location ->
            context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit()
                .putString("name", location.name)
                .putLong("latitude", java.lang.Double.doubleToRawLongBits(location.latitude))
                .putLong("longitude", java.lang.Double.doubleToRawLongBits(location.longitude)).apply()
            context.getSharedPreferences("signal_rain_forecast", Context.MODE_PRIVATE).edit().clear().apply()
        }
    }.getOrNull()
}
