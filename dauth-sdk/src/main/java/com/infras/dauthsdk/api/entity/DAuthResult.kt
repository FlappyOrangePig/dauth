package com.infras.dauthsdk.api.entity

import androidx.annotation.IntDef
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

    class SdkError<T>(@DAuthSdkErrorEnum val code: Int) : DAuthResult<T>() {
        override fun getError(): String {
            return "sdk error:${getSdkErrorDescription(code)}"
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
        const val SDK_ERROR_USER_CANCELED = 16

        fun getSdkErrorDescription(@DAuthSdkErrorEnum code: Int) = when (code) {
            SDK_ERROR_UNKNOWN -> "unknown error"
            SDK_ERROR_AA_ADDRESS_INVALID -> "aa address invalid"
            SDK_ERROR_BALANCE_TYPE -> "balance type"
            SDK_ERROR_BIND_WALLET -> "bind wallet"
            SDK_ERROR_CANNOT_GENERATE_ADDRESS -> "cannot generate address"
            SDK_ERROR_CANNOT_GET_ADDRESS -> "cannot get address"
            SDK_ERROR_CANNOT_GET_NONCE -> "cannot get nonce"
            SDK_ERROR_ESTIMATE_SIMULATE -> "estimate_simulate"
            SDK_ERROR_ESTIMATE_SIMULATE_DECODE -> "estimate simulate decode"
            SDK_ERROR_GET_SIGNER_BY_SIGNATURE -> "get signer by signature"
            SDK_ERROR_LOGGED_OUT -> "logged out"
            SDK_ERROR_MERGE_RESULT -> "merge result"
            SDK_ERROR_NO_BALANCE -> "no balance"
            SDK_ERROR_RESTORE_KEY_BY_MERGE_RESULT -> "restore key by merge result"
            SDK_ERROR_SET_KEY -> "set key"
            SDK_ERROR_SIGN -> "sign"
            SDK_ERROR_USER_CANCELED -> "user canceled"
            else -> "?"
        }
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    DAuthResult.SDK_ERROR_UNKNOWN,
    DAuthResult.SDK_ERROR_CANNOT_GET_NONCE,
    DAuthResult.SDK_ERROR_CANNOT_GET_ADDRESS,
    DAuthResult.SDK_ERROR_BIND_WALLET,
    DAuthResult.SDK_ERROR_LOGGED_OUT,
    DAuthResult.SDK_ERROR_MERGE_RESULT,
    DAuthResult.SDK_ERROR_RESTORE_KEY_BY_MERGE_RESULT,
    DAuthResult.SDK_ERROR_CANNOT_GENERATE_ADDRESS,
    DAuthResult.SDK_ERROR_SET_KEY,
    DAuthResult.SDK_ERROR_AA_ADDRESS_INVALID,
    DAuthResult.SDK_ERROR_NO_BALANCE,
    DAuthResult.SDK_ERROR_ESTIMATE_SIMULATE,
    DAuthResult.SDK_ERROR_ESTIMATE_SIMULATE_DECODE,
    DAuthResult.SDK_ERROR_BALANCE_TYPE,
    DAuthResult.SDK_ERROR_SIGN,
    DAuthResult.SDK_ERROR_GET_SIGNER_BY_SIGNATURE,
    DAuthResult.SDK_ERROR_USER_CANCELED,
)
annotation class DAuthSdkErrorEnum

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