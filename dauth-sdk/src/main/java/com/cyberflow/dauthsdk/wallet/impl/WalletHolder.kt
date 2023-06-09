package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.wallet.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.util.KeystoreUtil

object WalletHolder {
    private const val DEBUG = true
    val walletApi: IWalletApi = if (DEBUG) {
        EoaWallet()
    } else {
        DAuthWallet()
    }

    init {
        KeystoreUtil.setupBouncyCastle()
    }
}