package com.creations.runtime.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.creations.runtime.*
import com.creations.runtime.anim.*
import com.creations.runtime.state.State
import com.creations.runtime.state.Status
import kotlinx.android.synthetic.main.async_layout.view.*

typealias Animator = (target: View, runAfter: (() -> Unit)?) -> Unit

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

    var contentEnterAnim: Animator? = null
    var loadingEnterAnim: Animator? = null
    var errorEnterAnim: Animator? = null
    var noResultsEnterAnim: Animator? = null

    var noResultsExitAnim: Animator? = null
    var loadingExitAnim: Animator? = null
    var errorExitAnim: Animator? = null

    var order: Ordering? = null
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
                order = when (this) {
                    0 -> Ordering.Sequence
                    1 -> Ordering.Together
                    else -> Ordering.Sequence
                }
            }

            // Content enter animation
            getInt(R.styleable.StateLayout_contentEnterAnim, 0).apply {
                contentEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> null
                }
            }

            // Loading enter animation
            getInt(R.styleable.StateLayout_loadingEnterAnim, 0).apply {
                loadingEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> null
                }
            }

            // Loading exit animation
            getInt(R.styleable.StateLayout_loadingExitAnim, 0).apply {
                loadingExitAnim = when (this) {
                    0 -> fadeOut()
                    1 -> slideDown(false)
                    2 -> slideUp(false)
                    3 -> slideDownFadeOut()
                    4 -> slideUpFadeOut()
                    else -> null
                }
            }

            // Error exit animation
            getInt(R.styleable.StateLayout_errorExitAnim, 0).apply {
                errorExitAnim = when (this) {
                    0 -> fadeOut()
                    1 -> slideDown(false)
                    2 -> slideUp(false)
                    3 -> slideDownFadeOut()
                    4 -> slideUpFadeOut()
                    else -> null
                }
            }

            // Error enter animation
            getInt(R.styleable.StateLayout_errorEnterAnim, 0).apply {
                errorEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> null
                }
            }

            // No results enter animation
            getInt(R.styleable.StateLayout_noResultsEnterAnim, 0).apply {
                noResultsEnterAnim = when (this) {
                    0 -> fadeIn()
                    1 -> slideUp(true)
                    2 -> slideDown(true)
                    3 -> slideUpFadeIn()
                    4 -> slideDownFadeIn()
                    else -> null
                }
            }

            // No results exit animation
            getInt(R.styleable.StateLayout_noResultsExitAnim, 0).apply {
                noResultsExitAnim = when (this) {
                    0 -> fadeOut()
                    1 -> slideDown(false)
                    2 -> slideUp(false)
                    3 -> slideDownFadeOut()
                    4 -> slideUpFadeOut()
                    else -> null
                }
            }

            loadingSize = getDimensionPixelSize(R.styleable.StateLayout_loadingSize, 50.toPx)
            animate = getBoolean(R.styleable.StateLayout_animate, false)
        }

        loadingView.apply {
            layoutParams = LayoutParams(loadingSize, loadingSize, Gravity.CENTER)
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

    private fun showView(view: View?, anim: Animator?) {
        if (view == null) {
            return
        }

        if (animate && anim != null) {
            anim(view, null)
            return
        }

        show(view)
    }

    private fun hideView(view: View?, anim: Animator?, runAfter: () -> Unit = {}) {

        if (view == null) {
            return
        }

        if (animate && anim != null) {
            anim(view, runAfter)
            return
        }

        hide(view)
    }

    private fun updateState() {

        Log.d("StateLayout", "Prev state $prevState")
        Log.d("StateLayout", "New state $state")

        if (order is Ordering.Together) {
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
        var pair: Pair<View?, Animator?>? = null
        when (prevState?.status) {
            Status.Error -> {
                pair = mErrorView to errorExitAnim
            }
            Status.NoResults -> {
                pair = mNoResultsView to noResultsExitAnim
            }
            Status.Loading -> {
                pair = mLoadingView to loadingExitAnim
            }
        }

        if (pair != null) {
            hideView(pair.first, pair.second, runAfter)
        }
    }

    private fun runEnterAnim() {
        when (state?.status) {
            Status.Success -> {
                showView(mContentView, contentEnterAnim)
            }
            Status.Error -> {
                showView(mErrorView, errorEnterAnim)
            }
            Status.NoResults -> {
                showView(mNoResultsView, noResultsEnterAnim)
            }
            Status.Loading -> {
                showView(mLoadingView, loadingEnterAnim)
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