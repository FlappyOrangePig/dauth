package com.infras.dauth.ext

import android.content.Context
import com.infras.dauth.ui.wallet.HomeActivity

fun Context.launchMainPage() {
    //MainActivity.launch(this)
    HomeActivity.launch(this)
}