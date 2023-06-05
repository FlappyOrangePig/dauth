package com.cyberflow.dauthsdk.login.model

data class BindWalletParam(
    val access_token : String,
    val authid : String,
    val address : String,
    //钱包类型,10-AA钱包，11-EOA钱包
    val wallet_type : Int,
    val private_key : String,
    val mpc_result: String
)
