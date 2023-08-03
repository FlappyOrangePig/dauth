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
package com.infras.dauthsdk.login.model

import com.infras.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.JsonClass


/**
 * 
 * @param SessionID 登录凭证
 */

@JsonClass(generateAdapter = true)
data class CreateAccountRes(val data: Data) : BaseResponse() {
    @JsonClass(generateAdapter = true)
    class Data {
        /* 登录凭证 */
        var SessionID: String? = null
    }

}
