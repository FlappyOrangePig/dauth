package com.cyberflow.dauthsdk.wallet.util

import android.content.Context
import android.content.SharedPreferences

private const val FILE_WALLET_INFO = "FILE_WALLET_INFO"
private const val KEY_WALLET_FILE_NAME = "KEY_WALLET_FILE_NAME"
private const val KEY_WALLET_ADDRESS = "KEY_WALLET_ADDRESS"

internal class WalletPrefs(private val context: Context) {

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences(FILE_WALLET_INFO, Context.MODE_PRIVATE)
    }

    fun getWalletFileName(): String {
        return getPrefs().getString(KEY_WALLET_FILE_NAME, null).orEmpty()
    }

    fun setWalletFileName(walletFileName: String) {
        getPrefs().edit().putString(KEY_WALLET_FILE_NAME, walletFileName).apply()
    }

    fun getWalletAddress(): String {
        return getPrefs().getString(KEY_WALLET_ADDRESS, null).orEmpty()
    }

    fun setWalletAddress(walletAddress: String) {
        getPrefs().edit().putString(KEY_WALLET_ADDRESS, walletAddress).apply()
    }
}