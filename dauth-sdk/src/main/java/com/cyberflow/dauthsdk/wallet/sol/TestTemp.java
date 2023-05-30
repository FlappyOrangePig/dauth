package com.cyberflow.dauthsdk.wallet.sol;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.4.2.
 */
@SuppressWarnings("rawtypes")
public class TestTemp extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b503060405161001e90610070565b6001600160a01b039091168152602001604051809103906000f08015801561004a573d6000803e3d6000fd5b50600180546001600160a01b0319166001600160a01b039290921691909117905561007d565b6102948061026683390190565b6101da8061008c6000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c8063074dfaac1461005157806313af4035146100665780634a2ee8d0146100965780638da5cb5b146100bf575b600080fd5b61006461005f366004610154565b6100d2565b005b610064610074366004610180565b600080546001600160a01b0319166001600160a01b0392909216919091179055565b6001546001600160a01b03165b6040516001600160a01b03909116815260200160405180910390f35b6000546100a3906001600160a01b031681565b600154604051630b4db88960e21b81526001600160a01b0384811660048301526024820184905290911690632d36e22490604401600060405180830381600087803b15801561012057600080fd5b505af1158015610134573d6000803e3d6000fd5b505050505050565b6001600160a01b038116811461015157600080fd5b50565b6000806040838503121561016757600080fd5b82356101728161013c565b946020939093013593505050565b60006020828403121561019257600080fd5b813561019d8161013c565b939250505056fea2646970667358221220bc2227482e51e51fdf5bf3b93a346b21e73ad4789570cbdee0cf41c42341b19464736f6c63430008130033608060405234801561001057600080fd5b5060405161029438038061029483398101604081905261002f91610054565b600180546001600160a01b0319166001600160a01b0392909216919091179055610084565b60006020828403121561006657600080fd5b81516001600160a01b038116811461007d57600080fd5b9392505050565b610201806100936000396000f3fe60806040526004361061001f5760003560e01c80632d36e2241461003057005b3661002e573080316000819055005b005b61002e61003e366004610171565b6001546001600160a01b0316331461009d5760405162461bcd60e51b815260206004820152601860248201527f636f756e742063616c6c2074686973206279206f74686572000000000000000060448201526064015b60405180910390fd5b60005481106100de5760405162461bcd60e51b815260206004820152600d60248201526c0cae8d040dcdee840cadcc2e8d609b1b6044820152606401610094565b6001600160a01b0382166108fc6100f66002846101a9565b6040518115909202916000818181858888f1935050505015801561011e573d6000803e3d6000fd5b50732546bcd3c84621e976d8185a91a922ae77ecec30806108fc6101436002856101a9565b6040518115909202916000818181858888f1935050505015801561016b573d6000803e3d6000fd5b50505050565b6000806040838503121561018457600080fd5b82356001600160a01b038116811461019b57600080fd5b946020939093013593505050565b6000826101c657634e487b7160e01b600052601260045260246000fd5b50049056fea2646970667358221220218559ae5d58b73279c67ee96ae9762aab1d4e469763c6c79b7688c73d0b385664736f6c63430008130033";

    public static final String FUNC_TESTCALL = "TestCall";

    public static final String FUNC_GETACC = "getAcc";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_SETOWNER = "setOwner";

    @Deprecated
    protected TestTemp(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TestTemp(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TestTemp(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TestTemp(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> TestCall(String to, BigInteger count) {
        final Function function = new Function(
                FUNC_TESTCALL, 
                Arrays.<Type>asList(new Address(160, to),
                new org.web3j.abi.datatypes.generated.Uint256(count)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> getAcc() {
        final Function function = new Function(FUNC_GETACC, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> setOwner(String o) {
        final Function function = new Function(
                FUNC_SETOWNER, 
                Arrays.<Type>asList(new Address(160, o)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static TestTemp load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TestTemp(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TestTemp load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TestTemp(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TestTemp load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new TestTemp(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TestTemp load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TestTemp(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TestTemp> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TestTemp.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<TestTemp> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TestTemp.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TestTemp> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TestTemp.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TestTemp> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TestTemp.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
