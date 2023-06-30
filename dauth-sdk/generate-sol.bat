@echo off
start /B web3j generate truffle -t ..\..\..\CppProjects\dauthcontracts\pytest\DAuthAccountFactory.json -o .\src\main\java\ -p com.cyberflow.dauthsdk.wallet.sol
start /B web3j generate truffle -t ..\..\..\CppProjects\dauthcontracts\pytest\DAuthAccount.json -o .\src\main\java\ -p com.cyberflow.dauthsdk.wallet.sol
start /B web3j generate truffle -t ..\..\..\CppProjects\dauthcontracts\pytest\Dispatcher.json -o .\src\main\java\ -p com.cyberflow.dauthsdk.wallet.sol
start /B web3j generate truffle -t ..\..\..\CppProjects\dauthcontracts\pytest\EntryPoint.json -o .\src\main\java\ -p com.cyberflow.dauthsdk.wallet.sol