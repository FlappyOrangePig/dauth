package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityWalletTestBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.ext.mount
import com.cyberflow.dauthsdk.util.LogUtil
import com.cyberflow.dauthsdk.util.Web3jHelper
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.cyberflow.dauthsdk.widget.LoadingDialogFragment
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.utils.Numeric
import java.math.BigInteger

private const val TAG = "WalletTestActivity"

class WalletTestActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, WalletTestActivity::class.java)
            context.startActivity(intent)
        }

        // 苟建的测试链账号
        private const val TO_ADDRESS = "0xEF62f0cf0b67B789BBb45f73125cc308b3c53c3d"
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
                loadingDialog.dismiss()
                if (result !is DAuthResult.Success) {
                    ToastUtil.show(context, "创建失败")
                    return@launch
                }
                ToastUtil.show(context, "创建成功 ${result.data.address}")
            }
        }
        btnSign.setOnClickListener {
            val msg = "abcdef"
            lifecycleScope.launch{
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val r = api.mpcSign(msg)
                loadingDialog.dismiss()
                LogUtil.i(TAG, "hex=$r")
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
                    /*val nonce = BigInteger.valueOf(1)
                    val gasPrice = DefaultGasProvider.GAS_PRICE
                    val gasLimit = DefaultGasProvider.GAS_LIMIT
                    // 苟建的测试链账号
                    val to = TO_ADDRESS
                    val value = BigInteger.valueOf(21000L)
                    val rawTransaction: RawTransaction =
                        RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, value)
                    val encoded = TransactionEncoder.encode(rawTransaction)*/

                    /*val function = Function(
                        DAuthAccountFactory.FUNC_GETADDRESS,
                        listOf<Type<*>>(
                            Address(to),
                            Uint256(0)
                        ),
                        listOf<TypeReference<*>>(object : TypeReference<Address?>() {})
                    )
                    val encoded = Numeric.hexStringToByteArray(FunctionEncoder.encode(function))*/

                    val result = api.execute(TO_ADDRESS, BigInteger("111111"), byteArrayOf())
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
                    Web3jHelper.transferMoneyToAa(aa)
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
        btnGetDestBalance.setOnClickListener {
            lifecycleScope.launch {
                val address = TO_ADDRESS
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
                    when (Web3jHelper.isCodeExists(aaAddress = addressResult.data.aaAddress)) {
                        null -> "网络错误"
                        true -> "存在"
                        false -> "不存在"
                    }
                ToastUtil.show(it.context, text)
            }
        }
        btnConnectWallet.setOnClickListener {
            /*lifecycleScope.launch {
                val c = ConnectManager.instance
                withContext(Dispatchers.IO){
                    c.connect()
                }
                c.accountAddress.collect {
                    it?.let { nonNull ->
                        ToastUtil.show(context, nonNull)
                    }
                }
            }*/
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