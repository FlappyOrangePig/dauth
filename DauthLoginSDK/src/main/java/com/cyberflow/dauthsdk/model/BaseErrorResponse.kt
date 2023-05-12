package com.cyberflow.dauthsdk.model

data class BaseErrorResponse(
    val iRet: Long,
    val sMsg: String,
    val data: String?
)
