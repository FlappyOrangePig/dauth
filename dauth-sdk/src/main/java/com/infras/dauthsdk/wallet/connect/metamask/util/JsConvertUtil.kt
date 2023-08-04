package com.infras.dauthsdk.wallet.connect.metamask.util

object JsConvertUtil {

    private const val PREFIX = "javascript:"

    fun personalSignMessage(message: String): String {
        return "${PREFIX}window.setPersonalSignMessage(`${message}`);"
    }

    fun sendTransaction(transactionJson: String): String {
        return "${PREFIX}window.setSendTransactionParams(`$transactionJson`);"
    }

    /**
     * 展示按钮
     *
     * @param buttonIndex 0=连接 1=交易 2=签名 else=不展示
     */
    fun showButton(buttonIndex: Int): String {
        return "${PREFIX}window.showButton($buttonIndex);"
    }

    fun isConnected(): String {
        return "${PREFIX}window.ethereum.isConnected();"
    }
}