package com.infras.dauthsdk.wallet.ext

import com.infras.dauthsdk.wallet.util.sha3

fun String.digest(): String = this.sha3()