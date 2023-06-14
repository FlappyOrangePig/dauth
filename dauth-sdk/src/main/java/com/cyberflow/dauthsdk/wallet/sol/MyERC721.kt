package com.cyberflow.dauthsdk.wallet.sol

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.contracts.eip721.generated.ERC721
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

internal class MyERC721(
    contractAddress: String?,
    web3j: Web3j?,
    credentials: Credentials?,
    gasProvider: ContractGasProvider?
) : ERC721(contractAddress, web3j, credentials, gasProvider) {
    fun tokenOfOwnerByIndex(owner: String?, index: BigInteger?): RemoteFunctionCall<BigInteger> {
        val function = Function(
            "tokenOfOwnerByIndex",
            listOf<Type<*>>(Address(owner), Uint256(index)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }
}