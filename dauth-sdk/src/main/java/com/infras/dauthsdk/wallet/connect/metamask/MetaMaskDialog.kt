package com.infras.dauthsdk.wallet.connect.metamask

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.infras.dauthsdk.R
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.wallet.ext.getParcelableExtraCompat
import com.infras.dauthsdk.wallet.impl.manager.Managers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MetaMaskDialog : DialogFragment() {

    companion object {
        private const val TAG = "MetaMaskDialog"
        private const val EXTRA_INPUT = "EXTRA_INPUT"
        internal fun newInstance(input: MetaMaskInput) = MetaMaskDialog().apply {
            arguments = Bundle().also { it.putParcelable(EXTRA_INPUT, input) }
        }
    }

    private val input get() = arguments.getParcelableExtraCompat<MetaMaskInput>(EXTRA_INPUT)!!
    private val metaMask get() = childFragmentManager.findFragmentByTag(getString(R.string.meta_mask_fragment_tag)) as MetaMaskFragment
    private val eoaWallet get() = Managers.eoaWalletApi
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val r = super.onCreateDialog(savedInstanceState)
        r.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return r
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setGravity(Gravity.BOTTOM)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dauth_fragment_metamask_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleInput()
    }

    private fun handleInput() {
        metaMask.setMetaMaskInput(input)
        when (input) {
            MetaMaskInput.Connect -> {
                lifecycleScope.launch {
                    DAuthLogger.d("setJsHandlers:Connect", TAG)
                    metaMask.setJsHandlers(listOf(onAuthorizeHandler))
                    val isConnected = metaMask.isConnected()
                    DAuthLogger.d("isConnected=$isConnected", TAG)
                    if (isConnected == true) {
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
                    metaMask.setJsHandlers(listOf(onPersonalSignHandler))
                }
            }

            is MetaMaskInput.SendTransaction -> {
                lifecycleScope.launch {
                    DAuthLogger.d("setJsHandlers:SendTransaction", TAG)
                    metaMask.setJsHandlers(listOf(onSendTransactionHandler))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("onDestroy", TAG)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        DAuthLogger.d("onDismiss", TAG)
        // 可能会产生重复，作为兜底逻辑。而finishWithResult方法只会分发一次结果
        finishWithResult(DAuthResult.SdkError(DAuthResult.SDK_ERROR_USER_CANCELED), false)
    }

    /**
     * 关闭（不是真关闭）页面并返回结果。null代表失败。
     *
     * @param result
     */
    private fun finishWithResult(result: DAuthResult<String>, dismiss: Boolean = true) {
        DAuthLogger.d("finishWithResult=$result", TAG)
        eoaWallet.metamaskCallback?.let {
            DAuthLogger.d("finishWithResult dispatch", TAG)
            it.invoke(result)
            eoaWallet.metamaskCallback = null
        }
        if (dismiss) {
            dismiss()
        }
    }
}