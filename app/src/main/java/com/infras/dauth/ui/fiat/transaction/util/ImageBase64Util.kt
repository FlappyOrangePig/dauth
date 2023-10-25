package com.infras.dauth.ui.fiat.transaction.util

import android.util.Base64
import com.infras.dauth.util.LogUtil
import java.io.File

object ImageBase64Util {

    private const val TAG = "ImageBase64Util"

    fun getBase64EncodedImageFile(imageFilePath: String): String? {
        val bytes = try {
            File(imageFilePath).readBytes()
        } catch (e: Throwable) {
            LogUtil.e(TAG, e.stackTraceToString())
            null
        } ?: return null
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}