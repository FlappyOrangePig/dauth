package com.cyberflow.dauthsdk.mpc.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiUtil {

    inline fun <reified T> toJson(obj: T): String {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(T::class.java)
        return adapter.toJson(obj)
    }
}