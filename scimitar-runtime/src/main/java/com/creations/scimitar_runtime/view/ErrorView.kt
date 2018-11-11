package com.creations.scimitar_runtime.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.creations.scimitar_runtime.R
import kotlinx.android.synthetic.main.error_view.view.*

class ErrorView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var retryListener: View.OnClickListener? = null

    init {
        inflate(context, R.layout.error_view, this)
        visibility = View.VISIBLE
        errorButton.setOnClickListener { retryListener?.onClick(it) }
    }
}
