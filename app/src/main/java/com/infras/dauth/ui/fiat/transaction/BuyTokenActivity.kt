package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.entity.BuyTokenPageInputEntity
import com.infras.dauth.entity.BuyWithPageInputEntity
import com.infras.dauth.ext.launch
import com.infras.dauth.ui.fiat.transaction.viewmodel.BuyTokenViewModel
import com.infras.dauth.util.ConvertUtil
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.compose.constant.DColors
import com.infras.dauth.widget.compose.titleWith1Icon
import com.infras.dauthsdk.wallet.ext.getParcelableExtraCompat
import java.math.BigInteger

class BuyTokenActivity : BaseActivity() {

    companion object {
        private const val EXTRA_INPUT = "EXTRA_INPUT"
        fun launch(
            context: Context,
            input: BuyTokenPageInputEntity
        ) {
            context.launch(BuyTokenActivity::class.java) {
                it.putExtra(EXTRA_INPUT, input)
            }
        }
    }

    private val viewModel: BuyTokenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.attachInput(intent.getParcelableExtraCompat(EXTRA_INPUT)!!)
        setContent {
            BuyTokenScreenWithViewModel()
        }
    }

    @Composable
    private fun BuyTokenScreenWithViewModel() {
        val inputAmount by remember {
            viewModel.amount
        }
        val estimatedPrice by remember {
            viewModel.estimatedPrice
        }

        val fiat = viewModel.getCurrentFiatInfo()
        BuyTokenScreen(
            selectPayMethod = { amount ->
                val fiat = viewModel.getCurrentFiatInfo()
                if (amount >= BigInteger("10")) {
                    BuyWithActivity.launch(
                        this, BuyWithPageInputEntity(
                            crypto_info = viewModel.input.crypto_info,
                            fiat_info = fiat,
                            buyAmount = amount.toString()
                        )
                    )
                } else {
                    val toast = "Enter a minimum of 10 ${fiat.fiatCode}(${fiat.fiatSymbol})"
                    ToastUtil.show(this, toast)
                }
            },
            onClickOrders = {
                OrdersActivity.launch(this@BuyTokenActivity)
            },
            onAmountChanged = { amount ->
                viewModel.updateAmount(amount)
            },
            crypto = viewModel.input.crypto_info.cryptoCode.orEmpty(),
            fiat = "${fiat.fiatCode}",
            priceQuote = estimatedPrice,
            amount = inputAmount
        )
    }

    @Preview
    @Composable
    private fun BuyTokenScreen(
        selectPayMethod: (BigInteger) -> Unit = {},
        onClickOrders: () -> Unit = {},
        onAmountChanged: (String) -> Unit = {},
        crypto: String = "USDT",
        fiat: String = "CNY",
        priceQuote: String = "Enter a minimum of 10 $crypto",
        amount: String = "1234",
    ) {
        Scaffold(
            topBar = {
                /*TopAppBar(title = {Text("TopAppBar")})*/
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    val amountWithSeparator = ConvertUtil.addCommasToNumber(amount)

                    titleWith1Icon(
                        title = "Buy $crypto",
                        onClickBack = { finish() },
                        onClickRightIcon = { onClickOrders.invoke() },
                    )

                    Text(
                        text = "↑↓\n$fiat",
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 90.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "$amountWithSeparator $crypto",
                        color = Color.Black,
                        maxLines = 1,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 135.dp)
                    )

                    Text(
                        text = priceQuote,
                        color = Color(0xff494949),
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 190.dp)
                    )

                    Text(
                        text = "Select Payment Method",
                        color = Color.Black,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 360.dp)
                            .background(color = DColors.GRAY, shape = RoundedCornerShape(10.dp))
                            .clickable { selectPayMethod.invoke(BigInteger(amount)) }
                            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
                    )

                    CalculatorLayout(
                        onItemClick = { str ->
                            val newAmount = when (str) {
                                "⬅" -> {
                                    if (amount.length == 1) {
                                        "0"
                                    } else {
                                        amount.substring(0, amount.length - 1)
                                    }
                                }

                                in listOf(
                                    "1",
                                    "2",
                                    "3",
                                    "4",
                                    "5",
                                    "6",
                                    "7",
                                    "8",
                                    "9",
                                    "0",
                                ) -> {
                                    if (amount == "0") {
                                        str
                                    } else {
                                        amount.plus(str)
                                    }
                                }

                                else -> amount
                            }
                            onAmountChanged.invoke(newAmount)
                        }
                    )
                }
            }
        )
    }

    @Preview
    @Composable
    fun BoxScope.CalculatorLayout(
        onItemClick: (String) -> Unit = {}
    ) {
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            val stringList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "⬅")
            for (i in 0 until 4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 1.dp)
                ) {
                    for (j in 0 until 3) {
                        val str = stringList[i * 3 + j]
                        Text(
                            text = str,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                                .background(Color(0xFFCDCDCD))
                                .clickable { onItemClick.invoke(str) }
                                .layout { measurable, constraints ->
                                    val placeable =
                                        measurable.measure(
                                            constraints.copy(minWidth = 0, minHeight = 0)
                                        )
                                    layout(constraints.maxWidth, constraints.maxHeight) {
                                        val x = (constraints.maxWidth - placeable.width) / 2
                                        val y = (constraints.maxHeight - placeable.height) / 2
                                        placeable.placeRelative(x, y)
                                    }
                                },
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}