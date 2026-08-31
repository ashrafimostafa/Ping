package com.mostafa.ping.app.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mostafa.ping.app.IncomingPing
import com.mostafa.ping.app.PingApplication
import com.mostafa.ping.app.R
import com.mostafa.ping.app.data.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PingMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token")
        scope.launch {
            runCatching {
                FirebaseRepository(applicationContext).saveFcmToken(token)
            }.onFailure { Log.e(TAG, "save token failed", it) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "FCM received data=${message.data} notif=${message.notification}")
        val fromCode = message.data["fromCode"].orEmpty()
        val title = message.data["title"]
            ?: message.notification?.title
            ?: getString(R.string.love_notification_title)
        val body = message.data["body"]
            ?: message.data["message"]
            ?: message.notification?.body
            ?: getString(R.string.love_notification_body)

        val ping = IncomingPing(
            fromCode = fromCode,
            message = body,
            id = message.messageId ?: System.currentTimeMillis().toString()
        )
        PingApplication.emitIncomingPing(ping)

        if (!PingApplication.inForeground.value) {
            LoveNotifier.showFullScreen(this, title, body, fromCode)
        }
    }

    companion object {
        private const val TAG = "PingFcmService"
    }
}
