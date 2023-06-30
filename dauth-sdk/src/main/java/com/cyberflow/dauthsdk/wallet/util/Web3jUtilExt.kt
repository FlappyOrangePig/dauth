package com.cyberflow.dauthsdk.wallet.util

import org.web3j.crypto.Hash
import org.web3j.utils.Numeric

internal fun String.prependHexPrefix() = Numeric.prependHexPrefix(this)

internal fun String.cleanHexPrefix() = Numeric.cleanHexPrefix(this)

internal fun ByteArray?.toHexString() = this?.let { Numeric.toHexString(this) }.orEmpty()

internal fun String.hexStringToByteArray() = Numeric.hexStringToByteArray(this)

internal fun String.sha3() = Hash.sha3(this)

internal fun String.sha3String() = Hash.sha3String(this)

internal fun ByteArray.sha3() = Hash.sha3(this)


