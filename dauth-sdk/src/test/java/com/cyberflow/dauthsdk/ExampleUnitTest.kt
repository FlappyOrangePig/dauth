package com.cyberflow.dauthsdk

import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import org.junit.Assert.*
import org.junit.Test
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Bytes4
import org.web3j.abi.datatypes.generated.Uint256
import java.util.Arrays

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val function = Function(
            DAuthAccount.FUNC_ONERC1155RECEIVED,
            Arrays.asList<Type<*>>(
                Address("123"),
                Address("234"),
                Uint256(123L),
                Uint256(213L)
            ),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Bytes4?>() {})
        )

        val encoded = FunctionEncoder.encode(function)
        println("result=$encoded")
    }
}