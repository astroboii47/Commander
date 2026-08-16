package com.astroboii47.commander

import java.util.Locale
import kotlin.math.abs

object UnitConverter {
    private data class UnitDef(val aliases: Set<String>, val group: String, val toBase: (Double) -> Double, val fromBase: (Double) -> Double, val symbol: String)
    private fun linear(group: String, symbol: String, factor: Double, vararg aliases: String) =
        UnitDef((aliases.toSet() + symbol.lowercase()), group, { it * factor }, { it / factor }, symbol)

    private val units = listOf(
        linear("length", "mm", .001, "millimeter", "millimeters", "millimetre", "millimetres"),
        linear("length", "cm", .01, "centimeter", "centimeters", "centimetre", "centimetres"),
        linear("length", "m", 1.0, "meter", "meters", "metre", "metres"),
        linear("length", "km", 1000.0, "kilometer", "kilometers", "kilometre", "kilometres"),
        linear("length", "in", .0254, "inch", "inches"),
        linear("length", "ft", .3048, "foot", "feet"),
        linear("length", "yd", .9144, "yard", "yards"),
        linear("length", "mi", 1609.344, "mile", "miles"),
        linear("mass", "mg", .001, "milligram", "milligrams"), linear("mass", "g", 1.0, "gram", "grams"),
        linear("mass", "kg", 1000.0, "kilogram", "kilograms", "kilo", "kilos"),
        linear("mass", "oz", 28.349523125, "ounce", "ounces"), linear("mass", "lb", 453.59237, "lbs", "pound", "pounds"),
        linear("volume", "ml", .001, "milliliter", "milliliters", "millilitre", "millilitres"),
        linear("volume", "L", 1.0, "l", "liter", "liters", "litre", "litres"),
        linear("volume", "cup", .2365882365, "cups"), linear("volume", "gal", 3.785411784, "gallon", "gallons"),
        linear("speed", "m/s", 1.0, "mps", "meter per second", "meters per second"),
        linear("speed", "km/h", 1.0 / 3.6, "kph", "kmh", "kilometers per hour", "kilometres per hour"),
        linear("speed", "mph", .44704, "miles per hour"), linear("speed", "kn", .514444, "knot", "knots"),
        linear("time", "ms", .001, "millisecond", "milliseconds"), linear("time", "s", 1.0, "sec", "secs", "second", "seconds"),
        linear("time", "min", 60.0, "mins", "minute", "minutes"), linear("time", "h", 3600.0, "hr", "hrs", "hour", "hours"),
        linear("time", "day", 86400.0, "days"), linear("time", "week", 604800.0, "weeks"),
        linear("data", "B", 1.0, "byte", "bytes"), linear("data", "KB", 1000.0, "kb", "kilobyte", "kilobytes"),
        linear("data", "MB", 1_000_000.0, "mb", "megabyte", "megabytes"), linear("data", "GB", 1_000_000_000.0, "gb", "gigabyte", "gigabytes"),
        linear("data", "TB", 1_000_000_000_000.0, "tb", "terabyte", "terabytes"),
        linear("area", "m²", 1.0, "m2", "sqm", "square meter", "square meters", "square metre", "square metres"),
        linear("area", "km²", 1_000_000.0, "km2", "sqkm", "square kilometer", "square kilometers"),
        linear("area", "ft²", .09290304, "ft2", "sqft", "square foot", "square feet"),
        linear("area", "acre", 4046.8564224, "acres"), linear("area", "ha", 10000.0, "hectare", "hectares"),
        linear("energy", "J", 1.0, "j", "joule", "joules"), linear("energy", "kJ", 1000.0, "kj", "kilojoule", "kilojoules"),
        linear("energy", "cal", 4.184, "calorie", "calories"), linear("energy", "kcal", 4184.0, "kilocalorie", "kilocalories"),
        linear("power", "W", 1.0, "w", "watt", "watts"), linear("power", "kW", 1000.0, "kw", "kilowatt", "kilowatts"),
        linear("pressure", "Pa", 1.0, "pa", "pascal", "pascals"), linear("pressure", "kPa", 1000.0, "kpa", "kilopascal", "kilopascals"),
        linear("pressure", "bar", 100000.0, "bars"), linear("pressure", "psi", 6894.757293, "pounds per square inch"),
        linear("angle", "°", 1.0, "deg", "degree", "degrees"), linear("angle", "rad", 180.0 / Math.PI, "radian", "radians"),
        UnitDef(setOf("c", "°c", "celsius", "centigrade"), "temperature", { it }, { it }, "°C"),
        UnitDef(setOf("f", "°f", "fahrenheit"), "temperature", { (it - 32) * 5 / 9 }, { it * 9 / 5 + 32 }, "°F"),
        UnitDef(setOf("k", "kelvin"), "temperature", { it - 273.15 }, { it + 273.15 }, "K"),
    )

    fun convert(input: String): String? {
        val match = Regex("^\\s*(-?\\d+(?:[.,]\\d+)?)\\s*(.+?)\\s+(?:in|to|into|as)\\s+(.+?)\\s*$", RegexOption.IGNORE_CASE).matchEntire(input) ?: return null
        val amount = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        val from = find(match.groupValues[2]) ?: return null
        val to = find(match.groupValues[3]) ?: return null
        if (from.group != to.group) return null
        val result = to.fromBase(from.toBase(amount))
        if (!result.isFinite()) return null
        return "${format(result)} ${to.symbol}"
    }

    private fun find(raw: String): UnitDef? {
        val key = raw.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
        return units.firstOrNull { key in it.aliases }
    }

    private fun format(value: Double): String = when {
        abs(value) < 1e-12 -> "0"
        abs(value - value.toLong()) < 1e-10 -> value.toLong().toString()
        else -> "%.8f".format(Locale.US, value).trimEnd('0').trimEnd('.')
    }
}
