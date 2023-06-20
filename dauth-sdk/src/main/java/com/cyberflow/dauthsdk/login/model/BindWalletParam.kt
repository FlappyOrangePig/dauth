package com.cyberflow.dauthsdk.login.model

data class BindWalletParam(
    var access_token : String,
    val authid : String,
    val address : String,
    // 钱包类型,10-AA钱包，11-EOA钱包
    val wallet_type : Int,
    // mpc其中一个钱包私钥的加密密文
    val private_key : String,
    // 用mpc三个独立密钥生成的字符串
    val mpc_result: String
)
