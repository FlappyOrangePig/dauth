package com.infras.dauthsdk.wallet.connect.metamask.util

import android.webkit.WebView
import com.infras.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class JsInvoker(private val webViewHolder: () -> WebView) {

    companion object {
        private const val TAG = "JsInvoker"
    }

    /**
     * 执行js，例如：
     * "javascript:window.setPersonalSignMessage(`我擦你大爷`);"
     *
     * @param js
     */
    suspend fun evaluateJavascript(js: String): String {
        val r = suspendCancellableCoroutine { continuation ->
            webViewHolder.invoke().evaluateJavascript(js) {
                continuation.resume(it)
            }
        }
        DAuthLogger.v("evaluate $js => $r", TAG)
        return r
    }

    suspend fun isConnected(): Boolean? = when (evaluateJavascript(JsConvertUtil.isConnected())) {
        "true" -> true
        "false" -> false
        else -> null
    }

    suspend fun personalSignMessage(message: String) {
        evaluateJavascript(JsConvertUtil.personalSignMessage(message))
    }

    suspend fun sendTransaction(transactionJson: String) {
        evaluateJavascript(JsConvertUtil.sendTransaction(transactionJson))
    }

    suspend fun showButton(buttonIndex: Int) {
        evaluateJavascript(JsConvertUtil.showButton(buttonIndex))
    }
}