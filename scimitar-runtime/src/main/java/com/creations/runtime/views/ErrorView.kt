package com.creations.runtime.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.creations.runtime.R
import com.creations.runtime.inflate
import com.creations.runtime.readAttrs

class ErrorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var retryListener: () -> Unit = {}

    private val errorButton by lazy {
        findViewById<Button>(R.id.errorBtn)
    }

    private val errorTitle by lazy {
        findViewById<TextView>(R.id.errorTitle)
    }

    private val errorImage by lazy {
        findViewById<ImageView>(R.id.errorImage)
    }

    init {
        inflate(R.layout.error_view)

        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        errorButton.setOnClickListener {
            retryListener()
        }

        readAttrs(R.styleable.ErrorView, attrs) {
            errorButton.text = getString(R.styleable.ErrorView_buttonText)
            errorTitle.text = getString(R.styleable.ErrorView_errorMsg)
            errorImage.setImageDrawable(getDrawable(R.styleable.ErrorView_errorIcon))
        }
    }

}