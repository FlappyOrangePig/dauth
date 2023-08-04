package com.infras.dauthsdk.wallet.connect.metamask

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.infras.dauthsdk.R
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.databinding.DauthActivityMetamaskConnectorBinding
import com.infras.dauthsdk.login.impl.TopActivityCallback
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.base.BaseActivity
import com.infras.dauthsdk.wallet.connect.metamask.util.JsConvertUtil
import com.infras.dauthsdk.wallet.ext.getParcelableExtraCompat
import com.infras.dauthsdk.wallet.ext.runCatchingWithLog
import com.infras.dauthsdk.wallet.util.SystemUIUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.lang.ref.WeakReference

internal typealias MetaMaskCallback = (DAuthResult<String>) -> Unit

@Parcelize
sealed class MetaMaskInput : Parcelable {
    @Parcelize
    object Connect : MetaMaskInput()

    @Parcelize
    class PersonalSign(val message: String) : MetaMaskInput() {
        override fun toString(): String {
            return "PersonalSign(message='$message')"
        }
    }

    @Parcelize
    class SendTransaction(val transactionJson: String) : MetaMaskInput() {
        override fun toString(): String {
            return "SendTransaction(transactionJson='$transactionJson')"
        }
    }
}

class MetaMaskActivity : BaseActivity() {

    companion object {
        private const val TAG = "MetaMaskActivity"
        private const val EXTRA_INPUT = "MetaMaskInput"

        fun launch(context: Context, input: MetaMaskInput) {
            context.startActivity(
                Intent(
                    context,
                    MetaMaskActivity::class.java
                ).addFlags(FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EXTRA_INPUT, input)
            )
        }

        @Volatile
        private var reference: WeakReference<MetaMaskActivity>? = null
        internal fun getEoaAddress(): String? {
            return reference?.get()?.metaMask?.getCurrentEoaAddress()
        }
    }

    private var _binding: DauthActivityMetamaskConnectorBinding? = null
    private val binding: DauthActivityMetamaskConnectorBinding get() = _binding!!
    private val metaMask get() = supportFragmentManager.findFragmentById(R.id.f_webView) as? MetaMaskFragment
    private val eoaWallet = DAuthSDK.impl.eoaWalletApi
    private val onAuthorizeHandler = object : JSHandler("OnAuthorize") {
        override fun onHandle(arguments: Map<String, String>) {
            val account = arguments["message"]
            DAuthLogger.d("OnAuthorize account=$account", TAG)

            if (account.isNullOrEmpty()) {
                finishWithResult(DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN))
            } else {
                finishWithResult(DAuthResult.Success(account))
            }
        }
    }
    private val onPersonalSignHandler = object : JSHandler("OnPersonalSign") {
        override fun onHandle(arguments: Map<String, String>) {
            val codeStr = arguments["code"]
            val code1 = codeStr?.toIntOrNull()
            val code = code1 ?: -1
            val message = arguments["message"].orEmpty()
            DAuthLogger.d("onPersonalSign code=$code signedMessage=$message", TAG)

            if (code == 0) {
                if (message.isNotEmpty()) {
                    finishWithResult(DAuthResult.Success(message))
                } else {
                    finishWithResult(DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN))
                }
            } else {
                finishWithResult(DAuthResult.ServerError(code, message))
            }
        }
    }
    private val onSendTransactionHandler = object : JSHandler("OnSendTransaction") {
        override fun onHandle(arguments: Map<String, String>) {
            val codeStr = arguments["code"]
            val code1 = codeStr?.toIntOrNull()
            val code = code1 ?: -1
            val message = arguments["message"].orEmpty()
            DAuthLogger.d("onSendTransaction code=$code hash=$message", TAG)

            if (code == 0) {
                if (message.isNotEmpty()) {
                    finishWithResult(DAuthResult.Success(message))
                } else {
                    finishWithResult(DAuthResult.SdkError(DAuthResult.SDK_ERROR_UNKNOWN))
                }
            } else {
                finishWithResult(DAuthResult.ServerError(code, message))
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DAuthLogger.d("onCreate $savedInstanceState", TAG)
        reference = WeakReference(this)

        SystemUIUtil.show(window, SystemUIUtil.ThemeDrawByDeveloper(true))
        _binding = DauthActivityMetamaskConnectorBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.ivLogo.setOnLongClickListener {
            metaMask?.loadUrl()
            false
        }
        binding.ivLogo.setOnClickListener {
        }

        if (!handleInputIntent(intent)) {
            finish()
            return
        }
    }

    private fun getInput(intent: Intent?): MetaMaskInput? {
        return intent.getParcelableExtraCompat(EXTRA_INPUT)
    }

    private fun handleInputIntent(intent: Intent?): Boolean {
        DAuthLogger.d("handleInputIntent", TAG)
        val input: MetaMaskInput = getInput(intent) ?: run {
            finish()
            DAuthLogger.e("input error", TAG)
            return false
        }

        setIntent(intent)
        val metaMask = this.metaMask ?: return false
        metaMask.setMetaMaskInput(input)
        when (input) {
            MetaMaskInput.Connect -> {
                lifecycleScope.launch {
                    DAuthLogger.d("setJsHandlers:Connect", TAG)
                    metaMask.setJsHandlers(listOf(onAuthorizeHandler))
                    if (metaMask.isConnected() == true) {
                        val account = metaMask.getCurrentEoaAddress().orEmpty()
                        if (account.isNotEmpty()) {
                            delay(250L)
                            finishWithResult(DAuthResult.Success(account))
                        }
                    }
                }
            }

            is MetaMaskInput.PersonalSign -> {
                lifecycleScope.launch {
                    DAuthLogger.d("setJsHandlers:PersonalSign", TAG)
                    metaMask.setJsToBeExecutedOnPageRefresh(JsConvertUtil.personalSignMessage(input.message))
                    metaMask.setJsHandlers(listOf(onPersonalSignHandler))
                }
            }

            is MetaMaskInput.SendTransaction -> {
                lifecycleScope.launch {
                    DAuthLogger.d("setJsHandlers:SendTransaction", TAG)
                    metaMask.setJsToBeExecutedOnPageRefresh(JsConvertUtil.sendTransaction(input.transactionJson))
                    metaMask.setJsHandlers(listOf(onSendTransactionHandler))
                }
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DAuthLogger.d("onActivityResult requestCode=$requestCode resultCode=$resultCode", TAG)
    }

    override fun finish() {
        DAuthLogger.d("finish", TAG)
        //super.finish()
        finishWithResult(DAuthResult.SdkError(DAuthResult.SDK_ERROR_USER_CANCELED))
    }

    private fun fakeFinish() {
        TopActivityCallback.getTopActivityNotSingleInstance()?.let { clazz ->
            runCatchingWithLog {
                startActivity(Intent(this, clazz).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        DAuthLogger.d("onNewIntent", TAG)
        if (!handleInputIntent(intent)) {
            finish()
        }
    }

    /**
     * 关闭（不是真关闭）页面并返回结果。null代表失败。
     *
     * @param result
     */
    private fun finishWithResult(result: DAuthResult<String>) {
        eoaWallet.metamaskCallback?.let {
            it.invoke(result)
            eoaWallet.metamaskCallback = null
        }
        fakeFinish()
    }
}