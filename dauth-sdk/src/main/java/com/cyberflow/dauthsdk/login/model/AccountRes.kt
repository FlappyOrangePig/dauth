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

import com.cyberflow.dauthsdk.login.network.BaseResponse
import com.squareup.moshi.JsonClass


/**
 * 
 * @param account 用户账号
 * @param nickname 用户昵称
 * @param birthday 用户生日
 * @param sex 性别 0保密 1男 2女
 * @param email 邮箱
 * @param phone_area_code 电话区号
 * @param phone 电话号码
 * @param real_name 用户真实名称
 * @param identity 身份证
 * @param identity_Status 
 * @param head_img_url 用户图像地址
 * @param country 国家/地区
 * @param province 省份
 * @param city 城市
 * @param district 区
 * @param address 详细地址
 * @param user_type 注册来源
 * @param user_state 用户状态
 * @param create_time 注册时间
 */

@JsonClass(generateAdapter = true)
data class AccountRes(
    var data: Data? = null
) : BaseResponse() {
    @JsonClass(generateAdapter = true)
    class Data {
        /* 用户账号 */
        var account: String? = null

        /* 用户昵称 */
        var nickname: String? = null

        /* 用户生日 */
        var birthday: Int? = null

        /* 性别 0保密 1男 2女 */
        var sex: Int? = null

        /* 邮箱 */
        var email: String? = null

        /* 电话区号 */
        var phone_area_code: String? = null

        /* 电话号码 */
        var phone: String? = null

        /* 用户真实名称 */
        var real_name: String? = null

        /* 身份证 */
        var identity: String? = null

        var identity_Status: Int? = null

        /* 用户图像地址 */
        var head_img_url: String? = null

        /* 国家/地区 */
        var country: String? = null

        /* 省份 */
        var province: String? = null

        /* 城市 */
        var city: String? = null

        /* 区 */
        var district: String? = null

        /* 详细地址 */
        var address: String? = null

        /* 注册来源 */
        var user_type: Int? = null

        /* 用户状态 */
        var user_state: Int? = null

        /* 注册时间 */
        var create_time: Int? = null

        /* 是否设置密码 0-未设置 1-已设置 */
        var has_password: Int? = null

    }
}

