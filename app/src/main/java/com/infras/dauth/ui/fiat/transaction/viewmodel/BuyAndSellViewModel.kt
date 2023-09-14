package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyAndSellPageEntity
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import kotlinx.coroutines.launch

class BuyAndSellViewModel : BaseViewModel() {

    private var _verifyChannelExists = MutableLiveData<Boolean>()
    val verifyChannelExists: LiveData<Boolean> = _verifyChannelExists

    private val _tokenInfoOfTagList = mutableStateOf(getBuyAndSellPageEntity(listOf(), listOf()))
    val tokenInfoOfTagList: State<BuyAndSellPageEntity> = _tokenInfoOfTagList

    private val repo = FiatTxRepository()

    fun fetchAccountDetail() = viewModelScope.launch {
        val r = repo.accountDetail()
        if (r != null) {
            when {
                r.isSuccess() -> {
                    r.data != null
                    _verifyChannelExists.value = (r.data != null)
                }

                r.verifyChannelExists() -> {
                    _verifyChannelExists.value = false
                }
            }
        }
    }

    fun fetchCurrencyList() = viewModelScope.launch {
        val r = repo.currencyList()
        if (r != null && r.isSuccess()) {
            val cryptoList = r.data?.crypto_info ?: listOf()
            val tokenInfoList = cryptoList.map {
                BuyAndSellPageEntity.TokenInfo(
                    name = it.crypto_code.orEmpty(),
                    issuer = "tether",
                    avatarUrl = it.crypto_icon.orEmpty(),
                    changeRange = "+10%",
                    "$111",
                    it
                )
            }
            val fiatList = r.data?.fiat_info ?: listOf()
            _tokenInfoOfTagList.value = getBuyAndSellPageEntity(tokenInfoList, fiatList)
        }
    }

    private fun getBuyAndSellPageEntity(
        tokenInfoList: List<BuyAndSellPageEntity.TokenInfo>,
        fiatList: List<DigitalCurrencyListRes.FiatInfo>,
    ): BuyAndSellPageEntity {
        val list = listOf(
            BuyAndSellPageEntity.TokenInfoOfTag(
                tag = BuyAndSellPageEntity.TagsEntity(
                    title = "All",
                    onClick = {}
                ),
                tokenInfoList = tokenInfoList
            ),
            BuyAndSellPageEntity.TokenInfoOfTag(
                tag = BuyAndSellPageEntity.TagsEntity(
                    title = "Recent",
                    onClick = {}
                ),
                tokenInfoList = listOf()
            ),
        )

        return BuyAndSellPageEntity(
            list,
            fiatList,
            if (fiatList.isEmpty()) null else 0
        )
    }

    fun updateSelect(index: Int) {
        val value = tokenInfoOfTagList.value
        val newValue = BuyAndSellPageEntity(
            buyTab = value.buyTab,
            fiatList = value.fiatList,
            fiatSelectIndex = index
        )
        _tokenInfoOfTagList.value = newValue
    }
}