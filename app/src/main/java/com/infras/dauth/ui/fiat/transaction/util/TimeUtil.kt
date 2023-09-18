package com.infras.dauth.ui.fiat.transaction.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtil {

    fun getOrderTime(tick: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
            Date(tick)
        )
    }
}