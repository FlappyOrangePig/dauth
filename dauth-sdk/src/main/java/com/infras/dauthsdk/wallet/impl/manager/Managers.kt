package com.infras.dauthsdk.wallet.impl.manager

import android.annotation.SuppressLint
import android.content.Context
import com.infras.dauthsdk.login.network.RequestApi
import com.infras.dauthsdk.login.network.RequestApiFiat
import com.infras.dauthsdk.login.network.RequestApiMpc
import com.infras.dauthsdk.login.utils.LoginPrefs
import com.infras.dauthsdk.wallet.connect.wallectconnect.ConnectManager
import com.infras.dauthsdk.wallet.impl.ConfigurationManager
import com.infras.dauthsdk.wallet.impl.EoaWalletImpl
import com.infras.dauthsdk.wallet.impl.Web3Manager
import com.infras.dauthsdk.wallet.util.DeviceUtil
import com.infras.dauthsdk.wallet.util.WalletPrefsV2

/**
 * Managers
 * 统一维护与Context等有关的组件，方便单元测试时mock这些组件
 * @constructor Create empty Managers
 */
@SuppressLint("StaticFieldLeak")
internal object Managers {
    lateinit var context: Context
    lateinit var loginPrefs: LoginPrefs
    lateinit var walletManager: WalletManager
    lateinit var connectManager: ConnectManager
    lateinit var requestApi: RequestApi
    lateinit var requestApiMpc: RequestApiMpc
    lateinit var web3m: Web3Manager
    lateinit var deviceId: String
    lateinit var crashManager: CrashManager
    lateinit var logManager: DLogManager
    lateinit var fileManager: FileManager
    lateinit var eoaWalletApi: EoaWalletImpl
    lateinit var fiatApi: RequestApiFiat
    lateinit var preGenerateKeyManager: PreGenerateKeyManager
    lateinit var globalPrefsManager: GlobalPrefsManager
    lateinit var statsManager: StatsManager
    val mpcKeyStore get() = KeyStoreManager.getInstance(loginPrefs.getAuthId())
    val walletPrefsV2 get() = WalletPrefsV2(context, loginPrefs.getAuthId())

    fun inject(context: Context) {
        this.context = context
        loginPrefs = LoginPrefs(context)
        globalPrefsManager = GlobalPrefsManager(context)
        fileManager = FileManager(context)
        walletManager = WalletManager(
            loginPrefs = loginPrefs,
        )
        connectManager = ConnectManager(context)
        requestApi = RequestApi()
        requestApiMpc = RequestApiMpc()
        fiatApi = RequestApiFiat()
        web3m = Web3Manager().also { it.reset(ConfigurationManager.chain().rpcUrl) }
        deviceId = DeviceUtil.getDeviceId(context)
        logManager = DLogManager(fileManager)
        crashManager = CrashManager(logManager)
        eoaWalletApi = EoaWalletImpl()
        preGenerateKeyManager = PreGenerateKeyManager(fileManager, globalPrefsManager)
        statsManager = StatsManager(requestApi)
    }
}