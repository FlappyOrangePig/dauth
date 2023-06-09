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

    private fun getOkhttpClient() = OkHttpClient().newBuilder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
        addInterceptor(HttpLoggingInterceptor {
            DAuthLogger.d(it, TAG)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }.build()
}