package com.infras.dauth.ui.fiat.transaction.util

import android.app.Activity
import android.net.Uri
import com.infras.dauth.util.LogUtil
import java.io.File

object UriUtil {
    private const val TAG = "UriUtil"

    fun uriTransform(activity: Activity?, uri: Uri?): String? {
        uri ?: return null
        val a = activity ?: return null
        val dstDir = StorageUtil.getCacheImageDir(a)
        val file = CacheFileUtil.saveUriToCacheFile(a, uri) ?: return null
        val scaled = ImageScaleUtil.getScaledImage(dstDir, file.absolutePath)
        LogUtil.d(TAG, "${file.length()} -> ${File(scaled).length()}")
        return scaled
    }
}