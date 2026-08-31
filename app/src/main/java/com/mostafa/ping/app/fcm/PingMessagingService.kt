package com.mostafa.ping.app.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mostafa.ping.app.IncomingPing
import com.mostafa.ping.app.PingApplication
import com.mostafa.ping.app.R

class PingMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val fromCode = message.data["fromCode"].orEmpty()
        val title = message.notification?.title
            ?: getString(R.string.love_notification_title)
        val body = message.notification?.body
            ?: message.data["message"]
            ?: getString(R.string.love_notification_body)

        val ping = IncomingPing(
            fromCode = fromCode,
            message = body,
            id = message.messageId ?: System.currentTimeMillis().toString()
        )
        PingApplication.emitIncomingPing(ping)

        if (!PingApplication.inForeground.value) {
            LoveNotifier.show(this, title, body)
        }
    }
}
