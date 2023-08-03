package com.infras.dauthsdk.login.model

import java.math.BigInteger

class CommitTransParam(
    val open_id: String,
    val transdata: String,
    val client_id: String
) : IAccessTokenRequest

class WireUserOp(
    val sender: String,
    val nonce: BigInteger,
    val initCode: ByteArray,
    val callData: ByteArray,
    val callGasLimit: BigInteger,
    val verificationGasLimit: BigInteger,
    val preVerificationGas: BigInteger,
    val maxFeePerGas: BigInteger,
    val maxPriorityFeePerGas: BigInteger,
    val paymasterAndData: ByteArray,
    val signature: ByteArray,
)