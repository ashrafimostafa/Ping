package com.mostafa.ping.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Lovable-inspired palette. Grays are charcoal at opacity, never arbitrary hex.
 */
object PingColors {
    val Cream = Color(0xFFF7F4ED)
    val Charcoal = Color(0xFF1C1C1C)
    val OffWhite = Color(0xFFFCFBF8)
    val Muted = Color(0xFF5F5F5D)
    val Border = Color(0xFFECEAE4)

    val Charcoal83 = Charcoal.copy(alpha = 0.83f)
    val Charcoal82 = Charcoal.copy(alpha = 0.82f)
    val Charcoal40 = Charcoal.copy(alpha = 0.40f)
    val Charcoal04 = Charcoal.copy(alpha = 0.04f)
    val Charcoal03 = Charcoal.copy(alpha = 0.03f)

    val RingBlue = Color(0xFF3B82F6).copy(alpha = 0.50f)
    val FocusShadow = Color.Black.copy(alpha = 0.10f)
    val InsetHighlight = Color.White.copy(alpha = 0.20f)
    val InsetRing = Color.Black.copy(alpha = 0.20f)
    val SoftDrop = Color.Black.copy(alpha = 0.05f)

    val WashPink = Color(0xFFF5C6C0)
    val WashOrange = Color(0xFFF3D5B5)
    val WashBlue = Color(0xFFC9D7F5)
}
