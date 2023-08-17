package com.infras.dauth.ui.main

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
        sb.appendLine("ETH: $result")
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
        sb.appendLine(result?.let { "USDT: $it" }.orEmpty())
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
        sb.append(result?.let { "NFT: $it" }.orEmpty())
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

            binding.tvUserInfo.text = StringBuilder().apply {
                data.nickname.takeUnless { it.isNullOrEmpty() }?.let {
                    appendLine("昵称：${it}")
                }
                data.email.takeUnless { it.isNullOrEmpty() }?.let {
                    appendLine("邮箱：${it}")
                }
                data.phone.takeUnless { it.isNullOrEmpty() }?.let {
                    appendLine("电话：${it}")
                }
            }
            if (showDetail) {
                showAccountInfoDialog(data)
            }
        }
    }

    private fun runIfNotNull(input: Any?, block:()->Unit){
        if (input != null){
            block.invoke()
        }
    }

    private fun showAccountInfoDialog(data: AccountRes.Data) {
        val sb = StringBuilder().apply {
            data.account?.let { appendLine("account=$it") }
            data.nickname?.let { appendLine("nickname=$it") }
            data.birthday?.let { appendLine("birthday=$it") }
            data.sex?.let { appendLine("sex=$it") }
            data.email?.let { appendLine("email=$it") }
            data.phone?.let { appendLine("phone=$it") }
            data.phone_area_code?.let { appendLine("phone_area_code=$it") }
            data.real_name?.let { appendLine("real_name=$it") }
            data.identity?.let { appendLine("identity=$it") }
            data.identity_Status?.let { appendLine("identity_Status=$it") }
            data.head_img_url?.let { appendLine("head_img_url=$it") }
            data.country?.let { appendLine("country=$it") }
            data.province?.let { appendLine("province=$it") }
            data.city?.let { appendLine("city=$it") }
            data.district?.let { appendLine("district=$it") }
            data.address?.let { appendLine("address=$it") }
            data.user_type?.let { appendLine("user_type=$it") }
            data.user_state?.let { appendLine("user_state=$it") }
            data.create_time?.let { appendLine("create_time=$it") }
            data.has_password?.let { appendLine("has_password=$it") }
        }
        DialogHelper.show1ButtonDialogMayHaveLeak(this@MainActivity, sb.toString())
    }
}