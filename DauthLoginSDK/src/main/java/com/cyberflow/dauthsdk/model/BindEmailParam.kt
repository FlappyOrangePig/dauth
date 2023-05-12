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

import com.cyberflow.dauthsdk.model.AccessTokenInHeader

/**
 * 
 * @param  
 * @param openudid 用户id
 * @param account 邮箱
 * @param verify_code 验证码
 * @param sign 检验参数
 */
data class BindEmailParam (
    /* 用户id */
    val openudid: kotlin.String,
    /* 邮箱 */
    val account: kotlin.String,
    /* 验证码 */
    val verify_code: kotlin.Int,
    /* 检验参数 */
    val sign: kotlin.String,
    val accessTokenInHeader : AccessTokenInHeader? = null
) {

}

