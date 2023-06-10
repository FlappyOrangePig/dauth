package com.cyberflow.dauthsdk.login.impl

import com.cyberflow.dauthsdk.login.api.ILoginApi
import com.cyberflow.dauthsdk.wallet.util.KeystoreUtil

object LoginHolder {
    val loginApi: ILoginApi = DAuthLogin()

    init {
        KeystoreUtil.setupBouncyCastle()
    }
}