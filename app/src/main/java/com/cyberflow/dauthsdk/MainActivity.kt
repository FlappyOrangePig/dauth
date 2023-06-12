package com.cyberflow.dauthsdk


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityMainLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.EstimateGasResult
import com.cyberflow.dauthsdk.api.entity.GetBalanceResult
import com.cyberflow.dauthsdk.api.entity.SendTransactionResult
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
                val balance = DAuthSDK.instance.queryWalletBalance()
                var result: String? = null

                when (balance) {
                    is GetBalanceResult.Success -> {
                        result = balance.balance.toString()
                    }
                    else -> {}
                }
                Toast.makeText(this@MainActivity, "钱包余额：$result", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnGas.setOnClickListener {
            lifecycleScope.launch {
                val gas = DAuthSDK.instance.estimateGas(testAddress, BigInteger("100000"))
                var result: String? = null

                when (gas) {
                    is EstimateGasResult.Success -> {
                        result = gas.amountUsed.toString()
                    }
                    else -> {}
                }
                Toast.makeText(this@MainActivity, "gas费预估：$result", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnSendTransaction.setOnClickListener {
            lifecycleScope.launch {
                val transactionResult =
                    DAuthSDK.instance.sendTransaction(testAddress, BigInteger("1000000000"))
                var result: String? = null

                when (transactionResult) {
                    is SendTransactionResult.Success -> {
                        result = transactionResult.transactionHash
                    }
                    else -> {}
                }
                Toast.makeText(this@MainActivity, "转账结果：$result", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("MainActivity onDestroy")
    }
}