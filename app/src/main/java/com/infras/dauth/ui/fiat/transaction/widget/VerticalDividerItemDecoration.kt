package com.infras.dauth.ui.fiat.transaction.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class VerticalDividerItemDecoration(
    private val dividerHeight: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val pos = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        val count = parent.adapter!!.itemCount
        val h = (dividerHeight / 2f).roundToInt()
        val top = if (pos == 0) 0 else h
        val bottom = if (pos == count - 1) 0 else h
        outRect.set(0, top, 0, bottom)
    }
}