package com.cyberflow.dauthsdk.mpc.util

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.math.BigInteger

object MoshiUtil {
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(BigInteger::class.java, BigIntegerJsonAdapter)
            .build()
    }

    inline fun <reified T> toJson(obj: T) = try {
        val adapter = moshi.adapter(T::class.java)
        adapter.toJson(obj)
    } catch (t: Throwable) {
        DAuthLogger.e(t.stackTraceToString())
        null
    }.orEmpty()

    inline fun <reified T> fromJson(json: String): T? = try {
        val adapter = moshi.adapter(T::class.java)
        adapter.fromJson(json)
    } catch (t: Throwable) {
        DAuthLogger.e(t.stackTraceToString())
        null
    }
}

object BigIntegerJsonAdapter : JsonAdapter<BigInteger>() {
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): BigInteger {
        if (reader.peek() == JsonReader.Token.STRING) {
            val value = reader.nextString()
            return BigInteger(value)
        }
        throw IOException("Invalid JSON format for BigInteger")
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: BigInteger?) {
        writer.value(value?.toString())
    }
}