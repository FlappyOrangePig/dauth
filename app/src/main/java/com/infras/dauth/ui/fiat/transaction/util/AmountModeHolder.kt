package com.infras.dauth.ui.fiat.transaction.util

import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil.scale
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import java.math.BigDecimal
import java.math.RoundingMode

class AmountModeHolder(
    private val isAmountMode: Boolean,
    private val fiatInfo: DigitalCurrencyListRes.FiatInfo,
    private val cryptoInfo: DigitalCurrencyListRes.CryptoInfo,
) {

    fun getMaxLimit(): String {
        return if (isAmountMode) {
            "${fiatInfo.orderMaxLimit}"
        } else {
            "${cryptoInfo.orderMaxLimit}"
        }
    }

    private fun getMinLimit(): String {
        return if (isAmountMode) {
            "${fiatInfo.orderMinLimit}"
        } else {
            "${cryptoInfo.orderMinLimit}"
        }
    }

    private fun getMinimumText(isAmountMode: Boolean): String {
        val unit = if (isAmountMode) {
            "${fiatInfo.orderMinLimit} ${fiatInfo.fiatCode}"
        } else {
            "${cryptoInfo.orderMinLimit} ${cryptoInfo.cryptoCode}"
        }
        return "Enter a minimum of $unit"
    }

    private fun getMaximumText(isAmountMode: Boolean): String {
        val unit = if (isAmountMode) {
            "${fiatInfo.orderMaxLimit} ${fiatInfo.fiatCode}"
        } else {
            "${cryptoInfo.orderMaxLimit} ${cryptoInfo.cryptoCode}"
        }
        return "Enter a maximum of $unit"
    }

    private fun getScalePrecision(): Int {
        return if (isAmountMode) {
            fiatInfo.fiatPrecision
        } else {
            cryptoInfo.cryptoPrecision
        }.toInt()
    }

    fun isInputValueValid(input: String): Pair<Int, String> {
        val minCount = getMinLimit()
        val maxCount = getMaxLimit()
        return if (BigDecimal(input) < BigDecimal(minCount)) {
            -1 to getMinimumText(isAmountMode)
        } else if (BigDecimal(input) > BigDecimal(maxCount)) {
            1 to getMaximumText(isAmountMode)
        } else {
            0 to ""
        }
    }

    fun keepXDecimalPlace(input: String): String {
        val scalePrecision = getScalePrecision()
        val lastDot = input.lastIndexOf(".")
        val dstValue = if (lastDot != -1) {
            val precision = input.length - lastDot - 1
            if (precision > scalePrecision) {
                input.scale(scalePrecision, RoundingMode.DOWN)
            } else {
                input
            }
        } else {
            input
        }
        return dstValue
    }
}