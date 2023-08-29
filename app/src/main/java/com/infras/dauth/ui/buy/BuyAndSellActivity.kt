package com.infras.dauth.ui.buy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.InvalidationStrategy
import coil.compose.rememberAsyncImagePainter
import com.infras.dauth.R
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.entity.PagerEntity
import com.infras.dauth.entity.TagsEntity
import com.infras.dauth.entity.TokenInfo
import com.infras.dauth.entity.TokenInfoOfTag
import com.infras.dauth.ui.main.MainActivity
import com.infras.dauth.util.ToastUtil
import com.infras.dauth.widget.compose.DComingSoonLayout
import com.infras.dauth.widget.compose.DFlowRow
import com.infras.dauth.widget.compose.DViewPager

class BuyAndSellActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, BuyAndSellActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainLayout(onClickCurrencyToggle = { ToastUtil.show(this, "coming soon") })
        }
    }

    @Preview
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun TokenListView(
        modifier: Modifier = Modifier,
        tokens: List<TokenInfo> = BuyAndSellActivityMockData.tokenInfoList.first().tokenInfoList
    ) {
        LazyColumn(
            modifier = modifier
        ) {
            itemsIndexed(tokens) { index, t ->
                ListItem(text = {
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(37.dp)
                    ) {
                        val (ivAvatar, tvName, tvIssuer, tvPrice, tvChangeRange, vSpace) = createRefs()

                        val placeHolder = ColorPainter(Color.Gray)
                        val painter: Painter = rememberAsyncImagePainter(
                            model = t.avatarUrl,
                            placeholder = placeHolder,
                            fallback = placeHolder,
                            error = placeHolder,
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .constrainAs(ivAvatar) {
                                    start.linkTo(parent.start)
                                }
                                .width(37.dp)
                                .height(37.dp)
                                .clip(CircleShape)
                        )
                        Text(text = t.name,
                            color = Color.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.constrainAs(tvName) {
                                start.linkTo(ivAvatar.end, 10.dp)
                                top.linkTo(parent.top)
                            }
                        )
                        Text(text = t.issuer,
                            color = Color.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.constrainAs(tvIssuer) {
                                start.linkTo(ivAvatar.end, 10.dp)
                                bottom.linkTo(parent.bottom)
                            }
                        )
                        val color = if (t.changeRange.startsWith("-")) Color.Red else Color.Gray
                        Text(text = t.changeRange,
                            color = color,
                            fontSize = 11.sp,
                            modifier = Modifier.constrainAs(tvChangeRange) {
                                end.linkTo(parent.end, 15.dp)
                                bottom.linkTo(parent.bottom)
                            }
                        )
                        Text(text = t.price,
                            color = Color.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.constrainAs(tvPrice) {
                                end.linkTo(parent.end, 15.dp)
                                top.linkTo(parent.top)
                            }
                        )
                        Box(modifier = Modifier
                            .constrainAs(vSpace) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(ivAvatar.end, 10.dp)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }
                            .background(Color(0xffdcdcdc))
                            .height(0.2.dp))
                    }
                })
            }
        }
    }

    @Preview
    @Composable
    private fun BuyTab(tokens: List<TokenInfoOfTag> = BuyAndSellActivityMockData.tokenInfoList) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val (tvTokens, flTags, rvTokens) = createRefs()
            Text(text = "Tokens",
                modifier = Modifier.constrainAs(tvTokens) {
                    start.linkTo(parent.start, 20.dp)
                    top.linkTo(parent.top, 20.dp)
                }
            )
            var tagIndex by remember {
                mutableStateOf(0)
            }
            val entities = tokens.mapIndexed { index, tokenInfoOfTag ->
                TagsEntity(
                    title = tokenInfoOfTag.tag.title,
                    selected = tagIndex == index,
                    onClick = {
                        tagIndex = index
                        tokenInfoOfTag.tag.onClick.invoke()
                    }
                )
            }
            DFlowRow.DFlowRow(
                modifier = Modifier
                    .constrainAs(flTags) {
                        start.linkTo(parent.start, 20.dp)
                        top.linkTo(tvTokens.bottom, 20.dp)
                    }
                    .padding(8.dp),

                entities = entities,
            )

            val tokenListOfCurrentTag = tokens[tagIndex].tokenInfoList
            TokenListView(modifier = Modifier
                .constrainAs(rvTokens) {
                    top.linkTo(flTags.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .fillMaxWidth(),
                tokens = tokenListOfCurrentTag)
        }
    }

    @Composable
    private fun SellTab() {
        DComingSoonLayout.ComingSoonLayout()
    }

    @Preview
    @Composable
    private fun MainLayout(
        pagerEntities: List<PagerEntity> = listOf(
            PagerEntity("Buy") { BuyTab() },
            PagerEntity("Sale") { SellTab() },
        ),
        onClickCurrencyToggle: () -> Unit = {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            DViewPager.BundledViewPager(
                indicatorAtTop = true,
                style = 1,
                pagerEntities = pagerEntities
            )

            Text(
                text = "CNY ▼",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { onClickCurrencyToggle.invoke() }
                    .padding(start = 15.dp, end = 15.dp, top = 8.dp, bottom = 8.dp)
            )

            Image(
                painterResource(R.drawable.svg_ic_back_arrow),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp)
                    .clickable { finish() }
                    .padding(top = 8.dp, bottom = 8.dp, start = 10.dp, end = 10.dp)
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
            )
        }
    }
}