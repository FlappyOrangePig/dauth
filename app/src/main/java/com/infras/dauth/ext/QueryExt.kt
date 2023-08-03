package com.infras.dauth.ext

import com.infras.dauth.R
import com.infras.dauth.MyApplication
import com.infras.dauth.ToastUtil
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.WalletBalanceData
import com.infras.dauthsdk.login.network.BaseResponse
import com.infras.dauth.manager.sdk

suspend fun myAddress() =
    (sdk().queryWalletAddress() as? DAuthResult.Success)?.data?.aaAddress

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
    when {
        this == null -> {
            ToastUtil.show(
                context, context.getString(R.string.network_error)
            )
        }

        this.isSuccess() -> {
            ToastUtil.show(context, context.getString(R.string.success))
        }

        else -> {
            ToastUtil.show(
                context,
                "${context.getString(R.string.failure)}, ${ret}, $info"
            )
        }
    }
}