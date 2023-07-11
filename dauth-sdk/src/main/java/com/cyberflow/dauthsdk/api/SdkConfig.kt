package com.cyberflow.dauthsdk.api

class SdkConfig {

    /**
     * clientId 在DAuth后台注册得到的clientId
     */
    var clientId: String? = null
    /**
     * clientSecret 在DAuth后台注册得到的clientSecret
     */
    var clientSecret: String? = null

    /**
     * Twitter key
     */
    var twitterConsumerKey: String? = null

    /**
     * Twitter secret
     */
    var twitterConsumerSecret: String? = null

    /**
     * 打开日志
     */
    var isLogOpen = false

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

    /**
     * Google ClientID
     */
    var googleClientId: String? = null
}

