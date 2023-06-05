package com.cyberflow.dauthsdk.login.infrastructure

import com.squareup.moshi.Moshi

object Serializer {
    @JvmStatic
    val moshi: Moshi = Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()
}
