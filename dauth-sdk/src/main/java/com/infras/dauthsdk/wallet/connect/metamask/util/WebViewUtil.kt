package com.infras.dauthsdk.wallet.connect.metamask.util

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import com.infras.dauthsdk.login.utils.DAuthLogger

private const val TAG = "WebViewUtil"

internal class MetaMaskWebViewClient(
    private val on1stPageFinished: ((String) -> Unit)?
) : WebViewClient() {

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        DAuthLogger.d("onPageStarted: url=$url", TAG)
    }

    override fun onPageFinished(view: WebView, url: String) {
        DAuthLogger.d("onPageFinished: url=$url", TAG)
        on1stPageFinished?.invoke(url)
    }

    @Deprecated("see super class")
    override fun onReceivedError(
        webView: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        DAuthLogger.e(
            "onReceivedError: errorCode=$errorCode, description=$description, failingUrl=$failingUrl",
            TAG
        )
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        DAuthLogger.e("onReceivedError: request=$request, error=$error", TAG)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val result = if (url.startsWith("https://metamask.app.link/")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            view.context.startActivity(intent)
            true
        } else {
            super.shouldOverrideUrlLoading(view, request)
        }
        DAuthLogger.d("shouldOverrideUrlLoading url=$url result=$result")
        return result
    }

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builder.setMessage("SSL ERROR!")
        builder.setPositiveButton(
            "continue"
        ) { _, _ -> handler.proceed() }
        builder.setNegativeButton(
            "cancel"
        ) { _, _ -> handler.cancel() }
        val dialog: AlertDialog = builder.create()
        dialog.show()
        DAuthLogger.i("onReceivedSslError:$error")
    }
}

internal object MetaMaskWebChromeClient : WebChromeClient()