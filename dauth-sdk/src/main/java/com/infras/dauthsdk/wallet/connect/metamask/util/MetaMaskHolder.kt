package com.infras.dauthsdk.wallet.connect.metamask.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import com.infras.dauthsdk.wallet.connect.metamask.widget.MetaMaskWebView
import com.infras.dauthsdk.wallet.ext.app
import com.infras.dauthsdk.wallet.impl.manager.Managers

@SuppressLint("StaticFieldLeak")
internal object MetaMaskH5Holder {
    @Volatile
    private var webView: MetaMaskWebView? = null

    fun getGlobalWebView(): MetaMaskWebView {
        val r = webView ?: synchronized(this) {
            webView ?: createWebView(Managers.context).also {
                this.webView = it
            }
        }
        (r.parent as? ViewGroup)?.removeView(r)
        return r
    }

    private fun createWebView(context: Context): MetaMaskWebView {
        return MetaMaskWebView(context)
    }
}
