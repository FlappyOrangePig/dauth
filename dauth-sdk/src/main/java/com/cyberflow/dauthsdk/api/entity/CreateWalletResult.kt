package com.cyberflow.dauthsdk.api.entity

sealed class CreateWalletResult {
    class Success(val address: String) : CreateWalletResult()
    object CreateWalletFailure : CreateWalletResult()
    object BindWalletFailed: CreateWalletResult()
}