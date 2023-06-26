package com.cyberflow.dauthsdk.wallet.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.cyberflow.dauthsdk.api.DAuthSDK

private const val FILE_WALLET_INFO = "FILE_WALLET_INFO_V2"
private const val KEY_EOA_ADDRESS = "KEY_EOA_ADDRESS"
private const val KEY_AA_ADDRESS = "KEY_AA_ADDRESS"

class WalletPrefsV2 {

    private val context get() = DAuthSDK.impl.context

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences(FILE_WALLET_INFO, Context.MODE_PRIVATE)
    }

    fun getEoaAddress(): String {
        return getPrefs().getString(KEY_EOA_ADDRESS, null).orEmpty()
    }

    fun getAaAddress(): String {
        return getPrefs().getString(KEY_AA_ADDRESS, null).orEmpty()
    }

    @SuppressLint("ApplySharedPref")
    fun setAddresses(eoa: String, aa: String) {
        val et = getPrefs().edit()
        et.putString(KEY_EOA_ADDRESS, eoa)
        et.putString(KEY_AA_ADDRESS, aa)
        et.commit()
    }
}