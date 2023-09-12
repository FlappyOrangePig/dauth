package com.infras.dauth.ui.transaction.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyAndSellPageEntity
import com.infras.dauth.util.HideApiUtil
import com.infras.dauthsdk.login.model.AccountDetailRes
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BuyAndSellViewModel : BaseViewModel() {

    private var _accountDetail = MutableLiveData<AccountDetailRes.Data?>()
    val accountDetail: LiveData<AccountDetailRes.Data?> = _accountDetail

    private val _toastEvent = Channel<String>(capacity = Channel.UNLIMITED)
    val toastEvent: Flow<String> = _toastEvent.receiveAsFlow()

    private val _tokenInfoOfTagList = mutableStateOf(getBuyAndSellPageEntity(listOf(), listOf()))
    val tokenInfoOfTagList: State<BuyAndSellPageEntity> = _tokenInfoOfTagList

    fun fetchAccountDetail() = viewModelScope.launch {
        val r = HideApiUtil.getDepositApi().accountDetail()
        if (r != null && r.isSuccess()) {
            _accountDetail.value = r.data
        }
    }

    fun fetchCurrencyList() = viewModelScope.launch {
        val r = HideApiUtil.getDepositApi().currencyList()
        if (r != null && r.isSuccess()) {
            val cryptoList = r.data?.crypto_list ?: listOf()
            val tokenInfoList = cryptoList.map {
                BuyAndSellPageEntity.TokenInfo(
                    name = it.crypto_code.orEmpty(),
                    issuer = "tether",
                    avatarUrl = "https://img1.baidu.com/it/u=1535503495,3105965414&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
                    changeRange = "+10%",
                    "$111",
                    it
                )
            }
            val fiatList = r.data?.fiat_list ?: listOf()
            _tokenInfoOfTagList.value = getBuyAndSellPageEntity(tokenInfoList, fiatList)
        }
    }

    private fun getBuyAndSellPageEntity(
        tokenInfoList: List<BuyAndSellPageEntity.TokenInfo>,
        fiatList: List<DigitalCurrencyListRes.Fiat_list>,
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