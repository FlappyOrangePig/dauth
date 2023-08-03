package com.infras.dauthsdk.wallet.connect

import com.infras.dauthsdk.login.utils.DAuthLogger
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "DappDelegate"

object DAppDelegate : SignClient.DappDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Sign.Model> = MutableSharedFlow()
    val wcEventModels: SharedFlow<Sign.Model> = _wcEventModels.asSharedFlow()

    init {
        SignClient.setDappDelegate(this)
    }

    override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
        scope.launch {
            _wcEventModels.emit(approvedSession)
        }
    }

    override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
        scope.launch {
            _wcEventModels.emit(rejectedSession)
        }
    }

    override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {
        scope.launch {
            _wcEventModels.emit(updatedSession)
        }
    }

    override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {
        scope.launch {
            _wcEventModels.emit(sessionEvent)
        }
    }

    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onSessionExtend(session: Sign.Model.Session) {
        scope.launch {
            _wcEventModels.emit(session)
        }
    }

    override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
        scope.launch {
            _wcEventModels.emit(response)
        }
    }

    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        DAuthLogger.i("onConnectionStateChange($state)", TAG)
    }

    override fun onError(error: Sign.Model.Error) {
        DAuthLogger.i("onError ${error.throwable.stackTraceToString()}", TAG)
    }
}