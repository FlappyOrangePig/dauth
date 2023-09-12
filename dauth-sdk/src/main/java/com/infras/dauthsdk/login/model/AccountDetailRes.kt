package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse

class AccountDetailRes(
    val data: Data?,
) : BaseResponse() {
    class Data(
        val full_name: String,
        val first_name: String,
        val last_name: String,
        val middle_name: String,
        val issuing_country: String, // 发⾏国家（ISO标准国家码）
        val id_type: Long, // 证件类型 1：护照,2：驾照,4：⾝份证
        val id_num: String, // 证件号码
        val id_front_img: String,
        val id_back_img: String,
        val result: Long,// 开⼾状态 0000：开⼾成功 0001：开⼾处理中 0002：开⼾失败
    )
}