package com.creations.runtime.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.creations.runtime.R
import com.creations.runtime.inflate
import com.creations.runtime.readAttrs
import kotlinx.android.synthetic.main.error_view.view.*

class ErrorView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var retryListener: () -> Unit = {}

    init {
        inflate(R.layout.error_view)

        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        errorBtn.setOnClickListener {
            retryListener()
        }

        readAttrs(R.styleable.ErrorView, attrs) {
            errorBtn.text = getString(R.styleable.ErrorView_buttonText)
            (errorTitle as TextView).text = getString(R.styleable.ErrorView_errorMsg)
            errorImage.setImageDrawable(getDrawable(R.styleable.ErrorView_errorIcon))
        }
    }

}