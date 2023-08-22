package com.infras.dauth.util

import android.content.Context
import com.infras.dauth.MyApplication

private const val FILE = "DemoPrefs.xml"
private const val KEY_LAST_EMAIL = "KEY_LAST_EMAIL"
private const val KEY_LAST_LOGIN_TYPE = "KEY_LAST_LOGIN_TYPE"

object DemoPrefs {

    private val pref get() = MyApplication.app.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun setLastEmail(email: String) {
        val et = pref.edit()
        et.putString(KEY_LAST_EMAIL, email)
        et.apply()
    }

    fun getLastEmail(): String {
        return pref.getString(KEY_LAST_EMAIL, null).orEmpty()
    }

    fun setLastLoginType(type: Int) {
        val et = pref.edit()
        et.putInt(KEY_LAST_LOGIN_TYPE, type)
        et.apply()
    }

    fun getLastLoginType(): Int {
        return pref.getInt(KEY_LAST_LOGIN_TYPE, 0)
    }
}