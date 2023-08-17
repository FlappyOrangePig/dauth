package com.infras.dauthsdk.wallet.impl.manager

import com.infras.dauthsdk.api.annotation.DAuthLogLevel
import com.infras.dauthsdk.wallet.util.ExecutorUtil
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService

private const val TAG = "DLogManager"

internal class DLogManager internal constructor(
    private val fileManager: FileManager,
) {

    private val executor: ExecutorService by lazy {
        ExecutorUtil.buildSingleThreadScheduledExecutorService(TAG)
    }

    private val fileWriter by lazy {
        val folder = getLogFileFolder()
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
                DAuthLogLevel.LEVEL_VERBOSE -> "V"
                DAuthLogLevel.LEVEL_DEBUG -> "D"
                DAuthLogLevel.LEVEL_INFO -> "I"
                DAuthLogLevel.LEVEL_WARN -> "W"
                DAuthLogLevel.LEVEL_ERROR -> "E"
                DAuthLogLevel.LEVEL_FATAL -> "F"
                else -> "?"
            }
            w.appendLine("$timestamp $levelTag/$tag: $log")
            w.flush()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun getLogFileFolder(): File = fileManager.getFolder(FileManager.FOLDER_NAME_LOG, false)

    private fun getFileName(): String {
        return "${SimpleDateFormat("yyyy_MM_d", Locale.getDefault()).format(Date())}.txt"
    }
}