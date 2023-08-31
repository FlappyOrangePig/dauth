package com.infras.dauth.ui.transaction.test

import com.infras.dauth.entity.TagsEntity
import com.infras.dauth.entity.TokenInfo
import com.infras.dauth.entity.TokenInfoOfTag

object BuyAndSellActivityMockData {

    private val allData = mutableListOf(
        TokenInfo(
            name = "USDT",
            issuer = "tether",
            avatarUrl = "https://img1.baidu.com/it/u=1535503495,3105965414&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            changeRange = "+10%",
            "$111"
        ),
        TokenInfo(
            name = "ETH",
            issuer = "ethereum",
            avatarUrl = "https://img1.baidu.com/it/u=1777458741,3394283602&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            changeRange = "-10%",
            "$222"
        ),
    ).let { origin ->
        mutableListOf<TokenInfo>().also { created ->
            repeat(50) {
                created.addAll(origin)
            }
        }
    }
    private val recentData = mutableListOf(
        TokenInfo(
            name = "USDT",
            issuer = "tether",
            avatarUrl = "https://img1.baidu.com/it/u=1535503495,3105965414&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            changeRange = "0%",
            "$444"
        ),
        TokenInfo(
            name = "ETH",
            issuer = "ethereum",
            avatarUrl = "https://img1.baidu.com/it/u=1777458741,3394283602&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            changeRange = "-110%",
            "$333"
        ),
    ).let { origin ->
        mutableListOf<TokenInfo>().also { created ->
            repeat(50) {
                created.addAll(origin)
            }
        }
    }

    val tokenInfoList = listOf(
        TokenInfoOfTag(
            tag = TagsEntity(
                title = "All",
                onClick = {}
            ),
            tokenInfoList = allData
        ),
        TokenInfoOfTag(
            tag = TagsEntity(
                title = "Recent",
                onClick = {}
            ),
            tokenInfoList = recentData
        ),
    )
}