package com.infras.dauthsdk.login.model

class LogReportParam(val data: String) {
    class Data(
        val deviceId: String,
        val clientId: String,
        val user_id: String,
        val env: String,
        val timestamp: String,
        val events: List<Event>
    )

    class Event(
        val contextId: String,
        val contextName: String,
        val eventName: String,
        val duration: Long
    )
}