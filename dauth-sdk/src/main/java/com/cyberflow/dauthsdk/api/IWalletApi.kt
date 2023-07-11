package com.cyberflow.dauthsdk.api

import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.api.entity.WalletAddressData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import org.web3j.abi.FunctionEncoder
import org.web3j.crypto.Sign
import org.web3j.protocol.Web3j
import org.web3j.utils.Numeric
import java.math.BigInteger

interface IWalletApi {

    /**
     * 创建钱包
     * 登录如果报钱包未创建的错误，则应该调此接口创建钱包。创建失败回到登录页；
     * 创建成功则表示注册成功并成功登录，此时也可以绑定其他信息（可选）。
     * @param forceCreate 是否强制创建。强制创建每次都会创建钱包，不管是否可以恢复。
     * @return 创建结果
     */
    suspend fun createWallet(forceCreate: Boolean): DAuthResult<CreateWalletData>

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
     * 调用合约
     * 调用方法参见[DAuthAccount.execute]
     * 用法1：转账。填目标账户contractAddress和金额balance
     * 用法2：调用合约。填目标合约地址contractAddress和编码的函数func
     *
     * @param contractAddress 合约地址。如果是转账则填AA账户地址
     * @param balance 金额。调用合约时填[BigInteger.ZERO]
     * @param func 执行函数。使用[FunctionEncoder.encode]和[Numeric.toHexString]得到func
     * @return 执行结果
     */
    suspend fun execute(contractAddress: String, balance: BigInteger, func: ByteArray): DAuthResult<String>

    fun getWeb3j(): Web3j

    fun deleteWallet()

    suspend fun mpcSign(msgHash: String): Sign.SignatureData?

    suspend fun restoreKeys(keys: List<String>): DAuthResult<CreateWalletData>
}