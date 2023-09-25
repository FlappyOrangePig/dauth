package com.infras.dauth.util

import com.infras.dauth.BuildConfig
import com.infras.dauthsdk.api.DAuthChainEnum
import com.infras.dauthsdk.api.DAuthStageEnum

sealed class DAuthEnv {
    abstract val stage: Int
    abstract val chain: Int
    abstract val clientId: String
    abstract val chainName: String
    abstract val erc20Tokens: List<Pair<String, String>>

    object EnvProd : DAuthEnv() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_LIVE
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_ARBITRUM
        override val clientId: String
            get() = "ce475be992763ee5ff0c51faba901942"
        override val chainName: String
            get() = "Arbitrum One"
        override val erc20Tokens: List<Pair<String, String>>
            get() = listOf(
                "USDT" to "0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9"
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
        override val erc20Tokens: List<Pair<String, String>>
            get() = listOf(
                "USDC" to "0x6aAd876244E7A1Ad44Ec4824Ce813729E5B6C291"
            )
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
        override val erc20Tokens: List<Pair<String, String>>
            get() = listOf()
    }
}

internal fun getEnv(): DAuthEnv {
    return when (BuildConfig.IS_LIVE) {
        true -> DAuthEnv.EnvProd
        false -> DAuthEnv.EnvDevGoerli
    }
}