package com.infras.dauth.util

import org.web3j.utils.Convert
import java.math.BigInteger
import java.math.RoundingMode

private const val TAG = "GasUtil"

object GasUtil {

    private val reversedUnits: List<Convert.Unit> by lazy {
        Convert.Unit.values().reversed()
            .filterNot { listOf(Convert.Unit.SZABO, Convert.Unit.FINNEY).contains(it) }
    }

    fun getReadableGas(gasWei: BigInteger): String {
        return try {
            val gasWeiBigDecimal = gasWei.toBigDecimal()
            reversedUnits.forEach {
                if (gasWeiBigDecimal >= it.weiFactor) {
                    val divided = gasWeiBigDecimal.divide(it.weiFactor)
                    val scaled = divided.setScale(4, RoundingMode.DOWN)
                    return "$scaled ${it.name}"
                }
            }
            "0 ${reversedUnits.last().name}"
        } catch (t: Throwable) {
            LogUtil.e(TAG, t.stackTraceToString())
            ""
        }
    }
}