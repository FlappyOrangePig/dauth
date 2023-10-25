package com.infras.dauth.manager

import android.content.Context
import com.infras.dauth.R
import com.infras.dauthsdk.login.network.BaseResponse

class ResourceManager(
    private val context: Context
) {
    fun getString(resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }

    fun getResponseDigest(r: BaseResponse?): String {
        return when {
            r == null -> {
                context.getString(R.string.network_error)
            }

            r.isSuccess() -> {
                context.getString(R.string.success)
            }

            else -> {
                "${context.getString(R.string.failure)}, ${r.ret}, ${r.info}"
            }
        }
    }
}