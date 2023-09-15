package com.infras.dauth.ui.fiat.transaction.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.infras.dauth.R
import com.infras.dauth.ext.setDebouncedOnClickListener
import com.infras.dauth.widget.IFTextView

class OrderStateTagView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val _select = MutableLiveData(0)
    val select: LiveData<Int> = _select

    init {
        orientation = HORIZONTAL
    }

    fun setData(names: List<String>) {
        removeAllViews()
        names.forEachIndexed { index, s ->
            val tv = IFTextView(
                ContextThemeWrapper(
                    context,
                    R.style.Theme_client_TextView_OrderState
                )
            )
            tv.text = s
            tv.setDebouncedOnClickListener {
                _select.value = index
                updateSelect()
            }
            addView(tv, -2, -2)
        }
        updateSelect()
    }

    private fun updateSelect() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.isSelected = i == select.value
        }
    }
}