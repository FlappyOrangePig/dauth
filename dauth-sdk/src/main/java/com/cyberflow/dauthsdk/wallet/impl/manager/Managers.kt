package com.cyberflow.dauthsdk.wallet.impl.manager

import android.annotation.SuppressLint
import android.content.Context
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.wallet.connect.ConnectManager
import com.cyberflow.dauthsdk.wallet.impl.ConfigurationManager
import com.cyberflow.dauthsdk.wallet.impl.Web3Manager
import com.cyberflow.dauthsdk.wallet.util.DeviceUtil
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2

/**
 * Managers
 * 统一维护与Context等有关的组件，方便单元测试时mock这些组件
 * @constructor Create empty Managers
 */
@SuppressLint("StaticFieldLeak")
internal object Managers {
    lateinit var loginPrefs: LoginPrefs
    lateinit var mpcKeyStore: MpcKeyStore
    lateinit var walletPrefsV2: WalletPrefsV2
    lateinit var walletManager: WalletManager
    lateinit var connectManager: ConnectManager
    lateinit var requestApi: RequestApi
    lateinit var requestApiMpc: RequestApiMpc
    lateinit var web3m: Web3Manager
    lateinit var deviceId: String
    lateinit var crashManager: CrashManager
    lateinit var logManager: DLogManager

    fun inject(context: Context) {
        loginPrefs = LoginPrefs(context)
        mpcKeyStore = MpcKeyStore(context)
        walletPrefsV2 = WalletPrefsV2(context)
        walletManager = WalletManager()
        connectManager = ConnectManager(context)
        requestApi = RequestApi()
        requestApiMpc = RequestApiMpc()
        web3m = Web3Manager().also { it.reset(ConfigurationManager.chain().rpcUrl) }
        deviceId = DeviceUtil.getDeviceId(context)
        crashManager = CrashManager()
        logManager = DLogManager(context)
    }
}