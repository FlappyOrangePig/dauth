package com.infras.dauth.app

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    protected val logTag: String = this::class.java.simpleName

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