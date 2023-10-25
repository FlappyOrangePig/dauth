package com.infras.dauth.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.databinding.ActivityLoginLayoutBinding
import com.infras.dauth.ext.launch
import com.infras.dauth.ext.launchMainPage
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.repository.SignInRepository
import com.infras.dauth.ui.eoa.EoaBusinessActivity
import com.infras.dauth.ui.login.fragment.SignInByCodeFragment
import com.infras.dauth.ui.login.fragment.SignInByPasswordFragment
import com.infras.dauth.ui.main.WalletTestActivity
import com.infras.dauth.util.DemoPrefs
import com.infras.dauth.util.HideApiUtil
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.api.annotation.SignType3rd
import com.infras.dauthsdk.api.entity.DAuthResult
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    private var _binding: ActivityLoginLayoutBinding? = null
    private val binding: ActivityLoginLayoutBinding get() = _binding!!
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private val sdk get() = AccountManager.sdk

    private var currentFragmentType = 0 // 0=code 1=password
    private val signInByCode by lazy { SignInByCodeFragment.newInstance() }
    private val signInByPassword by lazy { SignInByPasswordFragment.newInstance() }

    companion object {
        fun launch(context: Context) {
            context.launch(LoginActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initView()
    }

    private fun ActivityLoginLayoutBinding.initView() {
        // 邮箱登录
        tvForgetPwd.setOnClickListener {
            ResetPasswordActivity.launch(this@LoginActivity)
        }

        tvRegister.setOnClickListener {
            RegisterActivity.launch(this@LoginActivity)
        }

        ivGoogle.setOnClickListener {
            val a = this@LoginActivity
            lifecycleScope.launch {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val result = SignInRepository().signIn {
                    sdk.loginWithType(SignType3rd.GOOGLE, a)
                }
                loadingDialog.dismissAllowingStateLoss()
                ToastUtil.show(
                    a,
                    getString(if (result) R.string.success else (R.string.failure))
                )
                if (result) {
                    a.launchMainPage()
                    finish()
                }
            }
        }

        ivTwitter.setOnClickListener {
            val a = this@LoginActivity
            lifecycleScope.launch {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val result = SignInRepository().signIn {
                    sdk.loginWithType(SignType3rd.TWITTER, a)
                }
                loadingDialog.dismissAllowingStateLoss()
                ToastUtil.show(
                    a,
                    getString(if (result) R.string.success else (R.string.failure))
                )
                if (result) {
                    a.launchMainPage()
                    finish()
                }
            }
        }

        ivWalletConnect.setOnClickListener {
            lifecycleScope.launch {
                kotlin.runCatching {
                    val connectResult =  HideApiUtil.getEoaApi().connectWallet()
                    ToastUtil.show(it.context, "$connectResult")
                }
            }
        }

        tvDauth.setOnClickListener {
            WalletTestActivity.launch(it.context)
        }

        ivFacebook.setOnClickListener {

        }

        ivMetamask.setOnClickListener {
            val activity = this@LoginActivity
            EoaBusinessActivity.launch(activity)
            return@setOnClickListener

            lifecycleScope.launch {
                when (val addressResult =
                    HideApiUtil.getEoaApi().connectMetaMask(this@LoginActivity)) {
                    is DAuthResult.Success -> {
                        ToastUtil.show(
                            activity,
                            "${getString(R.string.success)}，${addressResult.data}"
                        )
                        EoaBusinessActivity.launch(activity)
                        finish()
                    }

                    else -> {
                        ToastUtil.show(
                            activity,
                            "${getString(R.string.failure)}, ${addressResult.getError()}}"
                        )
                    }
                }
            }
        }

        tvPasswordLogin.setOnClickListener {
            when (currentFragmentType) {
                0 -> {
                    currentFragmentType = 1
                    updateCurrentFragment()
                }

                1 -> {
                    currentFragmentType = 0
                    updateCurrentFragment()
                }

                else -> throw RuntimeException()
            }
        }

        currentFragmentType = DemoPrefs.getLastLoginType()
        updateCurrentFragment()
    }

    private fun updateCurrentFragment() {
        when (currentFragmentType) {
            1 -> {
                val tx = supportFragmentManager.beginTransaction()
                tx.replace(R.id.fl_fragment_container, signInByPassword)
                tx.commit()
                binding.tvPasswordLogin.text = "Sign in with SMS"
            }

            0 -> {
                val tx = supportFragmentManager.beginTransaction()
                tx.replace(R.id.fl_fragment_container, signInByCode)
                tx.commit()
                binding.tvPasswordLogin.text = "Sign in with password"
            }

            else -> throw RuntimeException()
        }
    }
}