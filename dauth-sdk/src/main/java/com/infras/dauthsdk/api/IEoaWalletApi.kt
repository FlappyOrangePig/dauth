package com.infras.dauthsdk.api

import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.EstimateGasData
import com.infras.dauthsdk.api.entity.SendTransactionData
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