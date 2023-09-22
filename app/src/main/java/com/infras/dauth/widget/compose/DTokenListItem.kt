package com.infras.dauth.widget.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import coil.compose.rememberAsyncImagePainter
import com.infras.dauth.entity.BuyAndSellPageEntity
import com.infras.dauth.widget.compose.constant.DColors
import com.infras.dauth.widget.compose.constant.DStrings

@Composable
fun TokenListItem(
    t: BuyAndSellPageEntity.TokenInfo,
    onClickItem: (BuyAndSellPageEntity.TokenInfo) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(37.dp)
            .clickable { onClickItem.invoke(t) }
    ) {
        val (ivAvatar, tvName, tvIssuer, tvPrice, tvChangeRange, vSpace) = createRefs()

        val placeHolder = ColorPainter(DColors.GRAY)
        val painter: Painter = rememberAsyncImagePainter(
            model = t.avatarUrl,
            placeholder = placeHolder,
            fallback = placeHolder,
            error = placeHolder,
        )
        Image(
            painter = painter,
            contentDescription = DStrings.IMAGE_DEFAULT_CONTENT_DESCRIPTION,
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
        val color = if (t.changeRange.startsWith("-")) Color.Red else DColors.GRAY
        Text(text = t.changeRange,
            color = color,
            fontSize = 11.sp,
            modifier = Modifier.constrainAs(tvChangeRange) {
                end.linkTo(parent.end, 15.dp)
                bottom.linkTo(parent.bottom)
                visibility = Visibility.Invisible
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
            .background(Color.Transparent)
            .height(0.2.dp))
    }
}