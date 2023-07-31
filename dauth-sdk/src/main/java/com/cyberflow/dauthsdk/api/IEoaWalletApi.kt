package com.cyberflow.dauthsdk.api

import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import java.math.BigInteger

interface IEoaWalletApi {

    suspend fun connectWallet(): Boolean

    suspend fun getEoaWalletAddress(): DAuthResult<String>

    suspend fun estimateGas(toUserId: String, amount: BigInteger): DAuthResult<EstimateGasData>

    suspend fun sendTransaction(
        toAddress: String,
        amount: BigInteger
    ): DAuthResult<SendTransactionData>
}