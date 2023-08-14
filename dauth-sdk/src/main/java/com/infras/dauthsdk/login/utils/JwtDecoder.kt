package com.infras.dauthsdk.login.utils

import android.util.Base64
import com.infras.dauthsdk.login.model.JwtDecodeResponse
import com.infras.dauthsdk.mpc.util.MoshiUtil

class JwtDecoder {

    fun decoded(JWTEncoded: String): JwtDecodeResponse {
        return try {
            val split = JWTEncoded.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val header = getJson(split[0])
            val body = getJson(split[1])
            DAuthLogger.d("jwt-header:$header")
            DAuthLogger.d("jwt-body:$body")
            val response: JwtDecodeResponse? = MoshiUtil.fromJson(body)
            response
        } catch (e: Exception) {
            DAuthLogger.e("JwtDecoder error: $e")
            null
        } ?: JwtDecodeResponse.createInstance()
    }

    private fun getJson(strEncoded: String) = String(Base64.decode(strEncoded, Base64.URL_SAFE))
}