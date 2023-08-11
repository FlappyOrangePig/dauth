package com.infras.dauthsdk.api

import androidx.annotation.IntDef

object DAuthLogLevel {
    internal const val LEVEL_VERBOSE = 1
    internal const val LEVEL_DEBUG = 2
    internal const val LEVEL_INFO = 3
    internal const val LEVEL_WARN = 4
    internal const val LEVEL_ERROR = 5
    internal const val LEVEL_FATAL = 6
    internal const val LEVEL_NONE = 7
}

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
annotation class DAuthLogLevelEnum