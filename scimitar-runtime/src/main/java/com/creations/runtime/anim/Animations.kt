package com.creations.runtime.anim

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import com.creations.runtime.toPx

fun screenSize(context: Context): Pair<Int, Int> {
    val displayMetrics = DisplayMetrics()
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    wm.defaultDisplay.getMetrics(displayMetrics)
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

fun fadeIn(): Animation = FadeAnimation(true)
fun fadeOut(): Animation = FadeAnimation(false)
fun slideUp(enter: Boolean, translateY: Float = 150.toPx.toFloat()): Animation =
        SlideAnimation(enter, true, translateY)

fun slideDown(enter: Boolean, translateY: Float = 150.toPx.toFloat()): Animation =
        SlideAnimation(enter, false, translateY)

fun slideUpFadeIn(translateY: Float = 150.toPx.toFloat()): Animation =
        SlideFadeAnimation(true, true, translateY)

fun slideUpFadeOut(translateY: Float = 150.toPx.toFloat()): Animation =
        SlideFadeAnimation(true, false, translateY)

fun slideDownFadeIn(translateY: Float = 150.toPx.toFloat()): Animation =
        SlideFadeAnimation(false, true, translateY)

fun slideDownFadeOut(translateY: Float = 150.toPx.toFloat()): Animation =
        SlideFadeAnimation(false, false, translateY)

abstract class Animation(
        val duration: Long = 320,
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
) {
    abstract fun prepareView(target: View?)
    abstract fun run(target: View?, runAfter: () -> Unit = {})
}

private class FadeAnimation(private val fadeIn: Boolean = true) : Animation() {

    override fun prepareView(target: View?) {
        target?.apply {
            alpha = if (fadeIn) 0F else 1F
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?, runAfter: () -> Unit) {
        target?.apply {
            prepareView(this)
            ViewCompat
                    .animate(this)
                    .alpha(if (fadeIn) 1F else 0F)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .setListener(object : SimpleListener() {
                        override fun onAnimationEnd(view: View?) {
                            view?.visibility = if (fadeIn) View.VISIBLE else View.GONE
                            runAfter()
                        }
                    })
        }
    }
}

private class SlideAnimation(
        private val enter: Boolean,
        private val slideUp: Boolean = true,
        private val translateY: Float
) : Animation() {

    override fun prepareView(target: View?) {
        target?.apply {
            //y += if (slideUp) -translateY else translateY
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?, runAfter: () -> Unit) {
        target?.apply {
            prepareView(this)
            ViewCompat
                    .animate(this)
                    .yBy(if (slideUp) -translateY else translateY)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .setListener(object : SimpleListener() {
                        override fun onAnimationEnd(view: View?) {
                            view?.visibility = if (enter) View.VISIBLE else View.GONE
                            runAfter()
                        }
                    })
        }
    }
}

private class SlideFadeAnimation(
        private val slideUp: Boolean = true,
        private val fadeIn: Boolean = true,
        private val translateY: Float
) : Animation() {

    override fun prepareView(target: View?) {
        target?.apply {
            if (fadeIn) {
                y += if (slideUp) translateY else -translateY
            }
            alpha = if (fadeIn) 0F else 1F
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?, runAfter: () -> Unit) {
        target?.apply {
            prepareView(this)
            ViewCompat
                    .animate(this)
                    .alpha(if (fadeIn) 1F else 0F)
                    .yBy(if (slideUp) -translateY else translateY)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .setListener(object : SimpleListener() {
                        override fun onAnimationEnd(view: View?) {
                            view?.visibility = if (fadeIn) View.VISIBLE else View.GONE
                            runAfter()
                        }
                    })
        }
    }
}

abstract class SimpleListener : ViewPropertyAnimatorListener {
    override fun onAnimationEnd(view: View?) {}

    override fun onAnimationCancel(view: View?) {}

    override fun onAnimationStart(view: View?) {}
}