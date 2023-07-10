package com.cyberflow.dauthsdk

import android.content.Context
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.SdkConfig
import com.cyberflow.dauthsdk.login.model.GetSecretKeyParam
import com.cyberflow.dauthsdk.login.network.RequestApiMpc
import com.cyberflow.dauthsdk.login.utils.LoginPrefs
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.MpcKeyIds
import com.cyberflow.dauthsdk.mpc.MpcKeyStore
import com.cyberflow.dauthsdk.mpc.SignResult
import com.cyberflow.dauthsdk.mpc.util.MergeResultUtil
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.impl.manager.WalletManager
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.cyberflow.dauthsdk.wallet.util.SignUtil
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2
import com.cyberflow.dauthsdk.wallet.util.sha3
import com.cyberflow.dauthsdk.wallet.util.sha3String
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyList
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Bytes4
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.random.Random


private const val CONSUMER_KEY = "2tUyK3TbbjxHPUHOP25OnSL0r"
private const val CONSUMER_SECRET = "p9bAQDBtlNPdNiTQuMM8yLJuwwDsVCf8QZl2rRRa4eqHVIBFHs"
private const val CLIENT_ID = "e2fc714c4727ee9395f324cd2e7f331f"
private const val CLIENT_SECRET = "4657*@cde"

private inline fun <T> runSpending(log: String, crossinline block: () -> T): T {
    println("$log >>>")
    val start = System.currentTimeMillis()
    val r = block.invoke()
    val spent = System.currentTimeMillis() - start
    println("$log <<< spent $spent")
    return r
}

class ExampleUnitTest {

    private var allKeys : MutableList<String> = mutableListOf()
    private var mergeResult: String? = null

    @Before
    fun setup() {
        val context = mock(Context::class.java)
        val config = SdkConfig().apply {
            twitterConsumerKey = CONSUMER_KEY
            twitterConsumerSecret = CONSUMER_SECRET
            clientId = CLIENT_ID
            clientSecret = CLIENT_SECRET
            isLogOpen = true
            localSign = false
            useLocalRelayer = false
            useDevWebSocketServer = false
            useDevRelayerServer = false
        }
        val accessToken = "at115bec87af40a985853cda9b9e75c7a7"
        val authId = "d6a2a49bd1bd251e32cbf80ae6a52f1b"
        val didToken = ""

        mock(LoginPrefs::class.java).apply {
            `when`(getAccessToken()).thenReturn(accessToken)
            `when`(getAuthId()).thenReturn(authId)
            `when`(getDidToken()).thenReturn(didToken)
            `when`(getExpireTime()).thenReturn(Long.MAX_VALUE)
            Managers.loginPrefs = this
        }
        mock(MpcKeyStore::class.java).apply {
            `when`(getAllKeys()).thenReturn(listOf())
            `when`(getLocalKey()).thenReturn("")
            `when`(getMergeResult()).thenReturn("")
            `when`(this.setAllKeys(anyList())).then {
                println("setAllKeys")
                allKeys = (it.arguments[0] as List<String>).toMutableList()
                Unit
            }
            `when`(this.setLocalKey(anyString())).then {
                println("setLocalKey")
                allKeys.clear()
                allKeys.add(it.arguments[0] as String)
                Unit
            }
            `when`(this.setMergeResult(anyString())).then {
                println("setMergeResult")
                mergeResult = it.arguments[0] as String
                Unit
            }
            Managers.mpcKeyStore = this
        }
        mock(WalletPrefsV2::class.java).apply {
            Managers.walletPrefsV2 = this
        }
        Managers.walletManager = WalletManager()

        (DAuthSDK.instance as DAuthSDK).initSDKForTest(context, config)
    }

    @Test
    fun testCallContract() {
        val function = Function(
            DAuthAccount.FUNC_ONERC1155RECEIVED,
            listOf<Type<*>>(
                Address("123"),
                Address("234"),
                Uint256(123L),
                Uint256(213L)
            ),
            listOf<TypeReference<*>>(object : TypeReference<Bytes4?>() {})
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
        val result = long.toString().sha3String()
        println("sha3: [${result.length}]$result")
    }

    @Test
    fun testMoshi() {
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
        val msgHash = msg.toByteArray().sha3()
        val eoaAddress = invoker.getWalletAddress(msgHash, sd)
        println("address=$eoaAddress")
    }

    @Test
    fun testBigIntEncode() {
        val ba = byteArrayOf(1,0)
        val bi = BigInteger(ba)
        println(bi)

        val ba2 = "11".toByteArray()
        val bi2 = BigInteger(ba2)
        println(bi2)

        val ba3 = bi2.toByteArray()
        val bi3 = BigInteger(ba3)
        println(bi3)
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
        val encoded = runSpending("encode") {
            MergeResultUtil.encodeKey(input).also {
                println("encoded=$it")
            }
        }
        val decoded = runSpending("decode") {
            MergeResultUtil.decodeKey(encoded, arrayOf(key1, key2)).also {
                println("decode=$it")
            }
        }
        val result = decoded == key3
        println("result=$result")
        assert(result)
    }

    @Test
    fun testEncodeAndEncodePacked() {
        val faAddress = "0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0"
        val zaAddress =
            "0x9fe46736679d2d9a65f0992f2272de9f3c7fa6e05fbfb9cf000000000000000000000000df3e18d64bc6a983f673ab319ccae4f1a57c70970000000000000000000000000000000000000000000000000000000000000000"
        val aaAddress =
            "9fe46736679d2d9a65f0992f2272de9f3c7fa6e00x5fbfb9cf000000000000000000000000dd2fd4581271e230360230f9337d5c0430bf44c00000000000000000000000000000000000000000000000000000000000000000"

        val address = Address(faAddress)
        val encodedPacked = TypeEncoder.encodePacked(address)
        println("encodedPacked=$encodedPacked")
        val encoded = TypeEncoder.encode(address)
        println("encoded=$encoded")
    }

    @Test
    fun testEncodeParameter() {
        // 获取当前链的 chainId
        val v1 = Uint256(BigInteger("123"))
        val v2 = DynamicBytes(Numeric.hexStringToByteArray("0x123"))

        // 1.FunctionEncoder
        val params = listOf<Type<*>>(
            v1,
            v2
        )
        val e1: String = FunctionEncoder.encodeConstructorPacked(params)
        println("e1=$e1")

        // 2.DIY
        val e2 = StringBuilder().append(TypeEncoder.encodePacked(v1))
            .append(TypeEncoder.encodePacked(v2)).toString()
        println("e2=$e2")

        // 3.DIY + DynamicStruct
        val dynamicStruct = DynamicStruct(v1, v2)
        val e3 = TypeEncoder.encodePacked(dynamicStruct)
        println("e3:$e3")

        // 4.FunctionEncoder + DynamicStruct
        val e4: String = FunctionEncoder.encodeConstructorPacked(listOf(dynamicStruct))
        println("e4:$e4")
    }

    @Test
    fun testSignedMessageToKey() {
        // 这个账号是苟建的测试账号（有很多eth）
        val eoa = "0xdd2fd4581271e230360230f9337d5c0430bf44c0"
        val privateKey = "0xde9be858da4a475276426320d5e9262ecfc3ba460bfac56360bfa6c4c28b4ee0"

        val msg = DAuthJniInvoker.genRandomMsg().toByteArray()
        val msgHash = msg.sha3()

        val signatureData = SignUtil.signMessage(
            msgHash,
            Credentials.create(privateKey).ecKeyPair
        )

        val signer = DAuthJniInvoker.getWalletAddress(msgHash, signatureData)
        println("signer=$signer")

        // 确认getWalletAddress方法的正确性
        assert(eoa == signer)
    }

    @Test
    fun testGetParticipants() = runBlocking {
        val mergeResult = StringBuilder().apply {
            for (i in 1..1001) {
                append("1234567890")
            }
        }.toString()
        val key = "2c7dcea8766c27a1c43c9da169947caa"

        val mpcApi = RequestApiMpc()
        val servers = mpcApi.getParticipants()!!
        val participants = servers.data.participants

        val index = MpcKeyIds.KEY_INDEX_DAUTH_SERVER

        mpcApi.setKey(
            participants[index].set_key_url,
            key,
            mergeResult
        )

        val keyResult = mpcApi.getKey(participants[index].get_key_url, GetSecretKeyParam.TYPE_KEY)
        assert(keyResult?.isSuccess() == true)
        println("key=${keyResult?.data}")

        val mergeResultResult =
            mpcApi.getKey(participants[index].get_key_url, GetSecretKeyParam.TYPE_MERGE_RESULT)
        assert(mergeResultResult?.isSuccess() == true)
        println("mergeResult=${mergeResultResult?.data}")
    }
}