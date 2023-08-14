package com.infras.dauthsdk.api.entity

import com.infras.dauthsdk.wallet.sol.EntryPoint
import java.math.BigInteger

/**
 * Create user op and estimate gas data
 *
 * @property verificationCost 鉴权和初始化的消耗（wei）
 * @property callCost 执行[userOp]的消耗（wei）
 * @property userOp 用户操作对象
 * @constructor Create empty Create user op and estimate gas data
 */
class CreateUserOpAndEstimateGasData(
    val verificationCost: BigInteger,
    val callCost: BigInteger,
    val userOp: EntryPoint.UserOperation
) {
    override fun toString(): String {
        return "EstimateUserOpGasData(verificationCost=$verificationCost, callCost=$callCost, userOp=$userOp)"
    }
}