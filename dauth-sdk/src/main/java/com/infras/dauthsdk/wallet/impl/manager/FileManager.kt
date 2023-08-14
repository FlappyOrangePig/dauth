package com.infras.dauthsdk.wallet.impl.manager

import android.content.Context
import android.os.Environment
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import java.io.File

internal class FileManager constructor(
    private val context: Context
) {
    companion object {
        internal const val FOLDER_NAME_LOG = "dauth_log"
        internal const val FOLDER_NAME_KEYSTORE = "dauth_keystore"
        internal const val FOLDER_NAME_PRE_GENERATE_KEY = "pre_generate_key"
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getFolder(folderName: String, inner: Boolean): File {
        return File(
            if (inner || !isExternalStorageWritable()) {
                context.filesDir
            } else {
                context.getExternalFilesDir(null)
            }, folderName
        )
    }

    fun write(byteArray: ByteArray, f: File) {
        f.writeBytes(byteArray)
    }
}