package com.cyberflow.dauthsdk.login.viewmodel

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

class WalletWebViewViewModel : ViewModel() {

    val walletInfo = MutableLiveData<String>()

    fun initWebViewClient(webView: WebView) {
        initWebViewSettings(webView)
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                DAuthLogger.i( "onPageStarted, view:$view, url:$url")
            }

            override fun onPageFinished(view: WebView, url: String) {
                DAuthLogger.i(  "onPageFinished, view:$view, url:$url")
            }

            override fun onReceivedError(
                webView: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                DAuthLogger.e( "onReceivedError: " + errorCode
                        + ", description: " + description
                        + ", url: " + failingUrl
                )
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                DAuthLogger.e("shouldOverrideUrlLoading:  $url")
                return if (url != null && url.startsWith("wc:")) {
                    view.context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    )
                    true
                } else {
                    false
                }
            }

            override fun onReceivedSslError(
                p0: WebView?,
                p1: com.tencent.smtt.export.external.interfaces.SslErrorHandler?,
                p2: com.tencent.smtt.export.external.interfaces.SslError?
            ) {
                p1?.proceed()
                DAuthLogger.i( "sslError:$p2")
            }

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings(webView: WebView) {
        val webSetting: WebSettings = webView.settings
        webSetting.javaScriptEnabled = true
        webSetting.setSupportZoom(true)
        webSetting.databaseEnabled = true
        webSetting.domStorageEnabled = true
    }

}