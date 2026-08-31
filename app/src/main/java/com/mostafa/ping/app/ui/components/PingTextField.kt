package com.mostafa.ping.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.mostafa.ping.app.ui.theme.PingColors
import com.mostafa.ping.app.ui.theme.PingShapes
import com.mostafa.ping.app.ui.theme.PingSpacing
import com.mostafa.ping.app.ui.theme.PingTypography
import com.mostafa.ping.app.ui.theme.pingFocusGlow

@Composable
fun PingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor = if (focused) PingColors.RingBlue else PingColors.Border
    val borderWidth = if (focused) 2.dp else 1.dp

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .pingFocusGlow(focused, PingShapes.Input)
            .clip(PingShapes.Input)
            .background(PingColors.Cream)
            .border(borderWidth, borderColor, PingShapes.Input)
            .padding(horizontal = PingSpacing.Md, vertical = PingSpacing.Sm),
        enabled = enabled,
        singleLine = true,
        textStyle = PingTypography.bodyMedium.copy(color = PingColors.Charcoal),
        cursorBrush = SolidColor(PingColors.Charcoal),
        keyboardOptions = keyboardOptions,
        interactionSource = interaction,
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = PingTypography.bodyMedium.copy(color = PingColors.Muted)
                    )
                }
                inner()
            }
        }
    )
}
