package com.cyberflow.dauthsdk.api.entity

sealed class GetAddressResult {
    class Success(
        /**
         * 钱包地址。如：0x9b3b7d4a3b7d4a3b7d4a3b7d4a3b7d4a3b7d4a3b7d4a3
         */
        val address: String
    ) : GetAddressResult()

    object Failure : GetAddressResult()
}