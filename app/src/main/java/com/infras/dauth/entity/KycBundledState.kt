package com.infras.dauth.entity

import android.os.Parcelable
import com.infras.dauthsdk.login.model.AccountDetailRes
import kotlinx.parcelize.Parcelize

/**
 * Kyc bundled state
 *
 * @property isBound 是否绑定邮箱、手机号
 * @property kycState 参考[AccountDetailRes.Detail.state]
 * @constructor Create empty Kyc bundled state
 */
@Parcelize
data class KycBundledState(
    val isBound: Boolean,
    val kycState: Int?,
) : Parcelable