package com.cyberflow.dauthsdk.api

import android.content.Context
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.CreateWalletResult
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import java.math.BigInteger

interface IWalletApi {

    fun initWallet(context: Context)

    /**
     * 创建钱包
     * 登录如果报钱包未创建的错误，则应该调此接口创建钱包。创建失败回到登录页；
     * 创建成功则表示注册成功并成功登录，此时也可以绑定其他信息（可选）。
     * @return 创建结果
     */
    suspend fun createWallet(passcode:String?): DAuthResult<CreateWalletData>

    /**
     * 查询钱包地址
     * @return 查询结果
     */
    suspend fun queryWalletAddress(): DAuthResult<WalletAddressData>

    /**
     * 查询钱包余额
     * @return 查询结果
     */
    suspend fun queryWalletBalance(): DAuthResult<WalletBalanceData>

    /**
     * 预估交易费用
     * @param toUserId 目标账户
     * @param amount 转账金额，单位wei
     * @return 计算结果
     */
    suspend fun estimateGas(toUserId: String, amount: BigInteger): DAuthResult<EstimateGasData>

    /**
     * 发送交易
     * @param toAddress 目标地址
     * @param amount 交易金额
     * @return 交易结果
     */
    suspend fun sendTransaction(toAddress: String, amount: BigInteger): DAuthResult<SendTransactionData>
}