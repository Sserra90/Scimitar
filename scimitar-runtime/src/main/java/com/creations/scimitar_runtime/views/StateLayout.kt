package com.creations.scimitar_runtime.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.creations.scimitar_runtime.*
import com.creations.scimitar_runtime.state.State
import com.creations.scimitar_runtime.state.Status
import kotlinx.android.synthetic.main.async_layout.view.*

class StateLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.async_layout)

        readAttrs(attrs) {
            getInt(R.styleable.StateLayout_state, 0).apply {
                state = when (this) {
                    0 -> State()
                    1 -> State(Status.Success)
                    3 -> State(Status.Error)
                    4 -> State(Status.NoResults)
                    else -> State()
                }
            }
            loadingSize = getDimensionPixelSize(R.styleable.StateLayout_loadingSize, 100.toPx)
            animate = getBoolean(R.styleable.StateLayout_animate, true)
        }

        doOnPreDraw {
            mLayoutFinished = true
            updateState()
        }
    }

    private var animate: Boolean = true
    private var mLayoutFinished: Boolean = false

    var state: State<Any> = State(status = Status.Loading)
        set(value) {

            // Check if the state is the same.
            if (field == value) {
                return
            }

            field = value
            if (mLayoutFinished) {
                updateState()
            }
        }

    var loadingSize: Int = 100.toPx
    var onDestroyView: () -> Unit? = {}

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (child.id == R.id.errorView ||
                child.id == R.id.loadingView ||
                child.id == R.id.content ||
                child.id == R.id.noResults) {
            super.addView(child, index, params)
            return
        }
        content?.addView(child, params)
    }

    override fun onDetachedFromWindow() {
        onDestroyView()
        super.onDetachedFromWindow()
    }

    private fun updateState() {
        when (state.status) {
            Status.Success -> {
                content.show()
                hide(loadingView, errorView, noResults)
            }
            Status.Error -> {
                errorView.show()
                hide(content, loadingView, noResults)
            }
            Status.NoResults -> {
                noResults.show()
                hide(content, loadingView, errorView)
            }
            Status.Loading -> {
                loadingView.show()
                hide(content, noResults, errorView)
            }
        }
    }
}

private fun show(vararg views: View) {
    views.forEach { it.show() }
}

private fun hide(vararg views: View) {
    views.forEach { it.hide() }
}

@BindingAdapter("state")
fun StateLayout.setState(state: State<Any>) {
    this.state = state
}

@BindingAdapter("stateLive")
fun StateLayout.setStateLive(stateLive: LiveData<State<Any>>?) {
    stateLive?.apply {
        val observer: Observer<State<Any>> = Observer { it ->
            it?.apply {
                state = this
            }
        }
        observeForever(observer)
        onDestroyView = {
            stateLive.removeObserver(observer)
        }
    }
}