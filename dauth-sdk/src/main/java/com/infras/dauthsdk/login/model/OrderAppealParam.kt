package com.infras.dauthsdk.login.model

class OrderAppealParam(
    val order_id: String,
    val appeal_type: Int,
) : IAccessTokenRequest, IAuthorizationRequest