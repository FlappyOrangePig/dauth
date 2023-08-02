package com.cyberflow.dauthsdk.util

import org.web3j.utils.Convert
import java.math.BigInteger

private const val TAG = "GasUtil"

object GasUtil {

    private val reversedUnits: List<Convert.Unit> by lazy {
        Convert.Unit.values().reversed()
    }

    fun getReadableGas(gasWei: BigInteger): String {
        return try {
            val gasWeiBigDecimal = gasWei.toBigDecimal()
            reversedUnits.forEach {
                if (gasWeiBigDecimal >= it.weiFactor) {
                    return "${gasWeiBigDecimal.divide(it.weiFactor)} ${it.name}"
                }
            }
            "0"
        } catch (t: Throwable) {
            LogUtil.e(TAG, t.stackTraceToString())
            ""
        }
    }
}