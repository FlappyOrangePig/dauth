package com.infras.dauth.ui.fiat.transaction.util

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.infras.dauth.entity.FiatOrderDetailItemEntity
import com.infras.dauth.entity.FiatOrderState
import com.infras.dauth.ui.fiat.transaction.util.CurrencyCalcUtil.scale
import com.infras.dauthsdk.login.model.OrderDetailRes

object OrderDetailListComposeUtil {

    private const val RELEASED_TXS = "Released Txs"
    private const val STATUS = "Status"
    private const val TXS_INFO = "Txs info"
    private const val UNIT_PRICE = "Unit Price"
    private const val QUANTITY = "Quantity"
    private const val ORDER_AMOUNT = "Order amount"
    private const val PAYMENT_METHOD = "Payment method"
    private const val ORDER_INFORMATION = "Order information"
    private const val ORDER_ID = "Order ID"
    private const val ORDER_TIME = "Order time"

    fun all(title: FiatOrderDetailItemEntity.Title, data: OrderDetailRes.Data) =
        listOf(title) + publicInfo(data) + txInfo(data)

    private fun publicInfo(data: OrderDetailRes.Data): MutableList<FiatOrderDetailItemEntity> {
        val payMethodInfo = data.payMethodInfo?.payMethodValueInfo.orEmpty()
        val mapped = getMappedPayMethodInfo(payMethodInfo)

        val list = mutableListOf<FiatOrderDetailItemEntity>().apply {
            add(FiatOrderDetailItemEntity.Group("Buy ${data.cryptoCode}"))
            addAll(priceInfo(data))
            add(FiatOrderDetailItemEntity.Split)

            add(FiatOrderDetailItemEntity.Group(ORDER_INFORMATION))
            add(FiatOrderDetailItemEntity.Text(ORDER_ID, data.orderId.orEmpty(), canCopy = true))
            add(
                FiatOrderDetailItemEntity.Text(
                    ORDER_TIME,
                    TimeUtil.getOrderTime(data.createTime),
                    canCopy = false
                )
            )
            addAll(mapped)
            add(FiatOrderDetailItemEntity.Split)
        }
        return list
    }

    fun priceInfo(data: OrderDetailRes.Data): List<FiatOrderDetailItemEntity> {
        val fiatInfo = CurrencyCalcUtil.getFiatInfo(data.fiatCode)
        val fiatPrecision: Int? = fiatInfo?.fiatPrecision?.toInt()
        val fiatSymbol: String? = fiatInfo?.fiatSymbol ?: data.fiatCode
        val cryptoPrecision: Int? = CurrencyCalcUtil.getCryptoInfo(data.cryptoCode)?.cryptoPrecision
        return listOf(
            FiatOrderDetailItemEntity.Text(
                UNIT_PRICE,
                "$fiatSymbol ${data.price.scale(fiatPrecision)}"
            ),
            FiatOrderDetailItemEntity.Text(
                QUANTITY,
                "${data.quantity.scale(cryptoPrecision)} ${data.cryptoCode}"
            ),
            FiatOrderDetailItemEntity.Text(
                ORDER_AMOUNT,
                "$fiatSymbol ${data.amount.scale(fiatPrecision)}"
            ),
            FiatOrderDetailItemEntity.Text(
                PAYMENT_METHOD,
                data.payMethodInfo?.payMethodName.orEmpty()
            ),
        )
    }

    private fun txInfo(data: OrderDetailRes.Data): List<FiatOrderDetailItemEntity> {
        val txId = data.transactionId.orEmpty()
        if (txId.isEmpty()) {
            return listOf()
        }
        val state = data.state.orEmpty()

        val r = mutableListOf<FiatOrderDetailItemEntity>(
            FiatOrderDetailItemEntity.Group(TXS_INFO),
        )

        when (state) {
            FiatOrderState.PAID -> {
                r.add(
                    FiatOrderDetailItemEntity.Text(
                        STATUS,
                        SpannableStringBuilder("Pending for seller’s release").also {
                            it.setSpan(
                                ForegroundColorSpan(Color.parseColor("#D88100")),
                                0,
                                it.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    )
                )
            }

            FiatOrderState.COMPLETED -> {
                r.addAll(
                    listOf(
                        FiatOrderDetailItemEntity.Text(RELEASED_TXS, txId),
                        FiatOrderDetailItemEntity.Text(
                            STATUS,
                            SpannableStringBuilder("Pending").also {
                                it.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#D88100")),
                                    0,
                                    it.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            })
                    )
                )
            }

            FiatOrderState.WITHDRAW_SUCCESS -> {
                r.addAll(
                    listOf(
                        FiatOrderDetailItemEntity.Text(RELEASED_TXS, txId),
                        FiatOrderDetailItemEntity.Text(
                            STATUS,
                            SpannableStringBuilder("Success").also {
                                it.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#03A600")),
                                    0,
                                    it.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            })
                    )
                )
            }

            FiatOrderState.WITHDRAW_FAIL -> {
                r.addAll(
                    listOf(
                        FiatOrderDetailItemEntity.Text(RELEASED_TXS, txId),
                        FiatOrderDetailItemEntity.Text(
                            STATUS,
                            SpannableStringBuilder("Failure").also {
                                it.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#ff0000")),
                                    0,
                                    it.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            })
                    )
                )
            }

            else -> {}
        }

        return r
    }

    fun getMappedPayMethodInfo(payMethodInfo: List<OrderDetailRes.PayMethodValueInfo>): List<FiatOrderDetailItemEntity> {
        return payMethodInfo.mapNotNull {
            when (it.type) {
                "file" -> {
                    FiatOrderDetailItemEntity.Image(
                        it.name.orEmpty(),
                        it.value.orEmpty()
                    )
                }

                "number" -> {
                    FiatOrderDetailItemEntity.Text(
                        it.name.orEmpty(),
                        it.value.orEmpty(),
                        it.value.orEmpty()
                    )
                }

                "txt" -> {
                    FiatOrderDetailItemEntity.Text(
                        it.name.orEmpty(),
                        it.value.orEmpty(),
                        it.value.orEmpty()
                    )
                }

                else -> null
            }
        }
    }
}