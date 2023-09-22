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
import com.infras.dauth.app.BaseViewModel
import com.infras.dauth.entity.BuyTokenPageEntity
import com.infras.dauth.entity.BuyTokenPageInputEntity
import com.infras.dauth.ext.launch
import com.infras.dauth.ui.fiat.transaction.viewmodel.BuyTokenViewModel
import com.infras.dauth.util.ConvertUtil
import com.infras.dauth.util.LogUtil
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
        viewModel.selectPayMethodEvent.observe(this) {
            BuyWithActivity.launch(this, it)
        }
        setContent {
            BuyTokenScreenWithViewModel()
        }
    }

    override fun getDefaultViewModel(): BaseViewModel? {
        return viewModel
    }

    @Composable
    private fun BuyTokenScreenWithViewModel() {
        val pageData by remember {
            viewModel.pageData
        }

        BuyTokenScreen(
            selectPayMethod = { count ->
                viewModel.selectPayMethod(count)
            },
            onClickOrders = {
                OrdersActivity.launch(this@BuyTokenActivity)
            },
            onSwitchMethod = {
                viewModel.switchMethod()
            },
            onInputValueChanged = { inputValue ->
                viewModel.updateInputValue(inputValue)
            },
            pageData = pageData
        )
    }

    @Preview
    @Composable
    private fun BuyTokenScreen(
        selectPayMethod: (BigInteger) -> Unit = {},
        onClickOrders: () -> Unit = {},
        onInputValueChanged: (String) -> Unit = {},
        onSwitchMethod: () -> Unit = {},
        pageData: BuyTokenPageEntity = BuyTokenPageEntity(
            estimatedPrice = "Enter a minimum of 10 USDT",
            inputValue = "1234",
            isAmountMode = false,
            cryptoCode = "USDT",
            fiatCode = "CNY",
        )
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
                    val isAmountMode = pageData.isAmountMode
                    val inputValue = pageData.inputValue
                    val priceQuote = pageData.estimatedPrice
                    val fiatCode = pageData.fiatCode
                    val cryptoCode = pageData.cryptoCode
                    LogUtil.d(logTag, "inputValue=$inputValue")
                    val amountWithSeparator = ConvertUtil.addCommasToNumber(inputValue)

                    titleWith1Icon(
                        title = "Buy $cryptoCode",
                        onClickBack = { finish() },
                        onClickRightIcon = { onClickOrders.invoke() },
                    )

                    val dstUnit = if (!isAmountMode) fiatCode else cryptoCode
                    Text(
                        text = "↑↓\n$dstUnit",
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 90.dp)
                            .clickable { onSwitchMethod.invoke() },
                        textAlign = TextAlign.Center
                    )

                    val inputValueUnit = if (!isAmountMode) cryptoCode else fiatCode
                    Text(
                        text = "$amountWithSeparator $inputValueUnit",
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
                            .clickable { selectPayMethod.invoke(BigInteger(inputValue)) }
                            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
                    )

                    CalculatorLayout(
                        onItemClick = { str ->
                            val newAmount = when (str) {
                                "⬅" -> {
                                    if (inputValue.length == 1) {
                                        "0"
                                    } else {
                                        inputValue.substring(0, inputValue.length - 1)
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
                                    if (inputValue == "0") {
                                        str
                                    } else {
                                        inputValue.plus(str)
                                    }
                                }

                                else -> inputValue
                            }
                            onInputValueChanged.invoke(newAmount)
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