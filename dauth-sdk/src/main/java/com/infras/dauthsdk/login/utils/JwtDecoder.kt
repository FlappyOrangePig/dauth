package com.infras.dauthsdk.login.utils

import android.util.Base64
import com.infras.dauthsdk.login.model.JwtDecodeResponse
import com.infras.dauthsdk.login.utils.DAuthLogger.d
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class JwtDecoder {

    fun decoded(JWTEncoded: String) : JwtDecodeResponse {
        var response = JwtDecodeResponse()
        try {
            val split = JWTEncoded.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val gson = Gson()
            d("JWT_DECODED," + "Header: " + getJson(split[0]))
            d("JWT_DECODED," + "Body: " + getJson(split[1]))
            response = gson.fromJson(getJson(split[1]), JwtDecodeResponse::class.java)
        } catch (e: UnsupportedEncodingException) {
            DAuthLogger.e("JwtDecoder error: $e")
        }
        return response
    }

    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, Charset.forName("UTF-8"))
    }
}