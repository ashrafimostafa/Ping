package com.mostafa.ping.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mostafa.ping.app.R

/**
 * Camera Plain Variable is the brand face in the spec; Figtree is the bundled
 * humanist stand-in (rounded terminals, variable weight, system-ui fallback).
 */
private fun figtree(weight: Int) = Font(
    resId = R.font.figtree,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight)
    )
)

val PingFontFamily = FontFamily(
    figtree(400),
    figtree(480),
    figtree(600)
)

val WeightBody = FontWeight.W400
val WeightDisplayAlt = FontWeight(480)
val WeightHeading = FontWeight.W600

val PingTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightHeading,
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.2).sp,
        color = PingColors.Charcoal
    ),
    displayMedium = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightDisplayAlt,
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp,
        color = PingColors.Charcoal
    ),
    headlineLarge = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightHeading,
        fontSize = 36.sp,
        lineHeight = (36 * 1.10f).sp,
        letterSpacing = (-0.9).sp,
        color = PingColors.Charcoal
    ),
    headlineMedium = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightHeading,
        fontSize = 36.sp,
        lineHeight = (36 * 1.10f).sp,
        letterSpacing = (-0.9).sp,
        color = PingColors.Charcoal
    ),
    titleLarge = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightBody,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        color = PingColors.Charcoal
    ),
    titleMedium = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightBody,
        fontSize = 18.sp,
        lineHeight = (18 * 1.38f).sp,
        color = PingColors.Charcoal
    ),
    bodyLarge = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightBody,
        fontSize = 18.sp,
        lineHeight = (18 * 1.38f).sp,
        color = PingColors.Muted
    ),
    bodyMedium = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightBody,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = PingColors.Charcoal82
    ),
    labelLarge = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightBody,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = PingColors.OffWhite
    ),
    labelMedium = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightBody,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = PingColors.Charcoal
    ),
    bodySmall = TextStyle(
        fontFamily = PingFontFamily,
        fontWeight = WeightBody,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = PingColors.Muted
    )
)
