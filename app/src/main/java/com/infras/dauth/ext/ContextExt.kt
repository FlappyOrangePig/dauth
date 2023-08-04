package com.infras.dauth.ext

import com.infras.dauth.MyApplication

fun Float.dp(): Int {
    val scale = MyApplication.app.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

fun Int.dp(): Int = this.toFloat().dp()