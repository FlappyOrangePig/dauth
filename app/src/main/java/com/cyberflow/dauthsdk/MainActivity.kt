package com.cyberflow.dauthsdk


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityMainLayoutBinding
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
                Toast.makeText(it.context,"钱包余额：$balance",Toast.LENGTH_LONG).show()
            }
        }
        binding.btnGas.setOnClickListener {
            lifecycleScope.launch {
                val gas = DAuthSDK.instance.estimateGas(testAddress, BigInteger("100"))
                Toast.makeText(it.context,"gas费预估：$gas",Toast.LENGTH_LONG).show()
            }
        }
        binding.btnSendTransaction.setOnClickListener {
            lifecycleScope.launch {
                val result = DAuthSDK.instance.sendTransaction(testAddress, BigInteger("100000000000000"))
                Toast.makeText(it.context,"转账结果：${result.toString()}",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("MainActivity onDestroy")
    }
}