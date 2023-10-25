package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.Json

class QueryWithdrawConfRes(
    val data: Data?,
) : BaseResponse() {

    class Data(
        val list: List<WithdrawConf>
    )

    class WithdrawConf(
        @Json(name = "chain_coin_id")
        val chainCoinId: Long,
        @Json(name = "chain_coin_name")
        val chainCoinName: String,
        @Json(name = "chain_short_name")
        val chainShortName: String,
        @Json(name = "withdraw_fee")
        val withdrawFee: String,
        @Json(name = "min_withdraw_amount")
        val minWithdrawAmount: String,
    )
}