package com.infras.dauth.ui.eoa

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.util.HideApiUtil
import com.infras.dauth.util.LogUtil
import com.infras.dauthsdk.api.IEoaWalletApi
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.Transaction1559
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EoaBusinessViewModel : BaseViewModel() {

    companion object {
        private const val TAG = "EoaBusinessViewModel"
        private const val INIT_ACCOUNT_INFO = "no account"
    }

    private val api: IEoaWalletApi get() = HideApiUtil.getEoaApi()
    private val _textState = mutableStateOf(INIT_ACCOUNT_INFO)
    val textState: State<String> = _textState

    init {
        viewModelScope.launch {
            val address = api.getEoaWalletAddress()
            if (address is DAuthResult.Success) {
                _textState.value = address.data
            }
        }
    }

    fun connect(activity: EoaBusinessActivity) {
        viewModelScope.launch {
            LogUtil.d(TAG, "connect >>")
            val result = api.connectMetaMask(activity)
            LogUtil.d(TAG, "connect << $result")
            when (result) {
                is DAuthResult.Success -> {
                    _textState.value = result.data
                    toast("connect success:${result.data}")
                }

                else -> {
                    toast("connect failure:${result.getError()}")
                }
            }
        }
    }

    fun getAddress() {
        viewModelScope.launch {
            LogUtil.d(TAG, "getAddress >>")
            val result = api.getEoaWalletAddress()
            LogUtil.d(TAG, "getAddress << $result")
            when (result) {
                is DAuthResult.Success -> {
                    toast("getAddress success:${result.data}")
                }

                else -> {
                    toast("getAddress failure:${result.getError()}")
                }
            }
        }
    }

    fun personalSign(activity: EoaBusinessActivity) {
        viewModelScope.launch {
            LogUtil.d(TAG, "personalSign >>")
            val message = "我是你大爷"
            val result = api.personalSign(activity, message)
            LogUtil.d(TAG, "personalSign << $result")
            when (result) {
                is DAuthResult.Success -> {
                    toast("personalSign success:${result.data}")
                }

                else -> {
                    toast("personalSign failure:${result.getError()}")
                }
            }
        }
    }

    fun sendTransaction(activity: EoaBusinessActivity) {
        viewModelScope.launch {
            LogUtil.d(TAG, "sendTransaction >>")
            val tx = Transaction1559(
                from = "0xd3Ca5938af1Cce97A4B45ea775E8a291eF53BA8C",
                to = "0xd3Ca5938af1Cce97A4B45ea775E8a291eF53BA8C",
                value = "0x21000",
                /*gas = "0x5028",
                maxPriorityFeePerGas = "0x3b9aca00",
                maxFeePerGas = "0x2540be400",*/
                data = "0x",
            )
            val result = api.sendTransaction(activity, tx)
            LogUtil.d(TAG, "sendTransaction << $result")
            when (result) {
                is DAuthResult.Success -> {
                    toast("sendTransaction success:${result.data.txHash}")
                }

                else -> {
                    toast("sendTransaction failure:${result.getError()}")
                }
            }
        }
    }
}