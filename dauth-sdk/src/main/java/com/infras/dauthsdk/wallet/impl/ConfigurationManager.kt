package com.infras.dauthsdk.wallet.impl

import com.infras.dauthsdk.api.DAuthChainEnum
import com.infras.dauthsdk.api.DAuthSDK
import com.infras.dauthsdk.api.DAuthStageEnum

internal object ConfigurationManager {
    private val config get() = DAuthSDK.impl.config
    internal fun stage(): DAuthStage = when (config.stage) {
        DAuthStageEnum.STAGE_TEST -> DAuthStage.Test
        DAuthStageEnum.STAGE_LIVE -> DAuthStage.Live
        else -> throw RuntimeException()
    }

    internal fun chain(): DAuthChain = when (config.chain) {
        DAuthChainEnum.CHAIN_ARBITRUM_GOERLI -> DAuthChain.ArbitrumGoerli
        DAuthChainEnum.CHAIN_ARBITRUM -> DAuthChain.Arbitrum
        DAuthChainEnum.CHAIN_GOERLI -> DAuthChain.Goerli
        else -> throw RuntimeException()
    }

    val innerConfig: InnerConfig = DefaultInnerConfig
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

    /**
     * Arbitrum
     *
     * RpcAddr: https://arbitrum.blockpi.network/v1/rpc/ca5a9ed3d8ae6312c8e9d88b6097bb1e99e1b70c
     * WssAddr: wss://arbitrum.blockpi.network/v1/ws/ca5a9ed3d8ae6312c8e9d88b6097bb1e99e1b70c
     *
     * @constructor Create empty Arbitrum
     */
    internal object Arbitrum : DAuthChain() {
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_ARBITRUM
        override val rpcUrl: String
            get() = "https://arbitrum.blockpi.network/v1/rpc/ca5a9ed3d8ae6312c8e9d88b6097bb1e99e1b70c"
        override val factoryAddress: String
            get() = "0x9c3A6789EA865438eD3abb46dB28040b5352Bc3d"
        override val entryPointAddress: String
            get() = "0x337B92C0e068ec853660811159f16077D86E5Ef6"
    }

    internal object Goerli: DAuthChain() {
        override val chain: Int
            get() = DAuthChainEnum.CHAIN_GOERLI
        override val rpcUrl: String
            get() = "https://eth-goerli.g.alchemy.com/v2/Ut6n0P3ltpRhzmVCeJRm7GNdyZozMl73"
        override val factoryAddress: String
            get() = Arbitrum.factoryAddress
        override val entryPointAddress: String
            get() = Arbitrum.entryPointAddress

    }
}

internal sealed class DAuthStage {
    @get:DAuthStageEnum
    abstract val stage: Int
    abstract val baseUrlHost: String
    abstract val signSecurityKey: String

    internal object Test : DAuthStage() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_TEST
        override val baseUrlHost: String
            get() = "api-dev.infras.online"
        override val signSecurityKey: String
            get() = "123&*abc"
    }

    internal object Live : DAuthStage() {
        override val stage: Int
            get() = DAuthStageEnum.STAGE_LIVE
        override val baseUrlHost: String
            get() = "api.infras.online"
            //get() = "openapi.infras.online"
        override val signSecurityKey: String
            get() = "v8FzBppykcOPQhrfGJaNb386tQKGq2zS"
    }
}

internal interface InnerConfig {
    val serverGenerateKey: Boolean
    val localSign: Boolean
    val useLocalRelayer: Boolean
    val useDevWebSocketServer: Boolean
    val useDevRelayerServer: Boolean
    val useDevKeyGenServer: Boolean
    val doNotRestore: Boolean
}

private object DefaultInnerConfig : InnerConfig {
    override val serverGenerateKey: Boolean
        get() = true
    override val localSign: Boolean
        get() = false
    override val useLocalRelayer: Boolean
        get() = false
    override val useDevWebSocketServer: Boolean
        get() = false
    override val useDevRelayerServer: Boolean
        get() = false
    override val useDevKeyGenServer: Boolean
        get() = false
    override val doNotRestore: Boolean
        get() = false
}