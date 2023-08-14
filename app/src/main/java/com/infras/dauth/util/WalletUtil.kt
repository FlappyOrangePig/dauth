package com.infras.dauth.util

import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

object WalletUtil {

    private const val TAG = "WalletUtil"

    fun getWalletAddress(msgHash: ByteArray, sd: Sign.SignatureData): String? {
        return try {
            // 这个方法有时候崩溃，比如返回的r是31位
            val signedPublicKey = Sign.signedMessageHashToKey(msgHash, sd)
            val address = Keys.getAddress(signedPublicKey)
            Numeric.prependHexPrefix(address)
        } catch (e: Exception) {
            LogUtil.e(TAG, e.stackTraceToString())
            null
        }
    }
}