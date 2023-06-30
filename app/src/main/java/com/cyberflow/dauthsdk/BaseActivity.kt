package com.cyberflow.dauthsdk

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            dispatchRelease()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dispatchRelease()
    }

    private var dispatched = false

    private fun dispatchRelease() {
        if (!dispatched) {
            dispatched = true
            onRelease()
        }
    }

    open fun onRelease() = Unit
}