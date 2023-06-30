package com.cyberflow.dauthsdk.api.entity

sealed class DAuthResult<T> {

    abstract fun getError(): String?
    fun isSuccess() = getError() != null

    class Success<T>(val data: T) : DAuthResult<T>() {
        override fun getError() = null
        override fun toString(): String {
            return "Success(data=$data)"
        }
    }

    class Web3Error<T>(val code: Int, val msg: String) : DAuthResult<T>() {
        override fun getError(): String? {
            return msg
        }

        override fun toString(): String {
            return "Web3Error(code=$code, msg='$msg')"
        }

    }

    class NetworkError<T>(val throwable: Throwable? = null) : DAuthResult<T>() {
        override fun getError(): String? {
            return throwable?.stackTraceToString().orEmpty()
        }

        override fun toString(): String {
            return "NetworkError(throwable=${throwable.toString()})"
        }

    }

    class SdkError<T>(val code: Int = SDK_ERROR_UNKNOWN) : DAuthResult<T>() {
        override fun getError(): String? {
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
        const val SDK_ERROR_CANNOT_GENERATE_EOA_ADDRESS = 4
        const val SDK_ERROR_MERGE_RESULT = 5
        const val SDK_ERROR_GET_AA_ADDRESS_ERROR = 6
    }
}