package com.infras.dauthsdk.login.network

import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.login.infrastructure.ApiClient
import com.infras.dauthsdk.login.infrastructure.ReqUrl
import com.infras.dauthsdk.login.infrastructure.RequestConfig
import com.infras.dauthsdk.login.model.CommitTransParam
import com.infras.dauthsdk.login.model.CommitTransRes
import com.infras.dauthsdk.login.model.GenerateKeyParam
import com.infras.dauthsdk.login.model.GenerateKeyRes
import com.infras.dauthsdk.login.model.GetParticipantsParam
import com.infras.dauthsdk.login.model.GetParticipantsRes
import com.infras.dauthsdk.login.model.GetSecretKeyParam
import com.infras.dauthsdk.login.model.GetSecretKeyRes
import com.infras.dauthsdk.login.model.SetSecretKeyParam
import com.infras.dauthsdk.login.model.SetSecretKeyRes
import com.infras.dauthsdk.login.model.WireUserOp
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.util.MoshiUtil
import com.infras.dauthsdk.wallet.impl.ConfigurationManager
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.sol.EntryPoint

object MpcServiceConst {
    const val NotAllowedError = 2000000
    const val RequestParamsError = 2000001
    const val DatabaseError = 2000002
    const val RedisError = 2000003
    const val RequestSignError = 2000004
    const val SessionExpiredError = 2000005
    const val ServiceInternalError = 2000006

    const val MpcSecretNotFoundError = 2000007
    const val MpcSecretInvalidError = 2000008
    const val MpcSecretWalletNotFoundError = 2000009
    const val MpcSecretSetError = 2000010
    const val MpcSecretAlreadyBoundError = 2000011
}

internal class RequestApiMpc internal constructor(): ApiClient() {

    companion object {
        private const val TAG = "RequestApiMpc"
    }

    suspend fun getParticipants(): GetParticipantsRes? {
        val localVariableConfig = RequestConfig(ReqUrl.PathUrl("/wallet/v1/participants/get"))
        return request(localVariableConfig, GetParticipantsParam())
    }

    suspend fun setKey(url: String, key: String, mergeResult: String?): SetSecretKeyRes? {
        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        val param = SetSecretKeyParam(
            keyshare = key,
            keyresult = mergeResult
        )
        return request(localVariableConfig, param)
    }

    suspend fun getKey(url: String, type: Int): GetSecretKeyRes? {
        if (ConfigurationManager.innerConfig.doNotRestore) {
            return GetSecretKeyRes("").apply { ret = 0 }
        }

        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        val param = GetSecretKeyParam(
            type
        )
        return request(localVariableConfig, param)
    }

    private fun getHost(useDev: Boolean) = if (useDev) {
        "http://172.16.12.170" // Tyler-local
    } else {
        "https://${ConfigurationManager.stage().baseUrlHost}"
    }

    suspend fun commitOp(userOperation: EntryPoint.UserOperation): CommitTransRes? {
        val openId = Managers.loginPrefs.getAuthId()

        val wireUserOp = WireUserOp(
            userOperation.sender,
            userOperation.nonce,
            userOperation.initCode,
            userOperation.callData,
            userOperation.callGasLimit,
            userOperation.verificationGasLimit,
            userOperation.preVerificationGas,
            userOperation.maxFeePerGas,
            userOperation.maxPriorityFeePerGas,
            userOperation.paymasterAndData,
            userOperation.signature
        )
        val transData = MoshiUtil.toJson(wireUserOp)
        DAuthLogger.d("transData=$transData", TAG)
        val config = DAuthSDK.impl.config
        val param = CommitTransParam(
            open_id = openId,
            transdata = transData,
            client_id = config.clientId.orEmpty()
        )
        val host = getHost(ConfigurationManager.innerConfig.useDevRelayerServer)
        val url = "${host}/relayer/committrans"
        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        return request(localVariableConfig, param)
    }

    suspend fun generateKeys(signbm: String): GenerateKeyRes? {
        val param = GenerateKeyParam(signbm)
        val host = getHost(ConfigurationManager.innerConfig.useDevKeyGenServer)
        val url = "${host}/mpc/genkey"
        val localVariableConfig = RequestConfig(reqUrl = ReqUrl.WholePathUrl(url))
        return request(localVariableConfig, param)
    }
}