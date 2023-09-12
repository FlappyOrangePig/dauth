package com.infras.dauth.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.databinding.ActivityRegisterLayoutBinding
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.launchMainPage
import com.infras.dauth.manager.sdk
import com.infras.dauth.ui.main.MainActivity
import com.infras.dauth.widget.LoadingDialogFragment
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    private var _binding: ActivityRegisterLayoutBinding? = null
    private val binding: ActivityRegisterLayoutBinding get() = _binding!!
    private val loadingDialog = LoadingDialogFragment.newInstance()

    companion object {
        fun launch(context: Context) {
            context.launch(RegisterActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
    }

    private fun ActivityRegisterLayoutBinding.initView() {
        ivBack.setOnClickListener {
            finish()
        }

        btnDauthRegister.setOnClickListener {
            val context = it.context
            val account = edtAccount.text.toString()
            val password = edtPassword.text.toString()
            val ensurePassword = edtEnsurePassword.text.toString()
            if (account.isNotEmpty() && password.isNotEmpty() && ensurePassword.isNotEmpty()) {
                lifecycleScope.launch {
                    loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                    val code = sdk().createDAuthAccount(
                        account,
                        password,
                        ensurePassword
                    )
                    loadingDialog.dismiss()
                    if (code != null) {
                        if (code == 0) {
                            context.launchMainPage()
                        } else {
                            ToastUtil.show(context, "创建自有账号失败 errorCode: $code")
                        }
                    }
                }
            } else {
                ToastUtil.show(context, "请输入账号或密码")
            }
        }
    }
}