package com.infras.dauth.widget.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infras.dauth.entity.PagerEntity
import com.infras.dauth.widget.compose.constant.DColors
import kotlinx.coroutines.launch

object DViewPager {

    /**
     * 指示器和ViewPager全家桶
     * @param modifier
     * @param initialPage
     * @param style
     * @param indicatorAtTop
     * @param pagerEntities
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BundledViewPager(
        modifier: Modifier = Modifier,
        initialPage: Int = 0,
        style: Int = 0,
        indicatorAtTop: Boolean,
        pagerEntities: List<PagerEntity>,
    ) {
        Column(
            modifier = modifier
                .background(Color.White)
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            val pageCount = pagerEntities.size
            val pagerState = rememberPagerState(
                initialPage = initialPage,
                pageCount = { pageCount }
            )

            val createIndicator: @Composable () -> Unit = {
                when (style) {
                    0 -> ViewPagerIndicators(pagerState = pagerState, pagerEntities = pagerEntities)
                    1 -> ViewPagerIndicatorsWithUnderLine(
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .height(IntrinsicSize.Min)
                            .align(Alignment.CenterHorizontally),
                        pagerState = pagerState,
                        pagerEntities = pagerEntities
                    )

                    2 -> ViewPagerIndicatorsWithUnderLine(
                        Modifier
                            .width(IntrinsicSize.Min)
                            .height(IntrinsicSize.Min)
                            .align(Alignment.Start),
                        pagerState = pagerState,
                        pagerEntities = pagerEntities
                    )

                    else -> throw IllegalStateException()
                }
            }

            if (indicatorAtTop) {
                createIndicator.invoke()
                ViewPagerContent(pagerState = pagerState, pagerEntities = pagerEntities)
            } else {
                ViewPagerContent(pagerState = pagerState, pagerEntities = pagerEntities)
                createIndicator.invoke()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ColumnScope.ViewPagerContent(
        pagerState: PagerState,
        pagerEntities: List<PagerEntity>
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)
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
                .height(35.dp)
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
                    .weight(1F, true)) {
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
    fun ColumnScope.ViewPagerIndicatorsWithUnderLine(
        modifier: Modifier,
        pagerState: PagerState,
        pagerEntities: List<PagerEntity>
    ) {
        Row(
            modifier = modifier
        ) {
            val pagerCount = pagerEntities.size
            repeat(pagerCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.Black else Color.Black
                val underLineColor =
                    if (pagerState.currentPage == iteration) DColors.GRAY else Color.Transparent
                val rememberScope = rememberCoroutineScope()
                Box(modifier = Modifier
                    .padding(start = 0.dp, end = 0.dp)
                    .width(IntrinsicSize.Min)
                    .fillMaxHeight()
                    .clickable {
                        rememberScope.launch {
                            pagerState.animateScrollToPage(
                                iteration
                            )
                        }
                    }) {
                    Text(
                        text = pagerEntities.get(index = iteration).pagerTitle,
                        color = color,
                        minLines = 1,
                        maxLines = 1,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
                            .align(Alignment.Center)
                            .width(IntrinsicSize.Max)
                    )

                    Box(
                        modifier = Modifier
                            .background(underLineColor)
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}