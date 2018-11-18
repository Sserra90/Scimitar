package com.creations.runtime.anim

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import com.creations.runtime.toPx

abstract class Animation(
        val duration: Long = 320,
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
) {
    abstract fun prepareView(target: View?)
    abstract fun run(target: View?)
}

fun fadeIn(): Animation = FadeAnimation(true)
fun fadeOut(): Animation = FadeAnimation(false)
fun slideUp(): Animation = SlideAnimation(true)
fun slideDown(): Animation = SlideAnimation(false)

private class FadeAnimation(private val fadeIn: Boolean = true) : Animation() {

    override fun prepareView(target: View?) {
        target?.apply {
            alpha = if (fadeIn) 0F else 1F
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?) {
        target?.apply {
            prepareView(this)
            ViewCompat
                    .animate(this)
                    .alpha(if (fadeIn) 1F else 0F)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
        }
    }
}

private class SlideAnimation(
        private val slideUp: Boolean = true,
        private val translateY: Float = 100.toPx.toFloat()
) : Animation() {

    override fun prepareView(target: View?) {
        target?.apply {
            y += if (slideUp) -translateY else translateY
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?) {
        target?.apply {
            prepareView(this)
            ViewCompat
                    .animate(this)
                    .yBy(if (slideUp) translateY else -translateY)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
        }
    }
}

abstract class SimpleListener : ViewPropertyAnimatorListener {
    override fun onAnimationEnd(view: View?) {}

    override fun onAnimationCancel(view: View?) {}

    override fun onAnimationStart(view: View?) {}
}