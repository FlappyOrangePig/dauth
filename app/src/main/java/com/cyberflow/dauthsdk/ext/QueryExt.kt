package com.cyberflow.dauthsdk.ext

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import java.math.BigInteger

suspend fun myAddress() =
    (DAuthSDK.instance.queryWalletAddress() as? DAuthResult.Success)?.data?.aaAddress

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