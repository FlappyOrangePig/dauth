package com.cyberflow.dauthsdk.api.entity

object ErrorCode {
     const val WRONG_REQUEST_METHOD = 1000000                         // 请求方式错误
     const val WRONG_PARAMS = 1000001                                 // 参数错误
     const val WRONG_DATABASE = 1000002                               // 数据库错误
     const val WRONG_CACHE_SERVICE = 1000003                          // 缓存服务错误
     const val WRONG_SIGN_PARAMS = 1000004                            // sign参数校验错误
     const val ACCOUNT_IS_REGISTERED = 1000005                        // 用户已注册
     const val WRONG_PASSWORD_LENGTH = 1000006                        // 请输入8-16位包括大小写英文和数字的密码
     const val ACCESS_TOKEN_INVALID = 1000007                         // 会话已过期，请重新登录
     const val SERVICE_EXECUTION_ERROR = 1000008                      // 服务执行错误
     const val SERVICE_INTERNAL_ERROR = 1000009                       // 服务内部错误
     const val ILLEGAL_APP = 10000010                                 // 非法APP
     const val PASSWORD_IS_DIFFERENT = 10000011                       // 两次输入的密码不同
     const val ACCOUNT_OR_PASSWORD_INCORRECT = 10000012               // 账号或密码不正确，今日还剩{desc}次机会
     const val ENTER_STANDARD_ACCOUNT = 10000013                      // 请输入规范的账号名
     const val ENTER_STANDARD_EMAIL = 10000014                        // 请输入规范的邮箱
     const val VERIFY_CODE_INCORRECT = 10000015                       // 输入的验证码有误
     const val TOKEN_IS_INVALIDATE = 10000016                         // 会话已过期，请重新刷新
     const val INCORRECT_ACCESS_TOKEN = 10000017                      // 用户令牌非法
     const val DO_NOT_REFRESH_FREQUENTLY = 10000018                   // 请勿频繁刷新
     const val SEND_EMAIL_ERROR = 10000019                            // 发送邮件错误
     const val EMAIL_DO_NOT_EXIST = 10000020                          // 用户邮箱不存在
     const val ACCOUNT_NOT_EXIST_OR_REGISTERED = 10000021             // 账户不存在或未注册
     const val LOGIN_IN_PROGRESS = 10000022                           // 正在登录操作，请勿频繁操作
     const val USERS_CODE_IS_ILLEGAL = 10000023                       // 用户code非法
     const val ACCOUNT_CANT_EMPTY = 10000024                          // 用户账号不能为空
     const val ACCOUNT_EMAIL_CANT_EMPTY = 10000025                    // 用户邮箱不能为空
     const val USER_IS_NOT_LOGIN_OR_INVALID = 10000026                // 用户未登录或登录已失效
     const val APP_CLIENT_ID_ILLEGAL = 10000027                       // 应用ClientID非法
     const val VERIFICATION_PARAMS_INCOMPLETE = 10000028              // Token请求校验参数不全
     const val THIRD_PARTY_AUTHORIZATION_IS_EMPTY = 10000031          // 第三方授权参数为空
     const val VERIFY_CODE_INVALID = 10000032                         // 验证码已失效
     const val VERIFY_CODE_CANT_BE_NULL = 10000033                    // 验证码不能为空或长度不符
     const val CODE_IS_INVALID = 10000034                             // CODE已失效，请重新授权
     const val UNSUPPORTED_OPERATION_TYPE = 10000035                  // 未支持的操作类型
     const val ENTER_STANDARD_PHONE = 10000036                        // 请输入规范的手机号
     const val AA_WALLET_IS_NOT_CREATE = 200001                       // 用户未创建aa钱包
}