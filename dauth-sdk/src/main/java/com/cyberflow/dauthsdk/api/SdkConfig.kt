package com.cyberflow.dauthsdk.api

class SdkConfig {

    /**
     * DAuth app id
     */
    var appId: String? = null

    /**
     * DAuth app key
     */
    var appKey: String? = null

    /**
     * AppServerKey要提交到的AppServer-URL
     */
    var urlForAppServerKeyToSubmit: String? = null

    /**
     * web3-RPC-nodes
     *
     * test node below
     *
     * ARBITRUM GOERLI
     * https://goerli-rollup.arbitrum.io/rpc
     * https://endpoints.omniatech.io/v1/arbitrum/goerli/public
     * https://rpc.goerli.arbitrum.gateway.fm
     * https://arbitrum-goerli.publicnode.com
     * https://arb-goerli.g.alchemy.com/v2/demo
     * https://arbitrum-goerli.public.blastapi.io
     *
     * sepolia test
     * chainId：11155111 (0xaa36a7)
     * https://rpc.sepolia.org/
     * 民间USDT地址
     * 0x6175a8471C2122f778445e7E07A164250a19E661
     *
     * Gou 's local node
     * http://172.16.13.155:8545/
     */
    var chains: List<ChainInfo> = emptyList()

    /**
     * Twitter key
     */
    var twitterConsumerKey: String? = null

    /**
     * Twitter secret
     */
    var twitterConsumerSecret: String? = null

    /**
     * 使用内置账号（内置账号在sepolia test上有点钱）
     */
    var useInnerTestAccount: Boolean = false

    /**
     * 创建Bip44密钥对时，是否使用测试网络
     */
    var useTestNetwork: Boolean = false

    class ChainInfo(
        val info: Any,
        val rpcUrl: String,
        val usdtAddress: String,
        val nftAddress: String,
    )
}

