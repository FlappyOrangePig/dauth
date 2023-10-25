package com.infras.dauth.entity

import com.infras.dauthsdk.login.model.OrderDetailRes

sealed class FiatOrderDetailItemEntity {

    open fun getKey(): String = javaClass.simpleName

    data class Text(
        val title: String,
        val content: CharSequence,
        val displayContent: CharSequence = content,
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
        val list: List<OrderDetailRes.PayMethodValueInfo>,
        var imagePath: String,
        var payMethodName: String,
    ) : FiatOrderDetailItemEntity() {
        override fun getKey(): String {
            return cost
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

    data class Image(
        val title: String,
        val url: String,
    ) : FiatOrderDetailItemEntity() {
        override fun getKey(): String {
            return title
        }
    }
}