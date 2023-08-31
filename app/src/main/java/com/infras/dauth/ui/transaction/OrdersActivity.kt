package com.infras.dauth.ui.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infras.dauth.app.BaseActivity
import com.infras.dauth.entity.PagerEntity
import com.infras.dauth.widget.compose.DViewPager
import com.infras.dauth.widget.compose.titleWith1Icon

class OrdersActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, OrdersActivity::class.java))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrdersScreen()
        }
    }

    @Preview
    @Composable
    private fun PendingTab() {
        DViewPager.BundledViewPager(
            indicatorAtTop = true,
            style = 2,
            pagerEntities = listOf(
                PagerEntity("All") { },
                PagerEntity("In progress") { },
                PagerEntity("In dispute") { },
            ),
        )
    }

    @Preview
    @Composable
    private fun CompletedTab() {
        DViewPager.BundledViewPager(
            indicatorAtTop = true,
            style = 2,
            pagerEntities = listOf(
                PagerEntity("All") { },
                PagerEntity("Fulfilled") { },
                PagerEntity("Canceled") { },
            ),
        )
    }

    @Preview
    @Composable
    private fun OrdersScreen() {
        Column {
            titleWith1Icon(
                title = "Orders",
                id = null
            )
            DViewPager.BundledViewPager(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                indicatorAtTop = true,
                style = 2,
                pagerEntities = listOf(
                    PagerEntity("Pending") { PendingTab() },
                    PagerEntity("Completed") { CompletedTab() },
                ),
            )
        }
    }
}