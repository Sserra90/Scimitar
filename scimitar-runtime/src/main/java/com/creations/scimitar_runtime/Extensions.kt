package com.creations.scimitar_runtime

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat


val Float.toPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)
val Float.toDp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)

val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()


fun View.show(): View {
    this.visibility = View.VISIBLE
    return this
}

fun View.hide(): View {
    this.visibility = View.GONE
    return this
}

inline fun <T : View> T.doOnPreDraw(crossinline action: (view: T) -> Unit) {
    val vto = viewTreeObserver
    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            when {
                vto.isAlive -> vto.removeOnPreDrawListener(this)
                else -> viewTreeObserver.removeOnPreDrawListener(this)
            }
            action(this@doOnPreDraw)
            return true
        }
    })
}

fun <T : ViewGroup> T.anim(f: ViewPropertyAnimatorCompat.() -> Unit) {
    ViewCompat.animate(this).apply {
        f()
    }
}

fun <T : View> T.anim(f: ViewPropertyAnimatorCompat.() -> Unit) {
    ViewCompat.animate(this).apply {
        f()
    }
}

fun <T : View> T.fadeSlideInTop(translateY: Float = 100.toPx.toFloat(),
                                duration: Long = 500, startDelay: Long = 0): View {
    FadeSlideInTopAnim(this, translateY, duration, startDelay)
    return this
}

fun <T : ViewGroup> T.fadeSlideInTop(translateY: Float = 100.toPx.toFloat(),
                                     duration: Long = 500, startDelay: Long = 0): ViewGroup {
    FadeSlideInTopAnim(this, translateY, duration, startDelay)
    return this
}

fun <T : View> T.fadeSlideInBottom(translateY: Float = 100.toPx.toFloat(),
                                   duration: Long = 500, startDelay: Long = 0): View {
    FadeSlideInBottomAnim(this, translateY, duration, startDelay)
    return this
}


fun <T : ViewGroup> T.fadeSlideInBottom(translateY: Float = 100.toPx.toFloat(),
                                        duration: Long = 500, startDelay: Long = 0): ViewGroup {
    FadeSlideInBottomAnim(this, translateY, duration, startDelay)
    return this
}

private class FadeSlideInTopAnim(view: View, translateY: Float, mDuration: Long, mStartDelay: Long) {
    init {
        view.y += -translateY
        view.alpha = 0F
        view.doOnPreDraw {
            ViewCompat.animate(view).apply {
                yBy(translateY)
                alpha(1f)
                duration = mDuration
                interpolator = AccelerateDecelerateInterpolator()
                startDelay = mStartDelay
            }
        }
    }
}

private class FadeSlideInBottomAnim(view: View, translateY: Float, mDuration: Long, mStartDelay: Long) {
    init {
        view.y += translateY
        view.alpha = 0F
        view.doOnPreDraw {
            ViewCompat.animate(view).apply {
                yBy(-translateY)
                alpha(1f)
                duration = mDuration
                interpolator = AccelerateDecelerateInterpolator()
                startDelay = mStartDelay
            }
        }
    }
}
