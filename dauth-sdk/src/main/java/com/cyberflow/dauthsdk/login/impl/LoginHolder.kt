package com.cyberflow.dauthsdk.login.impl

import com.cyberflow.dauthsdk.login.api.ILoginApi
import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.util.KeystoreUtil

object LoginHolder {
    private const val DEBUG = true
    val loginApi: ILoginApi = if (DEBUG) {
        DummyLogin()
    } else {
        DAuthLogin()
    }

    init {
        KeystoreUtil.setupBouncyCastle()
    }
}