package com.infras.dauthsdk.wallet.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.impl.manager.WalletManager.Companion.STATE_INIT
import com.infras.dauthsdk.wallet.impl.manager.WalletManager.Companion.STATE_OK

private const val KEY_EOA_ADDRESS = "KEY_EOA_ADDRESS"
private const val KEY_AA_ADDRESS = "KEY_AA_ADDRESS"
private const val KEY_WALLET_STATE = "KEY_WALLET_STATE"

internal class WalletPrefsV2 internal constructor(
    private val context: Context,
    private val userId: String
) {
    companion object {
        private const val TAG = "WalletPrefsV2"
        private const val DEBUG = true
    }

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences("walletV2-$userId", Context.MODE_PRIVATE)
    }

    fun getEoaAddress(): String {
        return getPrefs().getString(KEY_EOA_ADDRESS, null).orEmpty().also {
            log("getEoaAddress:$it")
        }
    }

    fun getAaAddress(): String {
        return getPrefs().getString(KEY_AA_ADDRESS, null).orEmpty().also {
            log("getAaAddress:$it")
        }
    }

    @SuppressLint("ApplySharedPref")
    fun setAddresses(eoa: String, aa: String): Boolean {
        val et = getPrefs().edit()
        et.putString(KEY_EOA_ADDRESS, eoa)
        et.putString(KEY_AA_ADDRESS, aa)
        et.putInt(KEY_WALLET_STATE, STATE_OK)
        return et.commit().also {
            log("setAddresses:$eoa/$aa:$it")
        }
    }

    fun getWalletState(): Int {
        return getPrefs().getInt(KEY_WALLET_STATE, STATE_INIT).also {
            log("getWalletState:$it")
        }
    }

    @SuppressLint("ApplySharedPref")
    fun setWalletState(state: Int): Boolean {
        val et = getPrefs().edit()
        et.putInt(KEY_WALLET_STATE, state)
        return et.commit().also {
            log("setWalletState:$state:$it")
        }
    }

    @SuppressLint("ApplySharedPref")
    fun clear() {
        getPrefs().edit().clear().commit().also {
            log("clear:$it")
        }
    }

    private fun log(log: String) {
        if (DEBUG) {
            DAuthLogger.v("[$userId]$log", TAG)
        }
    }
}