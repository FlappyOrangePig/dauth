package com.cyberflow.dauthsdk.login.model

class GetSecretKeyParam(
    val type: Int
) : IAccessTokenRequest

internal object GetSecretKeyParamConst {
    const val TYPE_KEY = 0
    const val TYPE_MERGE_RESULT = 1
}