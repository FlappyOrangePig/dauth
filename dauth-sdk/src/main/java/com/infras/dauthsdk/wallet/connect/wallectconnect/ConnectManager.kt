package com.infras.dauthsdk.wallet.connect.wallectconnect

import android.content.Context
import com.infras.dauthsdk.login.utils.DAuthLogger
//import com.walletconnect.android.Core
//import com.walletconnect.android.CoreClient
//import com.walletconnect.android.internal.common.exception.WalletConnectException
//import com.walletconnect.android.relay.ConnectionType
//import com.walletconnect.sign.client.Sign
//import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex

private const val TAG = "ConnectManager"

internal class ConnectManager internal constructor(private val context: Context) {

    private val coroutineContext =
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            DAuthLogger.e(throwable.stackTraceToString(), TAG)
        }
    private val coroutineScope = CoroutineScope(coroutineContext)

    private val _accountAddress = MutableSharedFlow<String?>()
    val accountAddress = _accountAddress.asSharedFlow()

    private val sessionTopics = HashSet<String>()
    private val topicsLock = Mutex() // protect sessionTopics

    @Volatile
    private var clearJob : Job? = null

//    fun sdkInit() {
//        try {
//            initSdkInner()
//        } catch (e: Exception) {
//            DAuthLogger.e(e.stackTraceToString(), TAG)
//        }
//    }

//    private fun scheduleReleaseSessionsTask() {
//        DAuthLogger.d("scheduleReleaseSessionsTask", TAG)
//        clearJob?.cancel()
//        clearJob = coroutineScope.launch(Dispatchers.IO) {
//            delay(1_000)
//
//            DAuthLogger.v("release", TAG)
//            clearSessionTopic().forEach { topic ->
//                DAuthLogger.d("close $topic")
//
//                val param = Sign.Params.Disconnect(topic)
//                SignClient.disconnect(param,
//                    onSuccess = {
//                        DAuthLogger.e("disconnect onSuccess", TAG)
//                    },
//                    onError = {
//                        DAuthLogger.e(
//                            "disconnect onError ${it.throwable.stackTraceToString()}",
//                            TAG
//                        )
//                    })
//            }
//        }
//    }
//
//    private suspend fun clearSessionTopic(): List<String> {
//        val removed = topicsLock.withLock {
//            val topics = sessionTopics.toList()
//            sessionTopics.clear()
//            topics
//        }
//        DAuthLogger.d("clear session topics $removed", TAG)
//        return removed
//    }
//
//    private suspend fun addSessionTopic(topic: String) {
//        DAuthLogger.d("add session topic $topic", TAG)
//        topicsLock.withLock {
//            sessionTopics.add(topic)
//        }
//        scheduleReleaseSessionsTask()
//    }
//
//    private fun initSdkInner() {
//        DAuthLogger.d("init sdk", TAG)
//        val app = context.applicationContext as Application
//
//        // Get Project ID at https://cloud.walletconnect.com/
//        val projectId = "a6c17cb5bfc0634caee2819de0eafb80"
//        val relayUrl = "relay.walletconnect.com"
//        val serverUrl = "wss://$relayUrl?projectId=$projectId"
//        DAuthLogger.d("serverUrl=$serverUrl", TAG)
//        val connectionType = ConnectionType.AUTOMATIC
//        val appMetaData = Core.Model.AppMetaData(
//            name = "Vico",
//            description = "vico description",
//            url = "https://www.huya.com/"/*"Dapp URL"*/,
//            icons = listOf("")/*list of icon url strings*/,
//            redirect = null/*"kotlin-dapp-wc:/request"*/ // Custom Redirect URI
//        )
//
//        CoreClient.initialize(
//            relayServerUrl = serverUrl,
//            connectionType = connectionType,
//            application = app,
//            metaData = appMetaData
//        ) {
//            DAuthLogger.e("core onError ${it.throwable.stackTraceToString()}", TAG)
//        }
//
//        val init = Sign.Params.Init(core = CoreClient)
//
//        SignClient.initialize(init) {
//            DAuthLogger.e("sign onError ${it.throwable.stackTraceToString()}", TAG)
//        }
//        registerListener()
//    }
//
//    private fun registerListener() {
//        DAuthLogger.d("register listener", TAG)
//        DAppDelegate.wcEventModels.onEach {
//            DAuthLogger.d("on event $it", TAG)
//            when (it) {
//                is Sign.Model.ApprovedSession -> {
//                    addSessionTopic(it.topic)
//                    val destAccount = it.accounts.firstOrNull()
//                    DAuthLogger.d("_accountAddress -> $destAccount", TAG)
//                    val address = getAddressInWalletConnectAccount(destAccount)
//                    _accountAddress.emit(address)
//                }
//                is Sign.Model.UpdatedSession -> addSessionTopic(it.topic)
//                else -> {}
//            }
//        }.launchIn(coroutineScope)
//    }
//
//    @Throws(WalletConnectException::class)
//    suspend fun connect(): Boolean {
//        val sessionNamespaceKey = Chains.ETHEREUM_MAIN.chainNamespace
//        val sessionChains = listOf(Chains.ETHEREUM_MAIN.chainId)
//        val sessionMethods = listOf<String>()
//        val sessionEvents = listOf<String>()
//        val proposalNamespace =
//            Sign.Model.Namespace.Proposal(sessionChains, sessionMethods, sessionEvents)
//        val proposalNamespaces =
//            mapOf(sessionNamespaceKey to proposalNamespace)
//
//        DAuthLogger.d("connect", TAG)
//
//        val pairs = CoreClient.Pairing.getPairings()
//        val pair = runCatchingWithLog {
//            if (pairs.isEmpty()) {
//                DAuthLogger.d("create", TAG)
//                val created = CoreClient.Pairing.create { error ->
//                    // WalletConnect比较懒，onError时仍然能创建成功，我们要帮他处理
//                    throw RuntimeException(error.throwable)
//                }
//                created
//            } else {
//                DAuthLogger.d("get", TAG)
//                pairs.first()
//            }
//        } ?: run {
//            DAuthLogger.d("get pair null", TAG)
//            return false
//        }
//
//        DAuthLogger.d("pair=$pair", TAG)
//
//        val connectParams = withContext(Dispatchers.IO) {
//            Sign.Params.Connect(
//                namespaces = proposalNamespaces,
//                optionalNamespaces = null,
//                properties = null,
//                pairing = pair
//            )
//        }
//
//        val r = suspendCancellableCoroutine { continuation ->
//            DAuthLogger.d("SignClient connect", TAG)
//            SignClient.connect(connectParams, onSuccess = {
//                DAuthLogger.d("connect onSuccess", TAG)
//                val deeplinkPairingUri = pair.uri.replace("wc:", "wc://")
//                DAuthLogger.d("deep link=$deeplinkPairingUri", TAG)
//                try {
//                    context.startActivity(
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            deeplinkPairingUri.toUri()
//                        ).addFlags(FLAG_ACTIVITY_NEW_TASK)
//                    )
//                } catch (exception: ActivityNotFoundException) {
//                    DAuthLogger.e(exception.stackTraceToString(), TAG)
//                }
//                continuation.resume(true)
//            }, onError = {
//                DAuthLogger.d("connect onError ${it.throwable.message}", TAG)
//                kotlin.runCatching {
//                    continuation.resume(false)
//                }
//            })
//        }
//
//        DAuthLogger.d("connect result=$r", TAG)
//        return true
//    }
//
//    private fun getAddressInWalletConnectAccount(account: String?): String? {
//        account ?: return null
//        val index = account.indexOf("0x")
//        if (index == -1) {
//            return null
//        }
//        return account.substring(index)
//    }
}