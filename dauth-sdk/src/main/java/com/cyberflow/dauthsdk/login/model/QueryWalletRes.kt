package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse

class QueryWalletRes : BaseResponse(){
    var data: Data? = null
    class Data {
        val authid: String? = null

        val appid: String? = null

        val address: String? = null

        //钱包类型,10-AA钱包，11-EOA钱包
        val wallet_type: Int? = null

        // 账号状态,正常:0,注销:1,封禁:2
        val status: Int? = null

        //mpc其中一个钱包私钥的加密密文
        val private_key: String? = null

        //用mpc三个独立密钥生成的字符串
        val mpc_result: String? = null
    }
}