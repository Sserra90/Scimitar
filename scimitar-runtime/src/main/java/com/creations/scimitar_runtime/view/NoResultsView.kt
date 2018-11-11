package com.creations.scimitar_runtime.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.creations.scimitar_runtime.R


/**
 * @author Sérgio Serra.
 * sergioserra99@gmail.com
 */
class NoResultsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.no_results_view, this)
    }
}