package com.mostafa.ping.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mostafa.ping.app.ui.theme.PingColors

@Composable
fun PingScaffold(
    modifier: Modifier = Modifier,
    wash: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PingColors.Cream)
    ) {
        if (wash) {
            HeroWash(Modifier.fillMaxSize())
        }
        content()
    }
}

@Composable
fun HeroWash(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PingColors.WashPink.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(w * 0.18f, h * 0.08f),
                radius = w * 0.85f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PingColors.WashOrange.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(w * 0.82f, h * 0.18f),
                radius = w * 0.7f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PingColors.WashBlue.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.92f),
                radius = w * 0.9f
            )
        )
    }
}
