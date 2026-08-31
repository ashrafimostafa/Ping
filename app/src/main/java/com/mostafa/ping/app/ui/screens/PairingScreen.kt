package com.mostafa.ping.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mostafa.ping.app.ui.PingUiState
import com.mostafa.ping.app.ui.components.PingButton
import com.mostafa.ping.app.ui.components.PingCard
import com.mostafa.ping.app.ui.components.PingIconButton
import com.mostafa.ping.app.ui.components.PingScaffold
import com.mostafa.ping.app.ui.components.PingTextField
import com.mostafa.ping.app.ui.theme.PingColors
import com.mostafa.ping.app.ui.theme.PingRadius
import com.mostafa.ping.app.ui.theme.PingSpacing
import com.mostafa.ping.app.ui.theme.PingTypography

@Composable
fun PairingScreen(
    state: PingUiState,
    onPartnerInputChange: (String) -> Unit,
    onConnect: () -> Unit
) {
    val context = LocalContext.current
    PingScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PingSpacing.Xl, vertical = PingSpacing.Section),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Ping", style = PingTypography.displayLarge)
            Spacer(Modifier.height(PingSpacing.Sm))
            Text(
                "A private tap for the two of you.",
                style = PingTypography.bodyLarge
            )
            Spacer(Modifier.height(PingSpacing.Xxl))
            PingCard(
                modifier = Modifier.fillMaxWidth(),
                radius = PingRadius.Container
            ) {
                Text("Your ID", style = PingTypography.bodySmall)
                Spacer(Modifier.height(PingSpacing.Xxs))
                Text(
                    state.myCode.chunked(3).joinToString("  "),
                    style = PingTypography.headlineLarge
                )
                Spacer(Modifier.height(PingSpacing.Md))
                Row(horizontalArrangement = Arrangement.spacedBy(PingSpacing.Sm)) {
                    PingIconButton(onClick = { copyId(context, state.myCode) }) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = PingColors.Charcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    PingIconButton(onClick = { shareId(context, state.myCode) }) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share ID",
                            tint = PingColors.Charcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(PingSpacing.Xl))
            Text(
                "Ask your partner for their ID, or share yours.",
                style = PingTypography.bodyMedium.copy(color = PingColors.Muted)
            )
            Spacer(Modifier.height(PingSpacing.Md))
            PingTextField(
                value = state.partnerInput,
                onValueChange = onPartnerInputChange,
                placeholder = "Partner ID",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                )
            )
            Spacer(Modifier.height(PingSpacing.Md))
            PingButton(
                text = if (state.isLoading) "Connecting" else "Connect",
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.partnerInput.length == 6
            )
            StatusLines(state)
        }
    }
}

@Composable
fun SetupScreen(message: String) {
    PingScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(PingSpacing.Xl),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Almost there", style = PingTypography.displayLarge)
            Spacer(Modifier.height(PingSpacing.Md))
            Text(message, style = PingTypography.bodyLarge)
            Spacer(Modifier.height(PingSpacing.Sm))
            Text(
                "Create a Firebase project, add Android app com.mostafa.ping.app, put google-services.json in app/, create Firestore, paste firestore.rules and Publish. Use VPN if Google is blocked.",
                style = PingTypography.bodySmall
            )
        }
    }
}

fun copyId(context: Context, code: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("Ping ID", code))
}

fun shareId(context: Context, code: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Add me on Ping with this ID: $code")
    }
    context.startActivity(Intent.createChooser(intent, "Share your Ping ID"))
}
