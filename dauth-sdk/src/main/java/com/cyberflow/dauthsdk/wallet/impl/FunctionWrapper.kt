package com.cyberflow.dauthsdk.wallet.impl

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type

internal class FunctionWrapper(private val function: Function) {
    private val name: String? get() = function.name
    private val inputParameters: List<Type<*>>? = function.inputParameters
    private val outputParameters: List<TypeReference<Type<*>>>? = function.outputParameters
    override fun toString(): String {
        return "Function(name=$name, inputParameters=$inputParameters, outputParameters=$outputParameters)"
    }
}