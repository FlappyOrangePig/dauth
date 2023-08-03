package com.infras.dauthsdk.login.impl

import android.annotation.SuppressLint
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("ViewConstructor")
class TwitterWebView @JvmOverloads constructor(
    private val host: WebView,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(
    host.context,
    attrs,
    defStyleAttr
) {
    override fun getSettings(): WebSettings {
        return host.settings
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun loadUrl(url: String) {
        host.settings.javaScriptEnabled = true
        host.loadUrl(url)
    }

    override fun stopLoading() {
        host.stopLoading()
    }

    override fun setVerticalScrollBarEnabled(verticalScrollBarEnabled: Boolean) {
        host.isVerticalScrollBarEnabled = verticalScrollBarEnabled
    }

    override fun setHorizontalScrollBarEnabled(horizontalScrollBarEnabled: Boolean) {
        host.isHorizontalScrollBarEnabled = horizontalScrollBarEnabled
    }

    override fun setWebViewClient(client: WebViewClient) {
        host.webViewClient = client
    }

    override fun setVisibility(visibility: Int) {
        host.visibility = visibility
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        host.webChromeClient = client
    }
}