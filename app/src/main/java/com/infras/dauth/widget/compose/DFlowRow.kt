package com.infras.dauth.widget.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.infras.dauth.entity.TagsEntity

object DFlowRow {

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun DFlowRow(
        modifier: Modifier = Modifier,
        entities: List<TagsEntity> = listOf(),
    ) {
        FlowRow(modifier = modifier) {
            repeat(entities.size) { index ->
                val e = entities[index]
                val textColor = if (e.selected) Color.White else Color.Black
                val backgroundColor = if (e.selected) Color.Black else Color.Gray

                Text(text = e.title,
                    color = textColor,
                    modifier = Modifier
                        .padding(horizontal = 5.dp, vertical = 0.dp)
                        .background(
                            backgroundColor, RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp)
                        )
                        .clickable { e.onClick.invoke() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}