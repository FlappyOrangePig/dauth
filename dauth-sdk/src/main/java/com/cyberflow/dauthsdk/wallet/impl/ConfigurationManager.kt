package com.cyberflow.dauthsdk.wallet.impl

import com.cyberflow.dauthsdk.api.DAuthChainEnum
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.DAuthStageEnum

internal object ConfigurationManager {
    private val config get() = DAuthSDK.impl.config
    internal fun stage(): DAuthStage = when (config.stage) {
        DAuthStageEnum.STAGE_TEST -> DAuthStage.Test
        DAuthStageEnum.STAGE_LIVE -> DAuthStage.Test
        else -> throw RuntimeException()
    }

    internal fun chain(): DAuthChain = when (config.chain) {
        DAuthChainEnum.CHAIN_ARBITRUM_GOERLI -> DAuthChain.ArbitrumGoerli
        DAuthChainEnum.CHAIN_ARBITRUM -> DAuthChain.ArbitrumGoerli
        else -> throw RuntimeException()
    }
}

internal sealed class DAuthChain {
    @get:DAuthChainEnum
    abstract val chain: Int
    abstract val rpcUrl: String
    abstract val factoryAddress: String
    abstract val entryPointAddress: String

    internal object ArbitrumGoerli : DAuthChain() {
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_ARBITRUM_GOERLI
        override val rpcUrl: String
            //get() = "https://arbitrum-goerli.public.blastapi.io/"
            get() = "https://special-necessary-pond.arbitrum-goerli.discover.quiknode.pro/ca7528b6087182c1c8f8457abe1e58022c09d05f/"
        override val factoryAddress: String
            get() = "0x3feB9a9B764A54B46dB90c74001694329A90F2D5"
        override val entryPointAddress: String
            get() = "0x809a09b33DbF8730eACbDcAc945bA8e6299b2C49"
    }
}

internal sealed class DAuthStage {
    @get:DAuthStageEnum
    abstract val stage: Int
    abstract val baseUrlHost: String

    internal object Test : DAuthStage() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_TEST
        override val baseUrlHost: String
            get() = "api-dev.infras.online"
    }
}