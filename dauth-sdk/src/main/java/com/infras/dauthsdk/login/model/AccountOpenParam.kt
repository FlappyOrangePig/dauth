package com.infras.dauthsdk.login.model

class AccountOpenParam(
    val full_name: String?,
    val first_name: String?,
    val last_name: String?,
    val middle_name: String?,
    val id_type: Int, // 证件类型 1：护照,2：驾照,4：⾝份证
    val id_num: String?,
    val id_front_img: String,
    val id_back_img: String,
    val issuing_country: String,// 发⾏国家（ISO标准国家码）
) : IAccessTokenRequest, IAuthorizationRequest