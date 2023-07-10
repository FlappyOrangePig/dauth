package com.cyberflow.dauthsdk


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityMainLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.TokenType
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.ext.mount
import com.cyberflow.dauthsdk.ext.myAddress
import com.cyberflow.dauthsdk.ext.tokenIds
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.fasterxml.jackson.databind.ser.Serializers.Base
import kotlinx.coroutines.launch
import java.math.BigInteger


class MainActivity : BaseActivity() {
    private val testAddress = "0xA0590b28C219E6C26a93116D04C395A56E9135f5"
    private val testEmail = "453376077@qq.com"
    private val testAuthId = "6b5a96eb3fedc2e7bbf183eab6820b95"
    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var mainBinding: ActivityMainLayoutBinding? = null
    private val binding: ActivityMainLayoutBinding get() = mainBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    fun initView() {
        binding.btnQueryBalance.setOnClickListener {
            lifecycleScope.launch {
                val sb = StringBuilder()
                showEth(sb)
                showUsdt(sb)
                showNfts(sb)
                ToastUtil.show(this@MainActivity, sb.toString())
            }
        }
        binding.btnGas.setOnClickListener {
            lifecycleScope.launch {
                val result = when (val response = DAuthSDK.instance.estimateGas(testAddress, BigInteger("1000"))) {
                    is DAuthResult.Success -> {
                        "预估gas费：${response.data.amountUsed}"
                    }
                    else -> {
                        response.getError()
                    }
                }
                ToastUtil.show(this@MainActivity, result.orEmpty())
            }
        }
        binding.btnSendTransaction.setOnClickListener {
            lifecycleScope.launch {
                val transactionResult =
                    DAuthSDK.instance.sendTransaction(testAddress, BigInteger("1000"))
                var result: String? = null

                when (transactionResult) {
                    is DAuthResult.Success -> {
                        result = "hash:${transactionResult.data.txHash}"
                    }
                    else -> {}
                }
                ToastUtil.show(this@MainActivity, "转账结果：$result")
            }
        }

        binding.btnSetPwd.setOnClickListener {
            showInputDialog(this)
        }

        binding.btnQueryAccount.setOnClickListener {
            lifecycleScope.launch {
                val accountRes = DAuthSDK.instance.queryAccountByEmail(testEmail)
                DAuthLogger.d("用户信息：${accountRes?.data?.account}")
            }
        }

        binding.btnBindEmail.setOnClickListener {
            lifecycleScope.launch {
                DAuthSDK.instance.bindEmail(testEmail,"6877")
            }
        }

        binding.btnQueryAccountByOpenid.setOnClickListener {
            lifecycleScope.launch {
                val accountRes = DAuthSDK.instance.queryAccountByAuthid()
                val hasPassword = accountRes?.data?.has_password
                if(accountRes!= null && hasPassword == 1) {
                    ToastUtil.show(this@MainActivity,"该账号已设置密码")
                } else {
                    ToastUtil.show(this@MainActivity,"该账号未设置密码")
                }
            }
        }

        binding.btnQuit.setOnClickListener {
            DAuthSDK.instance.logout()
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("MainActivity onDestroy")
    }

    private fun showInputDialog(context: Context) {
        val inputEditText = EditText(context)
        val dialog = AlertDialog.Builder(context)
            .setTitle("Enter Password")
            .setView(inputEditText)
            .setPositiveButton("OK") { _, _ ->
                lifecycleScope.launch {
                    val password = inputEditText.text.toString()
                    val responseCode = DAuthSDK.instance.setPassword(password)
                    if (responseCode == 0) {
                        ToastUtil.show(this@MainActivity, "密码设置成功")
                    } else {
                        ToastUtil.show(
                            this@MainActivity,
                            "密码设置失败 errorCode == $responseCode")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private suspend fun showEth(sb: StringBuilder){
        val address = myAddress() ?: return
        val balanceResult = DAuthSDK.instance.queryWalletBalance(address, TokenType.Eth)
        var result: String? = null

        when (balanceResult) {
            is DAuthResult.Success -> {
                result = balanceResult.data.mount().toString()
            }
            else -> {}
        }
        sb.appendLine("eth余额：$result")
    }

    private suspend fun showUsdt(sb: StringBuilder) {
        val address = myAddress() ?: return
        val balanceResult = DAuthSDK.instance.queryWalletBalance(address, TokenType.ERC20(Web3Const.ERC20))
        var result: String? = null

        when (balanceResult) {
            is DAuthResult.Success -> {
                result = balanceResult.data.mount().toString()
            }
            else -> {}
        }
        sb.appendLine("usdt余额：$result")
    }

    private suspend fun showNfts(sb: StringBuilder) {
        val address = myAddress() ?: return
        val balance = DAuthSDK.instance.queryWalletBalance(address, TokenType.ERC721(Web3Const.ERC721))
        var result: String? = null

        when (balance) {
            is DAuthResult.Success -> {
                result = balance.data.tokenIds().toString()
            }

            else -> {}
        }
        sb.append("NFT余额：$result")
    }
}