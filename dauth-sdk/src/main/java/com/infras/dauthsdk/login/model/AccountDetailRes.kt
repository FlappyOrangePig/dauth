package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse

class AccountDetailRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        val detail: Detail?,
        val is_phone_bound: Int,
        val is_email_bound: Int,
    )

    class Detail(
        val full_name: String,
        val first_name: String,
        val last_name: String,
        val middle_name: String,
        val issuing_country: String, // 发⾏国家（ISO标准国家码）
        val id_type: String,
        val id_num: String, // 证件号码
        val id_front_img: String,
        val id_back_img: String,
        val state: Int,// 开⼾状态 0-未开户 1：开⼾成功 2：开⼾处理中 3：开⼾失败
    )
}