package com.infras.dauth.ext

import android.os.SystemClock
import android.view.View
import java.util.WeakHashMap
import kotlin.math.abs

private class DebouncedOnClickListener(
    private val onClickListener: View.OnClickListener,
    private val minimumIntervalMillis: Long = 500
) : View.OnClickListener {
    private val lastClickMap: MutableMap<View, Long> = WeakHashMap()

    override fun onClick(clickedView: View) {
        val previousClickTimestamp = lastClickMap[clickedView]
        val currentTimestamp = SystemClock.uptimeMillis()
        lastClickMap[clickedView] = currentTimestamp
        if (previousClickTimestamp == null || abs(currentTimestamp - previousClickTimestamp) > minimumIntervalMillis) {
            onClickListener.onClick(clickedView)
        }
    }
}

fun View.setDebouncedOnClickListener(onClickListener: View.OnClickListener?) {
    this.setOnClickListener(onClickListener?.let { DebouncedOnClickListener(onClickListener) })
}