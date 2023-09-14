package com.infras.dauthsdk.login.model

/**
 * Bind phone param
 *
 * @property phone
 * @property phone_area_code
 * @property verify_code
 * @constructor Create empty Bind phone param
 */
data class BindPhoneParam(
    val phone: String,
    val phone_area_code: String,
    val verify_code: String,
)

