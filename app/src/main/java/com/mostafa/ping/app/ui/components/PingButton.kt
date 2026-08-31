package com.mostafa.ping.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mostafa.ping.app.ui.theme.PingColors
import com.mostafa.ping.app.ui.theme.PingShapes
import com.mostafa.ping.app.ui.theme.PingSpacing
import com.mostafa.ping.app.ui.theme.PingTypography
import com.mostafa.ping.app.ui.theme.pingFocusGlow
import com.mostafa.ping.app.ui.theme.pingInsetShadow

enum class PingButtonStyle { Primary, Ghost, Cream, Pill }

@Composable
fun PingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PingButtonStyle = PingButtonStyle.Primary,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val active = pressed && enabled
    val shape = if (style == PingButtonStyle.Pill) PingShapes.Pill else PingShapes.Button
    val background = when (style) {
        PingButtonStyle.Primary -> PingColors.Charcoal
        PingButtonStyle.Ghost -> Color.Transparent
        PingButtonStyle.Cream, PingButtonStyle.Pill -> PingColors.Cream
    }
    val content = when (style) {
        PingButtonStyle.Primary -> PingColors.OffWhite
        else -> PingColors.Charcoal
    }
    val textStyle: TextStyle = PingTypography.labelLarge.copy(color = content)
    val alpha = when {
        !enabled -> 0.4f
        active -> 0.8f
        style == PingButtonStyle.Pill && !pressed -> 0.5f
        else -> 1f
    }

    Box(
        modifier = modifier
            .then(
                if (style == PingButtonStyle.Primary || style == PingButtonStyle.Pill) {
                    Modifier.pingInsetShadow(shape)
                } else {
                    Modifier
                }
            )
            .pingFocusGlow(active, shape)
            .clip(shape)
            .background(background)
            .then(
                if (style == PingButtonStyle.Ghost) {
                    Modifier.border(1.dp, PingColors.Charcoal40, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = PingSpacing.Md, vertical = PingSpacing.Xxs),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = textStyle, color = content.copy(alpha = alpha))
    }
}

@Composable
fun PingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val alpha = when {
        !enabled -> 0.4f
        pressed -> 0.8f
        else -> 0.5f
    }
    Box(
        modifier = modifier
            .size(size)
            .pingInsetShadow(shape)
            .clip(shape)
            .background(PingColors.Cream.copy(alpha = alpha))
            .border(1.dp, PingColors.Border, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun PingPrimaryCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 176.dp,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val alpha = when {
        !enabled -> 0.4f
        pressed -> 0.8f
        else -> 1f
    }
    Box(
        modifier = modifier
            .size(size)
            .pingInsetShadow(CircleShape)
            .pingFocusGlow(pressed && enabled, CircleShape)
            .clip(CircleShape)
            .background(PingColors.Charcoal.copy(alpha = alpha))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
