package com.infras.dauthsdk.wallet.ext

import com.infras.dauthsdk.wallet.util.AndroidKeystoreUtil
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
        val encrypt = AndroidKeystoreUtil.encode(text)
        file.writeText(encrypt)
        lock.delete()
    }

    fun safeReadText(): String? {
        return if (safeExists()) {
            val encrypted = file.readText()
            return AndroidKeystoreUtil.decode(encrypted)
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