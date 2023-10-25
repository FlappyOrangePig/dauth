package com.infras.dauth.widget.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infras.dauth.entity.PagerEntity
import com.infras.dauth.widget.compose.constant.DColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.ViewPagerContent(
    modifier: Modifier,
    pagerState: PagerState,
    pagerEntities: List<PagerEntity>,
    userScrollEnabled: Boolean = true,
) {
    HorizontalPager(
        userScrollEnabled = userScrollEnabled,
        state = pagerState,
        modifier = modifier
    ) { page ->
        // Our page content
        pagerEntities[page].createPager.invoke()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.ViewPagerIndicators(
    pagerState: PagerState,
    pagerEntities: List<PagerEntity>
) {
    Row(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
    ) {
        val pagerCount = pagerEntities.size
        repeat(pagerCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) Color.Red else Color.Black
            val rememberScope = rememberCoroutineScope()
            Box(modifier = Modifier
                .fillMaxHeight()
                .clickable {
                    rememberScope.launch {
                        pagerState.animateScrollToPage(
                            iteration
                        )
                    }
                }
                .weight(1F, true)
                .background(Color(0xfff1eded))
            ) {
                Text(
                    text = pagerEntities.get(index = iteration).pagerTitle,
                    color = color,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewPagerIndicatorsPacked(
    modifier: Modifier,
    pagerState: PagerState,
    pagerEntities: List<PagerEntity>
) {
    Row(
        modifier = modifier
    ) {
        val pagerCount = pagerEntities.size
        repeat(pagerCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) Color.Red else Color.Black
            val rememberScope = rememberCoroutineScope()
            Box(modifier = Modifier
                .fillMaxHeight()
                .clickable {
                    rememberScope.launch {
                        pagerState.animateScrollToPage(
                            iteration
                        )
                    }
                }
                .width(IntrinsicSize.Min)
                .background(Color.Transparent)
            ) {
                Text(
                    text = pagerEntities.get(index = iteration).pagerTitle,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewPagerIndicatorsWithUnderLine(
    modifier: Modifier = Modifier,
    pagerEntities: List<PagerEntity> = listOf(PagerEntity("") { Text("哈哈") }),
    pagerState: PagerState = rememberPagerState(initialPage = 0,
        pageCount = { pagerEntities.size }
    ),
) {
    Row(
        modifier = modifier
    ) {
        val pagerCount = pagerEntities.size
        repeat(pagerCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) Color.Black else Color.Black
            val underLineColor =
                if (pagerState.currentPage == iteration) DColors.GRAY else Color.Transparent
            val text = pagerEntities.get(index = iteration).pagerTitle
            Indicator(
                color = color,
                underLineColor = underLineColor,
                pagerState = pagerState,
                iteration = iteration,
                text = text,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
private fun Indicator(
    color: Color = Color.Black,
    underLineColor: Color = Color.Gray,
    pagerState: PagerState = rememberPagerState(initialPage = 1,
        pageCount = { 2 }
    ),
    iteration: Int = 0,
    text: String = "text",
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    val rememberScope = rememberCoroutineScope()
    Box(modifier = Modifier
        .padding(start = 0.dp, end = 0.dp)
        .width(IntrinsicSize.Min)
        .height(66.dp)
        .clickable {
            rememberScope.launch {
                pagerState.animateScrollToPage(
                    iteration
                )
            }
        }) {
        Text(
            text = text,
            color = color,
            minLines = 1,
            maxLines = 1,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 0.dp)
                .align(Alignment.Center)
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
        )

        Box(
            modifier = Modifier
                .background(underLineColor)
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp)
                .width(IntrinsicSize.Max)
                .height(3.dp)
        )
    }
}