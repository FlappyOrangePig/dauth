package com.cyberflow.dauthsdk.wallet.api

import android.content.Context
import java.math.BigInteger

interface IWalletApi {

    fun initWallet(context: Context)

    /**
     * 创建钱包
     * 登录如果报钱包未创建的错误，则应该调此接口创建钱包。创建失败回到登录页；
     * 创建成功则表示注册成功并成功登录，此时也可以绑定其他信息（可选）。
     * @return 0=成功
     */
    suspend fun createWallet(passcode:String?): Int?

    /**
     * 查询钱包地址
     * @return 钱包地址。如：0x9b3b7d4a3b7d4a3b7d4a3b7d4a3b7d4a3b7d4a3b7d4a3
     */
    suspend fun queryWalletAddress(): String

    /**
     * 查询钱包余额
     * @return 余额，单位wei
     */
    suspend fun queryWalletBalance(): BigInteger?

    /**
     * 预估交易费用
     * @param toUserId 目标账户
     * @param amount 转账金额，单位wei
     * @return 返回交易ID
     */
    suspend fun estimateGas(toUserId: String, amount: BigInteger): BigInteger?

    /**
     * 发送交易
     * @param toAddress 目标地址
     * @param amount 交易金额
     */
    suspend fun sendTransaction(toAddress: String, amount: BigInteger): String?
}