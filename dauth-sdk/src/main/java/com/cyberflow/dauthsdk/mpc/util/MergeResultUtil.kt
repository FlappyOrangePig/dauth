package com.cyberflow.dauthsdk.mpc.util

import android.util.Base64
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.wallet.ext.runCatchingWithLog
import java.math.BigInteger

private const val TAG = "MergeResultUtil"

/**
 * 秘钥分片加减法工具类
 */
object MergeResultUtil {

    private const val DEBUG = true

    private fun log(log: String) {
        if (DEBUG) {
            DAuthLogger.v(log, TAG)
        }
    }

    fun encodeKey(keys: Array<String>): String? {
        return runCatchingWithLog { encode(keys.map { it.toByteArray() }) }
    }

    fun decodeKey(compressedBase64: String, keys: Array<String>): String? {
        return runCatchingWithLog { String(decode(compressedBase64, keys.map { it.toByteArray() })) }
    }

    private fun encode(keys: List<ByteArray>): String {
        // 压缩
        val bas = keys.map { ZipUtil.compress(it) }
        // 求和
        val sum = keyAdd(bas)
        // zip
        val compressed = ZipUtil.compress(sum)
        // base64-encode
        val compressedBase64 = Base64.encodeToString(compressed, Base64.DEFAULT)
        log("compressedBase64 len=${compressedBase64.length}")
        return compressedBase64
    }

    private fun decode(compressedBase64: String, keys: List<ByteArray>): ByteArray {
        // base64-decode
        val decompressedBase64 = Base64.decode(compressedBase64, Base64.DEFAULT)
        // unzip
        val decompressed = ZipUtil.decompress(decompressedBase64)
        // 差
        val difference = keyMinus(decompressed, keys.map { ZipUtil.compress(it) })
        // 解压
        return ZipUtil.decompress(difference)
    }

    private fun keyAdd(addends: List<ByteArray>): ByteArray {
        var sum = BigInteger.ZERO
        addends.forEach {
            sum += BigInteger(it)
        }
        log("keyAdd sum=$sum")
        return sum.toByteArray()
    }

    private fun keyMinus(minuend: ByteArray, subtrahends: List<ByteArray>): ByteArray {
        var difference = BigInteger(minuend)
        log("keyMinus minuend=$difference")
        subtrahends.forEach {
            difference -= BigInteger(it)
        }
        log("keyMinus difference=$difference")
        return difference.toByteArray()
    }
}