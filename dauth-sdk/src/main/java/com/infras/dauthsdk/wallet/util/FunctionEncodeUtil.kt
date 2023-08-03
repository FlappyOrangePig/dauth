package com.infras.dauthsdk.wallet.util

import com.infras.dauthsdk.wallet.sol.DAuthAccountFactory
import com.infras.dauthsdk.wallet.sol.EntryPoint
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256

object FunctionEncodeUtil {

    fun getCreateAccountFunction(eoaAddress: String): ByteArray {
        val func = Function(
            com.infras.dauthsdk.wallet.sol.DAuthAccountFactory.FUNC_CREATEACCOUNT,
            listOf<Type<*>>(
                Address(eoaAddress),
                Uint256(0)
            ), emptyList<TypeReference<*>>()
        )
        val encoded = FunctionEncoder.encode(func)
        return encoded.hexStringToByteArray()
    }

    fun getSimulateHandleOpFunction(userOp: com.infras.dauthsdk.wallet.sol.EntryPoint.UserOperation): ByteArray {
        val func = Function(
            com.infras.dauthsdk.wallet.sol.EntryPoint.FUNC_SIMULATEHANDLEOP,
            listOf<Type<*>>(
                userOp
            ), emptyList<TypeReference<*>>()
        )
        val encoded = FunctionEncoder.encode(func)
        return encoded.hexStringToByteArray()
    }
}