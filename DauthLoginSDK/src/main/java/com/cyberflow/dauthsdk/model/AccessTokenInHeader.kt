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
package com.cyberflow.dauthsdk.model


/**
 * 
 * @param AccessToken 用户登录标识,调用凭证,接口header传入,有效期短
 */
data class AccessTokenInHeader (
    /* 用户登录标识,调用凭证,接口header传入,有效期短 */
    val AccessToken: kotlin.String? = null
) {

}

