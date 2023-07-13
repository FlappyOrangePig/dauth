package com.cyberflow.dauthsdk.wallet.ext

import android.app.Application
import com.cyberflow.dauthsdk.api.DAuthSDK

internal fun safeApp() = DAuthSDK.impl.context.applicationContext as? Application
internal fun app() = safeApp()!!