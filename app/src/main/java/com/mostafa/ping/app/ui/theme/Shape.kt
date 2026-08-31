package com.mostafa.ping.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object PingRadius {
    val Micro = 4.dp
    val Standard = 6.dp
    val Comfortable = 8.dp
    val Card = 12.dp
    val Container = 16.dp
}

object PingShapes {
    val Button = RoundedCornerShape(PingRadius.Standard)
    val Input = RoundedCornerShape(PingRadius.Standard)
    val Compact = RoundedCornerShape(PingRadius.Comfortable)
    val Card = RoundedCornerShape(PingRadius.Card)
    val Container = RoundedCornerShape(PingRadius.Container)
    val Pill = CircleShape
}

object PingSpacing {
    val Xxs = 8.dp
    val Xs = 10.dp
    val Sm = 12.dp
    val Md = 16.dp
    val Lg = 24.dp
    val Xl = 32.dp
    val Xxl = 40.dp
    val Section = 56.dp
    val Hero = 80.dp
}
