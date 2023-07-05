package com.cyberflow.dauthsdk.wallet.impl

internal object ConfigurationManager {
    private fun configuration() = Test
    internal fun urls() = configuration().getServerUrls
    internal fun addresses() = configuration().getAddress

    /**
     * 测试开关
     */
    internal const val saveAllKeys = true
}

internal interface DAuthConfiguration {
    val getAddress: DAuthAddress
    val getServerUrls: DAuthServerUrls
}

internal interface DAuthAddress {
    val factoryAddress: String
    val entryPointAddress: String
}

internal interface DAuthServerUrls {
    val providerRpc: String
    val webSocketUrl: String
    val relayerUrl: String
}

private object Dev : DAuthConfiguration {
    override val getAddress: DAuthAddress = object : DAuthAddress {
        override val factoryAddress: String
            get() = "0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0"
        override val entryPointAddress: String
            get() = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512"
    }

    override val getServerUrls: DAuthServerUrls = object : DAuthServerUrls {
        override val providerRpc: String
            get() = "http://172.16.13.155:8545"
        override val webSocketUrl: String
            get() = "ws://api-dev.infras.online/mpc/sign"
        override val relayerUrl: String
            get() = "https://api-dev.infras.online/relayer/committrans"
    }
}

private object Test : DAuthConfiguration {
    override val getAddress: DAuthAddress = object : DAuthAddress {
        override val factoryAddress: String
            get() = "0x3feB9a9B764A54B46dB90c74001694329A90F2D5"
        override val entryPointAddress: String
            get() = "0x809a09b33DbF8730eACbDcAc945bA8e6299b2C49"
    }

    override val getServerUrls: DAuthServerUrls = object : DAuthServerUrls {
        override val providerRpc: String
            get() = "https://arbitrum-goerli.public.blastapi.io/"
        override val webSocketUrl: String
            get() = "ws://api-dev.infras.online/mpc/sign"
        override val relayerUrl: String
            get() = "https://api-dev.infras.online/relayer/committrans"
    }
}

/*
private object Live : DAuthConfiguration {
    override val getAddress: DAuthAddress = object : DAuthAddress {
        override val factoryAddress: String
            get() = "0xaB308475416e673fcAbC6B9AD06D6bDa124DBB96"
        override val entryPointAddress: String
            get() = "0x0f6423874F25052c6B242ce6169dAC00f3E5E3C2"
    }

    override val getServerUrls: DAuthServerUrls = object : DAuthServerUrls {
        override val providerRpc: String
            get() = "https://rpc.pioneer.etm.network"
        override val webSocketUrl: String
            get() = "ws://api.infras.online/mpc/sign"
        override val relayerUrl: String
            get() = "https://api.infras.online/relayer/committrans"
    }
}*/
