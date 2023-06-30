package com.cyberflow.dauthsdk.wallet.util

import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign

object SignUtil {

    fun getMessageHash(message: ByteArray, prefixed: Boolean): ByteArray = if (prefixed) {
        Sign.getEthereumMessageHash(message)
    } else {
        message.sha3()
    }

    fun signMessage(messageHash: ByteArray, keyPair: ECKeyPair): Sign.SignatureData =
        Sign.signMessage(messageHash, keyPair, false)
}