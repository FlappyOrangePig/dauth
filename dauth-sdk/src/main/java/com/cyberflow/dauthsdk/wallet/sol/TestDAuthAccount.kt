package com.cyberflow.dauthsdk.wallet.sol

import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.wallet.impl.HttpClient
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger
import java.util.Arrays

internal object TestDAuthAccount {

    private val web3j: Web3j
        get() {
            return Web3j.build(
                HttpService(
                    DAuthSDK.impl.config.chain?.rpcUrl.orEmpty(),
                    HttpClient.client
                )
            )
        }

    fun test() {
        val contractAddress = ""
        val transactionManager = ClientTransactionManager(web3j, "0x")
        val dAuthAccount =
            DAuthAccount.load(contractAddress, web3j, transactionManager, DefaultGasProvider())
        dAuthAccount.addDeposit(BigInteger("1"))
    }

    fun testSame() {
        val contractAddress = ""
        val transactionManager = ClientTransactionManager(web3j, "0x")
        val dAuthAccount =
            DAuthAccount.load(contractAddress, web3j, transactionManager, DefaultGasProvider())


        val function = Function(
            DAuthAccount.FUNC_ADDDEPOSIT,
            Arrays.asList(), emptyList()
        )

        val encoded = FunctionEncoder.encode(function)

        val contractAddress2 = ""
        dAuthAccount.execute(contractAddress2, BigInteger("0"), encoded.toByteArray())
    }
}