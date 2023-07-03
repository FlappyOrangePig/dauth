package com.cyberflow.dauthsdk.wallet.impl

object ConfigurationManager {
    fun getAddressesByStage(): DAuthAddress {
        return TestAddress
    }
}

interface DAuthAddress {
    fun providerRpc(): String
    fun factoryAddress(): String
    fun entryPointAddress(): String
    fun nftAddress(): String
    fun webSocketUrl(): String
}

object TestAddress : DAuthAddress {
    override fun providerRpc() = "http://172.16.13.155:8545"
    override fun factoryAddress() = "0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0"
    override fun entryPointAddress() = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512"
    override fun nftAddress(): String = "0xCf7Ed3AccA5a467e9e704C703E8D87F634fB0Fc9"
    override fun webSocketUrl() = "ws://api-dev.infras.online/mpc/sign"
}

object LiveAddress : DAuthAddress {
    override fun providerRpc() = "https://rpc.pioneer.etm.network"
    override fun factoryAddress() = "0xaB308475416e673fcAbC6B9AD06D6bDa124DBB96"
    override fun entryPointAddress() = "0x0f6423874F25052c6B242ce6169dAC00f3E5E3C2"
    override fun nftAddress(): String = "0xb5605D6DEfc9e09d9a937ac49B3a4A959Ad73432"
    override fun webSocketUrl() = "ws://api.infras.online/mpc/sign"
}