package com.infras.dauth.ui.fiat.transaction.util

import android.app.Activity
import android.net.Uri
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.manager.StorageDir
import com.infras.dauth.util.LogUtil
import com.infras.dauth.manager.StorageManager
import java.io.File

object UriUtil {
    private const val TAG = "UriUtil"

    fun uriTransform(activity: Activity?, uri: Uri?, suffix: String = ""): String? {
        uri ?: return null
        val a = activity ?: return null
        val dstDir = AppManagers.storageManager.getDir(StorageDir.ImageCache)
        val file = CacheFileUtil.saveUriToCacheFile(a, uri, suffix) ?: return null
        val scaled = ImageScaleUtil.getScaledImage(dstDir, file.absolutePath, suffix)
        LogUtil.d(TAG, "${file.length()} -> ${File(scaled).length()} $scaled")
        return scaled
    }
}