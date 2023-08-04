package com.infras.dauthsdk.api.entity

/**
 * Transaction
 *
 * @constructor
 *
 * @param from Match pattern: ^0x[0-9,a-f,A-F]{40}$
 * @param to Match pattern: ^0x[0-9,a-f,A-F]{40}$
 * @param gas Match pattern: ^0x([1-9a-f]+[0-9a-f]*|0)$
 * @param value Match pattern: ^0x([1-9a-f]+[0-9a-f]*|0)$
 * @param data Match pattern: ^0x[0-9a-f]*$
 * @param gasPrice The gas price willing to be paid by the sender in wei. Used in pre-1559 transactions.
 * Match pattern: ^0x([1-9a-f]+[0-9a-f]*|0)$
 * @param maxPriorityFeePerGas Maximum fee per gas the sender is willing to pay to miners in wei. Used in 1559 transactions.
 * Match pattern: ^0x([1-9a-f]+[0-9a-f]*|0)$
 * @param maxFeePerGas The maximum total fee per gas the sender is willing to pay (includes the network / base fee and miner / priority fee) in wei. Used in 1559 transactions.
 * Match pattern: ^0x([1-9a-f]+[0-9a-f]*|0)$
 */
data class Transaction1559(
    val to: String? = null,
    val from: String,
    val gas: String? = null,
    val value: String? = null,
    val data: String,
    val gasPrice: String? = null,
    val maxPriorityFeePerGas: String? = null,
    val maxFeePerGas: String? = null,
)