package com.cyberflow.dauthsdk.login.utils

import android.content.Context
import android.content.SharedPreferences

private const val LOGIN_STATE_INFO = "LOGIN_STATE_INFO"
private const val LOGIN_TOKEN = "TOKEN"

internal class LoginPrefs(private val context: Context) {

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences(LOGIN_STATE_INFO, Context.MODE_PRIVATE)
    }

    fun getToken(): String {
        return getPrefs().getString(LOGIN_TOKEN, null).orEmpty()
    }

    fun setToken(walletFileName: String) {
        getPrefs().edit().putString(LOGIN_TOKEN, walletFileName).apply()
    }

}