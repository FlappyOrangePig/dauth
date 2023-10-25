package com.infras.dauth.manager

import android.content.Context
import java.io.File

sealed class StorageDir {
    abstract fun dirName(): String
    abstract fun cache(): Boolean

    object ImageCache : StorageDir() {
        override fun dirName(): String {
            return "ImageCache"
        }

        override fun cache(): Boolean {
            return true
        }
    }

    object JsonFile : StorageDir() {
        const val FILE_NAME_CURRENCY_LIST = "currency_list.json"

        override fun dirName(): String {
            return "Json"
        }

        override fun cache(): Boolean {
            return false
        }

    }
}

internal class StorageManager internal constructor(
    private val context: Context
) {
    fun getDir(dir: StorageDir): File {
        return if (dir.cache()) {
            File(context.cacheDir, dir.dirName())
        } else {
            File(context.filesDir, dir.dirName())
        }
    }
}