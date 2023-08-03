package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
class QueryWalletRes : BaseResponse() {
    var data: Data? = null
    @JsonClass(generateAdapter = true)
    class Data {
        var authid: String? = null

        var appid: Int? = null

        var address: String? = null

        //钱包类型,10-AA钱包，11-EOA钱包
        var wallet_type: Int? = null

        // 账号状态,正常:0,注销:1,封禁:2
        var status: Int? = null

    }
}