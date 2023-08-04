package com.infras.dauth.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.util.Web3Const
import com.infras.dauth.databinding.ActivityMainLayoutBinding
import com.infras.dauth.ext.handleByToast
import com.infras.dauth.ext.mount
import com.infras.dauth.ext.myAddress
import com.infras.dauth.ext.tokenIds
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.manager.sdk
import com.infras.dauth.util.DemoPrefs
import com.infras.dauth.util.DialogHelper
import com.infras.dauth.util.LogUtil
import com.infras.dauth.widget.LoadingDialogFragment
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.TokenType
import com.infras.dauthsdk.login.model.AccountRes
import com.infras.dauthsdk.login.model.SetPasswordParam
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var mainBinding: ActivityMainLayoutBinding? = null
    private val binding: ActivityMainLayoutBinding get() = mainBinding!!
    private val loadingDialog = LoadingDialogFragment.newInstance()
    private val sdk get() = sdk()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
        initData()
    }

    private fun initView() {
        val a = this@MainActivity
        binding.btnQueryBalance.setOnClickListener {
            lifecycleScope.launch {
                val sb = StringBuilder()
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                showEth(sb)
                showUsdt(sb)
                showNfts(sb)
                loadingDialog.dismiss()
                DialogHelper.show1ButtonDialogMayHaveLeak(this@MainActivity, sb.toString())
            }
        }

        binding.btnQueryAccountByOpenid.setOnClickListener {
            queryAccountInfo(true)
        }

        binding.btnQueryAccountByMail.setOnClickListener {
            DialogHelper.showInputDialogMayHaveLeak(
                activity = a,
                title = "enter email",
                defaultValue = DemoPrefs.getLastEmail()
            ) { mail ->
                if (mail.isNotEmpty()) {
                    DemoPrefs.setLastEmail(mail)
                    lifecycleScope.launch {
                        loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                        val accountRes = sdk.queryAccountByEmail(mail)
                        loadingDialog.dismiss()
                        val data = accountRes?.data
                        if (data == null) {
                            ToastUtil.show(a, "获取失败")
                            return@launch
                        }
                        showAccountInfoDialog(data)
                    }
                }
            }
        }

        binding.btnSetPwd.setOnClickListener {
            showInputDialog()
        }

        binding.btnBindEmail.setOnClickListener {
            DialogHelper.showInputDialogMayHaveLeak(
                activity = a,
                title = "input email",
                defaultValue = DemoPrefs.getLastEmail()
            ) { mail ->
                if (mail.isNotEmpty()) {
                    DemoPrefs.setLastEmail(mail)
                    lifecycleScope.launch {
                        loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                        val response = sdk.sendEmailVerifyCode(mail)
                        loadingDialog.dismiss()
                        if (response?.ret != 0) {
                            return@launch
                        }

                        ToastUtil.show(a, "验证码发送成功")
                        DialogHelper.showInputDialogMayHaveLeak(
                            activity = a,
                            title = "input verify code"
                        ) { code ->
                            if (code.isNotEmpty()) {
                                lifecycleScope.launch {
                                    loadingDialog.show(
                                        supportFragmentManager,
                                        LoadingDialogFragment.TAG
                                    )
                                    val result = sdk.bindEmail(mail, code)
                                    loadingDialog.dismiss()
                                    result.handleByToast()
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.btnCheckMail.setOnClickListener {
            DialogHelper.showInputDialogMayHaveLeak(
                activity = a,
                title = "input email",
                defaultValue = DemoPrefs.getLastEmail()
            ) { mail ->
                if (mail.isNotEmpty()) {
                    DemoPrefs.setLastEmail(mail)
                    lifecycleScope.launch {
                        loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                        val response = sdk.sendEmailVerifyCode(mail)
                        loadingDialog.dismiss()
                        if (response?.ret != 0) {
                            return@launch
                        }

                        ToastUtil.show(a, "验证码发送成功")
                        DialogHelper.showInputDialogMayHaveLeak(
                            activity = a,
                            title = "input verify code"
                        ) { code ->
                            if (code.isNotEmpty()) {
                                lifecycleScope.launch {
                                    loadingDialog.show(
                                        supportFragmentManager,
                                        LoadingDialogFragment.TAG
                                    )
                                    val result = sdk.checkEmail(mail, code)
                                    loadingDialog.dismiss()
                                    result.handleByToast()
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.btnBtnWalletPage.setOnClickListener {
            WalletTestActivity.launch(it.context)
        }

        binding.btnQuit.setOnClickListener {
            sdk.logout()
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(logTag, "MainActivity onDestroy")
    }

    private fun showInputDialog() {
        DialogHelper.showInputDialogMayHaveLeak(this, "Enter Password") { password ->
            lifecycleScope.launch {
                loadingDialog.show(supportFragmentManager, LoadingDialogFragment.TAG)
                val result = sdk.setPassword(SetPasswordParam().apply { this.password = password })
                loadingDialog.dismiss()
                result.handleByToast()
            }
        }
    }

    private suspend fun showEth(sb: StringBuilder) {
        val address = myAddress() ?: return
        val balanceResult = sdk.queryWalletBalance(address, TokenType.Eth)
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
        val balanceResult =
            sdk.queryWalletBalance(address, TokenType.ERC20(Web3Const.ERC20))
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
        val balance =
            sdk.queryWalletBalance(address, TokenType.ERC721(Web3Const.ERC721))
        var result: String? = null

        when (balance) {
            is DAuthResult.Success -> {
                result = balance.data.tokenIds().toString()
            }

            else -> {}
        }
        sb.append("NFT余额：$result")
    }

    private fun initData() {
        queryAccountInfo(false)
    }

    private fun queryAccountInfo(showDetail: Boolean) {
        lifecycleScope.launch {
            binding.tvWalletAddress.text = "AA钱包地址：${AccountManager.getAccountAddress()}"

            val accountRes = sdk.queryAccountByAuthid()
            val data = accountRes?.data
            if (data == null) {
                ToastUtil.show(this@MainActivity, "查询用户信息失败")
                return@launch
            }

            binding.tvUserInfo.text = StringBuilder()
                .appendLine("昵称：${data.nickname}")
                .appendLine("邮箱：${data.email}")
                .appendLine("电话：${data.phone}")



            if (showDetail) {
                showAccountInfoDialog(data)
            }
        }
    }

    private fun showAccountInfoDialog(data: AccountRes.Data) {
        val accountInfo = StringBuilder()
            .appendLine("account=${data.account}")
            .appendLine("nickname=${data.nickname}")
            .appendLine("birthday=${data.birthday}")
            .appendLine("sex=${data.sex}")
            .appendLine("email=${data.email}")
            .appendLine("phone=${data.phone}")
            .appendLine("phone_area_code=${data.phone_area_code}")
            .appendLine("real_name=${data.real_name}")
            .appendLine("identity=${data.identity}")
            .appendLine("identity_Status=${data.identity_Status}")
            .appendLine("head_img_url=${data.head_img_url}")
            .appendLine("country=${data.country}")
            .appendLine("province=${data.province}")
            .appendLine("city=${data.city}")
            .appendLine("district=${data.district}")
            .appendLine("address=${data.address}")
            .appendLine("user_type=${data.user_type}")
            .appendLine("user_state=${data.user_state}")
            .appendLine("create_time=${data.create_time}")
            .appendLine("has_password=${data.has_password}")

        DialogHelper.show1ButtonDialogMayHaveLeak(this@MainActivity, accountInfo.toString())
    }
}