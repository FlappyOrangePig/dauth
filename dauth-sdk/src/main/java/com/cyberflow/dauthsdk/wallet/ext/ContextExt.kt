package com.cyberflow.dauthsdk.wallet.ext

import android.app.Application
import com.cyberflow.dauthsdk.api.DAuthSDK

internal fun app() = DAuthSDK.impl.context.applicationContext as Application