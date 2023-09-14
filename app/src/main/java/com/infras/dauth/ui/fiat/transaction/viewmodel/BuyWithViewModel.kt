package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyWithPageInputEntity
import com.infras.dauth.entity.PayMethodChooseListEntity
import com.infras.dauth.ext.addressForShort
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.util.awaitLite
import com.infras.dauthsdk.login.model.OrderCreateParam
import kotlinx.coroutines.launch

class BuyWithViewModel : BaseViewModel() {

    private val repo = FiatTxRepository()

    lateinit var input: BuyWithPageInputEntity
        private set

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> = _address

    private val _payMethods = MutableLiveData<List<PayMethodChooseListEntity>>()
    val payMethods: LiveData<List<PayMethodChooseListEntity>> = _payMethods

    var selectMethod: Int? = null

    fun attachInput(input: BuyWithPageInputEntity) {
        this.input = input
    }

    private fun fetchAddress() {
        viewModelScope.launch {
            val accountAddress = AccountManager.getAccountAddress()
            accountAddress?.let {
                _address.value = "Receive Wallet: ${it.addressForShort()}"
            }
        }
    }

    private fun updateList() {
        input.fiat_info.payMethodInfoList.orEmpty().map {
            PayMethodChooseListEntity(
                it,
                "???"
            )
        }.let { data ->
            data.forEachIndexed { index, payMethodChooseListEntity ->
                payMethodChooseListEntity.isSelected = (index == selectMethod)
            }
            _payMethods.value = data
        }
    }

    fun updateUI() {
        fetchAddress()
        updateList()
    }

    fun selectItem(item: PayMethodChooseListEntity) {
        val value = payMethods.value ?: return
        val index = value.indexOfFirst {
            it.payMethodInfo.payMethodId == item.payMethodInfo.payMethodId
        }
        if (index != -1) {
            selectMethod = index
        }
        updateList()
    }

    fun buy() {
        viewModelScope.launch {
            val sel = selectMethod
            if (sel == null) {
                toast("select pay method")
                return@launch
            }

            val method = payMethods.value.orEmpty()[sel]
            val token = input.crypto_info.cryptoCode.orEmpty()
            val walletAddress = address.value.orEmpty()
            val chainId = AccountManager.sdk.getWeb3j().ethChainId().awaitLite {
                it.chainId
            }
            if (chainId == null) {
                toast("get chainId error")
                return@launch
            }

            repo.orderCreate(
                OrderCreateParam(
                    paymethod_id = method.payMethodInfo.payMethodId.orEmpty(),
                    withdraw_address = walletAddress,
                    amount = input.buyAmount,
                    chain_id = chainId.toString(),
                    quote_type = "AMOUNT",
                    asset_type = "CRYPTO",
                    trade_type = "FIAT",
                    asset_name = token,
                    quote_asset_name = token
                )
            )
        }

    }
}