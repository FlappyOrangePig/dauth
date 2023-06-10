package com.cyberflow.dauthsdk.api.entity

import java.math.BigInteger

sealed class GetBalanceResult {
    class Success(
        /**
         * 余额，单位wei
         */
        val balance: BigInteger
    ) : GetBalanceResult()

    object Failure : GetBalanceResult()
    object CannotGetAddress : GetBalanceResult()
}