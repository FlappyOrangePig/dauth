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
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil.scale
import com.infras.dauth.util.awaitLite
import com.infras.dauthsdk.login.model.OrderCreateParam
import com.infras.dauthsdk.login.model.OrderDetailParam
import com.infras.dauthsdk.login.model.PaymentQuoteParam
import com.infras.dauthsdk.login.model.QueryWithdrawConfParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _quoteState = MutableLiveData<String>()
    val quoteState: LiveData<String> = _quoteState

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
                ""
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
        realQuote()
        viewModelScope.launch {
            val cryptoCode = input.crypto_info.cryptoCode.orEmpty()
            val r = repo.queryWithdrawConf(QueryWithdrawConfParam(cryptoCode))
            if (r != null && r.isSuccess()) {

            }
        }
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

    fun buy(chainId: String?) {
        viewModelScope.launch {
            val sel = selectMethod
            if (sel == null) {
                toast("select pay method")
                return@launch
            }

            /*val queryConfResult = showLoading {
                val cryptoCode = input.crypto_info.cryptoCode.orEmpty()
                repo.queryWithdrawConf(QueryWithdrawConfParam(cryptoCode))
            }
            toast(resourceManager.getResponseDigest(queryConfResult))
            val skyPayChainId = if (queryConfResult != null && queryConfResult.isSuccess()) {
                val data = queryConfResult.data

            } else {
                return@launch
            }*/

            val method = payMethods.value.orEmpty()[sel]
            val token = input.crypto_info.cryptoCode.orEmpty()
            showLoading {
                val walletAddress = AccountManager.getAccountAddress()
                if (walletAddress.isNullOrEmpty()) {
                    toast("address error")
                } else {
                    val curChainId = chainId
                        ?: AccountManager.sdk.getWeb3j().ethChainId().awaitLite {
                            it.chainId
                        }?.toString()
                    if (curChainId == null) {
                        toast("get chainId error")
                    } else {
                        val r = repo.orderCreate(
                            OrderCreateParam(
                                trade_model = "FAST",
                                quote_type = if (input.isAmount) "AMOUNT" else "QUANTITY",
                                fiat_code = input.fiat_info.fiatCode.orEmpty(),
                                crypto_code = token,
                                amount = input.buyCount,
                                paymethod_id = method.payMethodInfo.payMethodId.orEmpty(),
                                withdraw_address = walletAddress,
                                chain_id = curChainId.toString(),
                            )
                        )
                        toast(resourceManager.getResponseDigest(r))
                        if (r != null && r.isSuccess()) {
                            val orderId = r.data?.orderId.orEmpty()
                            if (orderId.isNotEmpty()) {
                                // 等到订单OK才跳转
                                showLoading {
                                    var retry = 0
                                    while (true) {
                                        val detailRes = repo.orderDetail(OrderDetailParam(orderId))
                                        if (detailRes?.isSuccess() == true) {
                                            break
                                        } else {
                                            if (retry++ >= 5) {
                                                break
                                            }
                                            withContext(Dispatchers.IO) {
                                                delay(1000L)
                                            }
                                        }
                                    }
                                }
                                _createdOrderIdState.value = orderId
                            }
                        }
                    }
                }
            }
        }
    }

    private fun realQuote() {
        val fiatInfo = input.fiat_info
        val cryptoInfo = input.crypto_info

        viewModelScope.launch {
            val p = if (input.isAmount) {
                PaymentQuoteParam(
                    fiat_code = fiatInfo.fiatCode.orEmpty(),
                    crypto_code = cryptoInfo.cryptoCode.orEmpty(),
                    crypto_amount = null,
                    fiat_amount = input.buyCount,
                )
            } else {
                PaymentQuoteParam(
                    fiat_code = fiatInfo.fiatCode.orEmpty(),
                    crypto_code = cryptoInfo.cryptoCode.orEmpty(),
                    crypto_amount = input.buyCount,
                    fiat_amount = null,
                )
            }
            val r = repo.paymentQuote(p)
            if (r != null && r.isSuccess()) {
                val d = r.data
                if (d != null) {
                    val fiatAmount = d.fiatAmount
                    val cryptoAmount = d.cryptoAmount

                    val quoteText = if (input.isAmount) {
                        "You'll receive ${cryptoAmount.scale(cryptoInfo.cryptoPrecision)} ${cryptoInfo.cryptoCode}"
                    } else {
                        "You'll pay ${fiatAmount.scale(fiatInfo.fiatPrecision.toInt())} ${fiatInfo.fiatCode}"
                    }
                    _quoteState.value = quoteText
                }
            }
        }
    }

    fun fetchWithdrawConf(chainShortName: String) {
        viewModelScope.launch {
            val cryptoCode = input.crypto_info.cryptoCode.orEmpty()
            val r = showLoading {
                repo.queryWithdrawConf(QueryWithdrawConfParam(cryptoCode))
            }
            val info = if (r != null && r.isSuccess()) {
                val list = r.data?.list.orEmpty()
                list.firstOrNull {
                    it.chainShortName == chainShortName
                }
            } else {
                null
            }

            val skyPayChainId = info?.chainCoinId
            if (skyPayChainId != null) {
                buy(skyPayChainId.toString())
            }
        }
    }
}