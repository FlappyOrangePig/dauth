package com.infras.dauth.widget.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infras.dauth.entity.PagerEntity
import kotlinx.coroutines.launch

object DViewPager {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BundledViewPager(
        initialPage: Int = 0,
        style: Int = 0,
        indicatorAtTop: Boolean,
        pagerEntities: List<PagerEntity>,
    ) {
        Column(modifier = Modifier.background(Color.White).fillMaxHeight().fillMaxWidth()) {
            val pageCount = pagerEntities.size
            val pagerState = rememberPagerState(
                initialPage = initialPage,
                pageCount = { pageCount }
            )

            val createIndicator: @Composable () -> Unit = {
                if (style == 0) {
                    ViewPagerIndicators(pagerState = pagerState, pagerEntities = pagerEntities)
                } else {
                    ViewPagerIndicatorsWithUnderLine(
                        pagerState = pagerState,
                        pagerEntities = pagerEntities
                    )
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
    private fun ColumnScope.ViewPagerContent(
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
    private fun ColumnScope.ViewPagerIndicators(
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
    private fun ColumnScope.ViewPagerIndicatorsWithUnderLine(
        pagerState: PagerState,
        pagerEntities: List<PagerEntity>
    ) {
        Row(
            Modifier
                .height(35.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val pagerCount = pagerEntities.size
            repeat(pagerCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.Black else Color.Black
                val underLineColor = if (pagerState.currentPage == iteration) Color.Gray else Color.Transparent
                val rememberScope = rememberCoroutineScope()
                Box(modifier = Modifier
                    .width(80.dp)
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
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Box(modifier = Modifier.background(underLineColor)
                        .width(60.dp)
                        .height(2.dp)
                        .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}