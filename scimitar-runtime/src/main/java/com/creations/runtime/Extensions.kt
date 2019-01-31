package com.creations.runtime

import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

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

fun View.setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    lp.setMargins(
            left ?: lp.leftMargin,
            top ?: lp.topMargin,
            right ?: lp.rightMargin,
            bottom ?: lp.rightMargin
    )
    layoutParams = lp
}

fun ViewGroup.inflate(resId: Int, attach: Boolean = true): View =
        View.inflate(context, resId, if (attach) this else null)

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

fun View.readAttrs(styleable: IntArray, attrs: AttributeSet?, action: TypedArray.() -> Unit) {
    attrs?.apply {
        context.obtainStyledAttributes(this, styleable).apply {
            action()
            recycle()
        }
    }
}