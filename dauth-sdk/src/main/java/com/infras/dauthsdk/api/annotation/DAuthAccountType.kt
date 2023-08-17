package com.infras.dauthsdk.api.annotation

import androidx.annotation.IntDef

/**
 * D auth account type
 * 10邮箱注册,20钱包注册,30谷歌,40facebook,50苹果,60手机号,70自定义帐号,80一键注册,100Discord,110Twitter
 *
 * @constructor Create empty D auth account type
 */
@Retention(value = AnnotationRetention.SOURCE)
@IntDef(
    DAuthAccountType.ACCOUNT_TYPE_OF_EMAIL,
    DAuthAccountType.ACCOUNT_TYPE_OF_MOBILE,
    DAuthAccountType.ACCOUNT_TYPE_OF_OWN,
)
annotation class DAuthAccountType {
    companion object {
        const val ACCOUNT_TYPE_OF_EMAIL = 10
        const val ACCOUNT_TYPE_OF_MOBILE = 60
        const val ACCOUNT_TYPE_OF_OWN = 70
    }
}