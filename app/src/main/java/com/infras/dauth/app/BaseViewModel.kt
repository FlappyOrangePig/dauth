package com.infras.dauth.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

open class BaseViewModel : ViewModel() {

    private val _toastEvent = Channel<String>(capacity = Channel.UNLIMITED)
    val toastEvent: Flow<String> = _toastEvent.receiveAsFlow()
    private val _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    protected suspend fun <T> showLoading(block: suspend () -> T): T {
        _showLoading.value = true
        val r = block.invoke()
        _showLoading.value = false
        return r
    }

    protected suspend fun toast(toast: String) {
        _toastEvent.send(toast)
    }

    override fun onCleared() {
        super.onCleared()

        kotlin.runCatching {
            _toastEvent.close()
        }
    }
}