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


/**
 * 
 * @param access_token
 * @param authid 用户id
 */

data class QueryByAuthIdParam (
    /* 用户id */
     val authid: String,
     var access_token: String
) {

}
