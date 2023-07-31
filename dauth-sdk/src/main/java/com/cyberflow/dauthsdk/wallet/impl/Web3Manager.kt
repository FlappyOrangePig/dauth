package com.cyberflow.dauthsdk.wallet.impl

import androidx.annotation.VisibleForTesting
import com.cyberflow.dauthsdk.api.DAuthSDK
import com.cyberflow.dauthsdk.api.entity.CommitTransactionData
import com.cyberflow.dauthsdk.api.entity.CreateUserOpAndEstimateGasData
import com.cyberflow.dauthsdk.api.entity.DAuthResult
import com.cyberflow.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GET_NONCE
import com.cyberflow.dauthsdk.api.entity.EstimateGasData
import com.cyberflow.dauthsdk.api.entity.ResponseCode
import com.cyberflow.dauthsdk.api.entity.SendTransactionData
import com.cyberflow.dauthsdk.api.entity.WalletBalanceData
import com.cyberflow.dauthsdk.login.utils.DAuthLogger
import com.cyberflow.dauthsdk.mpc.DAuthJniInvoker
import com.cyberflow.dauthsdk.mpc.entity.Web3jResponseError
import com.cyberflow.dauthsdk.mpc.ext.ElapsedContext
import com.cyberflow.dauthsdk.mpc.util.MoshiUtil
import com.cyberflow.dauthsdk.mpc.websocket.WebsocketManager
import com.cyberflow.dauthsdk.wallet.impl.manager.Managers
import com.cyberflow.dauthsdk.wallet.sol.DAuthAccountFactory
import com.cyberflow.dauthsdk.wallet.sol.EntryPoint
import com.cyberflow.dauthsdk.wallet.sol.EntryPoint.UserOperation
import com.cyberflow.dauthsdk.wallet.util.CredentialsUtil
import com.cyberflow.dauthsdk.wallet.util.FunctionEncodeUtil
import com.cyberflow.dauthsdk.wallet.util.SignUtil
import com.cyberflow.dauthsdk.wallet.util.cleanHexPrefix
import com.cyberflow.dauthsdk.wallet.util.hexStringToByteArray
import com.cyberflow.dauthsdk.wallet.util.sha3
import com.cyberflow.dauthsdk.wallet.util.toHexString
import io.reactivex.FlowableSubscriber
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.reactivestreams.Subscription
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
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
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.coroutines.resume


private const val TAG = "Web3Manager"

private fun BigInteger.noTransactions() = compareTo(BigInteger.ZERO) == 0

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
 * 重新调用构造方法执行父类行为，使其在执行时可以被正确地序列化
 */
private fun UserOperation.reload() = UserOperation(
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

internal class Web3Manager {

    private var _web3j: Web3j? = null
    internal val web3j: Web3j get() = _web3j!!

    fun reset(url: String) {
        _web3j?.shutdown()
        _web3j = Web3j.build(
            HttpService(url, HttpClient.client)
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
                    DAuthResult.ServerError(r.error.code, r.error.message)
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

    private suspend fun <S, T : Response<*>> Request<S, T>.awaitException(): String? =
        withContext(Dispatchers.IO) {
            try {
                val r = send()
                if (r.hasError()) {
                    r.error.data
                } else {
                    null
                }
            } catch (t: Throwable) {
                DAuthLogger.e(t.stackTraceToString(), TAG)
                null
            }
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

    suspend fun getAaAddressByEoaAddress(eoaAddress: String): String? {
        val addresses = ConfigurationManager.addresses()
        val faAddress = addresses.factoryAddress
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

    suspend fun createUserOpAndEstimateGas(
        eoaAddress: String,
        aaAddress: String,
        callData: ByteArray
    ): DAuthResult<CreateUserOpAndEstimateGasData> {
        val addresses = ConfigurationManager.addresses()
        val faAddress = addresses.factoryAddress
        val entryPointAddress = addresses.entryPointAddress

        val context = ElapsedContext(TAG)

        val gasPrice = context.runSpending("getGasPrice") {
            web3j.ethGasPrice().awaitLite { it.gasPrice }
        }
        DAuthLogger.i("gasPrice=$gasPrice", TAG)
        if (gasPrice == null) {
            DAuthLogger.e("gas error")
            return DAuthResult.SdkError()
        }

        val nonce = context.runSpending("getNonce") {
            val entryPoint = EntryPoint.load(
                entryPointAddress,
                web3j,
                transactionManager(eoaAddress),
                StaticGasProvider(gasPrice, BigInteger.valueOf(2100000))
            )
            val nonce = entryPoint.getNonce(aaAddress, BigInteger.ZERO).awaitLite()
            nonce
        }
        DAuthLogger.i("nonce=$nonce")
        if (nonce == null) {
            DAuthLogger.e("nonce error")
            return DAuthResult.SdkError()
        }

        val noTransactions = nonce.noTransactions()
        DAuthLogger.i("noTransactions=$noTransactions")

        val isDeployed = context.runSpending("isDeployed") {
            isCodeDeployed(aaAddress)
        }
        DAuthLogger.i("isDeployed=$isDeployed")
        if (isDeployed == null) {
            DAuthLogger.e("not deployed")
            return DAuthResult.SdkError()
        }

        val initCode = context.runSpending("createInitCode") {
            if (noTransactions && !isDeployed) {
                // 创建新aa账号
                DAuthLogger.i("deploy aa account", TAG)

                // 生成initCode
                // initCode需要把工厂地址和方法打在一起
                val funcCreateAccount = FunctionEncodeUtil.getCreateAccountFunction(eoaAddress)
                val initCodeHex = encodePacked(Address(faAddress), DynamicBytes(funcCreateAccount))
                DAuthLogger.i("initCodeHex=$initCodeHex", TAG)
                initCodeHex.hexStringToByteArray()
            } else {
                // 创建新aa账号
                DAuthLogger.i("aa account exists", TAG)
                byteArrayOf()
            }
        }

        // 构造创建AA账号的userOperation
        val userOperation = context.runSpending("createUserOperation") {
            UserOperation(
                aaAddress,
                nonce,
                initCode,
                callData,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                gasPrice,
                gasPrice,
                byteArrayOf(),
                byteArrayOf()
            ).also { userOp ->
                DAuthLogger.i("initCode=${userOp.initCode.sha3().toHexString()}", TAG)
                DAuthLogger.i("callData=${userOp.callData.sha3().toHexString()}", TAG)
                DAuthLogger.i(
                    "paymasterAndData=${userOp.paymasterAndData.sha3().toHexString()}",
                    TAG
                )
            }
        }

        val gasException = context.runSpending("getGasException") {
            // 预估费用
            val funcSimulateHandleOp = FunctionEncodeUtil.getSimulateHandleOpFunction(userOperation)
            val funcSimulateHandleOpHex = funcSimulateHandleOp.toHexString()
            val funcCallTx = Transaction.createFunctionCallTransaction(
                null,
                null,
                null,
                null,
                entryPointAddress,
                funcSimulateHandleOpHex
            )
            val gasException = web3j.ethEstimateGas(funcCallTx).awaitException()
            gasException
        }
        DAuthLogger.i("gasException=$gasException", TAG)
        if (gasException == null) {
            DAuthLogger.e("estimate gas error", TAG)
            return DAuthResult.SdkError()
        }
        val estimateInfo = context.runSpending("getEstimateInfo") {
            getEstimateInfo(gasException)
        }
        if (estimateInfo == null) {
            DAuthLogger.e("estimateInfo error", TAG)
            return DAuthResult.SdkError()
        }

        val successResult = context.runSpending("create success result") {
            val estimateData = estimateInfo.cleanHexPrefix()
            val verificationGasLimit = Numeric.toBigInt(estimateData.substring(8, 8 + 64))
            val callGasLimit = Numeric.toBigInt(estimateData.substring(8 + 64))
            DAuthLogger.i("estimateData=$estimateData", TAG)
            DAuthLogger.i("verificationGasLimit=$verificationGasLimit", TAG)
            DAuthLogger.i("callGasLimit=$callGasLimit", TAG)

            userOperation.verificationGasLimit = verificationGasLimit
            userOperation.callGasLimit = callGasLimit.multiply(BigInteger("10"))

            DAuthResult.Success(
                CreateUserOpAndEstimateGasData(
                    verificationGasLimit.multiply(gasPrice),
                    callGasLimit.multiply(gasPrice),
                    userOp = userOperation
                )
            )
        }
        context.traceElapsedList()
        return successResult
    }

    suspend fun executeUserOperation(
        userOperation: UserOperation,
        aaAddress: String,
    ): DAuthResult<CommitTransactionData> {
        DAuthLogger.i("executeUserOperation useOp=$userOperation", TAG)

        val context = ElapsedContext(TAG)

        val balanceResult = context.runSpending("getBalance") { getBalance(aaAddress) }
        if (balanceResult !is DAuthResult.Success) {
            DAuthLogger.e("balance error")
            return DAuthResult.SdkError()
        }
        val balanceData = balanceResult.data
        DAuthLogger.i("balance=$balanceData", TAG)
        if (balanceData !is WalletBalanceData.Eth) {
            DAuthLogger.e("balance type error")
            return DAuthResult.SdkError()
        }
        if (balanceData.amount <= BigInteger.ZERO) {
            DAuthLogger.e("no balance")
            return DAuthResult.SdkError()
        }

        val addresses = ConfigurationManager.addresses()
        val entryPointAddress = addresses.entryPointAddress

        val code = context.runSpending("encodeUserOp") { userOperation.encodeUserOp() }
        DAuthLogger.i("code=$code", TAG)
        val codeHash = code.sha3()
        DAuthLogger.i("codeHash=$codeHash", TAG)
        val chainId = web3j.ethChainId().awaitLite { it.chainId }
        DAuthLogger.i("chainId=$chainId", TAG)
        if (chainId == null) {
            DAuthLogger.e("chainId error", TAG)
            return DAuthResult.SdkError()
        }
        val relCode = context.runSpending("encodeRelCode") {
            encodeRelCode(
                codeHash,
                entryPointAddress,
                chainId
            )
        }

        DAuthLogger.i("relCode=$relCode", TAG)
        val hash = relCode.sha3()
        DAuthLogger.i("hash=$hash", TAG)

        val ethMsgHash = context.runSpending("getMessageHash") {
            SignUtil.getMessageHash(
                hash.hexStringToByteArray(),
                true
            )
        }
        val ethMsgHashHex = ethMsgHash.toHexString()
        DAuthLogger.i("toBeSignedHex=$ethMsgHashHex", TAG)

        val signatureData = context.runSpending("sign") {
            val localSign = DAuthSDK.impl.config.localSign
            if (localSign) {
                // 本地2片签
                DAuthJniInvoker.localSignMsg(
                    ethMsgHashHex,
                    Managers.mpcKeyStore.getAllKeys().toTypedArray()
                )!!
                    .toSignatureData()

                // 模拟多轮签名
                //LocalMpcSign.mpcSign(ethMsgHashHex)
            } else if (true) {
                // mpc签
                WebsocketManager.instance.mpcSign(ethMsgHashHex)
            } else {
                // 本地内置密钥普通签
                val privateKey =
                    "0xde9be858da4a475276426320d5e9262ecfc3ba460bfac56360bfa6c4c28b4ee0"
                SignUtil.signMessage(ethMsgHash, Credentials.create(privateKey).ecKeyPair)
            }
        }
        if (signatureData == null) {
            DAuthLogger.e("mpc sign error")
            return DAuthResult.SdkError()
        }

        val signature = signatureData.r + signatureData.s + signatureData.v
        val signer = context.runSpending("getSigner") {
            DAuthJniInvoker.getWalletAddress(
                ethMsgHash,
                signatureData
            )
        }
        if (signer == null) {
            DAuthLogger.e("signer error")
            return DAuthResult.SdkError()
        }

        DAuthLogger.i("signer=$signer", TAG)

        userOperation.signature = signature

        val userOpCopy = userOperation.reload()
        DAuthLogger.i("signature=${signature.toHexString()} ${signature.size}", TAG)

        val useLocalRelayer = DAuthSDK.impl.config.useLocalRelayer
        if (useLocalRelayer) {
            val gasPrice = context.runSpending("getPrice") {
                web3j.ethGasPrice().awaitLite { it.gasPrice }
            }
            DAuthLogger.i("gasPrice=$gasPrice", TAG)
            if (gasPrice == null) {
                return DAuthResult.SdkError()
            }

            // 部署aa账号的eoa账号地址，最终部署在服务端，entryPoint的TransactionManager需要
            val relayerEoaAddress = "0xdD2FD4581271e230360230F9337D5c0430Bf44C0"
            val entryPointDeploy = EntryPoint.load(
                entryPointAddress,
                web3j,
                transactionManager(relayerEoaAddress),
                StaticGasProvider(gasPrice, BigInteger.valueOf(2100000))
            )

            val receipt = context.runSpending("handleOp") {
                entryPointDeploy.handleOp(userOpCopy).awaitLite()
            }
            DAuthLogger.i("handleOp receipt=$receipt", TAG)
            val transactionHash = receipt?.transactionHash ?: return DAuthResult.SdkError()
            DAuthLogger.i("transactionHash=$transactionHash", TAG)
            return DAuthResult.Success(CommitTransactionData(transactionHash))
        } else {
            val sendResult = RelayerRequester.sendRequest(userOpCopy)
            DAuthLogger.i("sendResult=$sendResult", TAG)
            if (sendResult == null) {
                return DAuthResult.NetworkError()
            }
            if (!sendResult.isSuccess()) {
                return if (ResponseCode.isLoggedOut(sendResult.ret)) {
                    DAuthResult.SdkError(DAuthResult.SDK_ERROR_LOGGED_OUT)
                } else {
                    DAuthResult.ServerError(sendResult.ret, sendResult.info)
                }
            }
            context.traceElapsedList()
            return DAuthResult.Success(CommitTransactionData(sendResult.info))
        }
    }

    /**
     * [aaAddress]位置是否部署代码
     *
     * @param aaAddress aa账户地址
     * @return 是否部署。空为请求失败
     */
    private suspend fun isCodeDeployed(aaAddress: String): Boolean? {
        val code = web3j.ethGetCode(aaAddress, DefaultBlockParameterName.LATEST).awaitLite {
            it.code
        } ?: return null
        val result = code != "0x"
        DAuthLogger.d("getCodeByAddress $aaAddress -> $code result=$result", TAG)
        return result
    }

    suspend fun userOpFlow(eoaAddress: String): DAuthResult<Boolean> {
        val gasPrice =
            web3j.ethGasPrice().awaitLite { it.gasPrice } ?: return DAuthResult.SdkError()
        DAuthLogger.d("gasPrice=$gasPrice", TAG)

        val entryPointAddress = ConfigurationManager.addresses().entryPointAddress
        val entryPoint = EntryPoint.load(
            entryPointAddress,
            web3j,
            transactionManager(eoaAddress),
            StaticGasProvider(gasPrice, BigInteger.valueOf(2100000))
        )

        val blockNumber = web3j.ethBlockNumber().await { it.blockNumber }
        if (blockNumber !is DAuthResult.Success) {
            return DAuthResult.SdkError()
        }
        val r: Boolean = suspendCancellableCoroutine { continuation ->
            val startBlock: DefaultBlockParameter = DefaultBlockParameterNumber(
                BigInteger.ZERO
            )
            val endBlock: DefaultBlockParameter = DefaultBlockParameterNumber(blockNumber.data)
            val flowable = entryPoint.userOperationEventEventFlowable(startBlock, endBlock)
            flowable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(object : FlowableSubscriber<EntryPoint.UserOperationEventEventResponse> {
                    override fun onSubscribe(s: Subscription) {
                        DAuthLogger.d("onSubscribe $s", TAG)
                    }

                    override fun onError(t: Throwable?) {
                        DAuthLogger.d("onError ${t?.stackTraceToString()}", TAG)
                        continuation.resume(false)
                    }

                    override fun onComplete() {
                        DAuthLogger.d("onComplete", TAG)
                        continuation.resume(true)
                    }

                    override fun onNext(t: EntryPoint.UserOperationEventEventResponse?) {
                        DAuthLogger.d("onNext ${MoshiUtil.toJson(t)}", TAG)
                    }
                })
        }
        return DAuthResult.Success(r)
    }

    private fun getEstimateInfo(gasException: String): String? {
        return kotlin.runCatching {
            MoshiUtil.fromJson<Web3jResponseError>(gasException, false)?.data
        }.getOrNull() ?: kotlin.runCatching {
            MoshiUtil.fromJson<String>(gasException, false)
        }.getOrNull()
    }
}