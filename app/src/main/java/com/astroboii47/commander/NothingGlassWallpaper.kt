package com.astroboii47.commander

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** MIT-derived rendering model inspired by Vauth/nothing-glass. */
object NothingGlassWallpaper {
    fun render(
        source: Bitmap,
        blurRadius: Float,
        reedWidth: Float,
        distortion: Float,
        lighting: Float,
    ): Bitmap {
        val width = source.width
        val height = source.height
        val sourcePixels = IntArray(width * height)
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
        val blurred = if (blurRadius >= .5f) {
            boxBlur(sourcePixels, width, height, blurRadius.roundToInt().coerceIn(1, 14))
        } else sourcePixels
        val output = IntArray(blurred.size)
        val scale = width / 1080f
        val period = (reedWidth * scale).coerceAtLeast(5f)
        val amplitude = distortion * scale
        val displacement = IntArray(width)
        val shade = IntArray(width)
        for (x in 0 until width) {
            val angle = 2.0 * PI * x / period
            displacement[x] = (amplitude * sin(angle)).roundToInt()
            shade[x] = (lighting * cos(angle)).roundToInt()
        }
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                val sampledX = (x + displacement[x]).coerceIn(0, width - 1)
                val color = blurred[row + sampledX]
                val light = shade[x]
                output[row + x] = Color.argb(
                    Color.alpha(color),
                    (Color.red(color) + light).coerceIn(0, 255),
                    (Color.green(color) + light).coerceIn(0, 255),
                    (Color.blue(color) + light).coerceIn(0, 255),
                )
            }
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun boxBlur(input: IntArray, width: Int, height: Int, radius: Int): IntArray {
        val horizontal = IntArray(input.size)
        val output = IntArray(input.size)
        val window = radius * 2 + 1
        for (y in 0 until height) {
            var red = 0; var green = 0; var blue = 0; var alpha = 0
            val row = y * width
            for (offset in -radius..radius) {
                val color = input[row + offset.coerceIn(0, width - 1)]
                alpha += Color.alpha(color); red += Color.red(color)
                green += Color.green(color); blue += Color.blue(color)
            }
            for (x in 0 until width) {
                horizontal[row + x] = Color.argb(alpha / window, red / window, green / window, blue / window)
                val leaving = input[row + (x - radius).coerceIn(0, width - 1)]
                val entering = input[row + (x + radius + 1).coerceIn(0, width - 1)]
                alpha += Color.alpha(entering) - Color.alpha(leaving)
                red += Color.red(entering) - Color.red(leaving)
                green += Color.green(entering) - Color.green(leaving)
                blue += Color.blue(entering) - Color.blue(leaving)
            }
        }
        for (x in 0 until width) {
            var red = 0; var green = 0; var blue = 0; var alpha = 0
            for (offset in -radius..radius) {
                val color = horizontal[offset.coerceIn(0, height - 1) * width + x]
                alpha += Color.alpha(color); red += Color.red(color)
                green += Color.green(color); blue += Color.blue(color)
            }
            for (y in 0 until height) {
                output[y * width + x] = Color.argb(alpha / window, red / window, green / window, blue / window)
                val leaving = horizontal[(y - radius).coerceIn(0, height - 1) * width + x]
                val entering = horizontal[(y + radius + 1).coerceIn(0, height - 1) * width + x]
                alpha += Color.alpha(entering) - Color.alpha(leaving)
                red += Color.red(entering) - Color.red(leaving)
                green += Color.green(entering) - Color.green(leaving)
                blue += Color.blue(entering) - Color.blue(leaving)
            }
        }
        return output
    }
}

