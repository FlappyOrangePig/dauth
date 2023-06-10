package com.cyberflow.dauthsdk.api.entity

import java.math.BigInteger

sealed class EstimateGasResult {
    class Success(
        val amountUsed: BigInteger
    ) : EstimateGasResult()

    object Failure : EstimateGasResult()
    object CannotGetAddress: EstimateGasResult()
}