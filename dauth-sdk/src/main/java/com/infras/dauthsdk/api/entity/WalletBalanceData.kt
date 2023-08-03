package com.infras.dauthsdk.api.entity

import java.math.BigInteger

sealed class WalletBalanceData {
    class Eth(val amount: BigInteger) : WalletBalanceData() {
        override fun toString(): String {
            return "Eth(amount=$amount)"
        }
    }

    class ERC20(val amount: BigInteger) : WalletBalanceData() {
        override fun toString(): String {
            return "ERC20(amount=$amount)"
        }
    }

    class ERC721(val tokenIds: List<BigInteger>) : WalletBalanceData() {
        override fun toString(): String {
            return "ERC721(tokenIds=$tokenIds)"
        }
    }
}