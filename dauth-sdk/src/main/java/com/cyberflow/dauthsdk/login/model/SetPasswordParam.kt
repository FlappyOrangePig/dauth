package com.cyberflow.dauthsdk.login.model


class SetPasswordParam : IAccessTokenRequest, IAuthorizationRequest {
    var password: String? = null
    var old_password: String? = null
    var access_token: String? = null
    var authid: String? = null
}