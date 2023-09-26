package com.infras.dauth.util

object ConvertUtil {
    fun addCommasToNumberOld(number: String): String {
        val reversed = number.reversed()
        val stringBuilder = StringBuilder()
        for (i in reversed.indices) {
            if (i != 0 && i % 3 == 0) {
                stringBuilder.append(',')
            }
            stringBuilder.append(reversed[i])
        }
        return stringBuilder.reverse().toString()
    }

    fun addCommasToNumber(number: String): String {
        val parts = number.split(".")
        val intPart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""
        val reversedInt = intPart.reversed()
        val stringBuilder = StringBuilder()
        for (i in reversedInt.indices) {
            if (i != 0 && i % 3 == 0) {
                stringBuilder.append(',')
            }
            stringBuilder.append(reversedInt[i])
        }
        val intWithCommas = stringBuilder.reverse().toString()
        return intWithCommas + decimalPart
    }
}