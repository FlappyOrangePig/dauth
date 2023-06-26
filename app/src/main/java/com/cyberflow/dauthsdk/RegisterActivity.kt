package com.cyberflow.dauthsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberflow.dauth.databinding.ActivityRegisterLayoutBinding
import com.cyberflow.dauthsdk.api.DAuthSDK
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    private var _binding: ActivityRegisterLayoutBinding? = null
    private val binding: ActivityRegisterLayoutBinding get() = _binding!!

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun initView() {
        _binding = ActivityRegisterLayoutBinding.inflate(LayoutInflater.from(this))
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnDauthRegister.setOnClickListener {
            val account = binding.edtAccount.text.toString()
            val password = binding.edtPassword.text.toString()
            val ensurePassword = binding.edtEnsurePassword.text.toString()
            if (account.isNotEmpty() && password.isNotEmpty() && ensurePassword.isNotEmpty()) {
                lifecycleScope.launch {
                    val code =
                        DAuthSDK.instance.createDAuthAccount(account, password, ensurePassword)
                    if (code != null) {
                        if (code == 0) {
                            MainActivity.launch(this@RegisterActivity)
                        } else {
                            ToastUtil.show(
                                this@RegisterActivity,
                                "创建自有账号失败 errorCode: $code"
                            )
                        }
                    }
                }
            } else {
                ToastUtil.show(this, "请输入账号或密码")
            }
        }
    }
}