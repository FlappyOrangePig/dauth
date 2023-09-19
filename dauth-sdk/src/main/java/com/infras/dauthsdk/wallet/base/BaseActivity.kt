package com.infras.dauthsdk.wallet.base

import androidx.appcompat.app.AppCompatActivity

internal abstract class BaseActivity : AppCompatActivity() {
    protected val logTag: String = javaClass.simpleName
}