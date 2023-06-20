package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.IWalletApi
import com.cyberflow.dauthsdk.wallet.util.KeystoreUtil

object WalletHolder {
    val walletApi: IWalletApi by lazy { EoaWallet() }

    init {
        KeystoreUtil.setupBouncyCastle()
    }
}