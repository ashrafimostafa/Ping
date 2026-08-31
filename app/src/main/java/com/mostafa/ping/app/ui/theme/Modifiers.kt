package com.mostafa.ping.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Signature dark-button depth: white top inset, dark inner ring, soft drop. */
fun Modifier.pingInsetShadow(shape: Shape = PingShapes.Button): Modifier =
    this
        .shadow(
            elevation = 1.dp,
            shape = shape,
            ambientColor = PingColors.SoftDrop,
            spotColor = PingColors.SoftDrop,
            clip = false
        )
        .drawWithContent {
            drawContent()
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, this),
                color = PingColors.InsetRing,
                style = Stroke(width = 0.5.dp.toPx())
            )
            drawLine(
                color = PingColors.InsetHighlight,
                start = Offset(0f, 0.5.dp.toPx()),
                end = Offset(size.width, 0.5.dp.toPx()),
                strokeWidth = 0.5.dp.toPx()
            )
        }

fun Modifier.pingFocusGlow(visible: Boolean, shape: Shape = RectangleShape): Modifier =
    if (!visible) this
    else shadow(
        elevation = 12.dp,
        shape = shape,
        ambientColor = PingColors.FocusShadow,
        spotColor = PingColors.FocusShadow,
        clip = false
    )

fun Modifier.pingHairline(shape: Shape, color: Color = PingColors.Border): Modifier =
    drawWithContent {
        drawContent()
        drawOutline(
            outline = shape.createOutline(size, layoutDirection, this),
            color = color,
            style = Stroke(width = 1.dp.toPx())
        )
    }
