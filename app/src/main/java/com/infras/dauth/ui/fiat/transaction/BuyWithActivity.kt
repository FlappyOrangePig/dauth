package com.infras.dauth.ui.fiat.transaction

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.entity.PayMethodEntity
import com.infras.dauth.ext.launch
import com.infras.dauth.widget.compose.titleWith1Icon

class BuyWithActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            context.launch(BuyWithActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuyWithScreen()
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Preview
    @Composable
    private fun BuyWithScreen(
        payMethods: List<PayMethodEntity> = mutableListOf(
            PayMethodEntity(
                "Wechat Pay",
                "Unit Price： 7.29 CNY",
                true
            ),
            PayMethodEntity(
                "Bank transfer",
                "Unit Price： 7.29 CNY",
                false
            ),
            PayMethodEntity(
                "AliPay",
                "Unit Price： 7.31 CNY",
                false
            ),
        ).let { it.plus(it) }
    ) {
        Scaffold(
            content = { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    titleWith1Icon(
                        title = "Buy with",
                        onClickBack = { finish() },
                        id = null,
                    )

                    Text(
                        text = "1,888 USDT",
                        color = Color.Black,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 70.dp)
                    )

                    Text(
                        text = "You'll pay 13,763.52 CNY",
                        color = Color.Black,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 15.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth()
                            .padding(top = 50.dp, bottom = 50.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(start = 15.dp, end = 15.dp)
                                .drawWithContent {
                                    drawRect(
                                        color = Color.Black,
                                        style = Stroke(width = 2.dp.toPx()),
                                    )
                                    drawContent()
                                }
                        ) {
                            itemsIndexed(payMethods) { index, t ->
                                ListItem(text = {
                                    Column() {
                                        Text(text = t.name)
                                        Text(text = t.price)
                                    }
                                })
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 30.dp)
                            .clickable { }
                            .background(Color.Black, shape = RoundedCornerShape(10.dp))
                            .padding(start = 50.dp, end = 50.dp, top = 10.dp, bottom = 10.dp)) {
                        Text(
                            text = "Buy USDT (29s)",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        )
    }
}