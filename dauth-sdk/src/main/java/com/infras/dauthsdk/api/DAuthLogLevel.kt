package com.infras.dauthsdk.api

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    DAuthLogLevel.LEVEL_VERBOSE,
    DAuthLogLevel.LEVEL_DEBUG,
    DAuthLogLevel.LEVEL_INFO,
    DAuthLogLevel.LEVEL_WARN,
    DAuthLogLevel.LEVEL_ERROR,
    DAuthLogLevel.LEVEL_FATAL,
    DAuthLogLevel.LEVEL_NONE
)
annotation class DAuthLogLevel {
    companion object {
        const val LEVEL_VERBOSE = 1
        const val LEVEL_DEBUG = 2
        const val LEVEL_INFO = 3
        const val LEVEL_WARN = 4
        const val LEVEL_ERROR = 5
        const val LEVEL_FATAL = 6
        const val LEVEL_NONE = 7
    }
}