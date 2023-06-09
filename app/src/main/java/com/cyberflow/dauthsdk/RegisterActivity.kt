package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyberflow.dauth.databinding.ActivityRegisterLayoutBinding

class RegisterActivity: AppCompatActivity() {
    private var _binding: ActivityRegisterLayoutBinding?= null
    private val binding: ActivityRegisterLayoutBinding get() = _binding!!

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    private fun initView() {

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnDauthRegister.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            val password = binding.edtPassword.text.toString()
            val ensurePassword = binding.edtEnsurePassword.text.toString()
            if(account.isNotEmpty() && password.isNotEmpty() && ensurePassword.isNotEmpty()) {
                 DAuthSDK.instance.createDAuthAccount(account, password, ensurePassword)
            } else {
                Toast.makeText(this, "请输入账号或密码", Toast.LENGTH_SHORT).show()
            }

        }
    }
}