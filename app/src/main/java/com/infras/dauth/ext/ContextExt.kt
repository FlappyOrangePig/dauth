package com.infras.dauth.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.infras.dauth.MyApplication

fun Float.dp(): Int {
    val scale = MyApplication.app.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

fun Int.dp(): Int = this.toFloat().dp()

fun Context.launch(cls: Class<out Any>, block: ((Intent) -> Unit)? = null) {
    val intent = Intent(this, cls)
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    block?.invoke(intent)
    startActivity(intent)
}