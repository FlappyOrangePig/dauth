package com.cyberflow.dauthsdk.wallet.impl

private const val ARBITRUM_TEST = "https://arbitrum-goerli.public.blastapi.io/"
private const val HOST_DEV = "api-dev.infras.online"
private const val HOST_TEST = "api-test.infras.online"

internal object ConfigurationManager {
    private fun configuration() = Dev
    internal fun urls() = configuration().getServerUrls
    internal fun addresses() = configuration().getAddress
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
    val baseUrlHost: String
    val providerRpc: String
    val baseUrl: String get() = "https://${baseUrlHost}"
    val webSocketUrl: String get() = "wss://${baseUrlHost}/mpc/sign"
    val relayerUrl: String get() = "${baseUrl}/relayer/committrans"
}

private object Dev : DAuthConfiguration {
    override val getAddress: DAuthAddress = object : DAuthAddress {
        override val factoryAddress: String
            get() = "0x3feB9a9B764A54B46dB90c74001694329A90F2D5"
        override val entryPointAddress: String
            get() = "0x809a09b33DbF8730eACbDcAc945bA8e6299b2C49"
    }

    override val getServerUrls: DAuthServerUrls = object : DAuthServerUrls {
        override val baseUrlHost: String
            get() = HOST_DEV
        override val providerRpc: String
            get() = ARBITRUM_TEST
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
        override val baseUrlHost: String
            get() = HOST_TEST
        override val providerRpc: String
            get() = ARBITRUM_TEST
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
