package com.creations.runtime.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.creations.runtime.*
import com.creations.runtime.state.State
import com.creations.runtime.state.Status
import kotlinx.android.synthetic.main.async_layout.view.*

class StateLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var animate: Boolean = true
    private var mLayoutFinished: Boolean = false

    private val mSuccessTag = context.getString(R.string.state_success)
    private val mErrorTag = context.getString(R.string.state_error)
    private val mLoadingTag = context.getString(R.string.state_loading)
    private val mNoResultsTag = context.getString(R.string.state_noResults)
    private val mTags: Set<String> = setOf(mSuccessTag, mErrorTag, mLoadingTag, mNoResultsTag)

    private var mErrorView: View? = null
    private var mLoadingView: View? = null
    private var mNoResultsView: View? = null
    private var mContentView: View? = null

    var loadingSize: Int = 100.toPx
    var onDetachFromWindow: () -> Unit? = {}

    init {

        inflate(R.layout.async_layout)

        readAttrs(attrs) {
            getInt(R.styleable.StateLayout_state, 0).apply {
                state = when (this) {
                    0 -> State<Any>()
                    1 -> State(Status.Success)
                    3 -> State(Status.Error)
                    4 -> State(Status.NoResults)
                    else -> State<Any>()
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

    var state: State<*> = State<Any>(status = Status.Loading)
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

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (child.tag != null && mTags.contains(child.tag)) {

            when (child.tag) {
                mNoResultsTag -> mNoResultsView = child
                mLoadingTag -> mLoadingView = child
                mErrorTag -> mErrorView = child
                mSuccessTag -> mContentView = child
            }

            super.addView(child, index, params)
            return
        }
        content?.addView(child, params)
    }

    override fun onDetachedFromWindow() {
        onDetachFromWindow()
        super.onDetachedFromWindow()
    }

    private fun updateState() {
        when (state.status) {
            Status.Success -> {
                mContentView?.show()
                hide(mLoadingView, mErrorView, mNoResultsView)
            }
            Status.Error -> {
                mErrorView?.show()
                hide(mContentView, mLoadingView, mNoResultsView)
            }
            Status.NoResults -> {
                mNoResultsView?.show()
                hide(mContentView, mLoadingView, mErrorView)
            }
            Status.Loading -> {
                mLoadingView?.show()
                hide(mContentView, mNoResultsView, mErrorView)
            }
        }
    }
}

private fun show(vararg views: View?) {
    views.forEach { it?.show() }
}

private fun hide(vararg views: View?) {
    views.forEach { it?.hide() }
}

@BindingAdapter("state")
fun <T> StateLayout.setState(state: State<T>) {
    this.state = state
}

@BindingAdapter("stateLive")
fun <T> StateLayout.setStateLive(stateLive: LiveData<State<T>>?) {
    stateLive?.apply {
        val observer: Observer<State<T>> = Observer { it ->
            it?.apply {
                state = this
            }
        }
        observeForever(observer)
        onDetachFromWindow = {
            stateLive.removeObserver(observer)
        }
    }
}