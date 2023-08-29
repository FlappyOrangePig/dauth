package com.infras.dauth.widget.compose

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.infras.dauth.R
import com.infras.dauth.widget.compose.constant.DStrings

class DTitleLayout {

    @SuppressLint("Range")
    @Preview
    @Composable
    fun titleWith1Icon(
        title: String = "Buy USDT",
        onClickBack: () -> Unit = {},
        @DrawableRes id: Int? = R.drawable.svg_ic_history,
        onClickRightIcon: () -> Unit = {},
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color.Transparent)
        ) {
            val (ivBack, tvTitle, ivRightIcon) = createRefs()

            Image(
                painter = painterResource(R.drawable.svg_ic_back_arrow),
                contentDescription = DStrings.IMAGE_DEFAULT_CONTENT_DESCRIPTION,
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .constrainAs(ivBack) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, 0.dp)
                    }
                    .width(44.dp)
                    .height(44.dp)
                    .clickable { onClickBack.invoke() }
                    .padding(8.dp)
            )

            Text(
                text = title,
                fontSize = 15.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .constrainAs(tvTitle) {
                        // 0dp等价的配置参考如下链接：
                        // https://stackoverflow.com/questions/64313409/equivalent-of-constraint-layout-0dp
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                        centerTo(parent)
                    }
            )

            id?.let { idNotNull ->
                Image(
                    painter = painterResource(idNotNull),
                    contentDescription = DStrings.IMAGE_DEFAULT_CONTENT_DESCRIPTION,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .constrainAs(ivRightIcon) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, 0.dp)
                        }
                        .width(44.dp)
                        .height(44.dp)
                        .clickable { onClickRightIcon.invoke() }
                        .padding(10.5.dp)
                )
            }
        }
    }
}