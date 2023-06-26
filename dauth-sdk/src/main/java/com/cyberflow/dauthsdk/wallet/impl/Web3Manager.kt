package com.cyberflow.dauthsdk.wallet.impl

import androidx.annotation.VisibleForTesting
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GET_NONCE
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.websocket.WebsocketManager
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccount
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccountFactory
import com.cyberflow.dauthsdk.wallet.sol.EntryPoint
import com.cyberflow.dauthsdk.wallet.sol.EntryPoint.UserOperation
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import com.cyberflow.dauthsdk.wallet.util.SignUtil
import com.cyberflow.dauthsdk.wallet.util.WalletPrefsV2
import com.cyberflow.dauthsdk.wallet.util.cleanHexPrefix
import com.cyberflow.dauthsdk.wallet.util.hexStringToByteArray
import com.cyberflow.dauthsdk.wallet.util.sha3
import com.cyberflow.dauthsdk.wallet.util.toHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.contracts.eip721.generated.ERC721
import org.web3j.contracts.eip721.generated.ERC721Enumerable
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Convert
import java.math.BigInteger


private const val TAG = "Web3Manager"

/**
 * 不是DAuthAccount的合约地址，是它的中转合约的地址，据说为了打包gas费
 * 转发者和DAuthAccount接口相同
 */
private const val DAUTH_FORWARDER_ADDRESS = ""

private fun BigInteger.isNewAccount(): Boolean {
    return compareTo(BigInteger.ZERO) == 0
}

private fun emptyCallData() = "0x".hexStringToByteArray()

/**
 * 把各字段打包到一起
 */
@VisibleForTesting
private fun encodePacked(vararg fields: Type<out Any>): String =
    FunctionEncoder.encodeConstructorPacked(fields.toList())

@VisibleForTesting
private fun UserOperation.encodeUserOp(): String {
    return StringBuilder().apply {
        append(
            TypeEncoder.encode(
                Address(sender)
            )
        )
        append(
            TypeEncoder.encodePacked(
                Uint256(nonce)
            )
        )
        append(
            TypeEncoder.encodePacked(
                DynamicBytes(initCode.sha3())
            )
        )
        append(
            TypeEncoder.encodePacked(
                DynamicBytes(callData.sha3())
            )
        )
        append(
            TypeEncoder.encodePacked(
                Uint256(callGasLimit)
            )
        )
        append(
            TypeEncoder.encodePacked(
                Uint256(verificationGasLimit)
            )
        )
        append(
            TypeEncoder.encodePacked(
                Uint256(preVerificationGas)
            )
        )
        append(
            TypeEncoder.encodePacked(
                Uint256(maxFeePerGas)
            )
        )
        append(
            TypeEncoder.encodePacked(
                Uint256(maxPriorityFeePerGas)
            )
        )
        append(
            TypeEncoder.encodePacked(
                Bytes32(paymasterAndData.sha3())
            )
        )
    }.toString()
}

private fun encodeRelCode(
    codeHash: String,
    entryPointAddress: String,
    chainId: BigInteger
): String {
    return StringBuilder().apply {
        append(
            codeHash.cleanHexPrefix()
        )
        append(
            TypeEncoder.encode(
                Address(entryPointAddress)
            )
        )
        append(
            TypeEncoder.encodePacked(
                Uint256(chainId)
            )
        )
    }.toString()
}

/**
 * 追加签名字段
 */
private fun UserOperation.addSignature(signature: ByteArray) = UserOperation(
    sender,
    nonce,
    initCode,
    callData,
    callGasLimit,
    verificationGasLimit,
    preVerificationGas,
    maxFeePerGas,
    maxPriorityFeePerGas,
    paymasterAndData,
    signature
)

object Web3Manager {

    private val web3j: Web3j by lazy {
        Web3j.build(
            HttpService(
                ConfigurationManager.getAddressesByStage().providerRpc(),
                HttpClient.client
            )
        )
    }

    /**
     * S=请求 T=应答 D=data
     */
    private suspend inline fun <S, T : Response<*>, D> Request<S, T>.await(crossinline block: (T) -> D): DAuthResult<D> {
        return withContext(Dispatchers.IO) {
            try {
                val r = send()
                if (r.hasError()) {
                    DAuthResult.Web3Error(r.error.code, r.error.message)
                } else {
                    val d = block.invoke(r)
                    DAuthResult.Success(d)
                }
            } catch (t: Throwable) {
                DAuthLogger.e(android.util.Log.getStackTraceString(t), TAG)
                DAuthResult.NetworkError(t)
            }
        }
    }

    private suspend inline fun <S, T : Response<*>, D> Request<S, T>.awaitLite(crossinline block: (T) -> D): D? {
        return (await(block) as? DAuthResult.Success)?.data
    }

    private suspend fun <T> RemoteCall<T>.await(): DAuthResult<T> = withContext(Dispatchers.IO) {
        try {
            val r = send()
            DAuthResult.Success(r)
        } catch (t: Throwable) {
            DAuthLogger.e(android.util.Log.getStackTraceString(t))
            DAuthResult.NetworkError(t)
        }
    }

    private suspend fun <T> RemoteCall<T>.awaitLite(): T? {
        return (await() as? DAuthResult.Success)?.data
    }

    private fun transactionManager(ownerWalletAddress: String): TransactionManager {
        return ClientTransactionManager(web3j, ownerWalletAddress)
    }

    suspend fun getBalance(address: String): DAuthResult<WalletBalanceData> {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).await {
            WalletBalanceData.Eth(it.balance)
        }
    }

    /*suspend fun getGasPrice(): BigInteger? {
        return web3j.ethGasPrice().await() { it.gasPrice }
    }*/

    suspend fun estimateGas(
        from: String,
        to: String,
        value: BigInteger
    ): DAuthResult<EstimateGasData> {
        return web3j.ethEstimateGas(
            Transaction.createEtherTransaction(
                from,
                null,
                null,
                null,
                to,
                value
            )
        ).await {
            EstimateGasData(it.amountUsed)
        }
    }

    suspend fun sendTransaction(to: String, amount: BigInteger): DAuthResult<SendTransactionData> {
        val credentials = CredentialsUtil.loadCredentials(false)
        val address = credentials.address
        val transactionCountResult =
            web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                .await {
                    it.transactionCount
                }
        if (transactionCountResult !is DAuthResult.Success) {
            return DAuthResult.SdkError(SDK_ERROR_CANNOT_GET_NONCE)
        }
        DAuthLogger.i("nonce=${transactionCountResult.data}", TAG)

        val gasProvider = DefaultGasProvider()
        val gasPrice = gasProvider.gasPrice
        val gasLimit = gasProvider.gasLimit
        val nonce = transactionCountResult.data
        val chainId = 11155111L
        val transaction =
            RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amount)
        val signedTransaction = TransactionEncoder.signMessage(transaction, chainId, credentials)
        val hexValue = signedTransaction.toHexString()

        return web3j.ethSendRawTransaction(hexValue).await {
            SendTransactionData(it.transactionHash)
        }
    }

    suspend fun getERC20Balance(
        contractAddress: String,
        accountAddress: String
    ): DAuthResult<WalletBalanceData> {
        val contract = withContext(Dispatchers.IO) {
            val transactionManager = ClientTransactionManager(web3j, accountAddress)
            ERC20.load(contractAddress, web3j, transactionManager, DefaultGasProvider())
        }
        val callResult = contract.balanceOf(accountAddress).await()
        DAuthLogger.d("getERC20Balance $callResult", TAG)
        if (callResult is DAuthResult.Success) {
            return DAuthResult.Success(WalletBalanceData.ERC20(callResult.data))
        }
        return DAuthResult.NetworkError()
    }

    suspend fun getERC721NftTokenIds(
        contractAddress: String,
        walletAddress: String
    ): DAuthResult<WalletBalanceData> {
        val (erC721, erC721Enumerable) = withContext(Dispatchers.IO) {
            val transactionManager = ClientTransactionManager(web3j, walletAddress)
            val erC721 =
                ERC721.load(contractAddress, web3j, transactionManager, DefaultGasProvider())
            val erC721Enumerable =
                ERC721Enumerable.load(
                    contractAddress,
                    web3j,
                    transactionManager,
                    DefaultGasProvider()
                )
            erC721 to erC721Enumerable
        }
        val callResult = erC721.balanceOf(walletAddress).await()
        DAuthLogger.d("getERC721Balance $callResult", TAG)
        val arrayList = arrayListOf<BigInteger>()
        if (callResult !is DAuthResult.Success) {
            DAuthLogger.d("cannot get balance", TAG)
            return DAuthResult.NetworkError()
        }
        for (i in 0 until callResult.data.toLong()) {
            // get the token ID at a specific index owned by a specific address
            val tokenIdResult =
                erC721Enumerable.tokenOfOwnerByIndex(walletAddress, BigInteger(i.toString()))
            val tokenId = tokenIdResult.await()
            DAuthLogger.d("$i)tokenId=$tokenId", TAG)
            if (tokenId !is DAuthResult.Success) {
                DAuthLogger.d("cannot get tokenId $i", TAG)
                return DAuthResult.NetworkError()
            }
            arrayList.add(tokenId.data)
        }
        DAuthLogger.d("success with $arrayList", TAG)
        return DAuthResult.Success(WalletBalanceData.ERC721(arrayList))
    }

    suspend fun execute(
        dest: String,
        value: BigInteger,
        func: Function
    ): DAuthResult<TransactionReceipt> {
        val encoded = FunctionEncoder.encode(func)
        val dAuthAccount = withContext(Dispatchers.IO) {
            val dauthAccountAddress = DAUTH_FORWARDER_ADDRESS
            val transactionManager = ClientTransactionManager(web3j, dauthAccountAddress)
            DAuthAccount.load(
                dest,
                web3j, transactionManager, DefaultGasProvider()
            )
        }
        val r = dAuthAccount.execute(dest, value, encoded.toByteArray()).await()
        DAuthLogger.d(
            "execute dest=$dest value=$value func=${FunctionWrapper(func)} result=$r",
            TAG
        )
        return r
    }

    suspend fun getAaAddressByEoaAddress(eoaAddress: String): String? {
        val addresses = ConfigurationManager.getAddressesByStage()
        val faAddress = addresses.factoryAddress()
        val accountFactory = DAuthAccountFactory.load(
            faAddress,
            web3j,
            transactionManager(eoaAddress),
            DefaultGasProvider()
        )
        val aaAddress = accountFactory.getAddress(eoaAddress, BigInteger.ZERO).awaitLite()
        DAuthLogger.d("get aa address by eoa address:$aaAddress", TAG)
        return aaAddress
    }

    private suspend fun executeUserOperation(
        eoaAddress: String,
        callData: ByteArray
    ): DAuthResult<String> {
        DAuthLogger.d("executeUserOperation eoaAddress=$eoaAddress", TAG)

        val addresses = ConfigurationManager.getAddressesByStage()
        val faAddress = addresses.factoryAddress()
        val entryPointAddress = addresses.entryPointAddress()

        // 部署aa账号的eoa账号地址，最终部署在服务端，entryPoint的TransactionManager需要
        val relayerEoaAddress = "0xdD2FD4581271e230360230F9337D5c0430Bf44C0"

        val gasPrice =
            web3j.ethGasPrice().awaitLite { it.gasPrice } ?: return DAuthResult.SdkError()
        DAuthLogger.d("gasPrice=$gasPrice", TAG)

        val accountFactory = DAuthAccountFactory.load(
            faAddress,
            web3j,
            transactionManager(eoaAddress),
            DefaultGasProvider()
        )
        val aaAddress = accountFactory.getAddress(eoaAddress, BigInteger.ZERO).awaitLite()
        DAuthLogger.d("get aa address $aaAddress", TAG)
        if (aaAddress == null) {
            DAuthLogger.e("aaAddress error")
            return DAuthResult.SdkError()
        }

        val entryPoint = EntryPoint.load(
            entryPointAddress,
            web3j,
            transactionManager(relayerEoaAddress),
            StaticGasProvider(gasPrice, BigInteger.valueOf(2100000))
        )

        val nonce = entryPoint.getNonce(aaAddress, BigInteger.ZERO).awaitLite()
        DAuthLogger.d("nonce=$nonce")
        if (nonce == null) {
            DAuthLogger.e("nonce error")
            return DAuthResult.SdkError()
        }

        val isNew = nonce.isNewAccount()
        DAuthLogger.d("isNew=$isNew")

        val initCode = if (isNew) {
            // 创建新aa账号
            DAuthLogger.d("deploy aa account", TAG)

            // 生成initCode
            // initCode需要把工厂地址和方法打在一起
            val funcCreateAccount = getCreateAccountFunction(eoaAddress)
            val initCodeHex = encodePacked(Address(faAddress), DynamicBytes(funcCreateAccount))
            DAuthLogger.d("initCodeHex=$initCodeHex", TAG)
            initCodeHex.hexStringToByteArray()
        } else {
            // 创建新aa账号
            DAuthLogger.d("aa account exists", TAG)
            emptyCallData()
        }

        // 构造创建AA账号的userOperation
        val userOperation = UserOperation(
            aaAddress,
            nonce,
            initCode,
            callData,
            BigInteger("2100000"),
            BigInteger("21000000"),
            BigInteger("2100000"),
            gasPrice,
            gasPrice,
            emptyCallData(),
            emptyCallData()
        )
        DAuthLogger.d("initCode=${userOperation.initCode.sha3().toHexString()}", TAG)
        DAuthLogger.d("callData=${userOperation.callData.sha3().toHexString()}", TAG)
        DAuthLogger.d(
            "paymasterAndData=${userOperation.paymasterAndData.sha3().toHexString()}",
            TAG
        )

        val code = userOperation.encodeUserOp()
        DAuthLogger.d("code=$code", TAG)
        val codeHash = code.sha3()
        DAuthLogger.d("codeHash=$codeHash", TAG)
        val chainId = web3j.ethChainId().awaitLite { it.chainId }
        DAuthLogger.d("chainId=$chainId", TAG)
        if (chainId == null) {
            DAuthLogger.d("chainId error", TAG)
            return DAuthResult.SdkError()
        }
        val relCode = encodeRelCode(
            codeHash,
            entryPointAddress,
            chainId
        )

        DAuthLogger.d("relCode=$relCode", TAG)
        val hash = relCode.sha3()
        DAuthLogger.d("hash=$hash", TAG)

        val ethMsgHash = SignUtil.getMessageHash(hash.hexStringToByteArray(), true)
        val ethMsgHashHex = ethMsgHash.toHexString()
        DAuthLogger.d("toBeSignedHex=$ethMsgHashHex", TAG)

        // 本地签
        /*val privateKey = "0xde9be858da4a475276426320d5e9262ecfc3ba460bfac56360bfa6c4c28b4ee0"
        val signatureData = SignUtil.signMessage(
            ethMsgHash,
            Credentials.create(privateKey).ecKeyPair
        )*/
        // 本地签2片
        //val signatureData = DAuthJniInvoker.localSignMsg(ethMsgHashHex, MpcKeyStore.getAllKeys().toTypedArray())!!.toSignatureData()
        // mpc签
        val signatureData = WebsocketManager.instance.mpcSign(ethMsgHashHex)
        if (signatureData == null) {
            DAuthLogger.e("mpc sign error")
            return DAuthResult.SdkError()
        }

        val signature = signatureData.r + signatureData.s + signatureData.v
        val signer = DAuthJniInvoker.getWalletAddress(
            ethMsgHash,
            signatureData
        )
        DAuthLogger.d("signer=$signer", TAG)

        val userOpCopy = userOperation.addSignature(signature)
        DAuthLogger.d("signature=${signature.toHexString()} ${signature.size}", TAG)

        val receipt = entryPoint.handleOp(userOpCopy).awaitLite()
        DAuthLogger.d("handleOp receipt=$receipt", TAG)

        val transactionHash = receipt?.transactionHash ?: return DAuthResult.SdkError()
        DAuthLogger.d("transactionHash=$transactionHash", TAG)
        return DAuthResult.Success(transactionHash)
    }

    suspend fun executeMyUserOperation(): DAuthResult<String> {
        val eoaAddress = WalletPrefsV2().getEoaAddress()
        return executeUserOperation(eoaAddress = eoaAddress)
    }

    suspend fun executeUserOperation(eoaAddress: String): DAuthResult<String> {
        /*val function = Function(
            DAuthAccountFactory.FUNC_GETADDRESS,
            listOf<Type<*>>(
                Address(eoaAddress),
                Uint256(0)
            ),
            listOf<TypeReference<*>>(object : TypeReference<Address?>() {})
            val encoded = FunctionEncoder.encode(function)
        )*/

        val nonce = BigInteger.valueOf(1)
        val gasPrice = DefaultGasProvider.GAS_PRICE
        val gasLimit = DefaultGasProvider.GAS_LIMIT
        val to = "0x1234567890abcdef"
        val value = BigInteger.valueOf(1L)
        val rawTransaction: RawTransaction =
            RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, value)
        val encoded = TransactionEncoder.encode(rawTransaction).toHexString()


        val ba = encoded.hexStringToByteArray()
        return executeUserOperation(eoaAddress, ba)
    }

    private fun getCreateAccountFunction(eoaAddress: String): ByteArray {
        val func = Function(
            DAuthAccountFactory.FUNC_CREATEACCOUNT,
            listOf<Type<*>>(
                Address(eoaAddress),
                Uint256(0)
            ), emptyList<TypeReference<*>>()
        )
        val encoded = FunctionEncoder.encode(func)
        return encoded.hexStringToByteArray()
    }

    suspend fun transferMoneyToAa(eoaAccount: String, aaAccount: String, eoaPrivateKey: String){
        val nonce = web3j.ethGetTransactionCount(eoaAccount, DefaultBlockParameterName.LATEST).awaitLite {
            it.transactionCount
        }
        DAuthLogger.d("nonce=$nonce", TAG)

        val value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger()
        val rawTransaction = RawTransaction.createEtherTransaction(
            nonce,
            BigInteger.valueOf(20000000000L),
            BigInteger.valueOf(21000000L),
            aaAccount,
            value
        )

        val signedMessage =
            TransactionEncoder.signMessage(rawTransaction, Credentials.create(eoaPrivateKey))
        val hexValue = signedMessage.toHexString()
        DAuthLogger.d("hexValue=$hexValue", TAG)
        val transactionHash = web3j.ethSendRawTransaction(hexValue).awaitLite {
            it.transactionHash
        }
        DAuthLogger.d("transactionHash=$transactionHash", TAG)
    }

    @Deprecated("使用entryPoint.getNonce")
    suspend fun isCodeExists(aaAddress: String): Boolean? {
        val code = web3j.ethGetCode(aaAddress, DefaultBlockParameterName.LATEST).awaitLite {
            it.code
        } ?: return null
        val result = code != "0x"
        DAuthLogger.d("getCodeByAddress $aaAddress -> $code result=$result", TAG)
        return result
    }
}