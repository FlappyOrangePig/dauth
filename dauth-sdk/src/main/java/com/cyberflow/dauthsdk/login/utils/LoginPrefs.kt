package com.cyberflow.dauthsdk.login.utils

import android.content.Context
import android.content.SharedPreferences
import com.cyberflow.dauthsdk.wallet.ext.app

private const val LOGIN_STATE_INFO = "LOGIN_STATE_INFO"
private const val ACCESS_TOKEN = "access_token"
private const val AUTH_ID = "auth_id"
private const val USER_ID = "user_id"
private const val DID_TOKEN = "did_token"
private const val REFRESH_TOKEN = "refresh_token"
private const val EXPIRE_TIME = "expire_in"
private const val USER_TYPE = "user_type"
private const val DEFAULT_USER_TYPE = 0

class LoginPrefs {

    private val context get() = app()
    private val defaultAsync = true

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

    fun getRefreshToken(): String {
        return getPrefs().getString(REFRESH_TOKEN, null).orEmpty()
    }

    fun setRefreshToken(refreshToken: String) {
        getPrefs().edit().putString(REFRESH_TOKEN, refreshToken).apply()
    }

    fun setExpireTime(expireTime: Long) {
        getPrefs().edit().putLong(EXPIRE_TIME, expireTime).apply()
    }

    fun getExpireTime(): Long {
        return getPrefs().getLong(EXPIRE_TIME, 0L)
    }

    fun setUserType(type: Int) {
        getPrefs().edit().putInt(USER_TYPE, type).apply()
    }

    fun getUserType() : Int {
        return getPrefs().getInt(USER_TYPE, DEFAULT_USER_TYPE)
    }

    fun putLoginInfo(
        accessToken: String? = null,
        authId: String? = null,
        userId: String? = null,
        refreshToken: String? = null,
        expireTime: Long?  = null,
        user_type: Int? = null,
        async: Boolean = false
    ) {
        val values = ArrayList<Pair<String, Any>>()
        userId?.let {
            values.add(USER_ID to it)
        }
        accessToken?.let {
            values.add(ACCESS_TOKEN to it)
        }
        authId?.let {
            values.add(AUTH_ID to it)
        }
        refreshToken?.let {
            values.add(REFRESH_TOKEN to it)
        }
        expireTime?.let {
            values.add(EXPIRE_TIME to it)
        }
        user_type?.let {
            values.add(USER_TYPE to it)
        }

        put(values, async)
    }

    fun put(kvs: Collection<Pair<String, Any>>, async: Boolean = defaultAsync) {
        modify(async) { editor ->
            kvs.forEach { pair ->
                putX(editor, pair.first, pair.second)
            }
        }
    }
    private fun putX(editor: SharedPreferences.Editor, key: String, value: Any) {
        when (value) {
            is Int -> {
                editor.putInt(key, value)
            }
            is String -> {
                editor.putString(key, value)
            }
            is Long -> {
                editor.putLong(key, value)
            }
            is Boolean -> {
                editor.putBoolean(key, value)
            }
            is Float -> {
                editor.putFloat(key, value)
            }
        }
    }

    protected fun modify(async: Boolean = true, block: (editor: SharedPreferences.Editor) -> Unit) {
        getPrefs().edit().apply {
            block.invoke(this)
            if (async) {
                apply()
            } else {
                commit()
            }
        }
    }


    fun clearLoginStateInfo() {
        val prefsEditor = getPrefs().edit()
        prefsEditor.clear()
        prefsEditor.apply()
    }

}