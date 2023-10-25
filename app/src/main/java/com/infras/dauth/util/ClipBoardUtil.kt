package com.infras.dauth.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object ClipBoardUtil {
    fun copyToClipboard(context: Context, text: String) {
        val clipboard: ClipboardManager =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        ToastUtil.show(context, "copied")
    }
}