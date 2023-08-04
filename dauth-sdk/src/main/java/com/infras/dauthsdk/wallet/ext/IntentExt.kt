package com.infras.dauthsdk.wallet.ext

import android.content.Intent
import android.os.Build
import android.os.Bundle

inline fun <reified T : Any> Intent?.getParcelableExtraCompat(name: String): T? =
    if (this == null) {
        null
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, T::class.java)
    } else {
        this.getParcelableExtra(name)
    }

inline fun <reified T : Any> Bundle?.getParcelableExtraCompat(name: String): T? =
    if (this == null) {
        null
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelable(name, T::class.java)
    } else {
        this.getParcelable(name)
    }