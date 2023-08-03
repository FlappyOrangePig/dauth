package com.infras.dauthsdk.login.callback

interface WalletCallback {
    fun onResult(walletInfo: String)
}