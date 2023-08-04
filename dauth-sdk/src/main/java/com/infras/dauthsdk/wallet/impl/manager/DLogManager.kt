package com.infras.dauthsdk.wallet.impl.manager

import android.content.Context
import android.os.Environment
import com.infras.dauthsdk.login.utils.LEVEL_D
import com.infras.dauthsdk.login.utils.LEVEL_E
import com.infras.dauthsdk.login.utils.LEVEL_F
import com.infras.dauthsdk.login.utils.LEVEL_I
import com.infras.dauthsdk.login.utils.LEVEL_V
import com.infras.dauthsdk.login.utils.LEVEL_W
import com.infras.dauthsdk.wallet.util.ExecutorUtl
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService

private const val TAG = "DLogManager"
private const val FOLDER_NAME = "dauth_log"

internal class DLogManager internal constructor(private val context: Context) {

    private val executor: ExecutorService by lazy {
        ExecutorUtl.buildSingleThreadScheduledExecutorService(TAG)
    }

    private val fileWriter by lazy {
        val folder = getLogFileFolder() ?: return@lazy null
        folder.mkdirs()
        val file = File(folder, getFileName())
        val fileWriter = FileWriter(file, true)
        println("DLogManager:${file.absolutePath}")
        fileWriter
    }

    fun log(level: Int, tag: String, log: String, async: Boolean) {
        if (async) {
            executor.run {
                logInner(level, tag, log)
            }
        } else {
            logInner(level, tag, log)
        }
    }

    private fun logInner(level: Int, tag: String, log: String) {
        try {
            val w = fileWriter ?: return
            val timestamp =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val levelTag = when (level) {
                LEVEL_V -> "V"
                LEVEL_D -> "D"
                LEVEL_I -> "I"
                LEVEL_W -> "W"
                LEVEL_E -> "E"
                LEVEL_F -> "F"
                else -> "?"
            }
            w.appendLine("$timestamp $levelTag/$tag: $log")
            w.flush()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun getLogFileFolder(): File? = kotlin.runCatching {
        if (!isExternalStorageWritable()) {
            null
        } else {
            File(context.getExternalFilesDir(null), FOLDER_NAME)
        }
    }.getOrNull()

    private fun getFileName(): String {
        return "${SimpleDateFormat("yyyy_MM_d", Locale.getDefault()).format(Date())}.txt"
    }
}