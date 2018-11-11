package com.creations.scimitar_runtime.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.creations.scimitar_runtime.*
import com.creations.scimitar_runtime.state.Resource
import com.creations.scimitar_runtime.state.State
import kotlinx.android.synthetic.main.async_layout_view.view.*

class StateLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class State(val value: Int) {
        LOADING(0), SUCCESS(1), ERROR(2), NO_RESULTS(3);

        companion object {
            fun fromValue(value: Int): State? = values().first { it.value == value }
        }
    }

    init {
        inflate(context, R.layout.async_layout_view, this)
        attrs?.apply {
            context.obtainStyledAttributes(this, R.styleable.StateLayout).apply {
                getInt(R.styleable.StateLayout_asyncState, 0).apply {
                    state = State.fromValue(this) ?: State.LOADING
                }
                loadingSize = getDimensionPixelSize(R.styleable.StateLayout_asyncLoadingSize, 100.toPx)
                animate = getBoolean(R.styleable.StateLayout_asyncAnimate, true)
                recycle()
            }
        }

        doOnPreDraw {
            mLayoutFinished = true
            updateState()
        }
    }

    private val reservedIds = hashMapOf(
            R.id.errorView to 0,
            R.id.loadingView to 0,
            R.id.content to 0,
            R.id.noResultsView to 0
    )
    private var animate: Boolean = true
    private var mLayoutFinished: Boolean = false

    var state: State = State.LOADING
        set(value) {
            field = value
            if (mLayoutFinished) {
                updateState()
            }
        }

    var loadingSize: Int = 100.toPx
    var onDestroyView: () -> Unit? = {}

    var errorClickListener: OnClickListener? = null
        set(value) {
            field = value
            errorView.retryListener = value
        }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        when {
            reservedIds.containsKey(child.id) -> super.addView(child, index, params)
            else -> content.addView(child, params)
        }
    }

    override fun onDetachedFromWindow() {
        onDestroyView()
        super.onDetachedFromWindow()
    }

    private fun updateState() {
        when (state) {
            State.LOADING -> {
                loadingView.show()
                hide(content, noResultsView, errorView)
            }

            State.ERROR -> {
                errorView.show()
                hide(content, noResultsView, loadingView)
            }

            State.SUCCESS -> {
                if (animate) {
                    content.fadeSlideInBottom().show()
                } else {
                    content.show()
                }
                hide(errorView, noResultsView, loadingView)
            }

            State.NO_RESULTS -> {
                hide(errorView, content, loadingView)
                noResultsView.show()
            }
        }
    }
}

fun show(vararg views: View) {
    views.forEach { it.show() }
}

fun hide(vararg views: View) {
    views.forEach { it.hide() }
}

@BindingAdapter("stateLive")
fun <T> StateLayout.stateLive(stateLive: LiveData<Resource<T>>?) {
    stateLive?.apply {
        val observer: Observer<Resource<T>> = Observer { it ->
            it?.apply {
                if (isLoading) {
                    state = StateLayout.State.LOADING
                    return@Observer
                }
                state = if (success()) StateLayout.State.SUCCESS else StateLayout.State.ERROR
            }
        }
        observeForever(observer)
        onDestroyView = {
            stateLive.removeObserver(observer)
        }
    }
}

@BindingAdapter("state")
fun <T> StateLayout.state(state: State) {
    /*stateLive?.apply {
        val observer: Observer<Resource<T>> = Observer { it ->
            it?.apply {
                if (isLoading) {
                    state = StateLayout.State.LOADING
                    return@Observer
                }
                state = if (success()) StateLayout.State.SUCCESS else StateLayout.State.ERROR
            }
        }
        observeForever(observer)
        onDestroyView = {
            stateLive.removeObserver(observer)
        }
    }*/
}

