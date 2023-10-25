package com.infras.dauth.ext

import android.util.Patterns
import java.util.regex.Pattern

fun String?.isMail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this.orEmpty()).matches()
}

fun String?.isPhone(): Boolean {
    return Patterns.PHONE.matcher(this.orEmpty()).matches()
}

fun String?.isVerifyCode(): Boolean {
    return Pattern.compile("^\\d{4}\$").matcher(this.orEmpty()).matches()
}

fun String?.isAreaCode(): Boolean {
    return Pattern.compile("^\\d{2}\$").matcher(this.orEmpty()).matches()
}