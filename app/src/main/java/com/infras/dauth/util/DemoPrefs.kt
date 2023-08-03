package com.infras.dauth.util

import android.content.Context
import com.infras.dauth.MyApplication

private const val FILE = "DemoPrefs.xml"
private const val KEY_LAST_EMAIL = "KEY_LAST_EMAIL"

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
}