package com.infras.dauth.util

import com.infras.dauth.BuildConfig
import com.infras.dauthsdk.api.DAuthChainEnum
import com.infras.dauthsdk.api.DAuthStageEnum
import org.web3j.utils.Convert
import java.math.BigInteger


sealed class DAuthEnv {
    abstract val stage: Int
    abstract val chain: Int
    abstract val clientId: String
    abstract val chainName: String
    abstract val erc20Tokens: List<Erc20TokenInfo>

    object EnvProd : DAuthEnv() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_LIVE
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_ARBITRUM
        override val clientId: String
            get() = "ce475be992763ee5ff0c51faba901942"
        override val chainName: String
            get() = "Arbitrum One"
        override val erc20Tokens: List<Erc20TokenInfo>
            get() = listOf(
                Erc20TokenInfo.USDT(
                    name = "USDT",
                    address = "0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9",
                )
            )
    }

    object EnvDevArbitrumGoerli : DAuthEnv() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_TEST
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_ARBITRUM_GOERLI
        override val clientId: String
            get() = "b86df4abc13f3bba4d4b2057bc6df910"
        override val chainName: String
            get() = "Arbitrum Goerli"
        override val erc20Tokens: List<Erc20TokenInfo>
            get() = listOf()
    }

    object EnvDevGoerli : DAuthEnv() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_TEST
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_GOERLI
        override val clientId: String
            get() = "b86df4abc13f3bba4d4b2057bc6df910"
        override val chainName: String
            get() = "Goerli"
        override val erc20Tokens: List<Erc20TokenInfo>
            get() = listOf()
    }
}

internal fun getEnv(): DAuthEnv {
    return when (BuildConfig.IS_LIVE) {
        true -> DAuthEnv.EnvProd
        false -> DAuthEnv.EnvDevGoerli
    }
}

sealed class Erc20TokenInfo {
    abstract val name: String
    abstract val address: String
    abstract fun getDisplayBalance(wei: BigInteger): String

    companion object {
        private const val TAG = "Erc20TokenInfo"
    }

    class USDT(override val name: String, override val address: String) : Erc20TokenInfo() {
        override fun getDisplayBalance(wei: BigInteger): String {
            return try {
                Convert.fromWei(wei.toString(), Convert.Unit.MWEI).toString()
            } catch (t: Throwable) {
                LogUtil.d(TAG, t.stackTraceToString())
                ""
            }
        }
    }
}