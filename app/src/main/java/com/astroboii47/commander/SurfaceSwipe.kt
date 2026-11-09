package com.astroboii47.commander

import android.app.Activity
import android.view.MotionEvent
import kotlin.math.abs

/** Adds a restrained, interactive edge-to-home gesture to Commander surfaces. */
internal class SurfaceSwipe(
    private val activity: Activity,
    private val returnDirection: Int,
) {
    private var downX = 0f
    private var downY = 0f

    fun observe(event: MotionEvent) {
        val view = activity.window.decorView
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                view.animate().cancel()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downX
                val dy = event.rawY - downY
                if (dx * returnDirection > 0f && abs(dx) > abs(dy) * 1.15f) {
                    val progress = (abs(dx) / view.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                    view.translationX = dx * .16f
                    view.scaleX = 1f - progress * .012f
                    view.scaleY = 1f - progress * .012f
                    view.alpha = 1f - progress * .28f
                }
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.rawX - downX
                val dy = event.rawY - downY
                if (dx * returnDirection > 0f && abs(dx) > view.width * .13f && abs(dx) > abs(dy) * 1.15f) {
                    view.translationX = 0f
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.alpha = 1f
                    activity.finish()
                    @Suppress("DEPRECATION")
                    activity.overridePendingTransition(
                        if (returnDirection < 0) R.anim.home_from_right else R.anim.home_from_left,
                        if (returnDirection < 0) R.anim.surface_to_left else R.anim.surface_to_right,
                    )
                } else settle(view)
            }
            MotionEvent.ACTION_CANCEL -> settle(view)
        }
    }

    private fun settle(view: android.view.View) {
        view.animate().translationX(0f).scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(220L).setInterpolator(android.view.animation.DecelerateInterpolator(1.7f)).start()
    }
}
