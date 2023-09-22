package com.infras.dauth.ui.fiat.transaction.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtil {

    fun getOrderTime(tickInS: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
            Date(tickInS * 1000L)
        )
    }
}