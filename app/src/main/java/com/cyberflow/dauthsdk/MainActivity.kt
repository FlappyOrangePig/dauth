package com.cyberflow.dauthsdk


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauth.databinding.ActivityMainLayoutBinding
import com.cyberflow.dauthsdk.login.DAuthSDK
import com.cyberflow.dauthsdk.login.utils.DAuthLogger

import java.math.BigInteger


class MainActivity : AppCompatActivity() {

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
        //testWeb3j()
    }

    private fun initView() {
        binding.btnQueryBalance.setOnClickListener {
            DAuthSDK.instance.queryWalletBalance()
        }
        binding.btnGas.setOnClickListener {
            DAuthSDK.instance.estimateGas("", BigInteger("100000000"))
        }
        binding.btnSendTransaction.setOnClickListener {
            DAuthSDK.instance.sendTransaction("", BigInteger("100000000"))
        }
    }

    private fun testWeb3j() {
        val sdk = DAuthSDK.instance
        val address = sdk.queryWalletAddress()
        DAuthLogger.d("address=$address")
        val balance = sdk.queryWalletBalance()
        DAuthLogger.d("balance=$balance")
        val to = "0x386F221660f58157aa05C107dDae69295316d82D"
        val amount = BigInteger("10")
        val estimateGas = sdk.estimateGas(to, amount)
        DAuthLogger.d("estimateGas=$estimateGas")
        val gasUsed = sdk.sendTransaction(to, amount)
        DAuthLogger.d("gasUsed=$gasUsed")
    }

    override fun onDestroy() {
        super.onDestroy()
        DAuthLogger.d("MainActivity onDestroy")
    }
}