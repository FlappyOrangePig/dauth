package com.infras.dauth.entity

sealed class FiatOrderState {

    companion object {
        const val CREATE_FAIL = "CREATE_FAIL"
        const val UNPAID = "UNPAID"
        const val PAID = "PAID"
        const val CANCEL = "CANCEL"
        const val COMPLETED = "COMPLETED" // 指下单支付后商家已放币
        const val APPEAL = "APPEAL" // 申诉中
        const val WITHDRAW_FAIL = "WITHDRAW_FAIL"
        const val WITHDRAW_SUCCESS = "WITHDRAW_SUCCESS" // 指商家放币以后，SkyPay会对交易所发起提现操作，发起成功，就是提现成功
    }

    abstract val displayText: String
    abstract val wireState: Array<String>

    sealed class Pending : FiatOrderState() {
        object All : Pending() {
            override val displayText: String
                get() = "All"
            override val wireState: Array<String>
                get() = arrayOf(UNPAID, PAID, COMPLETED, APPEAL)
        }

        object InProgress : Pending() {
            override val displayText: String
                get() = "In progress"
            override val wireState: Array<String>
                get() = arrayOf(UNPAID, PAID, COMPLETED)
        }

        object InDispute : Pending() {
            override val displayText: String
                get() = "In dispute"
            override val wireState: Array<String>
                get() = arrayOf(APPEAL)
        }
    }

    sealed class Completed : FiatOrderState() {
        object All : Pending() {
            override val displayText: String
                get() = "All"
            override val wireState: Array<String>
                get() = arrayOf(CREATE_FAIL, CANCEL, WITHDRAW_FAIL, WITHDRAW_SUCCESS)
        }

        object Fulfilled : Pending() {
            override val displayText: String
                get() = "Fulfilled"
            override val wireState: Array<String>
                get() = arrayOf(WITHDRAW_SUCCESS)
        }

        object Canceled : Pending() {
            override val displayText: String
                get() = "Canceled"
            override val wireState: Array<String>
                get() = arrayOf(CANCEL)
        }
    }
}