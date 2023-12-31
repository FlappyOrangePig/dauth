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

import com.infras.dauthsdk.api.annotation.DAuthAccountType


/**
 *
 * @param
 * @param account 用户账号,自定义账号时必填
 * @param user_type 账号类型:10邮箱注册,20钱包注册,30谷歌,40facebook,50苹果,60手机号,70自定义帐号,80一键注册,100Discord,110Twitter
 * @param password 密码
 * @param confirm_password 确认密码
 * @param nickname 昵称
 * @param birthday 生日
 * @param sex 性别 0保密 1男 2女
 * @param email 邮箱，当邮箱注册时必填
 * @param phone_area_code 手机区号，当手机号注册时必填
 * @param phone 手机号，当手机号注册时必填
 * @param real_name 用户真实名称
 * @param user_card_no 身份证号
 * @param identity_status 是否实名认证
 * @param head_img_url 用户图像地址
 * @param nationality 国家/地区
 * @param province 省份
 * @param city 城市
 * @param district 区
 * @param address 详细地址
 * @param verify_code 验证码
 * @param uuid 设备唯一标识
 * @param is_login 是否登录 0不登陆 1登录
 * @param sign 检验参数
 */

data class CreateAccountParam(
    /* 账号类型:10邮箱注册,20钱包注册,30谷歌,40facebook,50苹果,60手机号,70自定义帐号,80一键注册,100Discord,110Twitter */
    @DAuthAccountType val user_type: Int,

    /* 设备唯一标识 */
    val uuid: String,

    /* 检验参数 */
    val sign: String? = null,

    /* 是否登录 0不登陆 1登录 */
    val is_login: Int,

    /* 密码 */
    val password: String? = null,
    /* 确认密码 */
    val confirm_password: String? = null,
    /* 昵称 */
    val nickname: String? = null,
    /* 生日 */
    val birthday: Int? = null,
    /* 性别 0保密 1男 2女 */
    val sex: Int? = null,
    /* 是否实名认证 */
    val identity_status: Int? = null,

    val commonHeader: CommonHeader? = null,
    /* 用户账号,自定义账号时必填 */
    val account: String? = null,
    /* 邮箱，当邮箱注册时必填 */
    val email: String? = null,
    /* 手机区号，当手机号注册时必填 */
    val phone_area_code: String? = null,
    /* 手机号，当手机号注册时必填 */
    val phone: String? = null,
    /* 用户真实名称 */
    val real_name: String? = null,
    /* 身份证号 */
    val user_card_no: String? = null,
    /* 用户图像地址 */
    val head_img_url: String? = null,
    /* 国家/地区 */
    val nationality: String? = null,
    /* 省份 */
    val province: String? = null,
    /* 城市 */
    val city: String? = null,
    /* 区 */
    val district: String? = null,
    /* 详细地址 */
    val address: String? = null,
    /* 验证码 */
    val verify_code: String? = null
)

