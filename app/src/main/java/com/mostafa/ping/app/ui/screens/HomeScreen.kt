package com.mostafa.ping.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mostafa.ping.app.R
import com.mostafa.ping.app.ui.PingUiState
import com.mostafa.ping.app.ui.components.PingButton
import com.mostafa.ping.app.ui.components.PingButtonStyle
import com.mostafa.ping.app.ui.components.PingCard
import com.mostafa.ping.app.ui.components.PingScaffold
import com.mostafa.ping.app.ui.theme.PingRadius
import com.mostafa.ping.app.ui.theme.PingSpacing
import com.mostafa.ping.app.ui.theme.PingTypography

@Composable
fun HomeScreen(
    state: PingUiState,
    onSend: () -> Unit,
    onUnpair: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val pulse = rememberInfiniteTransition(label = "heart-pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        label = "press"
    )

    PingScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = PingSpacing.Xl, vertical = PingSpacing.Xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ping", style = PingTypography.titleLarge)
                PingButton(
                    text = "Unpair",
                    onClick = onUnpair,
                    style = PingButtonStyle.Ghost
                )
            }
            Spacer(Modifier.height(PingSpacing.Xl))
            PingCard(
                modifier = Modifier.fillMaxWidth(),
                radius = PingRadius.Card
            ) {
                Text("Connected", style = PingTypography.bodySmall)
                Spacer(Modifier.height(PingSpacing.Xxs))
                Text(state.partnerCode.orEmpty(), style = PingTypography.headlineLarge)
                Spacer(Modifier.height(PingSpacing.Sm))
                Text(
                    "Your ID  ${state.myCode}",
                    style = PingTypography.bodySmall
                )
            }
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.img_love_heart),
                contentDescription = "Send I love you",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale * pressScale)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = !state.sending,
                        role = Role.Button
                    ) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSend()
                    }
            )
            Spacer(Modifier.height(PingSpacing.Lg))
            Text(
                if (state.sending) "Sending…" else "Tap to say I love you",
                style = PingTypography.bodyLarge,
                textAlign = TextAlign.Center
            )
            StatusLines(state)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun IncomingLoveOverlay(
    message: String,
    onDismiss: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "incoming")
    val scale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "incoming-scale"
    )
    PingScaffold(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(PingSpacing.Xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.img_love_heart),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
            )
            Spacer(Modifier.height(PingSpacing.Lg))
            Text(message, style = PingTypography.displayLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(PingSpacing.Sm))
            Text("Tap to close", style = PingTypography.bodySmall)
        }
    }
}
