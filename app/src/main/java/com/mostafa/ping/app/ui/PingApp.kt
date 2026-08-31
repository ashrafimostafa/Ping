package com.mostafa.ping.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mostafa.ping.app.ui.components.PingScaffold
import com.mostafa.ping.app.ui.screens.HomeScreen
import com.mostafa.ping.app.ui.screens.IncomingLoveOverlay
import com.mostafa.ping.app.ui.screens.PairingScreen
import com.mostafa.ping.app.ui.screens.SetupScreen
import com.mostafa.ping.app.ui.theme.PingColors

@Composable
fun PingApp(viewModel: PingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.setupMessage != null && state.myCode.isBlank() -> {
                SetupScreen(message = state.setupMessage.orEmpty())
            }

            state.isLoading && state.myCode.isBlank() -> {
                PingScaffold {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PingColors.Charcoal)
                    }
                }
            }

            state.partnerCode.isNullOrBlank() -> {
                PairingScreen(
                    state = state,
                    onPartnerInputChange = viewModel::onPartnerInputChange,
                    onConnect = viewModel::connectPartner
                )
            }

            else -> {
                HomeScreen(
                    state = state,
                    onSend = viewModel::sendLove,
                    onUnpair = viewModel::unpair
                )
            }
        }

        AnimatedVisibility(
            visible = state.incomingLove,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IncomingLoveOverlay(
                message = state.incomingMessage,
                onDismiss = viewModel::consumeIncoming
            )
        }
    }
}
