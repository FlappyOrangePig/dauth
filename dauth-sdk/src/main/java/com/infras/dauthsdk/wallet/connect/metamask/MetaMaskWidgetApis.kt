package com.infras.dauthsdk.wallet.connect.metamask

import android.content.Context
import androidx.fragment.app.FragmentActivity

internal interface MetaMaskWidgetApi {
    fun launch(context: Context, input: MetaMaskInput)
}

internal object MetaMaskWidgetApis {

    private const val META_MASK_SHOW_DIALOG_TAG = "META_MASK_SHOW_DIALOG_TAG"

    fun getMetaMaskWidgetApi(useDialog: Boolean): MetaMaskWidgetApi = if (useDialog) {
        object : MetaMaskWidgetApi {
            override fun launch(context: Context, input: MetaMaskInput) {
                val a = context as? FragmentActivity ?: return
                val d = MetaMaskDialog.newInstance(input)
                d.show(a.supportFragmentManager, META_MASK_SHOW_DIALOG_TAG)
            }
        }
    } else {
        object : MetaMaskWidgetApi {
            override fun launch(context: Context, input: MetaMaskInput) {
                MetaMaskActivity.launch(context, input)
            }
        }
    }
}