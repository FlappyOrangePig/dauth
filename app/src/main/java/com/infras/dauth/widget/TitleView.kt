package com.infras.dauth.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView

class TitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val titleTextView = TextView(context).also { tv ->
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F)
        tv.setTextColor(Color.BLACK)
        addView(tv, LayoutParams(-2, -2).also { it.gravity = Gravity.CENTER })
    }
}