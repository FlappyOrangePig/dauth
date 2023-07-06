package com.cyberflow.dauthsdk.wallet.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.cyberflow.dauthsdk.wallet.ext.app
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager.Companion.STATE_INIT
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager.Companion.STATE_OK

private const val FILE_WALLET_INFO = "FILE_WALLET_INFO_V2"
private const val KEY_EOA_ADDRESS = "KEY_EOA_ADDRESS"
private const val KEY_AA_ADDRESS = "KEY_AA_ADDRESS"
private const val KEY_WALLET_STATE = "KEY_WALLET_STATE"

object WalletPrefsV2 {

    private val context get() = app()

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
    fun setAddresses(eoa: String, aa: String): Boolean {
        val et = getPrefs().edit()
        et.putString(KEY_EOA_ADDRESS, eoa)
        et.putString(KEY_AA_ADDRESS, aa)
        et.putInt(KEY_WALLET_STATE, STATE_OK)
        return et.commit()
    }

    fun getWalletState(): Int {
        return getPrefs().getInt(KEY_WALLET_STATE, STATE_INIT)
    }

    @SuppressLint("ApplySharedPref")
    fun setWalletState(state: Int): Boolean {
        val et = getPrefs().edit()
        et.putInt(KEY_WALLET_STATE, state)
        return et.commit()
    }
}