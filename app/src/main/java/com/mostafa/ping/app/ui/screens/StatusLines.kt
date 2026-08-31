package com.mostafa.ping.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mostafa.ping.app.ui.PingUiState
import com.mostafa.ping.app.ui.theme.PingColors
import com.mostafa.ping.app.ui.theme.PingSpacing
import com.mostafa.ping.app.ui.theme.PingTypography

@Composable
fun StatusLines(state: PingUiState) {
    val text = state.error ?: state.notice
    if (!text.isNullOrBlank()) {
        Spacer(Modifier.height(PingSpacing.Md))
        Text(
            text,
            style = PingTypography.bodySmall.copy(
                color = if (state.error != null) PingColors.Charcoal else PingColors.Muted
            )
        )
    }
}
