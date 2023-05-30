package com.cyberflow.dauthsdk.wallet.api

data class SignKeys(val ownerKey: String, val dauthServerKey: String, val appServerKey: String)

/**
 * 为了写技术文档写的测试类
 */
interface IMockMPC {

    fun generateSignKeys(): SignKeys

    fun refreshKeys(dauthServerKey: String, appServerKey: String, mergeResult: String)

    fun signTransaction(txJson: String): String
}