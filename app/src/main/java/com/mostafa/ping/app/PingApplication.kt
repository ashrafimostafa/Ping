package com.mostafa.ping.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.mostafa.ping.app.fcm.LoveNotifier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class PingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        runCatching { FirebaseApp.initializeApp(this) }
        LoveNotifier.ensureChannel(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    _inForeground.value = true
                }

                override fun onStop(owner: LifecycleOwner) {
                    _inForeground.value = false
                }
            }
        )
    }

    companion object {
        lateinit var instance: PingApplication
            private set

        private val _inForeground = MutableStateFlow(false)
        val inForeground: StateFlow<Boolean> = _inForeground.asStateFlow()

        private val _incomingPings = MutableSharedFlow<IncomingPing>(extraBufferCapacity = 8)
        val incomingPings: SharedFlow<IncomingPing> = _incomingPings.asSharedFlow()

        fun emitIncomingPing(ping: IncomingPing) {
            _incomingPings.tryEmit(ping)
        }
    }
}

data class IncomingPing(
    val fromCode: String,
    val message: String,
    val id: String,
)
