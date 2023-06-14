package com.cyberflow.dauthsdk.api

import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger

interface IWalletApi {

    /**
     * 切换链
     */
    fun initWallet(chain: SdkConfig.ChainInfo)

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
    suspend fun queryWalletBalance(walletAddress: String, tokenType: TokenType): DAuthResult<WalletBalanceData>

    /**
     * 预估交易费用
     * @param toUserId 目标账户
     * @param amount 转账金额，单位wei
     * @return 计算结果
     */
    @Deprecated("不再提供，直接调用合约计算Gas")
    suspend fun estimateGas(toUserId: String, amount: BigInteger): DAuthResult<EstimateGasData>

    /**
     * 发送交易
     * @param toAddress 目标地址
     * @param amount 交易金额
     * @return 交易结果
     */
    @Deprecated("不再提供，直接调用合约发送")
    suspend fun sendTransaction(toAddress: String, amount: BigInteger): DAuthResult<SendTransactionData>

    /**
     * 执行合约
     * @param dest 合约地址
     * @param value 当合约账户余额不足时会扣调用者的费用
     * @param func
     * @return 执行结果
     */
    suspend fun execute(dest: String, value: BigInteger, func: Function): DAuthResult<TransactionReceipt>
}