package com.cyberflow.dauthsdk

import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.SignResult
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Bytes4
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Hash
import java.util.Arrays
import kotlin.random.Random

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

    @Test
    fun testSha3() {
        val seed = System.currentTimeMillis()
        val random = Random(seed)
        val long = random.nextLong()
        println("long=$long")
        val result = Hash.sha3String(long.toString())
        println("sha3: [${result.length}]$result")
    }

    @Test
    fun testMoshi(){
        val src = "{\n" +
                "    rh:9C1EBE663DEC7297A789865C093AD4316A2C439A45AB678AD2E33B99B3036C40\n" +
                "    sh:7B70CDA795A9D7E531D3235C0942BFC5EFF3AE9FEE04A87E2D09077DE827962C\n" +
                "    r:70615123879174677076162287035806532589155507673192136049551130616831817247808\n" +
                "    s:55833786623070531251084825236099197801247607792756364041738090651206036198956\n" +
                "    v:0\n" +
                "    }"

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
            .add(KotlinJsonAdapterFactory()).build()
        val j = moshi.adapter(SignResult::class.java).lenient().fromJson(src)
        println(j)
    }

    @Test
    fun testVerify() {
        val invoker = DAuthJniInvoker
        val msg = invoker.genRandomMsg()
        println("msg=$msg")
        val signResult = SignResult(
            "9C1EBE663DEC7297A789865C093AD4316A2C439A45AB678AD2E33B99B3036C40",
            "7B70CDA795A9D7E531D3235C0942BFC5EFF3AE9FEE04A87E2D09077DE827962C",
            "70615123879174677076162287035806532589155507673192136049551130616831817247808",
            "55833786623070531251084825236099197801247607792756364041738090651206036198956",
            0,
        )
        println("localSignMsg $signResult")
        val sd = signResult.toSignatureData()
        val eoaAddress = invoker.getWalletAddressBySignature(msg, sd)
        println("address=$eoaAddress")
    }
}