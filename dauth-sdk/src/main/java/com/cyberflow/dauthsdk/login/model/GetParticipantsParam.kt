package com.cyberflow.dauthsdk.login.model

class GetParticipantsParam(
    override var access_token: String,
    override val authid: String
) : BaseTokenRequestParam()