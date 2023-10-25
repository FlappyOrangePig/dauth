package com.infras.dauth.entity

import androidx.compose.runtime.Composable

data class PagerEntity(
    val pagerTitle: String,
    val createPager: @Composable () -> Unit,
)