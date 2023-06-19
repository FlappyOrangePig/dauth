package com.cyberflow.dauthsdk.login.viewmodel

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import com.cyberflow.dauthsdk.login.utils.DAuthLogger


class WalletWebViewViewModel : ViewModel() {

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

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
                DAuthLogger.i( "sslError:$error")
            }

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings(webView: WebView) {
        val webSetting: WebSettings = webView.settings
        webSetting.javaScriptEnabled = true
        webSetting.setSupportZoom(true)
        webSetting.databaseEnabled = true
        webSetting.domStorageEnabled = false
    }

}