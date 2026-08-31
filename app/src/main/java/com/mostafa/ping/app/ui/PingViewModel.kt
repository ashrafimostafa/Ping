package com.mostafa.ping.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ListenerRegistration
import com.mostafa.ping.app.IncomingPing
import com.mostafa.ping.app.PingApplication
import com.mostafa.ping.app.data.FcmSender
import com.mostafa.ping.app.data.FirebaseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PingUiState(
    val isLoading: Boolean = true,
    val setupMessage: String? = null,
    val myCode: String = "",
    val partnerCode: String? = null,
    val partnerInput: String = "",
    val pushConfigured: Boolean = false,
    val sending: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val incomingLove: Boolean = false,
    val incomingMessage: String = "I love you ❤️",
)

class PingViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FirebaseRepository()

    private val _state = MutableStateFlow(PingUiState())
    val state: StateFlow<PingUiState> = _state.asStateFlow()

    private var partnerListener: ListenerRegistration? = null
    private var inboxListener: ListenerRegistration? = null
    private var loveResetJob: Job? = null
    private val seenPingIds = ArrayDeque<String>()

    init {
        viewModelScope.launch {
            PingApplication.incomingPings.collect { ping ->
                onIncoming(ping)
            }
        }
        start()
    }

    fun onPartnerInputChange(value: String) {
        _state.update { it.copy(partnerInput = value.uppercase().take(6), error = null) }
    }

    fun connectPartner() {
        val myCode = _state.value.myCode
        val input = _state.value.partnerInput
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = repo.pairWith(myCode, input)
                _state.update {
                    it.copy(
                        isLoading = false,
                        partnerCode = profile.partnerCode,
                        notice = "You're connected"
                    )
                }
                attachListeners(myCode)
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Could not connect")
                }
            }
        }
    }

    fun unpair() {
        val current = _state.value
        viewModelScope.launch {
            try {
                repo.unpair(current.myCode, current.partnerCode)
                _state.update {
                    it.copy(partnerCode = null, partnerInput = "", notice = "Disconnected")
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Could not unpair") }
            }
        }
    }

    fun sendLove() {
        val current = _state.value
        val partner = current.partnerCode ?: return
        if (current.sending) return
        viewModelScope.launch {
            _state.update { it.copy(sending = true, error = null, notice = null) }
            try {
                repo.sendPing(current.myCode, partner)
                _state.update { it.copy(sending = false, notice = "Sent with love") }
            } catch (e: Exception) {
                _state.update {
                    it.copy(sending = false, error = e.message ?: "Could not send ping")
                }
            }
        }
    }

    fun consumeIncoming() {
        _state.update { it.copy(incomingLove = false) }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, notice = null) }
    }

    private fun start() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, pushConfigured = FcmSender.isConfigured) }
            if (FirebaseApp.getApps(getApplication()).isEmpty()) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        setupMessage = "Add google-services.json from Firebase Console, then rebuild."
                    )
                }
                return@launch
            }
            try {
                val uid = repo.signIn()
                val profile = repo.loadOrCreateProfile(uid)
                _state.update {
                    it.copy(
                        isLoading = false,
                        myCode = profile.code,
                        partnerCode = profile.partnerCode,
                        setupMessage = null
                    )
                }
                attachListeners(profile.code)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        setupMessage = e.message
                            ?: "Add google-services.json from Firebase Console, then rebuild."
                    )
                }
            }
        }
    }

    private fun attachListeners(myCode: String) {
        partnerListener?.remove()
        inboxListener?.remove()
        partnerListener = repo.listenPartner(myCode) { partner ->
            _state.update { it.copy(partnerCode = partner) }
        }
        inboxListener = repo.listenInbox(myCode) { ping ->
            onIncoming(ping)
        }
    }

    private fun onIncoming(ping: IncomingPing) {
        if (ping.id in seenPingIds) return
        seenPingIds.addLast(ping.id)
        while (seenPingIds.size > 20) seenPingIds.removeFirst()
        loveResetJob?.cancel()
        _state.update {
            it.copy(incomingLove = true, incomingMessage = ping.message)
        }
        loveResetJob = viewModelScope.launch {
            delay(3_200)
            _state.update { it.copy(incomingLove = false) }
        }
    }

    override fun onCleared() {
        partnerListener?.remove()
        inboxListener?.remove()
        super.onCleared()
    }
}
