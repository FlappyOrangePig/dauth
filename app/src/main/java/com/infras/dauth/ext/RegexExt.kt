package com.infras.dauth.ext

import android.util.Patterns

fun String?.isMail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this.orEmpty()).matches()
}

fun String?.isPhone(): Boolean {
    return Patterns.PHONE.matcher(this.orEmpty()).matches()
}