package com.infras.dauth.util

import com.infras.dauthsdk.api.DAuthChainEnum
import com.infras.dauthsdk.api.DAuthStageEnum

sealed class DAuthEnv {
    abstract val stage: Int
    abstract val chain: Int
    abstract val clientId: String

    object EnvProd : DAuthEnv() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_LIVE
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_ARBITRUM
        override val clientId: String
            get() = "ce475be992763ee5ff0c51faba901942"
    }

    object EnvDev : DAuthEnv() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_TEST
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_ARBITRUM_GOERLI
        override val clientId: String
            get() = "b86df4abc13f3bba4d4b2057bc6df910"
    }
}