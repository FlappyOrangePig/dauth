package com.infras.dauthsdk.wallet.impl.manager

import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.DAuthStageEnum
import com.infras.dauthsdk.login.model.LogReportParam
import com.infras.dauthsdk.login.model.LogReportParam.Event
import com.infras.dauthsdk.login.network.RequestApi
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.util.MoshiUtil
import com.infras.dauthsdk.wallet.ext.DAuthIoScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class StatsManager internal constructor(
    private val requestApi: RequestApi
) {

    companion object {
        private const val TAG = "StatsManager"
        private const val SEND_INTERVAL = 5000L
    }

    private val queue = ArrayList<Event>()
    private val clientId get() = DAuthSDK.impl.config.clientId.orEmpty()
    @Volatile
    private var job: Job? = null
    private val featureOpen = true

    fun initialize() {
        if (!featureOpen) return
        DAuthLogger.v("initialize", TAG)
        startSenderCoroutine()
    }

    fun sendStats(event: Event) {
        if (!featureOpen) return
        DAuthLogger.v("sendStats $event", TAG)
        synchronized(this) {
            queue.add(event)
        }
    }

    private fun startSenderCoroutine() {
        job ?: DAuthIoScope.launch {
            while (true) {
                delay(SEND_INTERVAL)
                synchronized(this@StatsManager) {
                    ArrayList<Event>().also {
                        it.addAll(queue)
                        queue.clear()
                    }
                }.let {
                    DAuthLogger.v("check queue ${it.size}", TAG)
                    if (it.isNotEmpty()) {
                        realSend(it)
                    }
                }
            }
        }.also { job = it }
    }

    private fun realSend(events: List<Event>) {
        DAuthLogger.v("realSend ${events.size}", TAG)
        DAuthIoScope.launch {
            LogReportParam.Data(
                deviceId = Managers.deviceId,
                clientId = clientId,
                user_id = Managers.loginPrefs.getAuthId(),
                env = when (DAuthSDK.impl.config.stage) {
                    DAuthStageEnum.STAGE_LIVE -> "prod"
                    DAuthStageEnum.STAGE_TEST -> "test"
                    else -> "test"
                },
                timestamp = (System.currentTimeMillis() / 1000L).toString(),
                events = events,
            ).let {
                MoshiUtil.toJson(it)
            }.let {
                LogReportParam(it)
            }.let {
                DAuthLogger.d("realSend ${it.data}", TAG)
                requestApi.sendLogReport(it)
            }
        }
    }
}