package com.infras.dauth.entity

sealed class FiatOrderDetailItemEntity {

    open fun getKey(): String = javaClass.simpleName

    data class Text(
        val title: String,
        val content: String,
        val displayContent: String = content,
        val boldContent: Boolean = false,
        val canCopy: Boolean = false,
    ) : FiatOrderDetailItemEntity() {
        override fun getKey(): String {
            return title
        }
    }

    data class Group(val name: String) : FiatOrderDetailItemEntity() {
        override fun getKey(): String {
            return name
        }
    }

    object Split : FiatOrderDetailItemEntity()

    data class Tips(
        val cost: String,
        val accountName: String,
        val accountNumber: String,
        var imagePath: String,
    ) : FiatOrderDetailItemEntity() {
        override fun getKey(): String {
            return accountName
        }
    }

    data class Title(
        val icon: Int,
        val title: String,
        val desc: CharSequence,
    ) : FiatOrderDetailItemEntity() {
        override fun getKey(): String {
            return title
        }
    }
}