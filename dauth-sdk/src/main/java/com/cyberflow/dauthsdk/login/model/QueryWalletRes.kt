package com.cyberflow.dauthsdk.login.model

import com.cyberflow.dauthsdk.login.network.BaseResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
class QueryWalletRes : BaseResponse(){
    var data: @Contextual Data? = null
    @Serializable
    class Data {
        val authid: String? = null

        val appid: Int? = null

        val address: String? = null

        //钱包类型,10-AA钱包，11-EOA钱包
        val wallet_type: Int? = null

        // 账号状态,正常:0,注销:1,封禁:2
        val status: Int? = null

    }
}