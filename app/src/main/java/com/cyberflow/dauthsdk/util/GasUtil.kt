package com.cyberflow.dauthsdk.util

import org.web3j.utils.Convert
import java.math.BigInteger

object GasUtil {

    private val reversedUnits: List<Convert.Unit> by lazy {
        Convert.Unit.values().reversed()
    }

    fun getReadableGas(gasWei: BigInteger): String {
        reversedUnits.forEach {
            if (gasWei.toBigDecimal() >= it.weiFactor) {
                return "${gasWei.toBigDecimal().divide(it.weiFactor)} ${it.name}"
            }
        }
        throw RuntimeException()
    }
}