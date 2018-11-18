package com.creations.runtime.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.creations.runtime.*
import com.creations.runtime.anim.*
import com.creations.runtime.state.State
import com.creations.runtime.state.Status
import kotlinx.android.synthetic.main.async_layout.view.*


sealed class Ordering {
    object Sequence : Ordering()
    object Together : Ordering()
}

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

    private var mContentEnterAnim: Animation? = null
    private var mLoadingEnterAnim: Animation? = null
    private var mErrorEnterAnim: Animation? = null
    private var mNoResultsEnterAnim: Animation? = null

    private var mNoResultsExitAnim: Animation? = null
    private var mLoadingExitAnim: Animation? = null
    private var mErrorExitAnim: Animation? = null

    private var mOrder: Ordering? = null

    var loadingSize: Int = 100.toPx
    var onDetachFromWindow: () -> Unit? = {}

    private var prevState: State<*>? = null
    var state: State<*>? = null
        set(value) {

            // Check if the state is the same.
            if (field == value) {
                return
            }

            prevState = field
            field = value
            if (mLayoutFinished) {
                updateState()
            }
        }


    init {
        inflate(R.layout.async_layout)

        readAttrs(attrs) {
            getInt(R.styleable.StateLayout_ordering, 1).apply {
                mOrder = when (this) {
                    0 -> Ordering.Sequence
                    1 -> Ordering.Together
                    else -> Ordering.Sequence
                }
            }

            // Content enter animation
            getInt(R.styleable.StateLayout_contentEnterAnim, 0).apply {
                mContentEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> {
                        fadeIn()
                    }
                }
            }

            // Loading enter animation
            getInt(R.styleable.StateLayout_loadingEnterAnim, 0).apply {
                mLoadingEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> {
                        fadeIn()
                    }
                }
            }

            // Loading exit animation
            getInt(R.styleable.StateLayout_loadingExitAnim, 0).apply {
                mLoadingExitAnim = when (this) {
                    0 -> fadeOut()
                    1 -> slideDown(false)
                    2 -> slideUp(false)
                    3 -> slideDownFadeOut()
                    4 -> slideUpFadeOut()
                    else -> {
                        fadeOut()
                    }
                }
            }

            // Error exit animation
            getInt(R.styleable.StateLayout_errorExitAnim, 0).apply {
                mErrorExitAnim = when (this) {
                    0 -> fadeOut()
                    1 -> slideDown(false)
                    2 -> slideUp(false)
                    3 -> slideDownFadeOut()
                    4 -> slideUpFadeOut()
                    else -> {
                        fadeOut()
                    }
                }
            }

            // Error enter animation
            getInt(R.styleable.StateLayout_errorEnterAnim, 0).apply {
                mErrorEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> {
                        fadeIn()
                    }
                }
            }

            // No results enter animation
            getInt(R.styleable.StateLayout_noResultsEnterAnim, 0).apply {
                mNoResultsEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> {
                        fadeIn()
                    }
                }
            }

            // No results exit animation
            getInt(R.styleable.StateLayout_noResultsExitAnim, 0).apply {
                mNoResultsExitAnim = when (this) {
                    0 -> fadeOut()
                    1 -> slideDown(false)
                    2 -> slideUp(false)
                    3 -> slideDownFadeOut()
                    4 -> slideUpFadeOut()
                    else -> {
                        fadeOut()
                    }
                }
            }

            loadingSize = getDimensionPixelSize(R.styleable.StateLayout_loadingSize, 100.toPx)
            animate = getBoolean(R.styleable.StateLayout_animate, false)
        }

        doOnPreDraw {
            mLayoutFinished = true
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

    private fun showView(view: View?, anim: Animation?) {
        if (animate && anim != null) {
            anim.run(view)
        } else {
            show(view)
        }
    }

    private fun hideView(view: View?, anim: Animation?, runAfter: () -> Unit = {}) {
        if (animate && anim != null) {
            anim.run(view, runAfter)
        } else {
            hide(view)
        }
    }

    private fun updateState() {

        Log.d("StateLayout", "Prev state $prevState")
        Log.d("StateLayout", "New state $state")

        if (mOrder is Ordering.Together) {
            runExitAnim()
            runEnterAnim()
        } else {
            if (prevState != null) {
                runExitAnim { runEnterAnim() }
            } else {
                runEnterAnim()
            }
        }
    }

    // Run exit animations from previous state.
    private fun runExitAnim(runAfter: () -> Unit = {}) {
        var pair: Pair<View?, Animation?>? = null
        when (prevState?.status) {
            Status.Error -> {
                pair = mErrorView to mErrorExitAnim
            }
            Status.NoResults -> {
                pair = mNoResultsView to mNoResultsExitAnim
            }
            Status.Loading -> {
                pair = mLoadingView to mLoadingExitAnim
            }
        }

        if (pair != null) {
            hideView(pair.first, pair.second, runAfter)
        }
    }

    private fun runEnterAnim() {
        when (state?.status) {
            Status.Success -> {
                showView(mContentView, mContentEnterAnim)
            }
            Status.Error -> {
                showView(mErrorView, mErrorEnterAnim)
            }
            Status.NoResults -> {
                showView(mNoResultsView, mNoResultsEnterAnim)
            }
            Status.Loading -> {
                showView(mLoadingView, mLoadingEnterAnim)
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