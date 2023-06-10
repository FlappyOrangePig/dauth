package com.cyberflow.dauthsdk.api.entity

sealed class SendTransactionResult {
    class Success(
        val transactionHash: String,
        val gasUsed: String,
    ) : SendTransactionResult()

    object CannotFetchTransactionCount : SendTransactionResult()
    object CannotFetchHash : SendTransactionResult()
    class CannotFetchReceipt(
        val transactionHash: String
    ) : SendTransactionResult()
}
