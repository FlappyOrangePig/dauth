package com.infras.dauthsdk.wallet.impl

import androidx.annotation.VisibleForTesting
import com.infras.dauthsdk.api.entity.CommitTransactionData
import com.infras.dauthsdk.api.entity.CreateUserOpAndEstimateGasData
import com.infras.dauthsdk.api.entity.DAuthResult
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_BALANCE_TYPE
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_CANNOT_GET_NONCE
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_ESTIMATE_SIMULATE_DECODE
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_GET_SIGNER_BY_SIGNATURE
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_NO_BALANCE
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_SIGN
import com.infras.dauthsdk.api.entity.DAuthResult.Companion.SDK_ERROR_UNKNOWN
import com.infras.dauthsdk.api.entity.EstimateGasData
import com.infras.dauthsdk.api.entity.ResponseCode
import com.infras.dauthsdk.api.entity.SendTransactionData
import com.infras.dauthsdk.api.entity.WalletBalanceData
import com.infras.dauthsdk.api.entity.traceResult
import com.infras.dauthsdk.api.entity.transformError
import com.infras.dauthsdk.login.network.traceResult
import com.infras.dauthsdk.login.utils.DAuthLogger
import com.infras.dauthsdk.mpc.DAuthJniInvoker
import com.infras.dauthsdk.mpc.entity.Web3jResponseError
import com.infras.dauthsdk.mpc.ext.ElapsedContext
import com.infras.dauthsdk.mpc.util.MoshiUtil
import com.infras.dauthsdk.mpc.websocket.WebsocketManager
import com.infras.dauthsdk.wallet.impl.manager.Managers
import com.infras.dauthsdk.wallet.sol.EntryPoint.UserOperation
import com.infras.dauthsdk.wallet.util.CredentialsUtil
import com.infras.dauthsdk.wallet.util.FunctionEncodeUtil
import com.infras.dauthsdk.wallet.util.SignUtil
import com.infras.dauthsdk.wallet.util.cleanHexPrefix
import com.infras.dauthsdk.wallet.util.hexStringToByteArray
import com.infras.dauthsdk.wallet.util.sha3
import com.infras.dauthsdk.wallet.util.toHexString
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

    private suspend fun <T> RemoteCall<T>.await(): DAuthResult<T> = withContext(Dispatchers.IO) {
        try {
            val r = send()
            DAuthResult.Success(r)
        } catch (t: Throwable) {
            DAuthLogger.e(t.stackTraceToString(), TAG)
            DAuthResult.NetworkError(t)
        }
    }

    private suspend fun <S, T : Response<*>> Request<S, T>.awaitException(): DAuthResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val r = send()
                if (r.hasError()) {
                    val success = r.error.message == "execution reverted"
                    if (success) {
                        val data = getEstimateInfo(r.error.data)
                        if (data == null) {
                            DAuthResult.SdkError(SDK_ERROR_ESTIMATE_SIMULATE_DECODE)
                        } else {
                            DAuthResult.Success(data)
                        }
                    } else {
                        DAuthResult.ServerError(r.error.code, r.error.message)
                    }
                } else {
                    DAuthResult.SdkError(SDK_ERROR_UNKNOWN)
                }
            } catch (t: Throwable) {
                DAuthLogger.e(t.stackTraceToString(), TAG)
                DAuthResult.NetworkError(t)
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
        val callResult = contract.balanceOf(accountAddress)
            .await()
            .traceResult(TAG, "getERC20Balance")
        if (callResult !is DAuthResult.Success) {
            return callResult.transformError()
        }
        return DAuthResult.Success(WalletBalanceData.ERC20(callResult.data))
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

    suspend fun getAaAddressByEoaAddress(eoaAddress: String): DAuthResult<String> {
        val chain = ConfigurationManager.chain()
        val faAddress = chain.factoryAddress
        val accountFactory = com.infras.dauthsdk.wallet.sol.DAuthAccountFactory.load(
            faAddress,
            web3j,
            transactionManager(eoaAddress),
            DefaultGasProvider()
        )
        return accountFactory.getAddress(eoaAddress, BigInteger.ZERO)
            .await()
            .traceResult(TAG, "getAAAddress")
    }

    suspend fun createUserOpAndEstimateGas(
        context: ElapsedContext,
        eoaAddress: String,
        aaAddress: String,
        callData: ByteArray
    ): DAuthResult<CreateUserOpAndEstimateGasData> {
        val chain = ConfigurationManager.chain()
        val faAddress = chain.factoryAddress
        val entryPointAddress = chain.entryPointAddress

        val gasPriceResult = context.runSpending("getGasPrice") { rh ->
            web3j.ethGasPrice()
                .await { it.gasPrice }
                .also {
                    it.traceResult(TAG, "getGasPrice")
                    rh.result = it.isSuccess()
                }
        }
        if (gasPriceResult !is DAuthResult.Success) {
            return gasPriceResult.transformError()
        }
        val gasPrice = gasPriceResult.data

        val nonceResult = context.runSpending("getNonce") { rh ->
            val entryPoint = com.infras.dauthsdk.wallet.sol.EntryPoint.load(
                entryPointAddress,
                web3j,
                transactionManager(eoaAddress),
                StaticGasProvider(gasPrice, BigInteger.valueOf(2100000))
            )
            entryPoint.getNonce(aaAddress, BigInteger.ZERO)
                .await()
                .also {
                    it.traceResult(TAG, "getNonce")
                    rh.result = it.isSuccess()
                }
        }
        if (nonceResult !is DAuthResult.Success) {
            return nonceResult.transformError()
        }
        val nonce = nonceResult.data

        val noTransactions = nonce.noTransactions()
        DAuthLogger.i("noTransactions=$noTransactions", TAG)

        val isDeployedResult = context.runSpending("isDeployed") { rh ->
            isCodeDeployed(aaAddress)
                .also {
                    it.traceResult(TAG, "isDeployed")
                    rh.result = it.isSuccess()
                }
        }
        if (isDeployedResult !is DAuthResult.Success) {
            return isDeployedResult.transformError()
        }
        val isDeployed = isDeployedResult.data

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

        val fee = BigInteger("1000")

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
                gasPrice.add(fee),
                fee,
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

        val gasException = context.runSpending("getGasException") { rh ->
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
            web3j.ethEstimateGas(funcCallTx).awaitException()
                .also {
                    it.traceResult(TAG, "getGasException")
                    rh.result = it.isSuccess()
                }
        }
        if (gasException !is DAuthResult.Success) {
            return gasException.transformError()
        }
        val estimateInfo = gasException.data

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
        return successResult
    }

    suspend fun executeUserOperation(
        context: ElapsedContext,
        userOperation: UserOperation,
        aaAddress: String,
    ): DAuthResult<CommitTransactionData> {
        DAuthLogger.i("executeUserOperation useOp=$userOperation", TAG)

        val balanceResult = context.runSpending("getBalance") { rh ->
            getBalance(aaAddress).also { it.traceResult(TAG, "getBalance") }
                .let { r ->
                    if (r !is DAuthResult.Success) {
                        r
                    } else {
                        val balanceData = r.data
                        DAuthLogger.i("balance=$balanceData", TAG)
                        if (balanceData !is WalletBalanceData.Eth) {
                            DAuthLogger.e("balance type error")
                            DAuthResult.SdkError(SDK_ERROR_BALANCE_TYPE)
                        } else if (balanceData.amount <= BigInteger.ZERO) {
                            DAuthLogger.e("no balance")
                            DAuthResult.SdkError(SDK_ERROR_NO_BALANCE)
                        } else {
                            r
                        }
                    }.also { rh.result = it.isSuccess() }
                }
        }
        if (balanceResult !is DAuthResult.Success) {
            return balanceResult.transformError()
        }

        val chain = ConfigurationManager.chain()
        val entryPointAddress = chain.entryPointAddress

        val code = context.runSpending("encodeUserOp") { userOperation.encodeUserOp() }
        DAuthLogger.i("code=$code", TAG)
        val codeHash = code.sha3()
        DAuthLogger.i("codeHash=$codeHash", TAG)

        val chainIdResult = context.runSpending("ethChainId") {
            web3j.ethChainId()
                .await { it.chainId }
                .also { it.traceResult(TAG, "ethChainId") }
        }
        if (chainIdResult !is DAuthResult.Success) {
            return chainIdResult.transformError()
        }
        val chainId = chainIdResult.data
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

        val signatureData = context.runSpending("sign") { rh ->
            val localSign = ConfigurationManager.innerConfig.localSign
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
                WebsocketManager.instance.mpcSign(ethMsgHashHex).also {
                    rh.result = (it != null)
                }
            } else {
                // 本地内置密钥普通签
                val privateKey =
                    "0xde9be858da4a475276426320d5e9262ecfc3ba460bfac56360bfa6c4c28b4ee0"
                SignUtil.signMessage(ethMsgHash, Credentials.create(privateKey).ecKeyPair)
            }
        }
        if (signatureData == null) {
            DAuthLogger.e("mpc sign error")
            return DAuthResult.SdkError(SDK_ERROR_SIGN)
        }

        val signature = signatureData.r + signatureData.s + signatureData.v
        val signer = context.runSpending("getSigner") { rh ->
            DAuthJniInvoker.getWalletAddress(
                ethMsgHash,
                signatureData
            ).also {
                rh.result = (!it.isNullOrEmpty())
            }
        }
        if (signer == null) {
            DAuthLogger.e("signer error")
            return DAuthResult.SdkError(SDK_ERROR_GET_SIGNER_BY_SIGNATURE)
        }

        DAuthLogger.i("signer=$signer", TAG)

        userOperation.signature = signature

        val userOpCopy = userOperation.reload()
        DAuthLogger.i("signature=${signature.toHexString()} ${signature.size}", TAG)

        val useLocalRelayer = ConfigurationManager.innerConfig.useLocalRelayer
        if (useLocalRelayer) {
            val gasPriceResult = context.runSpending("getPrice") { rh ->
                web3j.ethGasPrice()
                    .await { it.gasPrice }
                    .also {
                        it.traceResult(TAG, "getPrice")
                        rh.result = it.isSuccess()
                    }
            }
            if (gasPriceResult !is DAuthResult.Success) {
                return gasPriceResult.transformError()
            }
            val gasPrice = gasPriceResult.data

            // 部署aa账号的eoa账号地址，最终部署在服务端，entryPoint的TransactionManager需要
            val relayerEoaAddress = "0xdD2FD4581271e230360230F9337D5c0430Bf44C0"
            val entryPointDeploy = com.infras.dauthsdk.wallet.sol.EntryPoint.load(
                entryPointAddress,
                web3j,
                transactionManager(relayerEoaAddress),
                StaticGasProvider(gasPrice, BigInteger.valueOf(2100000))
            )

            val receiptResult = context.runSpending("handleOp") { rh ->
                entryPointDeploy.handleOp(userOpCopy)
                    .await()
                    .also {
                        it.traceResult(TAG, "handleOp")
                        rh.result = it.isSuccess()
                    }
            }
            if (receiptResult !is DAuthResult.Success) {
                return receiptResult.transformError()
            }
            val receipt = receiptResult.data

            val transactionHash = receipt?.transactionHash
                ?: return DAuthResult.SdkError(SDK_ERROR_UNKNOWN)
            DAuthLogger.i("transactionHash=$transactionHash", TAG)
            return DAuthResult.Success(CommitTransactionData(transactionHash))
        } else {
            val sendResult = context.runSpending("commitOp") { rh ->
                Managers.requestApiMpc.commitOp(userOpCopy, chainId.toString())
                    .also {
                        it.traceResult(TAG, "commitOp")
                        rh.result = (it != null)
                    }
            } ?: return DAuthResult.NetworkError()
            if (!sendResult.isSuccess()) {
                return if (ResponseCode.isLoggedOut(sendResult.ret)) {
                    DAuthResult.SdkError(DAuthResult.SDK_ERROR_LOGGED_OUT)
                } else {
                    DAuthResult.ServerError(sendResult.ret, sendResult.info.orEmpty())
                }
            }
            return DAuthResult.Success(CommitTransactionData(sendResult.info.orEmpty()))
        }
    }

    /**
     * [aaAddress]位置是否部署代码
     *
     * @param aaAddress aa账户地址
     * @return 是否部署。空为请求失败
     */
    private suspend fun isCodeDeployed(aaAddress: String): DAuthResult<Boolean> {
        val ethGetCodeResult = web3j.ethGetCode(aaAddress, DefaultBlockParameterName.LATEST)
            .await { it.code }
        if (ethGetCodeResult !is DAuthResult.Success) {
            return ethGetCodeResult.transformError()
        }
        val code = ethGetCodeResult.data
        val isCodeDeployed = code != "0x"
        return DAuthResult.Success(isCodeDeployed)
    }

    suspend fun userOpFlow(eoaAddress: String): DAuthResult<Boolean> {
        val gasPriceResult =
            web3j.ethGasPrice().await { it.gasPrice }
                .also { it.traceResult(TAG, "gasPrice") }
        if (gasPriceResult !is DAuthResult.Success) {
            return gasPriceResult.transformError()
        }
        val gasPrice = gasPriceResult.data

        val entryPointAddress = ConfigurationManager.chain().entryPointAddress
        val entryPoint = com.infras.dauthsdk.wallet.sol.EntryPoint.load(
            entryPointAddress,
            web3j,
            transactionManager(eoaAddress),
            StaticGasProvider(gasPrice, BigInteger.valueOf(2100000))
        )

        val blockNumber = web3j.ethBlockNumber().await { it.blockNumber }
        if (blockNumber !is DAuthResult.Success) {
            return DAuthResult.SdkError(SDK_ERROR_UNKNOWN)
        }
        val r: Boolean = suspendCancellableCoroutine { continuation ->
            val startBlock: DefaultBlockParameter = DefaultBlockParameterNumber(
                BigInteger.ZERO
            )
            val endBlock: DefaultBlockParameter = DefaultBlockParameterNumber(blockNumber.data)
            val flowable = entryPoint.userOperationEventEventFlowable(startBlock, endBlock)
            flowable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(object :
                    FlowableSubscriber<com.infras.dauthsdk.wallet.sol.EntryPoint.UserOperationEventEventResponse> {
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

                    override fun onNext(t: com.infras.dauthsdk.wallet.sol.EntryPoint.UserOperationEventEventResponse?) {
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