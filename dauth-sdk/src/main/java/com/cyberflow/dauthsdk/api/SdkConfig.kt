package com.cyberflow.dauthsdk.api

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(DAuthStageEnum.STAGE_TEST, DAuthStageEnum.STAGE_LIVE)
annotation class DAuthStageEnum {
    companion object {
        const val STAGE_TEST = 1
        const val STAGE_LIVE = 2
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(DAuthChainEnum.CHAIN_ARBITRUM_GOERLI, DAuthChainEnum.CHAIN_ARBITRUM)
annotation class DAuthChainEnum {
    companion object {
        const val CHAIN_ARBITRUM_GOERLI = 1
        const val CHAIN_ARBITRUM = 2
    }
}

class SdkConfig {

    /**
     * Stage 开发阶段
     */
    @DAuthStageEnum
    var stage = DAuthStageEnum.STAGE_TEST

    /**
     * Chain 链枚举
     */
    @DAuthChainEnum
    var chain = DAuthChainEnum.CHAIN_ARBITRUM_GOERLI

    /**
     * clientId 在DAuth后台注册得到的clientId
     */
    var clientId: String? = null

    /**
     * clientSecret 在DAuth后台注册得到的clientSecret
     */
    var clientSecret: String? = null

    /**
     * Google ClientID
     */
    var googleClientId: String? = null

    /**
     * Twitter key
     */
    var twitterConsumerKey: String? = null

    /**
     * Twitter secret
     */
    var twitterConsumerSecret: String? = null

    /**
     * 打开控制台日志
     */
    var isLogOpen = false

    /**
     * File log level
     * 1=v 2=d 3=i 4=w 5=e 6=f
     */
    var fileLogLevel = 3
    var consoleLogLevel = 1

    /**
     * 【test】不使用分布式签名，直接本地签名。（测试期本地会保存所有密钥分片，上线时移除）
     */
    var localSign = false

    /**
     * 【test】在本地直接提交UserOperation，不使用服务端的relayer。上链后本地提交会有权限问题
     */
    var useLocalRelayer = false

    /**
     * 【test】
     */
    var useDevWebSocketServer = false

    /**
     * 【test】
     */
    var useDevRelayerServer = false
}

