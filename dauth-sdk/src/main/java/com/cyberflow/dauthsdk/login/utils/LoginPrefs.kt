package com.cyberflow.dauthsdk.login.utils

import android.content.Context
import android.content.SharedPreferences

private const val LOGIN_STATE_INFO = "LOGIN_STATE_INFO"
private const val ACCESS_TOKEN = "access_token"
private const val AUTH_ID = "auth_id"
private const val DID_TOKEN = "did_token"

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

    fun setDidToken(didToken: String) {
        getPrefs().edit().putString(DID_TOKEN, didToken).apply()
    }

    fun getDidToken(): String {
        return getPrefs().getString(DID_TOKEN, null).orEmpty()
    }

    fun clearLoginStateInfo() {
        val prefsEditor = getPrefs().edit()
        prefsEditor.clear()
        prefsEditor.apply()
    }

}