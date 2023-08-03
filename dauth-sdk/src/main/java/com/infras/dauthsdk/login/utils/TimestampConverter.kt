package com.infras.dauthsdk.login.utils

import java.text.SimpleDateFormat
import java.util.*


object TimestampConverter {

    fun timestampToCalendar(timestamp: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar
    }

    fun calendarToTimestamp(calendar: Calendar): Long {
        return calendar.timeInMillis / 1000
    }

    fun formatCalendar(calendar: Calendar, pattern: String?): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(calendar.time)
    }
}