package com.mostafa.ping.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mostafa.ping.app.ui.theme.PingColors
import com.mostafa.ping.app.ui.theme.PingRadius
import com.mostafa.ping.app.ui.theme.PingSpacing

@Composable
fun PingCard(
    modifier: Modifier = Modifier,
    radius: Dp = PingRadius.Card,
    contentPadding: Dp = PingSpacing.Lg,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(PingColors.Cream)
            .border(1.dp, PingColors.Border, shape)
            .padding(contentPadding),
        content = content
    )
}
