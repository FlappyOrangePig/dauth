package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

private const val TAG = "HttpClient"

object HttpClient {

    val client by lazy {
        getOkhttpClient()
    }

    private val redactHeader
        get() = arrayListOf(
            "client_id",
            "client_secret"
        )

    private fun getOkhttpClient() = OkHttpClient().newBuilder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
        addInterceptor(HttpLoggingInterceptor { message ->
            DAuthLogger.d(message, TAG)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader.forEach {
                redactHeader(it)
            }
        })
    }.build()
}