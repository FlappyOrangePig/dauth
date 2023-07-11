package com.cyberflow.dauthsdk.wallet.ext

import com.cyberflow.dauthsdk.wallet.util.sha3

fun String.digest(): String = this.sha3()