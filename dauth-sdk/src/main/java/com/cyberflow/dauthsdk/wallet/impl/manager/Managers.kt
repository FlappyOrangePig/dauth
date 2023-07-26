package com.cyberflow.dauthsdk.wallet.impl.manager

import android.annotation.SuppressLint
import android.content.Context
import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
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
    lateinit var requestApi: RequestApi
    lateinit var requestApiMpc: RequestApiMpc
    lateinit var web3m: Web3Manager
    lateinit var deviceId: String

    fun inject(context: Context) {
        loginPrefs = LoginPrefs(context)
        mpcKeyStore = MpcKeyStore(context)
        walletPrefsV2 = WalletPrefsV2(context)
        walletManager = WalletManager()
        requestApi = RequestApi()
        requestApiMpc = RequestApiMpc()
        web3m = Web3Manager().also { it.reset(ConfigurationManager.urls().providerRpc) }
        deviceId = DeviceUtil.getDeviceId(context)
    }
}