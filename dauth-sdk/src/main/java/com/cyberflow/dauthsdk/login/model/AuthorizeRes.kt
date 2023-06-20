/**
* dauthwallet
* 账号注册，登录，授权接口
*
* OpenAPI spec version: v1.0
* 
*
* NOTE: This class is auto generated by the swagger code generator program.
* https://github.com/swagger-api/swagger-codegen.git
* Do not edit the class manually.
*/
package com.cyberflow.dauthsdk.login.model

import com.squareup.moshi.JsonClass

/**
 * 
 * @param code 临时code
 */
@JsonClass(generateAdapter = true)
data class AuthorizeRes (
    /* 临时code */
    val data: Data
) {
    @JsonClass(generateAdapter = true)
    class Data {
        var code: String? = null
    }
}

