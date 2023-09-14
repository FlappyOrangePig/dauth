package com.infras.dauth.ui.fiat.transaction.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyTokenPageInputEntity
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauthsdk.login.model.DigitalCurrencyListRes
import com.infras.dauthsdk.login.model.PaymentQuoteParam
import kotlinx.coroutines.launch
import java.math.BigDecimal

class BuyTokenViewModel : BaseViewModel() {

    lateinit var input: BuyTokenPageInputEntity
        private set

    private var _amount = mutableStateOf("0")
    val amount: State<String> = _amount
    private var _estimatedPrice = mutableStateOf("")
    val estimatedPrice: State<String> = _estimatedPrice

    private val repo = FiatTxRepository()

    fun attachInput(input: BuyTokenPageInputEntity) {
        this.input = input
        estimatePrice()
    }

    fun getCurrentFiatInfo(): DigitalCurrencyListRes.FiatInfo {
        return input.fiat_info[input.selectedFiatIndex]
    }

    fun updateAmount(newAmount: String) {
        _amount.value = newAmount
        estimatePrice()
    }


    private fun estimatePrice() {
        val currentAmount = amount.value
        val crypto = input.crypto_info.cryptoCode.orEmpty()
        if (BigDecimal(currentAmount) < BigDecimal("10")) {
            _estimatedPrice.value = "Enter a minimum of 10 $crypto"
            return
        }

        viewModelScope.launch {
            val fiatCode = getCurrentFiatInfo().fiatCode.orEmpty()
            val r = showLoading {
                val p = PaymentQuoteParam(
                    fiat_code = fiatCode,
                    crypto_code = crypto,
                    crypto_amount = currentAmount,
                    fiat_amount = "0"
                )
                repo.paymentQuote(p)
            }
            if (r != null && r.isSuccess()) {
                val d = r.data
                if (d != null) {
                    val fiatAmount = d.fiatAmount
                    _estimatedPrice.value = "Approx $fiatAmount $fiatCode"
                }
            }
        }
    }
}