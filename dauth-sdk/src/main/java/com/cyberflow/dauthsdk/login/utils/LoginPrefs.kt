package com.cyberflow.dauthsdk.login.utils

import android.content.Context
import android.content.SharedPreferences
import com.cyberflow.dauthsdk.login.DAuthSDK

private const val LOGIN_STATE_INFO = "LOGIN_STATE_INFO"
private const val ACCESS_TOKEN = "access_token"
private const val AUTH_ID = "auth_id"

internal class LoginPrefs(private val context: Context) {

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences(LOGIN_STATE_INFO, Context.MODE_PRIVATE)
    }

    fun getAccessToken(): String {
        return getPrefs().getString(ACCESS_TOKEN, null).orEmpty()
    }

    fun setAccessToken(accessToken: String) {
        getPrefs().edit().putString(ACCESS_TOKEN, accessToken).apply()
    }

    fun getAuthId(): String {
        return getPrefs().getString(AUTH_ID, null).orEmpty()
    }

    fun setAuthID(authId: String) {
        getPrefs().edit().putString(AUTH_ID, authId).apply()
    }

}