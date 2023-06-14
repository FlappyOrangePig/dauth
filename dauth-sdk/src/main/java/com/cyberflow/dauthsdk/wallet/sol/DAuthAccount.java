package com.cyberflow.dauthsdk.wallet.sol;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
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
public class DAuthAccount extends Contract {
    public static final String BINARY = "0x60c0604052306080523480156200001557600080fd5b5060405162001dc538038062001dc5833981016040819052620000389162000118565b6001600160a01b03811660a0526200004f62000056565b506200014a565b600054610100900460ff1615620000c35760405162461bcd60e51b815260206004820152602760248201527f496e697469616c697a61626c653a20636f6e747261637420697320696e697469604482015266616c697a696e6760c81b606482015260840160405180910390fd5b60005460ff908116101562000116576000805460ff191660ff9081179091556040519081527f7f26b83ff96e1f2b6a682f133852f6798a09c465da95921460cefb38474024989060200160405180910390a15b565b6000602082840312156200012b57600080fd5b81516001600160a01b03811681146200014357600080fd5b9392505050565b60805160a051611c08620001bd600039600081816102c401528181610608015281816106890152818161090301528181610aad01528181610ae701528181610d640152610f8101526000818161050b0152818161054b0152818161071b0152818161075b01526107ee0152611c086000f3fe60806040526004361061010c5760003560e01c806352d1902d11610095578063bc197c8111610064578063bc197c8114610308578063c399ec8814610337578063c4d66de81461034c578063d087d2881461036c578063f23a6e611461038157600080fd5b806352d1902d146102625780638da5cb5b14610277578063b0d691fe146102b5578063b61d27f6146102e857600080fd5b80633659cfe6116100dc5780633659cfe6146101d95780633a871cdd146101f95780634a58db19146102275780634d44560d1461022f5780634f1ef2861461024f57600080fd5b806223de291461011857806301ffc9a71461013f578063150b7a021461017457806318dfb3c7146101b957600080fd5b3661011357005b600080fd5b34801561012457600080fd5b5061013d6101333660046114e3565b5050505050505050565b005b34801561014b57600080fd5b5061015f61015a366004611594565b6103ae565b60405190151581526020015b60405180910390f35b34801561018057600080fd5b506101a061018f3660046115be565b630a85bd0160e11b95945050505050565b6040516001600160e01b0319909116815260200161016b565b3480156101c557600080fd5b5061013d6101d4366004611676565b610400565b3480156101e557600080fd5b5061013d6101f43660046116e2565b610500565b34801561020557600080fd5b506102196102143660046116ff565b6105e0565b60405190815260200161016b565b61013d610606565b34801561023b57600080fd5b5061013d61024a366004611753565b61067f565b61013d61025d366004611795565b610710565b34801561026e57600080fd5b506102196107e1565b34801561028357600080fd5b5060005461029d906201000090046001600160a01b031681565b6040516001600160a01b03909116815260200161016b565b3480156102c157600080fd5b507f000000000000000000000000000000000000000000000000000000000000000061029d565b3480156102f457600080fd5b5061013d610303366004611859565b610894565b34801561031457600080fd5b506101a06103233660046118a9565b63bc197c8160e01b98975050505050505050565b34801561034357600080fd5b506102196108e3565b34801561035857600080fd5b5061013d6103673660046116e2565b610974565b34801561037857600080fd5b50610219610a86565b34801561038d57600080fd5b506101a061039c366004611947565b63f23a6e6160e01b9695505050505050565b60006001600160e01b03198216630a85bd0160e11b14806103df57506001600160e01b03198216630271189760e51b145b806103fa57506001600160e01b031982166301ffc9a760e01b145b92915050565b610408610adc565b8281146104525760405162461bcd60e51b815260206004820152601360248201527277726f6e67206172726179206c656e6774687360681b60448201526064015b60405180910390fd5b60005b838110156104f9576104e7858583818110610472576104726119c3565b905060200201602081019061048791906116e2565b600085858581811061049b5761049b6119c3565b90506020028101906104ad91906119d9565b8080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250610b7192505050565b806104f181611a20565b915050610455565b5050505050565b306001600160a01b037f00000000000000000000000000000000000000000000000000000000000000001614156105495760405162461bcd60e51b815260040161044990611a49565b7f00000000000000000000000000000000000000000000000000000000000000006001600160a01b0316610592600080516020611b8c833981519152546001600160a01b031690565b6001600160a01b0316146105b85760405162461bcd60e51b815260040161044990611a95565b6105c181610be1565b604080516000808252602082019092526105dd91839190610be9565b50565b60006105ea610d59565b6105f48484610dd1565b90506105ff82610eaa565b9392505050565b7f000000000000000000000000000000000000000000000000000000000000000060405163b760faf960e01b81523060048201526001600160a01b03919091169063b760faf99034906024016000604051808303818588803b15801561066b57600080fd5b505af11580156104f9573d6000803e3d6000fd5b610687610ef7565b7f000000000000000000000000000000000000000000000000000000000000000060405163040b850f60e31b81526001600160a01b03848116600483015260248201849052919091169063205c287890604401600060405180830381600087803b1580156106f457600080fd5b505af1158015610708573d6000803e3d6000fd5b505050505050565b306001600160a01b037f00000000000000000000000000000000000000000000000000000000000000001614156107595760405162461bcd60e51b815260040161044990611a49565b7f00000000000000000000000000000000000000000000000000000000000000006001600160a01b03166107a2600080516020611b8c833981519152546001600160a01b031690565b6001600160a01b0316146107c85760405162461bcd60e51b815260040161044990611a95565b6107d182610be1565b6107dd82826001610be9565b5050565b6000306001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146108815760405162461bcd60e51b815260206004820152603860248201527f555550535570677261646561626c653a206d757374206e6f742062652063616c60448201527f6c6564207468726f7567682064656c656761746563616c6c00000000000000006064820152608401610449565b50600080516020611b8c83398151915290565b61089c610adc565b6108dd848484848080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250610b7192505050565b50505050565b6040516370a0823160e01b81523060048201526000906001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016906370a08231906024015b602060405180830381865afa15801561094b573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061096f9190611ae1565b905090565b600054610100900460ff16158080156109945750600054600160ff909116105b806109ae5750303b1580156109ae575060005460ff166001145b610a115760405162461bcd60e51b815260206004820152602e60248201527f496e697469616c697a61626c653a20636f6e747261637420697320616c72656160448201526d191e481a5b9a5d1a585b1a5e995960921b6064820152608401610449565b6000805460ff191660011790558015610a34576000805461ff0019166101001790555b610a3d82610f4e565b80156107dd576000805461ff0019169055604051600181527f7f26b83ff96e1f2b6a682f133852f6798a09c465da95921460cefb38474024989060200160405180910390a15050565b604051631aab3f0d60e11b8152306004820152600060248201819052906001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016906335567e1a9060440161092e565b336001600160a01b037f0000000000000000000000000000000000000000000000000000000000000000161480610b2357506000546201000090046001600160a01b031633145b610b6f5760405162461bcd60e51b815260206004820181905260248201527f6163636f756e743a206e6f74204f776e6572206f7220456e747279506f696e746044820152606401610449565b565b600080846001600160a01b03168484604051610b8d9190611b26565b60006040518083038185875af1925050503d8060008114610bca576040519150601f19603f3d011682016040523d82523d6000602084013e610bcf565b606091505b5091509150816104f957805160208201fd5b6105dd610ef7565b7f4910fdfa16fed3260ed0e7147f7cc6da11a60208b5b9406d12a635614ffd91435460ff1615610c2157610c1c83610fca565b505050565b826001600160a01b03166352d1902d6040518163ffffffff1660e01b8152600401602060405180830381865afa925050508015610c7b575060408051601f3d908101601f19168201909252610c7891810190611ae1565b60015b610cde5760405162461bcd60e51b815260206004820152602e60248201527f45524331393637557067726164653a206e657720696d706c656d656e7461746960448201526d6f6e206973206e6f74205555505360901b6064820152608401610449565b600080516020611b8c8339815191528114610d4d5760405162461bcd60e51b815260206004820152602960248201527f45524331393637557067726164653a20756e737570706f727465642070726f786044820152681a58589b195555525160ba1b6064820152608401610449565b50610c1c838383611066565b336001600160a01b037f00000000000000000000000000000000000000000000000000000000000000001614610b6f5760405162461bcd60e51b815260206004820152601c60248201527f6163636f756e743a206e6f742066726f6d20456e747279506f696e74000000006044820152606401610449565b600080610e2b836040517f19457468657265756d205369676e6564204d6573736167653a0a3332000000006020820152603c8101829052600090605c01604051602081830303815290604052805190602001209050919050565b9050610e7b610e3e6101408601866119d9565b8080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250859392505061108b9050565b6000546201000090046001600160a01b03908116911614610ea05760019150506103fa565b5060009392505050565b80156105dd57604051600090339060001990849084818181858888f193505050503d80600081146104f9576040519150601f19603f3d011682016040523d82523d6000602084013e6104f9565b6000546201000090046001600160a01b0316331480610f1557503330145b610b6f5760405162461bcd60e51b815260206004820152600a60248201526937b7363c9037bbb732b960b11b6044820152606401610449565b6000805462010000600160b01b031916620100006001600160a01b038481168202929092178084556040519190048216927f0000000000000000000000000000000000000000000000000000000000000000909216917f2984971f9f840be64c4350109dc88eebe35e5e24d46daab1dc8dc0fe9f9842df91a350565b6001600160a01b0381163b6110375760405162461bcd60e51b815260206004820152602d60248201527f455243313936373a206e657720696d706c656d656e746174696f6e206973206e60448201526c1bdd08184818dbdb9d1c9858dd609a1b6064820152608401610449565b600080516020611b8c83398151915280546001600160a01b0319166001600160a01b0392909216919091179055565b61106f836110af565b60008251118061107c5750805b15610c1c576108dd83836110ef565b600080600061109a8585611114565b915091506110a78161115a565b509392505050565b6110b881610fca565b6040516001600160a01b038216907fbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b90600090a250565b60606105ff8383604051806060016040528060278152602001611bac602791396112a8565b60008082516041141561114b5760208301516040840151606085015160001a61113f87828585611320565b94509450505050611153565b506000905060025b9250929050565b600081600481111561116e5761116e611b42565b14156111775750565b600181600481111561118b5761118b611b42565b14156111d95760405162461bcd60e51b815260206004820152601860248201527f45434453413a20696e76616c6964207369676e617475726500000000000000006044820152606401610449565b60028160048111156111ed576111ed611b42565b141561123b5760405162461bcd60e51b815260206004820152601f60248201527f45434453413a20696e76616c6964207369676e6174757265206c656e677468006044820152606401610449565b600381600481111561124f5761124f611b42565b14156105dd5760405162461bcd60e51b815260206004820152602260248201527f45434453413a20696e76616c6964207369676e6174757265202773272076616c604482015261756560f01b6064820152608401610449565b6060600080856001600160a01b0316856040516112c59190611b26565b600060405180830381855af49150503d8060008114611300576040519150601f19603f3d011682016040523d82523d6000602084013e611305565b606091505b5091509150611316868383876113e4565b9695505050505050565b6000807f7fffffffffffffffffffffffffffffff5d576e7357a4501ddfe92f46681b20a083111561135757506000905060036113db565b6040805160008082526020820180845289905260ff881692820192909252606081018690526080810185905260019060a0016020604051602081039080840390855afa1580156113ab573d6000803e3d6000fd5b5050604051601f1901519150506001600160a01b0381166113d4576000600192509250506113db565b9150600090505b94509492505050565b60608315611450578251611449576001600160a01b0385163b6114495760405162461bcd60e51b815260206004820152601d60248201527f416464726573733a2063616c6c20746f206e6f6e2d636f6e74726163740000006044820152606401610449565b508161145a565b61145a8383611462565b949350505050565b8151156114725781518083602001fd5b8060405162461bcd60e51b81526004016104499190611b58565b6001600160a01b03811681146105dd57600080fd5b60008083601f8401126114b357600080fd5b50813567ffffffffffffffff8111156114cb57600080fd5b60208301915083602082850101111561115357600080fd5b60008060008060008060008060c0898b0312156114ff57600080fd5b883561150a8161148c565b9750602089013561151a8161148c565b9650604089013561152a8161148c565b955060608901359450608089013567ffffffffffffffff8082111561154e57600080fd5b61155a8c838d016114a1565b909650945060a08b013591508082111561157357600080fd5b506115808b828c016114a1565b999c989b5096995094979396929594505050565b6000602082840312156115a657600080fd5b81356001600160e01b0319811681146105ff57600080fd5b6000806000806000608086880312156115d657600080fd5b85356115e18161148c565b945060208601356115f18161148c565b935060408601359250606086013567ffffffffffffffff81111561161457600080fd5b611620888289016114a1565b969995985093965092949392505050565b60008083601f84011261164357600080fd5b50813567ffffffffffffffff81111561165b57600080fd5b6020830191508360208260051b850101111561115357600080fd5b6000806000806040858703121561168c57600080fd5b843567ffffffffffffffff808211156116a457600080fd5b6116b088838901611631565b909650945060208701359150808211156116c957600080fd5b506116d687828801611631565b95989497509550505050565b6000602082840312156116f457600080fd5b81356105ff8161148c565b60008060006060848603121561171457600080fd5b833567ffffffffffffffff81111561172b57600080fd5b8401610160818703121561173e57600080fd5b95602085013595506040909401359392505050565b6000806040838503121561176657600080fd5b82356117718161148c565b946020939093013593505050565b634e487b7160e01b600052604160045260246000fd5b600080604083850312156117a857600080fd5b82356117b38161148c565b9150602083013567ffffffffffffffff808211156117d057600080fd5b818501915085601f8301126117e457600080fd5b8135818111156117f6576117f661177f565b604051601f8201601f19908116603f0116810190838211818310171561181e5761181e61177f565b8160405282815288602084870101111561183757600080fd5b8260208601602083013760006020848301015280955050505050509250929050565b6000806000806060858703121561186f57600080fd5b843561187a8161148c565b935060208501359250604085013567ffffffffffffffff81111561189d57600080fd5b6116d6878288016114a1565b60008060008060008060008060a0898b0312156118c557600080fd5b88356118d08161148c565b975060208901356118e08161148c565b9650604089013567ffffffffffffffff808211156118fd57600080fd5b6119098c838d01611631565b909850965060608b013591508082111561192257600080fd5b61192e8c838d01611631565b909650945060808b013591508082111561157357600080fd5b60008060008060008060a0878903121561196057600080fd5b863561196b8161148c565b9550602087013561197b8161148c565b94506040870135935060608701359250608087013567ffffffffffffffff8111156119a557600080fd5b6119b189828a016114a1565b979a9699509497509295939492505050565b634e487b7160e01b600052603260045260246000fd5b6000808335601e198436030181126119f057600080fd5b83018035915067ffffffffffffffff821115611a0b57600080fd5b60200191503681900382131561115357600080fd5b6000600019821415611a4257634e487b7160e01b600052601160045260246000fd5b5060010190565b6020808252602c908201527f46756e6374696f6e206d7573742062652063616c6c6564207468726f7567682060408201526b19195b1959d85d1958d85b1b60a21b606082015260800190565b6020808252602c908201527f46756e6374696f6e206d7573742062652063616c6c6564207468726f7567682060408201526b6163746976652070726f787960a01b606082015260800190565b600060208284031215611af357600080fd5b5051919050565b60005b83811015611b15578181015183820152602001611afd565b838111156108dd5750506000910152565b60008251611b38818460208701611afa565b9190910192915050565b634e487b7160e01b600052602160045260246000fd5b6020815260008251806020840152611b77816040850160208701611afa565b601f01601f1916919091016040019291505056fe360894a13ba1a3210667c828492db98dca3e2076cc3735a920a3ca505d382bbc416464726573733a206c6f772d6c6576656c2064656c65676174652063616c6c206661696c6564a2646970667358221220e1c344fb16b00d53f624a1ab7949deaae1be0094fe91603fddd3652cf856a31264736f6c634300080a0033";

    public static final String FUNC_ADDDEPOSIT = "addDeposit";

    public static final String FUNC_ENTRYPOINT = "entryPoint";

    public static final String FUNC_EXECUTE = "execute";

    public static final String FUNC_EXECUTEBATCH = "executeBatch";

    public static final String FUNC_GETDEPOSIT = "getDeposit";

    public static final String FUNC_GETNONCE = "getNonce";

    public static final String FUNC_INITIALIZE = "initialize";

    public static final String FUNC_ONERC1155BATCHRECEIVED = "onERC1155BatchReceived";

    public static final String FUNC_ONERC1155RECEIVED = "onERC1155Received";

    public static final String FUNC_ONERC721RECEIVED = "onERC721Received";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PROXIABLEUUID = "proxiableUUID";

    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

    public static final String FUNC_TOKENSRECEIVED = "tokensReceived";

    public static final String FUNC_UPGRADETO = "upgradeTo";

    public static final String FUNC_UPGRADETOANDCALL = "upgradeToAndCall";

    public static final String FUNC_VALIDATEUSEROP = "validateUserOp";

    public static final String FUNC_WITHDRAWDEPOSITTO = "withdrawDepositTo";

    public static final Event ADMINCHANGED_EVENT = new Event("AdminChanged", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}));
    ;

    public static final Event BEACONUPGRADED_EVENT = new Event("BeaconUpgraded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event DAUTHACCOUNTINITIALIZED_EVENT = new Event("DAuthAccountInitialized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event INITIALIZED_EVENT = new Event("Initialized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
    ;

    public static final Event UPGRADED_EVENT = new Event("Upgraded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<String, String>();
    }

    @Deprecated
    protected DAuthAccount(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected DAuthAccount(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected DAuthAccount(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected DAuthAccount(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    /*public static List<AdminChangedEventResponse> getAdminChangedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ADMINCHANGED_EVENT, transactionReceipt);
        ArrayList<AdminChangedEventResponse> responses = new ArrayList<AdminChangedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            AdminChangedEventResponse typedResponse = new AdminChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousAdmin = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newAdmin = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }*/

    public Flowable<AdminChangedEventResponse> adminChangedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, AdminChangedEventResponse>() {
            @Override
            public AdminChangedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(ADMINCHANGED_EVENT, log);
                AdminChangedEventResponse typedResponse = new AdminChangedEventResponse();
                typedResponse.log = log;
                typedResponse.previousAdmin = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.newAdmin = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<AdminChangedEventResponse> adminChangedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ADMINCHANGED_EVENT));
        return adminChangedEventFlowable(filter);
    }

    /*public static List<BeaconUpgradedEventResponse> getBeaconUpgradedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BEACONUPGRADED_EVENT, transactionReceipt);
        ArrayList<BeaconUpgradedEventResponse> responses = new ArrayList<BeaconUpgradedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            BeaconUpgradedEventResponse typedResponse = new BeaconUpgradedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.beacon = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }*/

    public Flowable<BeaconUpgradedEventResponse> beaconUpgradedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, BeaconUpgradedEventResponse>() {
            @Override
            public BeaconUpgradedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(BEACONUPGRADED_EVENT, log);
                BeaconUpgradedEventResponse typedResponse = new BeaconUpgradedEventResponse();
                typedResponse.log = log;
                typedResponse.beacon = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<BeaconUpgradedEventResponse> beaconUpgradedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BEACONUPGRADED_EVENT));
        return beaconUpgradedEventFlowable(filter);
    }

    /*public static List<DAuthAccountInitializedEventResponse> getDAuthAccountInitializedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DAUTHACCOUNTINITIALIZED_EVENT, transactionReceipt);
        ArrayList<DAuthAccountInitializedEventResponse> responses = new ArrayList<DAuthAccountInitializedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            DAuthAccountInitializedEventResponse typedResponse = new DAuthAccountInitializedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.entryPoint = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }*/

    public Flowable<DAuthAccountInitializedEventResponse> dAuthAccountInitializedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, DAuthAccountInitializedEventResponse>() {
            @Override
            public DAuthAccountInitializedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(DAUTHACCOUNTINITIALIZED_EVENT, log);
                DAuthAccountInitializedEventResponse typedResponse = new DAuthAccountInitializedEventResponse();
                typedResponse.log = log;
                typedResponse.entryPoint = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DAuthAccountInitializedEventResponse> dAuthAccountInitializedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DAUTHACCOUNTINITIALIZED_EVENT));
        return dAuthAccountInitializedEventFlowable(filter);
    }

    /*public static List<InitializedEventResponse> getInitializedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(INITIALIZED_EVENT, transactionReceipt);
        ArrayList<InitializedEventResponse> responses = new ArrayList<InitializedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            InitializedEventResponse typedResponse = new InitializedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }*/

    public Flowable<InitializedEventResponse> initializedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, InitializedEventResponse>() {
            @Override
            public InitializedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(INITIALIZED_EVENT, log);
                InitializedEventResponse typedResponse = new InitializedEventResponse();
                typedResponse.log = log;
                typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<InitializedEventResponse> initializedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INITIALIZED_EVENT));
        return initializedEventFlowable(filter);
    }

    /*public static List<UpgradedEventResponse> getUpgradedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(UPGRADED_EVENT, transactionReceipt);
        ArrayList<UpgradedEventResponse> responses = new ArrayList<UpgradedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            UpgradedEventResponse typedResponse = new UpgradedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.implementation = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }*/

    public Flowable<UpgradedEventResponse> upgradedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, UpgradedEventResponse>() {
            @Override
            public UpgradedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(UPGRADED_EVENT, log);
                UpgradedEventResponse typedResponse = new UpgradedEventResponse();
                typedResponse.log = log;
                typedResponse.implementation = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<UpgradedEventResponse> upgradedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(UPGRADED_EVENT));
        return upgradedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> addDeposit(BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADDDEPOSIT, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<String> entryPoint() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ENTRYPOINT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> execute(String dest, BigInteger value, byte[] func) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_EXECUTE, 
                Arrays.<Type>asList(new Address(dest),
                new Uint256(value),
                new DynamicBytes(func)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> executeBatch(List<String> dest, List<byte[]> func) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_EXECUTEBATCH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<Address>(
                        Address.class,
                        org.web3j.abi.Utils.typeMap(dest, Address.class)),
                new org.web3j.abi.datatypes.DynamicArray<DynamicBytes>(
                        DynamicBytes.class,
                        org.web3j.abi.Utils.typeMap(func, DynamicBytes.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getDeposit() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETDEPOSIT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> getNonce() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETNONCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> initialize(String anOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_INITIALIZE, 
                Arrays.<Type>asList(new Address(anOwner)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<byte[]> onERC1155BatchReceived(String param0, String param1, List<BigInteger> param2, List<BigInteger> param3, byte[] param4) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ONERC1155BATCHRECEIVED, 
                Arrays.<Type>asList(new Address(param0),
                new Address(param1),
                new org.web3j.abi.datatypes.DynamicArray<Uint256>(
                        Uint256.class,
                        org.web3j.abi.Utils.typeMap(param2, Uint256.class)),
                new org.web3j.abi.datatypes.DynamicArray<Uint256>(
                        Uint256.class,
                        org.web3j.abi.Utils.typeMap(param3, Uint256.class)),
                new DynamicBytes(param4)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes4>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<byte[]> onERC1155Received(String param0, String param1, BigInteger param2, BigInteger param3, byte[] param4) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ONERC1155RECEIVED, 
                Arrays.<Type>asList(new Address(param0),
                new Address(param1),
                new Uint256(param2),
                new Uint256(param3),
                new DynamicBytes(param4)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes4>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<byte[]> onERC721Received(String param0, String param1, BigInteger param2, byte[] param3) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ONERC721RECEIVED, 
                Arrays.<Type>asList(new Address(param0),
                new Address(param1),
                new Uint256(param2),
                new DynamicBytes(param3)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes4>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<byte[]> proxiableUUID() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_PROXIABLEUUID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SUPPORTSINTERFACE, 
                Arrays.<Type>asList(new Bytes4(interfaceId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> upgradeTo(String newImplementation) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_UPGRADETO, 
                Arrays.<Type>asList(new Address(newImplementation)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> upgradeToAndCall(String newImplementation, byte[] data, BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_UPGRADETOANDCALL, 
                Arrays.<Type>asList(new Address(newImplementation),
                new DynamicBytes(data)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> validateUserOp(UserOperation userOp, byte[] userOpHash, BigInteger missingAccountFunds) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_VALIDATEUSEROP, 
                Arrays.<Type>asList(userOp, 
                new Bytes32(userOpHash),
                new Uint256(missingAccountFunds)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawDepositTo(String withdrawAddress, BigInteger amount) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_WITHDRAWDEPOSITTO, 
                Arrays.<Type>asList(new Address(withdrawAddress),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static DAuthAccount load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new DAuthAccount(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static DAuthAccount load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new DAuthAccount(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static DAuthAccount load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new DAuthAccount(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static DAuthAccount load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new DAuthAccount(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<DAuthAccount> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String anEntryPoint) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(anEntryPoint)));
        return deployRemoteCall(DAuthAccount.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<DAuthAccount> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String anEntryPoint) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(anEntryPoint)));
        return deployRemoteCall(DAuthAccount.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<DAuthAccount> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String anEntryPoint) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(anEntryPoint)));
        return deployRemoteCall(DAuthAccount.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<DAuthAccount> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String anEntryPoint) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(anEntryPoint)));
        return deployRemoteCall(DAuthAccount.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static class UserOperation extends DynamicStruct {
        public String sender;

        public BigInteger nonce;

        public byte[] initCode;

        public byte[] callData;

        public BigInteger callGasLimit;

        public BigInteger verificationGasLimit;

        public BigInteger preVerificationGas;

        public BigInteger maxFeePerGas;

        public BigInteger maxPriorityFeePerGas;

        public byte[] paymasterAndData;

        public byte[] signature;

        public UserOperation(String sender, BigInteger nonce, byte[] initCode, byte[] callData, BigInteger callGasLimit, BigInteger verificationGasLimit, BigInteger preVerificationGas, BigInteger maxFeePerGas, BigInteger maxPriorityFeePerGas, byte[] paymasterAndData, byte[] signature) {
            super(new Address(sender),
                    new Uint256(nonce),
                    new DynamicBytes(initCode),
                    new DynamicBytes(callData),
                    new Uint256(callGasLimit),
                    new Uint256(verificationGasLimit),
                    new Uint256(preVerificationGas),
                    new Uint256(maxFeePerGas),
                    new Uint256(maxPriorityFeePerGas),
                    new DynamicBytes(paymasterAndData),
                    new DynamicBytes(signature));
            this.sender = sender;
            this.nonce = nonce;
            this.initCode = initCode;
            this.callData = callData;
            this.callGasLimit = callGasLimit;
            this.verificationGasLimit = verificationGasLimit;
            this.preVerificationGas = preVerificationGas;
            this.maxFeePerGas = maxFeePerGas;
            this.maxPriorityFeePerGas = maxPriorityFeePerGas;
            this.paymasterAndData = paymasterAndData;
            this.signature = signature;
        }

        public UserOperation(Address sender, Uint256 nonce, DynamicBytes initCode, DynamicBytes callData, Uint256 callGasLimit, Uint256 verificationGasLimit, Uint256 preVerificationGas, Uint256 maxFeePerGas, Uint256 maxPriorityFeePerGas, DynamicBytes paymasterAndData, DynamicBytes signature) {
            super(sender, nonce, initCode, callData, callGasLimit, verificationGasLimit, preVerificationGas, maxFeePerGas, maxPriorityFeePerGas, paymasterAndData, signature);
            this.sender = sender.getValue();
            this.nonce = nonce.getValue();
            this.initCode = initCode.getValue();
            this.callData = callData.getValue();
            this.callGasLimit = callGasLimit.getValue();
            this.verificationGasLimit = verificationGasLimit.getValue();
            this.preVerificationGas = preVerificationGas.getValue();
            this.maxFeePerGas = maxFeePerGas.getValue();
            this.maxPriorityFeePerGas = maxPriorityFeePerGas.getValue();
            this.paymasterAndData = paymasterAndData.getValue();
            this.signature = signature.getValue();
        }
    }

    public static class AdminChangedEventResponse extends BaseEventResponse {
        public String previousAdmin;

        public String newAdmin;
    }

    public static class BeaconUpgradedEventResponse extends BaseEventResponse {
        public String beacon;
    }

    public static class DAuthAccountInitializedEventResponse extends BaseEventResponse {
        public String entryPoint;

        public String owner;
    }

    public static class InitializedEventResponse extends BaseEventResponse {
        public BigInteger version;
    }

    public static class UpgradedEventResponse extends BaseEventResponse {
        public String implementation;
    }
}
