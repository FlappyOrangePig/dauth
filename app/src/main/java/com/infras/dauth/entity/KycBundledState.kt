package com.infras.dauth.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KycBundledState(
    val isBound: Boolean,// 是否绑定邮箱、手机号
    val kycState: Int?,// 开⼾状态 0000：开⼾成功 0001：开⼾处理中 0002：开⼾失败
) : Parcelable