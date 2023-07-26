package com.cyberflow.dauthsdk.wallet.impl.manager.task.util

import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers

private const val TAG = "WalletTaskUtil"

object WalletTaskUtil {
    suspend fun generateAddress(keys: Array<String>): Pair<String, String>? {
        // 生成EOA账号
        val msg = DAuthJniInvoker.genRandomMsg()
        val eoaAddress = DAuthJniInvoker.generateEoaAddress(msg.toByteArray(), keys)
        if (eoaAddress == null) {
            DAuthLogger.e("eoaAddress error", TAG)
            return null
        }
        DAuthLogger.d("eoaAddress=$eoaAddress", TAG)

        // 根据EOA地址获取AA地址
        val web3m = Managers.web3m
        val aaAddress = web3m.getAaAddressByEoaAddress(eoaAddress)
        if (aaAddress == null) {
            DAuthLogger.d("aa address failed")
            return null
        }
        if (aaAddress.length <= 2) {
            DAuthLogger.d("aa address too short")
            return null
        }
        DAuthLogger.d("aa address=$aaAddress")
        return eoaAddress to aaAddress
    }
}