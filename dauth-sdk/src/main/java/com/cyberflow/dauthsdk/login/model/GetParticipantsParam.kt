package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.utils.LoginPrefs

class GetParticipantsParam : BaseTokenRequestParam() {
    override var access_token: String = LoginPrefs().getAccessToken()
    override val authid = LoginPrefs().getAuthId()
}