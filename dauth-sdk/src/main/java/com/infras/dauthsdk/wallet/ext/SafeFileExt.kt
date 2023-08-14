package com.infras.dauthsdk.wallet.ext

import java.io.File
import java.io.IOException

internal class SafeFile private constructor(
    private val file: File
) {
    companion object {
        internal fun File.safe() = SafeFile(this)
    }

    private fun getLockFile(file: File): File = File("${file.absolutePath}.lock")

    @Throws(IOException::class)
    fun safeWriteText(text: String) {
        val lock = getLockFile(file)
        lock.createNewFile()
        file.writeText(text)
        lock.delete()
    }

    fun safeReadText(): String? {
        return if (safeExists()) {
            return file.readText()
        } else {
            null
        }
    }

    fun safeExists(): Boolean {
        val lock = getLockFile(file)
        return if (lock.exists()) {
            false
        } else {
            file.exists()
        }
    }
}