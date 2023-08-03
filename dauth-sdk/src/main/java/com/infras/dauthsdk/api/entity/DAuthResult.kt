package com.infras.dauthsdk.api.entity

import com.infras.dauthsdk.login.utils.DAuthLogger

sealed class DAuthResult<T> {

    abstract fun getError(): String?
    fun isSuccess() = getError() != null

    class Success<T>(val data: T) : DAuthResult<T>() {
        override fun getError() = null
        override fun toString(): String {
            return "Success(data=$data)"
        }
    }

    class ServerError<T>(val code: Int, val msg: String) : DAuthResult<T>() {
        override fun getError(): String {
            return "server error:$msg"
        }

        override fun toString(): String {
            return "ServerError(code=$code, msg='$msg')"
        }

    }

    class NetworkError<T>(val throwable: Throwable? = null) : DAuthResult<T>() {
        override fun getError(): String {
            return "network error:${throwable?.message.orEmpty()}"
        }

        override fun toString(): String {
            return "NetworkError(throwable=${throwable.toString()})"
        }

    }

    class SdkError<T>(val code: Int) : DAuthResult<T>() {
        override fun getError(): String {
            return "sdk error:$code"
        }

        override fun toString(): String {
            return "SdkError(code=$code)"
        }
    }

    companion object {
        const val SDK_ERROR_UNKNOWN = 0
        const val SDK_ERROR_CANNOT_GET_NONCE = 1
        const val SDK_ERROR_CANNOT_GET_ADDRESS = 2
        const val SDK_ERROR_BIND_WALLET = 3
        const val SDK_ERROR_LOGGED_OUT = 4
        const val SDK_ERROR_MERGE_RESULT = 5
        const val SDK_ERROR_RESTORE_KEY_BY_MERGE_RESULT = 6
        const val SDK_ERROR_CANNOT_GENERATE_ADDRESS = 7
        const val SDK_ERROR_SET_KEY = 8
        const val SDK_ERROR_AA_ADDRESS_INVALID = 9
        const val SDK_ERROR_NO_BALANCE = 10
        const val SDK_ERROR_ESTIMATE_SIMULATE = 11
        const val SDK_ERROR_ESTIMATE_SIMULATE_DECODE = 12
        const val SDK_ERROR_BALANCE_TYPE = 13
        const val SDK_ERROR_SIGN = 14
        const val SDK_ERROR_GET_SIGNER_BY_SIGNATURE = 15
    }
}

internal fun <T> DAuthResult<T>.traceResult(
    tag: String,
    log: String
): DAuthResult<T> {
    when (this) {
        is DAuthResult.Success -> {
            DAuthLogger.i("$log:${this.data}", tag)
        }

        is DAuthResult.NetworkError, is DAuthResult.SdkError, is DAuthResult.ServerError -> {
            DAuthLogger.e("$log:${this.getError()}", tag)
        }
    }
    return this
}

internal fun <SRC, DST> DAuthResult<SRC>.transformError(): DAuthResult<DST> {
    val error: DAuthResult<DST> = when (this) {
        is DAuthResult.Success -> {
            throw IllegalArgumentException("cannot transform success")
        }

        is DAuthResult.NetworkError -> {
            DAuthResult.NetworkError(this.throwable)
        }

        is DAuthResult.SdkError -> {
            DAuthResult.SdkError(this.code)
        }

        is DAuthResult.ServerError -> {
            DAuthResult.ServerError(this.code, this.msg)
        }
    }
    return error
}