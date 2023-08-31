package com.infras.dauth.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infras.dauth.util.SystemUIUtil

open class BaseActivity : AppCompatActivity() {

    protected val logTag: String = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSystemUI()
    }

    open fun showSystemUI() {
        SystemUIUtil.show(window, SystemUIUtil.ThemeDrawByDeveloper())
    }

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