package com.infras.dauth.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyAndSellPageEntity
import com.infras.dauth.entity.PersonalInfoEntity
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.util.GasUtil
import com.infras.dauth.util.getEnv
import com.infras.dauthsdk.api.annotation.DAuthAccountType
import com.infras.dauthsdk.api.entity.CreateUserOpAndEstimateGasData
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.TokenType
import com.infras.dauthsdk.api.entity.WalletBalanceData
import com.infras.dauthsdk.login.model.AccountRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import kotlinx.coroutines.launch
import java.math.BigInteger

class HomeViewModel : BaseViewModel() {

    private val sdk get() = AccountManager.sdk
    private val _balance = mutableStateOf("")
    val balance: State<String> = _balance
    private val _address = mutableStateOf("")
    val address: State<String> = _address
    private val _tokenInfoList = mutableStateOf(listOf<BuyAndSellPageEntity.TokenInfo>())
    val tokenInfoList: State<List<BuyAndSellPageEntity.TokenInfo>> = _tokenInfoList
    private val _account = mutableStateOf("")
    val account: State<String> = _account
    private val _avatarUrl = mutableStateOf("")
    val avatarUrl: State<String> = _avatarUrl
    private val _personalInfoList = mutableStateOf(listOf<PersonalInfoEntity>())
    val personalInfoList: State<List<PersonalInfoEntity>> = _personalInfoList

    fun fetch() {
        fetchBalance()
        viewModelScope.launch {
            val accountResult = sdk.queryAccountByAuthid()
            if (accountResult != null && accountResult.isSuccess()) {
                val d = accountResult.data ?: return@launch
                d.account?.takeIf { it.isNotEmpty() }?.let { _account.value = it }
                d.head_img_url?.takeIf { it.isNotEmpty() }?.let { _avatarUrl.value = it }
                generatePersonInfo(d)
            }
        }

        val authId = kotlin.runCatching {
            val loginApi = Class.forName("com.infras.dauthsdk.api.DAuthSDK")
                .getDeclaredField("loginApi")
                .let {
                    it.isAccessible = true
                    it.get(sdk)
                }

            Class.forName("com.infras.dauthsdk.login.utils.LoginPrefs")
                .getDeclaredMethod("getAuthId")
                .invoke(loginApi) as String
        }.getOrNull()

        fetchErc20Tokens()
    }

    fun fetchBalance() {
        viewModelScope.launch {
            val address = fetchAddress()
            if (address != null) {
                _address.value = address
                val balance = fetchBalance(address)
                if (balance != null) {
                    val readable = GasUtil.getReadableGas(balance)
                    _balance.value = readable
                }
            }
        }
    }

    private fun getAuthId(): String {
        return AccountManager.getAuthId()
    }

    private fun generatePersonInfo(d: AccountRes.Data) {
        mutableListOf<PersonalInfoEntity>().apply {
            repeat(1) {
                getAuthId().takeUnless { it.isEmpty() }?.let {
                    add(PersonalInfoEntity.CopyableText("authid", it))
                }
                d.nickname.takeUnless { it.isNullOrEmpty() }?.let {
                    add(PersonalInfoEntity.CopyableText("nickname", it))
                }
                d.email.takeUnless { it.isNullOrEmpty() }?.let {
                    add(PersonalInfoEntity.CopyableText("email", it))
                }
                d.phone.takeUnless { it.isNullOrEmpty() }?.let {
                    add(PersonalInfoEntity.CopyableText("phone", it))
                }

                d.user_type.takeIf { it != null }?.let {
                    val typeStr = when (it) {
                        DAuthAccountType.ACCOUNT_TYPE_OF_EMAIL -> "mail"
                        DAuthAccountType.ACCOUNT_TYPE_OF_MOBILE -> "phone"
                        DAuthAccountType.ACCOUNT_TYPE_OF_OWN -> "own"
                        else -> "$it"
                    }
                    add(PersonalInfoEntity.CopyableText("user_type", typeStr))
                }

                d.has_password.takeIf { it != null }?.let {
                    add(PersonalInfoEntity.CopyableText("has_password", "${it == 1}"))
                }
            }
            _personalInfoList.value = this
        }
    }

    private suspend fun fetchAddress(): String? {
        val addressResult = sdk.queryWalletAddress()
        if (addressResult is DAuthResult.Success) {
            return addressResult.data.aaAddress
        }
        return null
    }

    private suspend fun fetchBalance(address: String): BigInteger? {
        val result = sdk.queryWalletBalance(address, TokenType.Eth)
        if (result is DAuthResult.Success) {
            val d = result.data
            if (d is WalletBalanceData.Eth) {
                return d.amount
            }
        }
        return null
    }

    suspend fun createUserOpAndEstimateGas(
        to: String,
        amount: BigInteger
    ): DAuthResult<CreateUserOpAndEstimateGasData> {
        return sdk.createUserOpAndEstimateGas(
            to,
            amount,
            byteArrayOf()
        )
    }

    private fun fetchErc20Tokens() {
        viewModelScope.launch {
            val addressResult = sdk.queryWalletAddress()
            if (addressResult is DAuthResult.Success) {
                val address = addressResult.data.aaAddress
                val erc20Tokens = getEnv().erc20Tokens
                val results = mutableListOf<Pair<String,String>>()
                erc20Tokens.forEachIndexed { _, pair ->
                    val tokenName = pair.first
                    val contractAddress = pair.second
                    val erc20Result =
                        sdk.queryWalletBalance(address, TokenType.ERC20(contractAddress))
                    if (erc20Result is DAuthResult.Success) {
                        val erc20 = erc20Result.data as WalletBalanceData.ERC20
                        results.add(tokenName to erc20.amount.toString())
                    }
                }
                if (results.isNotEmpty()) {
                    _tokenInfoList.value = results.map {
                        BuyAndSellPageEntity.TokenInfo(
                            it.first,
                            it.second,
                            "",
                            "",
                            "",
                            DigitalCurrencyListRes.CryptoInfo()
                        )
                    }
                }
            }
        }
    }
}