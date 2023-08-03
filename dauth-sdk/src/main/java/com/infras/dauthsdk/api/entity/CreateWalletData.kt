package com.infras.dauthsdk.api.entity

class CreateWalletData(val address: String) {
    override fun toString(): String {
        return "CreateWalletData(address='$address')"
    }
}