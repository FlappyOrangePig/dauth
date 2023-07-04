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
import com.cyberflow.dauthsdk.mpc.websocket.WebsocketManager
import com.cyberflow.dauthsdk.wallet.connect.ConnectManager
import com.cyberflow.dauthsdk.wallet.impl.Web3Manager
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2
import com.cyberflow.dauthsdk.widget.LoadingDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

private const val TAG = "WalletTestActivity"

class WalletTestActivity : BaseActivity() {

    companion object {
        private const val RICH_EOA_ACCOUNT = "0xdD2FD4581271e230360230F9337D5c0430Bf44C0"
        private const val RICH_EOA_PRIVATE_KEY = "0xde9be858da4a475276426320d5e9262ecfc3ba460bfac56360bfa6c4c28b4ee0"

        fun launch(context: Context) {
            val intent = Intent(context, WalletTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var _binding: ActivityWalletTestBinding? = null
    private val binding: ActivityWalletTestBinding get() = _binding!!
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private val api by lazy { DAuthSDK.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWalletTestBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
    }

    private fun ActivityWalletTestBinding.initView() {
        val context = this.root.context
        btnCreateWallet.setOnClickListener {
            lifecycleScope.launch {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val result = api.createWallet("")
                if (result !is DAuthResult.Success) {
                    ToastUtil.show(context, "创建失败")
                    return@launch
                }
                ToastUtil.show(context, "创建成功 ${result.data.address}")
                loadingDialog.dismiss()
            }
        }
        btnSign.setOnClickListener {
            val msg = "abcdef"
            lifecycleScope.launch{
                val eoaAddress = WalletPrefsV2.getEoaAddress()
                Log.i(TAG, "eoaAddress=$eoaAddress")
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val r = WebsocketManager.instance.mpcSign(msg)
                loadingDialog.dismiss()
                Log.i(TAG, "hex=$r")
                ToastUtil.show(context, r.toString())
            }
        }
        btnExecuteUserOp.setOnClickListener {
            lifecycleScope.launch {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)

                val addressResult = api.queryWalletAddress()
                if (addressResult !is DAuthResult.Success) {
                    ToastUtil.show(context, "获取地址失败")
                } else {
                    val nonce = BigInteger.valueOf(1)
                    val gasPrice = DefaultGasProvider.GAS_PRICE
                    val gasLimit = DefaultGasProvider.GAS_LIMIT
                    val to = "0x1234567890abcdef"
                    val value = BigInteger.valueOf(1L)
                    val rawTransaction: RawTransaction =
                        RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, value)
                    val encoded = TransactionEncoder.encode(rawTransaction)

                    /*val function = Function(
                        DAuthAccountFactory.FUNC_GETADDRESS,
                        listOf<Type<*>>(
                            Address(eoaAddress),
                            Uint256(0)
                        ),
                        listOf<TypeReference<*>>(object : TypeReference<Address?>() {})
                    )
                    val encoded = FunctionEncoder.encode(function)*/

                    val result = api.execute(encoded)
                    if (result !is DAuthResult.Success) {
                        ToastUtil.show(context, "执行失败")
                    }else{
                        ToastUtil.show(context, "执行成功 ${result.data}")
                    }
                }

                loadingDialog.dismiss()
            }
        }
        btnTransferMoneyToAa.setOnClickListener {
            lifecycleScope.launch {
                val addressResult = api.queryWalletAddress()
                if (addressResult !is DAuthResult.Success) {
                    ToastUtil.show(context, "获取地址失败")
                    return@launch
                }
                val aa = addressResult.data.aaAddress
                val result =
                    Web3Manager.transferMoneyToAa(RICH_EOA_ACCOUNT, aa, RICH_EOA_PRIVATE_KEY)
                if (!result.isNullOrEmpty()) {
                    ToastUtil.show(context, "转账成功，hash=$result")
                } else {
                    ToastUtil.show(context, "转账失败")
                }
            }
        }
        btnGetBalance.setOnClickListener {
            lifecycleScope.launch {
                val addressResult = api.queryWalletAddress()
                if (addressResult !is DAuthResult.Success) {
                    ToastUtil.show(context, "获取地址失败")
                    return@launch
                }
                val address = addressResult.data.aaAddress
                val balanceResult = api.queryWalletBalance(address, TokenType.Eth)
                if (balanceResult !is DAuthResult.Success) {
                    ToastUtil.show(context, "查询余额失败")
                    return@launch
                }
                val result = balanceResult.data.mount().toString()
                ToastUtil.show(context, result)
            }
        }
        btnAaExists.setOnClickListener {
            lifecycleScope.launch {
                val addressResult = api.queryWalletAddress()
                if (addressResult !is DAuthResult.Success) {
                    ToastUtil.show(context, "获取地址失败")
                    return@launch
                }
                val text =
                    when (Web3Manager.isCodeExists(aaAddress = addressResult.data.aaAddress)) {
                        null -> "网络错误"
                        true -> "存在"
                        false -> "不存在"
                    }
                ToastUtil.show(it.context, text)
            }
        }
        btnConnectWallet.setOnClickListener {
            lifecycleScope.launch {
                val c = ConnectManager.instance
                withContext(Dispatchers.IO){
                    c.connect()
                }
                c.accountAddress.collect {
                    it?.let { nonNull ->
                        ToastUtil.show(context, nonNull)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    private fun initData() {
        lifecycleScope.launch {
            when(val address = api.queryWalletAddress()){
                is DAuthResult.Success -> {
                    val aa = address.data.aaAddress
                    val signer = address.data.signerAddress
                    val addressText = "aa=$aa\nsigner=$signer"
                    binding.tvAddress.text = addressText
                }
                else -> return@launch
            }
        }
    }
}