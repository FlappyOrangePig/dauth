package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityWalletTestBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.ext.mount
import com.cyberflow.dauthsdk.ext.myAddress
import com.cyberflow.dauthsdk.mpc.websocket.DAuthWsSession
import com.cyberflow.dauthsdk.mpc.websocket.WebsocketManager
import com.cyberflow.dauthsdk.wallet.impl.Web3Manager
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2
import com.cyberflow.dauthsdk.widget.LoadingDialogFragment
import kotlinx.coroutines.launch
import org.web3j.utils.Numeric

private const val TAG = "WalletTestActivity"

class WalletTestActivity : BaseActivity() {

    companion object {
        private const val EOA_ACCOUNT = "0xdD2FD4581271e230360230F9337D5c0430Bf44C0"

        fun launch(context: Context) {
            val intent = Intent(context, WalletTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var _binding: ActivityWalletTestBinding? = null
    private val binding: ActivityWalletTestBinding get() = _binding!!
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private var lastSession: DAuthWsSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWalletTestBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
    }

    private fun ActivityWalletTestBinding.initView() {
        btnCreateWallet.setOnClickListener {
            lifecycleScope.launch {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val result = DAuthSDK.instance.createWallet("")
                loadingDialog.dismiss()
            }
        }
        btnSign.setOnClickListener {
            val msg = "abcdef"
            lifecycleScope.launch{
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val eoaAddress = WalletPrefsV2().getEoaAddress()
                Log.i(TAG, "eoaAddress=$eoaAddress")
                val r = WebsocketManager.instance.mpcSign(msg)
                Log.i(TAG, "hex=$r")
                loadingDialog.dismiss()
                ToastUtil.show(this@WalletTestActivity, r.toString())
            }
        }
        btnCreateAa.setOnClickListener {
            lifecycleScope.launch {
                //Web3Manager.executeUserOperation(EOA_ACCOUNT)

                Web3Manager.executeMyUserOperation()
            }
        }
        btnTransferMoneyToAa.setOnClickListener {
            lifecycleScope.launch {
                //val aa = "0x87e89d39b07ee35061933f688657761d1acdfb35"
                val aa = "0xce2bc0e7c76d8409ff1eaccba2b6535cce40d0c8"
                val privateKey = "0xde9be858da4a475276426320d5e9262ecfc3ba460bfac56360bfa6c4c28b4ee0"
                Web3Manager.transferMoneyToAa(EOA_ACCOUNT, aa, privateKey)
            }
        }
        btnGetBalance.setOnClickListener {
            lifecycleScope.launch{
                when (val balanceResult = DAuthSDK.instance.queryWalletBalance(EOA_ACCOUNT, TokenType.Eth)) {
                    is DAuthResult.Success -> {
                        val result = balanceResult.data.mount().toString()
                        ToastUtil.show(this@WalletTestActivity, result)
                    }
                    else -> {}
                }
            }
        }
        btnAaExists.setOnClickListener {
            lifecycleScope.launch {
                val text =
                    when (Web3Manager.isCodeExists("0x87e89d39b07ee35061933f688657761d1acdfb35")) {
                        null -> "网络错误"
                        true -> "存在"
                        false -> "不存在"
                    }
                ToastUtil.show(it.context, text)
            }
        }
    }

    override fun onRelease() {
        super.onRelease()
    }
}