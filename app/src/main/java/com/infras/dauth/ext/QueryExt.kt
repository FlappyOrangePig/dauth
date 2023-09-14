package com.infras.dauth.ext

import com.infras.dauth.MyApplication
import com.infras.dauth.R
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.util.ToastUtil
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.WalletBalanceData
import com.infras.dauthsdk.login.network.BaseResponse

suspend fun myAddress() =
    (AccountManager.sdk.queryWalletAddress() as? DAuthResult.Success)?.data?.aaAddress

fun WalletBalanceData.mount() = when (this) {
    is WalletBalanceData.Eth -> {
        this.amount
    }
    is WalletBalanceData.ERC20 -> {
        this.amount
    }
    else -> null
}

fun WalletBalanceData.tokenIds() = (this as? WalletBalanceData.ERC721)?.tokenIds

fun BaseResponse?.handleByToast() {
    val context = MyApplication.app
    ToastUtil.show(context, MyApplication.app.resourceManager.getResponseDigest(this))
}

internal fun String.addressForShort(): String {
    return if (this.length <= 8){
        this
    }else{
        "${substring(0, 5)}***${substring(length - 3, length)}"
    }
}