package com.cyberflow.dauthsdk.mpc.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiUtil {

    inline fun <reified T> toJson(obj: T) = try {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(T::class.java)
        adapter.toJson(obj)
    } catch (t: Throwable) {
        null
    }.orEmpty()
}