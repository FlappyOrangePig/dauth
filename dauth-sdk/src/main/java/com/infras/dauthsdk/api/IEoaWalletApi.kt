package com.infras.dauthsdk.api

import android.content.Context
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.EstimateGasData
import com.infras.dauthsdk.api.entity.SendTransactionData
import com.infras.dauthsdk.api.entity.Transaction1559
import java.math.BigInteger

interface IEoaWalletApi {

    suspend fun connectWallet(): Boolean

    suspend fun connectMetaMask(context: Context): DAuthResult<String>

    suspend fun getEoaWalletAddress(): DAuthResult<String>

    suspend fun estimateGas(toUserId: String, amount: BigInteger): DAuthResult<EstimateGasData>

    suspend fun sendTransaction(context: Context, transaction1559: Transaction1559): DAuthResult<SendTransactionData>

    suspend fun personalSign(context: Context, message: String): DAuthResult<String>
}