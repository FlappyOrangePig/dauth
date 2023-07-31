package com.cyberflow.dauthsdk.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauthsdk.databinding.ActivityWebviewBinding
import com.cyberflow.dauthsdk.login.viewmodel.WalletWebViewViewModel
import androidx.activity.viewModels
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.login.callback.ThirdPartyCallback
import com.cyberflow.dauthsdk.login.callback.WalletCallback
import com.cyberflow.dauthsdk.login.constant.LoginConst.USER_TYPE
import com.cyberflow.dauthsdk.login.model.AuthorizeToken2Param
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.utils.*

private const val TAG = "X5WebViewActivity"
//private const val H5_URL = "https://wallet.51ailike.com/#/login"
private const val H5_URL = "http://172.16.12.186:5500/index.html"
private const val WEB_VIEW_ACTIVITY_EXTRA = "WEB_VIEW_ACTIVITY_EXTRA"

class WalletWebViewActivity : AppCompatActivity() {
    private var _binding: ActivityWebviewBinding? = null
    private val binding: ActivityWebviewBinding get() = _binding!!
    private lateinit var webView: WebView
    private val viewModel: WalletWebViewViewModel by viewModels()

    companion object {
        private var callback: WalletCallback? = null

        fun launch(context: Context, isFinish: Boolean, callback: WalletCallback) {
            val intent = Intent(context, WalletWebViewActivity::class.java)
            intent.putExtra(WEB_VIEW_ACTIVITY_EXTRA, isFinish)
            this.callback = callback
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWebviewBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initWebView()
    }

    private fun initWebView() {
        webView = WebView(this)
        val mContainer = binding.webViewContainer
        mContainer.addView(webView)
        viewModel.initWebViewClient(webView)
        webView.addJavascriptInterface(JavaScriptMethods(this
            ,
            object : WalletCallback {
                override fun onResult(walletInfo: String) {
                    callback?.onResult(walletInfo)
                }
            }
        ), "android")
        webView.loadUrl(H5_URL)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val isFinishWebView = intent.getBooleanExtra(WEB_VIEW_ACTIVITY_EXTRA,false)
        DAuthLogger.d("isFinishWebView==$isFinishWebView")
        if(isFinishWebView) {
            finishAffinity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.onPause()
        webView.removeAllViews()
        webView.destroy()
    }

}