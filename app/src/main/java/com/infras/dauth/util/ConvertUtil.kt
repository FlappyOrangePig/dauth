package com.infras.dauth.util

object ConvertUtil {
    fun addCommasToNumber(number: String): String {
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
}