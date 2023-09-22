package com.infras.dauth.widget.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.infras.dauth.widget.compose.constant.DStrings

object DComingSoonLayout {

    @Composable
    fun ComingSoonLayout(text: String? = null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Transparent)
        ) {
            Text(text = text ?: DStrings.COMING_SOON, modifier = Modifier.align(Alignment.Center))
        }
    }
}