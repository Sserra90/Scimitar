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
import com.creations.runtime.views.Animator

fun screenSize(context: Context): Pair<Int, Int> {
    val displayMetrics = DisplayMetrics()
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    wm.defaultDisplay.getMetrics(displayMetrics)
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

fun fadeIn(): Animator = { v, run -> FadeAnimation(true).run(v, run) }

fun fadeOut(): Animator = { v, run -> FadeAnimation(false).run(v, run) }

fun slideUp(enter: Boolean, translateY: Float = 150.toPx.toFloat()): Animator =
        { v, run -> SlideAnimation(enter, true, translateY).run(v, run) }

fun slideDown(enter: Boolean, translateY: Float = 150.toPx.toFloat()): Animator =
        { v, run -> SlideAnimation(enter, false, translateY).run(v, run) }

fun slideUpFadeIn(translateY: Float = 150.toPx.toFloat()): Animator =
        { v, run -> SlideFadeAnimation(true, true, translateY).run(v, run) }

fun slideUpFadeOut(translateY: Float = 150.toPx.toFloat()): Animator =
        { v, run -> SlideFadeAnimation(true, false, translateY).run(v, run) }

fun slideDownFadeIn(translateY: Float = 150.toPx.toFloat()): Animator =
        { v, run -> SlideFadeAnimation(false, true, translateY).run(v, run) }

fun slideDownFadeOut(translateY: Float = 150.toPx.toFloat()): Animator =
        { v, run -> SlideFadeAnimation(false, false, translateY).run(v, run) }

abstract class Animation(
        val duration: Long = 320,
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
) {
    abstract fun run(target: View?, runAfter: (() -> Unit)?)
}

private class FadeAnimation(private val fadeIn: Boolean = true) : Animation() {

    fun prepareView(target: View?) {
        target?.apply {
            alpha = if (fadeIn) 0F else 1F
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?, runAfter: (() -> Unit)?) {
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
                            runAfter?.invoke()
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

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?, runAfter: (() -> Unit)?) {
        target?.apply {
            visibility = View.VISIBLE
            ViewCompat
                    .animate(this)
                    .yBy(if (slideUp) -translateY else translateY)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .setListener(object : SimpleListener() {
                        override fun onAnimationEnd(view: View?) {
                            if (!enter) {
                                view?.apply {
                                    visibility = View.GONE
                                    // Reset state
                                    view.y += if (slideUp) translateY else -translateY
                                }
                            }
                            runAfter?.invoke()
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

    fun prepareView(target: View?) {
        target?.apply {
            if (fadeIn) {
                y += if (slideUp) translateY else -translateY
            }
            alpha = if (fadeIn) 0F else 1F
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?, runAfter: (() -> Unit)?) {
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
                            if (!fadeIn) {
                                view?.apply {
                                    visibility = View.GONE
                                    // Reset state
                                    view.y += if (slideUp) translateY else -translateY
                                }
                            }
                            runAfter?.invoke()
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