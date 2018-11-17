package com.creations.runtime.anim

import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener

abstract class Animation(
        val duration: Long = 320,
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
) {
    abstract fun prepareView(target: View?)
    abstract fun run(target: View?)
}

class FadeInAnimation : Animation() {

    override fun prepareView(target: View?) {
        target?.apply {
            alpha = 0F
            visibility = View.VISIBLE
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun run(target: View?) {
        target?.apply {
            prepareView(this)
            ViewCompat
                    .animate(this)
                    .alpha(1F)
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