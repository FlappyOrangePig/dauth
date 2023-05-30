package com.cyberflow.dauthsdk.login.utils

import android.util.Patterns

class ValidatorUtil {
    fun isEmail(email: CharSequence?) = if (email.isNullOrEmpty()) {
        false
    } else {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}