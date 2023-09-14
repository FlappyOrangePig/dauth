package com.infras.dauth.ui.fiat.transaction.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.infras.dauth.util.LogUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object CacheFileUtil {

    private const val TAG = "StorageUtil"

    fun saveUriToCacheFile(context: Context, uri: Uri): File? {
        return try {
            val dir = StorageUtil.getCacheImageDir(context = context)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val f = File(dir, "copied_image.jpg")
            context.contentResolver.openInputStream(uri).use { ins ->
                createFileFromStream(ins!!, f)
            }
            f
        } catch (ex: Exception) {
            LogUtil.e(TAG, Log.getStackTraceString(ex))
            null
        }
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, Log.getStackTraceString(ex))
        }
    }
}