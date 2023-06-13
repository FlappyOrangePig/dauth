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
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import kotlinx.coroutines.launch
import java.math.BigInteger


class MainActivity : AppCompatActivity() {
    private val testAddress = "0xA0590b28C219E6C26a93116D04C395A56E9135f5"
    companion object {
        fun launch(context : Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var mainBinding: ActivityMainLayoutBinding?  = null
    private val binding: ActivityMainLayoutBinding get() = mainBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.btnQueryBalance.setOnClickListener {
            lifecycleScope.launch {
                val sb = StringBuilder()
                showEth(sb)
                showUsdt(sb)
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
        val balance = DAuthSDK.instance.queryWalletBalance()
        var result: String? = null

        when (balance) {
            is DAuthResult.Success -> {
                result = balance.data.balance.toString()
            }
            else -> {}
        }
        sb.append("eth余额：$result")
    }

    private suspend fun showUsdt(sb: StringBuilder) {
        val balance = DAuthSDK.instance.queryERC20Balance(0)
        var result: String? = null

        when (balance) {
            is DAuthResult.Success -> {
                result = balance.data.balance.toString()
            }

            else -> {}
        }
        sb.append("usdt余额：$result")
    }
}