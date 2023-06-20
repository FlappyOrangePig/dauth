package com.cyberflow.dauthsdk.mpc

import android.util.Base64
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.zip.Deflater
import java.util.zip.Inflater

private const val TAG = "MergeResultUtil"

/**
 * 秘钥分片加减法工具类
 */
object MergeResultUtil {

    private const val bytes = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val reverseMap = HashMap<Char, Int>()

    private const val DEBUG = false
    private const val Base62 = 62

    init {
        bytes.forEachIndexed { index, c ->
            reverseMap[c] = index
        }
    }

    private fun log(log: String) {
        if (DEBUG) {
            DAuthLogger.d(log, TAG)
        }
    }

    fun encode(keys: Array<String>): String {
        // 求和
        val sum = keyAdd(keys)
        log("sum=$sum")

        // 压缩
        val compressed = compress(sum.toString().toByteArray())
        // base64
        val compressedBase64 = Base64.encodeToString(compressed, Base64.DEFAULT)
        log("len=${compressedBase64.length}compressedBase64=$compressedBase64 ")
        return compressedBase64
    }

    fun decode(compressedBase64: String, keys: Array<String>): String {
        // 解base64
        val decompressedBase64 = Base64.decode(compressedBase64, Base64.DEFAULT)
        // 解压
        val decompressed = decompress(decompressedBase64)
        val decompressedSum = BigInteger(String(decompressed))

        // 相减
        val result = keyMinus(decompressedSum, keys)
        log("result=$result")
        return result
    }

    private fun toBigInt(key: String): BigInteger {
        var sum = BigInteger("0")
        for (i in key.indices) {
            val v = key[key.length - 1 - i]
            val index = reverseMap[v]
            val currentByteValue = BigInteger(Base62.toString())
                .pow(i)
                .multiply(BigInteger(index.toString()))
            sum = sum.add(currentByteValue)
        }
        log("toBigInt=$sum")
        return sum
    }

    private fun fromBigInt(bigInt: BigInteger): String {
        val bigInt62 = BigInteger(Base62.toString())
        var curBigInt = bigInt
        val result = StringBuilder()
        while (curBigInt > BigInteger("0")) {
            val v = curBigInt.mod(bigInt62)
            curBigInt = curBigInt.divide(bigInt62)
            val char = bytes[v.toInt()]
            result.insert(0, char)
        }
        log("fromBigInt=$result")
        return result.toString()
    }

    private fun keyAdd(keys: Array<String>): BigInteger {
        var result = BigInteger("0")
        for (i in keys.indices) {
            val bi = toBigInt(keys[i])
            result = result.add(bi)
        }
        return result
    }

    private fun keyMinus(sum: BigInteger, keys: Array<String>): String {
        var result = sum
        for (i in keys.indices) {
            val bi = toBigInt(keys[i])
            result = result.minus(bi)
        }
        return fromBigInt(result)
    }

    private fun compress(data: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(data)
        deflater.finish()

        val buffer = ByteArray(1024)
        val outputStream = ByteArrayOutputStream()
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        return outputStream.toByteArray()
    }

    private fun decompress(data: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(data)

        val buffer = ByteArray(1024)
        val outputStream = ByteArrayOutputStream()
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        return outputStream.toByteArray()
    }
}