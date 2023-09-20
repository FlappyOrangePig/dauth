package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyWithPageInputEntity
import com.infras.dauth.entity.PayMethodChooseListEntity
import com.infras.dauth.ext.addressForShort
import com.infras.dauth.manager.AccountManager
import com.infras.dauth.manager.AppManagers
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.util.awaitLite
import com.infras.dauthsdk.login.model.OrderCreateParam
import kotlinx.coroutines.launch

class BuyWithViewModel : BaseViewModel() {

    private val repo = FiatTxRepository()

    lateinit var input: BuyWithPageInputEntity
        private set
    private val resourceManager get() = AppManagers.resourceManager

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> = _address

    private val _payMethods = MutableLiveData<List<PayMethodChooseListEntity>>()
    val payMethods: LiveData<List<PayMethodChooseListEntity>> = _payMethods

    private val _createdOrderIdState = MutableLiveData<String>()
    val createdOrderIdState: LiveData<String> = _createdOrderIdState

    var selectMethod: Int? = null

    fun attachInput(input: BuyWithPageInputEntity) {
        this.input = input
    }

    fun getTokenCode(): String {
        return if (input.isAmount) {
            input.fiat_info.fiatCode.orEmpty()
        } else {
            input.crypto_info.cryptoCode.orEmpty()
        }
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
            val walletAddress = AccountManager.getAccountAddress()
            if (walletAddress.isNullOrEmpty()) {
                toast("address error")
                return@launch
            }

            val chainId = AccountManager.sdk.getWeb3j().ethChainId().awaitLite {
                it.chainId
            }
            if (chainId == null) {
                toast("get chainId error")
                return@launch
            }

            val r = showLoading {
                repo.orderCreate(
                    OrderCreateParam(
                        trade_model = "FAST",
                        quote_type = if (input.isAmount) "AMOUNT" else "QUANTITY",
                        fiat_code = input.fiat_info.fiatCode.orEmpty(),
                        crypto_code = token,
                        amount = input.buyCount,
                        paymethod_id = method.payMethodInfo.payMethodId.orEmpty(),
                        withdraw_address = walletAddress,
                        chain_id = chainId.toString(),
                    )
                )
            }
            toast(resourceManager.getResponseDigest(r))
            if (r != null && r.isSuccess()){
                val orderId = r.data?.orderId.orEmpty()
                if (orderId.isNotEmpty()){
                    _createdOrderIdState.value = orderId
                }
            }
        }
    }
}