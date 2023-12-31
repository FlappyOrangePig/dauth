package com.infras.dauth.ui.fiat.transaction.viewmodel

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyTokenPageEntity
import com.infras.dauth.entity.BuyTokenPageInputEntity
import com.infras.dauth.entity.BuyWithPageInputEntity
import com.infras.dauth.repository.FiatTxRepository
import com.infras.dauth.ui.fiat.transaction.util.AmountModeHolder
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil.scale
import com.infras.dauthsdk.login.model.PaymentQuoteParam
import kotlinx.coroutines.launch
import java.math.BigDecimal

class BuyTokenViewModel : BaseViewModel() {

    companion object {
        private const val INTERVAL = 1000L
    }

    private lateinit var input: BuyTokenPageInputEntity
    private val repo = FiatTxRepository()
    private var _pageData = mutableStateOf(BuyTokenPageEntity())
    val pageData: State<BuyTokenPageEntity> = _pageData
    private val fiatInfo get() = input.fiat_info[input.selectedFiatIndex]
    private val cryptoInfo get() = input.crypto_info
    private val cryptoCode get() = cryptoInfo.cryptoCode.orEmpty()
    private val fiatCode get() = fiatInfo.fiatCode.orEmpty()
    private val _selectPayMethodEvent = MutableLiveData<BuyWithPageInputEntity>()
    val selectPayMethodEvent: LiveData<BuyWithPageInputEntity> = _selectPayMethodEvent
    private var lastQuoteTick: Long = 0L
    private var pendingQuote: Boolean = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }

    fun attachInput(input: BuyTokenPageInputEntity) {
        this.input = input
        updatePageData(
            pageData.value.copy(
                cryptoCode = cryptoCode,
                fiatCode = fiatCode,
                inputValue = "0"
            )
        )
    }

    fun updateInputValue(inputValue: String) {
        updatePageData(
            pageData.value.copy(
                inputValue = inputValue,
            ),
            quote = true
        )
    }

    private fun quotePrice() {
        synchronized(this) {
            if (pendingQuote) {
                return
            }
            val l = lastQuoteTick
            val now = SystemClock.elapsedRealtime()
            val delta = now - l
            if (delta < INTERVAL) {
                pendingQuote = true
                restartQuoteTimer(INTERVAL - delta)
                return
            }
        }

        realQuote()
    }

    private fun realQuote() {
        synchronized(this) {
            pendingQuote = false
            lastQuoteTick = SystemClock.elapsedRealtime()
        }

        val curPageData = pageData.value
        viewModelScope.launch {
            val p = if (curPageData.isAmountMode) {
                PaymentQuoteParam(
                    fiat_code = curPageData.fiatCode,
                    crypto_code = curPageData.cryptoCode,
                    crypto_amount = null,
                    fiat_amount = curPageData.inputValue.removeSuffix("."),
                )
            } else {
                PaymentQuoteParam(
                    fiat_code = curPageData.fiatCode,
                    crypto_code = curPageData.cryptoCode,
                    crypto_amount = curPageData.inputValue.removeSuffix("."),
                    fiat_amount = null,
                )
            }
            val r = repo.paymentQuote(p)
            if (r != null && r.isSuccess()) {
                val d = r.data
                if (d != null) {
                    val fiatAmount = d.fiatAmount
                    val cryptoAmount = d.cryptoAmount

                    val quoteText = if (curPageData.isAmountMode) {
                        "Approx ${cryptoAmount.scale(fiatInfo.fiatPrecision.toInt())} $cryptoCode"
                    } else {
                        "Approx ${fiatAmount.scale(cryptoInfo.cryptoPrecision)} $fiatCode"
                    }
                    updatePageData(
                        pageData.value.copy(
                            estimatedPrice = quoteText
                        )
                    )
                }
            }
        }
    }

    private fun updatePageData(newValue: BuyTokenPageEntity, quote: Boolean = false) {
        val mh = modeHolder(newValue.isAmountMode)
        val keepX = mh.keepXDecimalPlace(newValue.inputValue)

        val (valid, tips) = mh.isInputValueValid(keepX)
        _pageData.value = if (valid < 0) {
            newValue.copy(inputValue = keepX, estimatedPrice = tips)
        } else if (valid > 0) {
            val max = mh.getMaxLimit()
            newValue.copy(inputValue = max)
        } else {
            newValue.copy(inputValue = keepX)
        }

        if (quote && valid == 0) {
            quotePrice()
        }
    }

    fun selectPayMethod(count: BigDecimal) {
        val fiat = fiatInfo
        val mh = modeHolder()
        val (valid, tips) = mh.isInputValueValid(count.toString())
        if (valid == 0) {
            _selectPayMethodEvent.value = BuyWithPageInputEntity(
                crypto_info = cryptoInfo,
                fiat_info = fiat,
                buyCount = count.toString(),
                isAmount = pageData.value.isAmountMode
            )
        } else {
            viewModelScope.launch {
                toast(tips)
            }
        }
    }

    fun switchMethod() {
        val lastMode = pageData.value.isAmountMode
        val created = pageData.value.copy(isAmountMode = !lastMode)
        updatePageData(created, quote = true)
    }

    private val runnable = Runnable { realQuote() }

    private fun restartQuoteTimer(delayMs: Long) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, delayMs)
    }

    private fun modeHolder(isAmountMode: Boolean = pageData.value.isAmountMode): AmountModeHolder {
        return AmountModeHolder(isAmountMode = isAmountMode,
            fiatInfo = fiatInfo,
            cryptoInfo = cryptoInfo,
        )
    }
}