package com.cyberflow.dauthsdk.wallet.util

import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

object ConvertUtil {

    inline fun weiToEth(crossinline weiHolder: () -> BigInteger): BigDecimal? {
        val balance = try {
            val eth = Convert.fromWei(BigDecimal(weiHolder.invoke()), Convert.Unit.ETHER)
            eth
        } catch (e: Exception) {
            null
        }
        return balance
    }
}