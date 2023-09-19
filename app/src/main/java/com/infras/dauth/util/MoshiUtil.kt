package com.infras.dauth.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.math.BigInteger

object MoshiUtil {
    private const val TAG = "MoshiUtil"
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(BigInteger::class.java, BigIntegerJsonAdapter)
            .build()
    }

    internal inline fun <reified T> toJson(obj: T, throws: Boolean = true) = try {
        val adapter = moshi.adapter(T::class.java)
        adapter.toJson(obj)
    } catch (t: Throwable) {
        if (throws) {
            LogUtil.e(TAG, t.stackTraceToString())
        }
        null
    }.orEmpty()

    internal inline fun <reified T> fromJson(json: String, throws: Boolean = true): T? = try {
        val adapter = moshi.adapter(T::class.java)
        adapter.fromJson(json)
    } catch (t: Throwable) {
        if (throws) {
            LogUtil.e(TAG, t.stackTraceToString())
        }
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