package com.infras.dauthsdk.wallet.ext

import com.infras.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

private const val TAG = "DAuthCoroutine"

private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    DAuthLogger.e(throwable.stackTraceToString(), TAG)
}

internal object DAuthIoScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler
}

internal object DAuthCpuScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Default + coroutineExceptionHandler
}