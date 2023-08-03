package com.infras.dauthsdk.api.entity

object ResponseCode {
    const val RESPONSE_CORRECT_CODE = 0                              // 请求成功
    const val WRONG_REQUEST_METHOD = 100_0000                        // 请求方式错误
    const val WRONG_PARAMS = 100_0001                                // 参数错误
    const val WRONG_DATABASE = 100_0002                              // 数据库错误
    const val WRONG_CACHE_SERVICE = 100_0003                         // 缓存服务错误
    const val WRONG_SIGN_PARAMS = 100_0004                           // sign参数校验错误
    const val ACCOUNT_IS_REGISTERED = 100_0005                       // 用户已注册
    const val WRONG_PASSWORD_LENGTH = 100_0006                       // 请输入8-16位包括大小写英文和数字的密码
    const val ACCESS_TOKEN_INVALID = 100_0007                        // 会话已过期，请重新登录
    const val SERVICE_EXECUTION_ERROR = 100_0008                     // 服务执行错误
    const val SERVICE_INTERNAL_ERROR = 100_0009                      // 服务内部错误
    const val ILLEGAL_APP = 100_0010                                 // 非法APP
    const val PASSWORD_IS_DIFFERENT = 100_0011                       // 两次输入的密码不同
    const val ACCOUNT_OR_PASSWORD_INCORRECT = 100_0012               // 账号或密码不正确，今日还剩{desc}次机会
    const val ENTER_STANDARD_ACCOUNT = 100_0013                      // 请输入规范的账号名
    const val ENTER_STANDARD_EMAIL = 100_0014                        // 请输入规范的邮箱
    const val VERIFY_CODE_INCORRECT = 100_0015                       // 输入的验证码有误
    const val TOKEN_IS_INVALIDATE = 100_0016                         // 会话已过期，请重新刷新
    const val INCORRECT_ACCESS_TOKEN = 100_0017                      // 用户令牌非法
    const val DO_NOT_REFRESH_FREQUENTLY = 100_0018                   // 请勿频繁刷新
    const val SEND_EMAIL_ERROR = 100_0019                            // 发送邮件错误
    const val EMAIL_DO_NOT_EXIST = 100_0020                          // 用户邮箱不存在
    const val ACCOUNT_NOT_EXIST_OR_REGISTERED = 100_0021             // 账户不存在或未注册
    const val LOGIN_IN_PROGRESS = 100_0022                           // 正在登录操作，请勿频繁操作
    const val USERS_CODE_IS_ILLEGAL = 100_0023                       // 用户code非法
    const val ACCOUNT_CANT_EMPTY = 100_0024                          // 用户账号不能为空
    const val ACCOUNT_EMAIL_CANT_EMPTY = 100_0025                    // 用户邮箱不能为空
    const val USER_IS_NOT_LOGIN_OR_INVALID = 100_0026                // 用户未登录或登录已失效
    const val APP_CLIENT_ID_ILLEGAL = 100_0027                       // 应用ClientID非法
    const val VERIFICATION_PARAMS_INCOMPLETE = 100_0028              // Token请求校验参数不全
    const val THIRD_PARTY_AUTHORIZATION_IS_EMPTY = 100_0031          // 第三方授权参数为空
    const val VERIFY_CODE_INVALID = 100_0032                         // 验证码已失效
    const val VERIFY_CODE_CANT_BE_NULL = 100_0033                    // 验证码不能为空或长度不符
    const val CODE_IS_INVALID = 100_0034                             // CODE已失效，请重新授权
    const val UNSUPPORTED_OPERATION_TYPE = 100_0035                  // 未支持的操作类型
    const val ENTER_STANDARD_PHONE = 100_0036                        // 请输入规范的手机号
    const val VERIFY_CODE_OR_PASSWORD_EMPTY_ERROR = 100_0037         // 验证码或密码不能为空
    const val BINDING_EMAIL_EXIST_ERROR = 100_0038                      // 该邮箱已绑定
    const val ACCOUNT_NOT_AUTHED_ERROR = 100_0039                       // 账户未授权
    const val PHONE_EMPTY_ERROR = 100_0040                             // 用户手机区号或手机号不能为空
    const val BINDING_PHONE_EXIST_ERROR = 100_0041                      // 该手机号已绑定
    const val PARSE_EMAIL_ERROR = 100_0042                             // 解析邮件模版错误

    fun isLoggedOut(code: Int) = when (code) {
        USER_IS_NOT_LOGIN_OR_INVALID,
        ACCESS_TOKEN_INVALID,
        INCORRECT_ACCESS_TOKEN -> true

        else -> false
    }
}