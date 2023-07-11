package com.cyberflow.dauthsdk.wallet.impl.manager

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.CreateWalletData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.login.model.GetParticipantsRes
import com.cyberflow.dauthsdk.login.model.GetParticipantsRes.Companion.getHookedGetKeyUrl
import com.cyberflow.dauthsdk.login.model.GetSecretKeyParam
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.MpcKeyIds
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.MpcServers
import com.cyberflow.dauthsdk.wallet.ext.digest
import com.cyberflow.dauthsdk.wallet.impl.manager.task.CreateWalletTask
import com.cyberflow.dauthsdk.wallet.impl.manager.task.RestoreWalletTask
import com.cyberflow.dauthsdk.wallet.util.AssertUtil
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2
import com.cyberflow.dauthsdk.wallet.util.sha3
import java.lang.RuntimeException

sealed class KeysToRestoreResult {
    class KeyInfo(
        val k1: String,
        val k2: String,
        val mergeResult: String
    ) : KeysToRestoreResult()

    object CannotRestore : KeysToRestoreResult()
    object NetworkError : KeysToRestoreResult()
}

/**
 * 创建钱包步骤：
 * 1.创建密钥
 * 2.密钥求和
 * 3.生成AA和EOA账号
 * 5.绑定钱包地址
 * 6.拉取MPC服务节点 & 分发密钥
 * 7.移除本地的远端密钥（成功）
 */
class WalletManager {
    companion object {
        private const val TAG = "WalletManager"

        const val STATE_INIT = 0
        const val STATE_KEY_GENERATED = 1
        const val STATE_OK = 2
    }

    private val walletPrefsV2 get() = Managers.walletPrefsV2
    private val loginPrefs get() = Managers.loginPrefs
    private val mpcKeyStore get() = Managers.mpcKeyStore

    fun getState(): Int {
        val state = walletPrefsV2.getWalletState()
        DAuthLogger.d("state=$state", TAG)
        return state
    }

    fun clearData() {
        walletPrefsV2.clear()
        mpcKeyStore.clear()
    }

    suspend fun initWallet(forceCreate: Boolean): DAuthResult<CreateWalletData> {
        DAuthLogger.d("createWallet", TAG)
        val mpcApi = RequestApiMpc()

        val state = getState()
        DAuthLogger.d("state=$state", TAG)
        // 钱包已OK，不需要创建
        if (state == STATE_OK) {
            val data = CreateWalletData(Managers.walletPrefsV2.getAaAddress())
            return DAuthResult.Success(data)
        }

        // 拉取MPC服务器信息
        val participantsResult = MpcServers.getServers()
        if (participantsResult == null) {
            DAuthLogger.d("participantsResult error", TAG)
            return DAuthResult.SdkError()
        }
        val participants = participantsResult.participants
        DAuthLogger.d("participants=$participants", TAG)

        val restoreKeyInfo = if (forceCreate) {
            null
        } else {
            when (state) {
                STATE_KEY_GENERATED -> {
                    null
                }

                STATE_INIT -> {
                    when (val keysResult = getKeysToRestore(mpcApi, participants)) {
                        is KeysToRestoreResult.NetworkError -> {
                            DAuthLogger.e("network error when check restore", TAG)
                            return DAuthResult.SdkError()
                        }

                        is KeysToRestoreResult.CannotRestore -> {
                            null
                        }

                        is KeysToRestoreResult.KeyInfo -> {
                            keysResult
                        }
                    }
                }

                else -> {
                    throw RuntimeException("cannot happen")
                }
            }
        }

        DAuthLogger.d("restoreKeyInfo=$restoreKeyInfo", TAG)

        return if (restoreKeyInfo != null) {
            RestoreWalletTask(
                restoreKeyInfo
            ).execute()
        } else {
            CreateWalletTask(
                loginPrefs,
                participants
            ).execute()
        }
    }

    private suspend fun getKeysToRestore(
        mpcApi: RequestApiMpc,
        participants: List<GetParticipantsRes.Participant>
    ): KeysToRestoreResult {
        val dauthParticipant = participants.find { it.id == MpcKeyIds.KEY_INDEX_DAUTH_SERVER }
        if (dauthParticipant == null || !dauthParticipant.isValid()) {
            DAuthLogger.d("dauthParticipant invalid", TAG)
            return KeysToRestoreResult.CannotRestore
        }

        val appParticipant = participants.find { it.id == MpcKeyIds.KEY_INDEX_APP_SERVER }
        if (appParticipant == null || !appParticipant.isGetAndSetValid()) {
            DAuthLogger.d("appParticipant invalid", TAG)
            return KeysToRestoreResult.CannotRestore
        }

        DAuthLogger.d("check keys...", TAG)
        // 检查密钥
        // k1
        val k1Result =
            mpcApi.getKey(
                dauthParticipant.get_key_url,
                GetSecretKeyParam.TYPE_KEY
            )
        if (k1Result == null) {
            DAuthLogger.d("k1Result error", TAG)
            return KeysToRestoreResult.NetworkError
        }
        val k1 = k1Result.data.orEmpty()
        DAuthLogger.d("k1:${k1.digest()}", TAG)
        if (k1.isEmpty()) {
            DAuthLogger.d("k1 empty", TAG)
            return KeysToRestoreResult.CannotRestore
        }

        // k2
        val k2Result =
            mpcApi.getKey(
                appParticipant.getHookedGetKeyUrl(),
                GetSecretKeyParam.TYPE_KEY
            )
        if (k2Result == null) {
            DAuthLogger.d("k2Result error", TAG)
            return KeysToRestoreResult.NetworkError
        }
        val k2 = k2Result.data.orEmpty()
        DAuthLogger.d("k2:${k2.digest()}", TAG)
        if (k2.isEmpty()) {
            DAuthLogger.d("k2 empty", TAG)
            return KeysToRestoreResult.CannotRestore
        }

        // mr
        val mrResult = mpcApi.getKey(
            dauthParticipant.get_key_url,
            GetSecretKeyParam.TYPE_MERGE_RESULT
        )
        if (mrResult == null) {
            DAuthLogger.d("mrResult error", TAG)
            return KeysToRestoreResult.NetworkError
        }
        val mr = mrResult.data.orEmpty()
        DAuthLogger.d("mr:${mr.digest()}", TAG)
        if (mr.isEmpty()) {
            DAuthLogger.d("mr empty", TAG)
            return KeysToRestoreResult.CannotRestore
        }

        DAuthLogger.d("can restore", TAG)
        return KeysToRestoreResult.KeyInfo(k1, k2, mr)
    }
}