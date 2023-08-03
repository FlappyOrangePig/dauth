package com.infras.dauth

import android.content.Context
import android.widget.Toast
import java.lang.ref.WeakReference

object ToastUtil {

    private var lastToast: WeakReference<Toast>? = null

    fun show(context: Context, text: String) {
        lastToast?.get()?.let {
            it.cancel()
            lastToast = null
        }

        Toast.makeText(context, text, Toast.LENGTH_LONG).let {
            it.show()
            lastToast = WeakReference(it)
        }
    }
}