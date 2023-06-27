package com.cyberflow.dauthsdk

import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
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

    @Test
    fun testKeysMergeResult() {
        val key1 =
            "Cg5EQXV0aEdlbmVyYXRlchACGAMqrkYKC0RBdXRoUGFydHkwGgIwMTKnJAqABEM3RTczRDRGNDNENEEyNUE2RjU5RTdGNkRCNzhBMzFBMUI5NDQ4MjgyRjQ4MDdFQjE4QkY0MzIyNTVDNTgyMDk5MkUzQkE2NEQ2OUUwOTdCMjIyQTkzODE2MjQ0RTQwNjczQUQzMzc3MzY1MkVCQUUxMEQ0OUVEODMzNDc2MzBBNzAwRjYwMzhGMkFFNjY1MUUxN0ZBNjRGNkJFRDlGMkQxRUVERkI4MTdBNDMzNDY2RENFRTZGNDQ4RDlDREE5MkNDMzlDNDE4MDg3QkQxOTFGODNCQ0U4NTg3NDg0MjU4NUE0MjNDMUZCRjA3RTVFN0UwNzYyMjgyOTVFQzVCODU5M0U0OUYzRTc2QUVFRkYyOEQyMjJCQ0QyMEQ1MTVCQ0I4NEM5MTFCQjE4OUQ1QjE3NUM2NzFCNUU0RjNBODhGMUQ2M0JBNzFFNkFGM0Q3RTM3MTRFQjUyMzVDOTNCOUExQzY2QzZGMEVDNUY2QzU0NUVDNjU0MjFBODg1QkI3MjlERkU1REY5ODBFMjg2MTQ2NDI2NkIwQzY3MTdDQjNBNURBMThEQkI5REEwOEVGRTRDNzAxQkNENkZFQjBFNEFDMkQ3RjQ3NjI2MkY4NTJFRDEzMUFBM0Q1OEE2MzAzRjdDOUI0QzMzMkRFRjJCNzc1NEFBOEFFNkQ5OTBCQjc1EoAEQzdFNzNENEY0M0Q0QTI1QTZGNTlFN0Y2REI3OEEzMUExQjk0NDgyODJGNDgwN0VCMThCRjQzMjI1NUM1ODIwOTkyRTNCQTY0RDY5RTA5N0IyMjJBOTM4MTYyNDRFNDA2NzNBRDMzNzczNjUyRUJBRTEwRDQ5RUQ4MzM0NzYzMEE3MDBGNjAzOEYyQUU2NjUxRTE3RkE2NEY2QkVEOUYyRDFFRURGQjgxN0E0MzM0NjZEQ0VFNkY0NDhEOUNEQTkyQ0MzOUM0MTgwO"
        val key2 =
            "Cg5EQXV0aEdlbmVyYXRlchACGAMqrkYKC0RBdXRoUGFydHkxGgIwMjKnJAqABEI2NzBDMDg5QjhGNjNEQkU0NTNGQ0Y4NUFCQTI0MDRDQTJCQzMwMjUyRUExQTUyOTYxOUJGQjU1MUE1MEY0NjZGQzkxQ0VFOTUxNjlFRDFFN0MwRjU5QUM3NkE3MzlCMjIxRTk1MEEyQUZBQjlDMTMzNDEzMDA4MzU1MTQ1QTQ2MUE4NjI1NjM1RUY2MTA1N0Q1QTc2NEM3MjYzQUIzNzIzRTU1OTdENkJDQTg3Nzk3RDlFNTVEQUE5OUQ4N0RGMTA1MEU3ODM0NERBNEY3Mzk2ODhDNTFCOEU1NTAzQjU0OEU0NkQwNjQ2Q0ExM0JENEZEQzZDNENCMUYyNTY1RjgwNkFBMEZGMTc4Q0QyQ0RDNkI4MkZBQkVDNEVBNEFEMUZGQTNGNDM3RTdFM0UyREJDNkRENURCOTgyNkIwNEQwNEEwNDRFNjdDNkY3M0EwNUQ0MkU2RkU0QTlCRUJFNTc5NzI0RTkxNEY0RUVFNjFGREVCMUE0MDI0QkVCODEyMTJFMkRDQ0VCNzkxMDRDMEM1QTNBQkJENDQzOTIzQkNDOEVFMTU2QjJGMTc3RjM2MUZBMjU1NDk1RjM4MkYwRjQ2MjAzNDMyNUZEREEwQzZDMjIxMjdBNzE3MjFEOTZDMUExQTBEQTZBMjMxMUE0MDhGNDEyRjBGOUNCQjhEQTg5EoAEQjY3MEMwODlCOEY2M0RCRTQ1M0ZDRjg1QUJBMjQwNENBMkJDMzAyNTJFQTFBNTI5NjE5QkZCNTUxQTUwRjQ2NkZDOTFDRUU5NTE2OUVEMUU3QzBGNTlBQzc2QTczOUIyMjFFOTUwQTJBRkFCOUMxMzM0MTMwMDgzNTUxNDVBNDYxQTg2MjU2MzVFRjYxMDU3RDVBNzY0QzcyNjNBQjM3MjNFNTU5N0Q2QkNBODc3OTdEOUU1NURBQTk5RDg3REYxMDUwRTc4MzQ0R"
        val key3 =
            "Cg5EQXV0aEdlbmVyYXRlchACGAMqrkYKC0RBdXRoUGFydHkyGgIwMzKnJAqABEUxNjY0MkZBRTRGMzc4NERDQzk1MzdEM0M1QzY0RTM0NEUyQjgyMzAyNEY4QThEQjNBQkI5Njc1ODYzNkMzODMwMjU5NENGQjlBMEM0MzRBQjI0NUFGOTgxNDYxMUQ3NDU5RkZDRUUxOUQ4QjlBRDhCRDk2ODc4REJENEU1ODIzRjA1OTNGOEU5NzdDQzBEMThCQjY0NDE1QzQ5QjVGRTE4MDYxNUZEMDM5MzhDQUY3ODNCNEY1RkEwMTU2ODgxNzc1MUYyQzgzRjlDRTJGM0I1NEVDMTUxM0MwRTBCRTIwRkE5N0ExNTczRDkwNjY4Q0Y2MUM1REYzMDc1NDZGMkIzQ0I5RDJBMkMzN0Q2RDE0NEVCQkI3MDdCNkVEQjUxNERDMTcxQkZGQzIwMjc2OEZGRURGQzY5MUYzNjEzMzcyNjVBQkNBRDEyOURFNzJDNzIxRjU2ODZCOUZDOUQ5OTcyOURCRDk3MDBEOTI3NDEzQkZBOTQxQjQzNzA3Q0MzNEM5OTBDNzcwQzE5MDQzQ0FDOThGMTM4NDE1RTY4RjgwN0I0MDQ4NjhGNEVGNjlENEQ4OTY5MEIwRkU5NkM2NzYwMDlCODg5NDJFNzFFQUFDMkQ0QUVGMDg0RTUyNDE3ODY5MzhGMTRDMTkwNERCM0QyN0I5RUYwRkU2QzYzNjk1EoAERTE2NjQyRkFFNEYzNzg0RENDOTUzN0QzQzVDNjRFMzQ0RTJCODIzMDI0RjhBOERCM0FCQjk2NzU4NjM2QzM4MzAyNTk0Q0ZCOUEwQzQzNEFCMjQ1QUY5ODE0NjExRDc0NTlGRkNFRTE5RDhCOUFEOEJEOTY4NzhEQkQ0RTU4MjNGMDU5M0Y4RTk3N0NDMEQxOEJCNjQ0MTVDNDlCNUZFMTgwNjE1RkQwMzkzOENBRjc4M0I0RjVGQTAxNTY4ODE3NzUxRjJDODNGO"

        val input = arrayOf(key1, key2, key3)
        val encoded = MergeResultUtil.encode(input)
        println("encoded=$encoded")
        val decoded =
            MergeResultUtil.decode(encoded, arrayOf(key1, key2))

        val result = decoded == key3
        println("result=$result")
        assert(result)
    }
}