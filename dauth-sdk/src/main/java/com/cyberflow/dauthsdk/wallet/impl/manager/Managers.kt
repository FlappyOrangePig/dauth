package com.cyberflow.dauthsdk.wallet.impl.manager

import com.cyberflow.dauthsdk.login.network.RequestApi
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2

internal object Managers {
    lateinit var loginPrefs: LoginPrefs
    lateinit var mpcKeyStore: MpcKeyStore
    lateinit var walletPrefsV2: WalletPrefsV2
    lateinit var walletManager: WalletManager
    lateinit var requestApi: RequestApi
    lateinit var requestApiMpc: RequestApiMpc

    fun inject() {
        loginPrefs = LoginPrefs()
        mpcKeyStore = MpcKeyStore()
        walletPrefsV2 = WalletPrefsV2()
        walletManager = WalletManager()
        requestApi = RequestApi()
        requestApiMpc = RequestApiMpc()
    }
}