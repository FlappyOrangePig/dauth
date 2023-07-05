//package com.cyberflow.dauthsdk.wallet.connect
//
//import androidx.annotation.DrawableRes
//import com.cyberflow.dauthsdk.R
//
//enum class Chains(
//    val chainName: String,
//    val chainNamespace: String,
//    val chainReference: String,
//    @DrawableRes val icon: Int,
//    val color: String,
//    val methods: List<String>,
//    val events: List<String>,
//    val order: Int,
//    val chainId: String = "$chainNamespace:$chainReference"
//) {
//
//    ETHEREUM_MAIN(
//        chainName = "Ethereum",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "1",
//        icon = R.drawable.ic_ethereum,
//        color = "#617de8",
//        methods = Info.Eth.defaultMethods,
//        events = Info.Eth.defaultEvents,
//        order = 1
//    ),
//
//    POLYGON_MATIC(
//        chainName = "Polygon Matic",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "137",
//        icon = R.drawable.ic_polygon,
//        color = "#8145e4",
//        methods = Info.Eth.defaultMethods,
//        events = Info.Eth.defaultEvents,
//        order = 2
//    ),
//
//    ETHEREUM_KOVAN(
//        chainName = "Ethereum Kovan",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "42",
//        icon = R.drawable.ic_ethereum,
//        color = "#617de8",
//        methods = Info.Eth.defaultMethods,
//        events = Info.Eth.defaultEvents,
//        order = 3
//    ),
//
//    OPTIMISM_KOVAN(
//        chainName = "Optimism Kovan",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "69",
//        icon = R.drawable.ic_optimism,
//        color = "#e70000",
//        methods = Info.Eth.defaultMethods,
//        events = Info.Eth.defaultEvents,
//        order = 4
//    ),
//
//    POLYGON_MUMBAI(
//        chainName = "Polygon Mumbai",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "80001",
//        icon = R.drawable.ic_polygon,
//        color = "#8145e4",
//        methods = Info.Eth.defaultMethods,
//        events = Info.Eth.defaultEvents,
//        order = 5
//    ),
//
//    ARBITRUM_RINKBY(
//        chainName = "Arbitrum Rinkeby",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "421611",
//        icon = R.drawable.ic_arbitrum,
//        color = "#95bbda",
//        methods = Info.Eth.defaultMethods,
//        events = Info.Eth.defaultEvents,
//        order = 6
//    ),
//
//    CELO_ALFAJORES(
//        chainName = "Celo Alfajores",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "44787",
//        icon = R.drawable.ic_celo,
//        color = "#f9cb5b",
//        methods = Info.Eth.defaultMethods,
//        events = Info.Eth.defaultEvents,
//        order = 7
//    ),
//    COSMOS(
//        chainName = "Cosmos",
//        chainNamespace = Info.Cosmos.chain,
//        chainReference = "cosmoshub-4",
//        icon = R.drawable.ic_cosmos,
//        color = "#B2B2B2",
//        methods = Info.Cosmos.defaultMethods,
//        events = Info.Cosmos.defaultEvents,
//        order = 7
//    ),
//    BNB(
//        chainName = "BNB Smart Chain",
//        chainNamespace = Info.Eth.chain,
//        chainReference = "56",
//        icon = R.drawable.bnb,
//        color = "#F3BA2F",
//        methods = Info.Cosmos.defaultMethods,
//        events = Info.Cosmos.defaultEvents,
//        order = 8
//    );
//
//    sealed class Info {
//        abstract val chain: String
//        abstract val defaultEvents: List<String>
//        abstract val defaultMethods: List<String>
//
//        object Eth: Info() {
//            override val chain = "eip155"
//            override val defaultEvents: List<String> = listOf("chainChanged", "accountsChanged")
//            override val defaultMethods: List<String> = listOf(
//                "eth_sendTransaction",
//                "personal_sign",
//                "eth_sign",
//                "eth_signTypedData"
//            )
//        }
//
//        object Cosmos: Info() {
//            override val chain = "cosmos"
//            override val defaultEvents: List<String> = listOf("chainChanged", "accountsChanged")
//            override val defaultMethods: List<String> = listOf(
//                "cosmos_signDirect",
//                "cosmos_signAmino"
//            )
//        }
//    }
//}