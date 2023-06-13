package com.cyberflow.dauthsdk.api.entity

sealed class DAuthResult<T> {

    abstract fun getError(): String?
    fun isSuccess() = getError() != null

    class Success<T>(val data: T) : DAuthResult<T>() {
        override fun getError() = null
    }

    class Web3Error<T>(val code: Int, val error: String) : DAuthResult<T>() {
        override fun getError(): String? {
            return error
        }
    }

    class NetworkError<T>(val throwable: Throwable? = null) : DAuthResult<T>() {
        override fun getError(): String? {
            return throwable?.stackTraceToString().orEmpty()
        }
    }

    class SdkError<T>(val code: Int = SDK_ERROR_UNKNOWN) : DAuthResult<T>() {
        override fun getError(): String? {
            return "sdk error:$code"
        }
    }

    companion object {
        const val SDK_ERROR_UNKNOWN = 0
        const val SDK_ERROR_CANNOT_GET_NONCE = 1
        const val SDK_ERROR_CANNOT_GET_ADDRESS = 2
        const val SDK_ERROR_BIND_WALLET = 3
    }
}