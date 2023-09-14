package com.infras.dauth.ui.fiat.transaction.util

import android.content.Context
import java.io.File

object StorageUtil {
    fun getCacheImageDir(context: Context): File {
        return File(context.cacheDir, "ImageCache")
    }
}