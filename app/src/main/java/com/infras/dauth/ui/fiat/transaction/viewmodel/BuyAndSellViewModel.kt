package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyAndSellPageEntity
import com.infras.dauth.entity.KycBundledState
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauthsdk.login.model.CurrencyPriceParam
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import kotlinx.coroutines.launch

class BuyAndSellViewModel : BaseViewModel() {

    private var _kycBundledState = MutableLiveData<KycBundledState>()
    val kycBundledState: LiveData<KycBundledState> = _kycBundledState

    private val _tokenInfoOfTagList = mutableStateOf(getBuyAndSellPageEntity(listOf(), listOf()))
    val tokenInfoOfTagList: State<BuyAndSellPageEntity> = _tokenInfoOfTagList

    private val repo = FiatTxRepository()

    fun fetchAccountDetail() = viewModelScope.launch {
        val r = repo.accountDetail()
        if (r != null && r.isSuccess()) {
            r.data?.let {
                val newValue = KycBundledState(
                    kycState = it.detail?.state,
                    isBound = it.is_bind == 1
                )
                _kycBundledState.value = newValue
            }
        }
    }

    fun fetchCurrencyList() = viewModelScope.launch {
        val r = showLoading { repo.currencyList() }
        if (r != null && r.isSuccess()) {
            val cryptoList = r.data?.cryptoList ?: listOf()
            val tokenInfoList = cryptoList.map {
                BuyAndSellPageEntity.TokenInfo(
                    name = it.cryptoCode.orEmpty(),
                    issuer = it.cryptoIssuer.orEmpty(),
                    avatarUrl = it.cryptoIcon.orEmpty(),
                    changeRange = "+10%",
                    "$111",
                    it
                )
            }
            val fiatList = r.data?.fiatList ?: listOf()
            _tokenInfoOfTagList.value = getBuyAndSellPageEntity(tokenInfoList, fiatList)

            fetchPrice(fiatList, cryptoList)
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

    private suspend fun fetchPrice(
        fiatList: List<DigitalCurrencyListRes.FiatInfo>,
        cryptoList: List<DigitalCurrencyListRes.CryptoInfo>
    ) {
        val r = showLoading {
            repo.currencyPrice(
                CurrencyPriceParam(
                    fiat_list = fiatList.map { it.fiatCode }.first().toString(),
                    crypto_list = cryptoList.map { it.cryptoCode }.first().toString()
                )
            )
        }
    }
}