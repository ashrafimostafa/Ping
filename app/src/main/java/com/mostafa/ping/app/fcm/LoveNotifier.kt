package com.mostafa.ping.app.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mostafa.ping.app.LoveAlarmActivity
import com.mostafa.ping.app.MainActivity
import com.mostafa.ping.app.R

object LoveNotifier {
    const val CHANNEL_ID = "love_pings_fullscreen"
    private const val NOTIFICATION_ID = 1001
    @Volatile private var lastShownAt = 0L

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        runCatching { manager.deleteNotificationChannel("love_pings") }
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 120, 80, 120, 80, 280)
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setAllowBubbles(true)
            }
        }
        manager.createNotificationChannel(channel)
    }

    fun showFullScreen(context: Context, title: String, body: String, fromCode: String = "") {
        val now = System.currentTimeMillis()
        if (now - lastShownAt < 2_500) return
        lastShownAt = now

        ensureChannel(context)
        wakeScreen(context)

        val fullScreenIntent = Intent(context, LoveAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(LoveAlarmActivity.EXTRA_MESSAGE, body)
            putExtra(LoveAlarmActivity.EXTRA_FROM, fromCode)
        }
        val fullScreenPending = PendingIntent.getActivity(
            context,
            2001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val loveBitmap = loveBitmap(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_heart)
            .setColor(context.getColor(R.color.charcoal))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(loveBitmap)
                    .bigLargeIcon(null as Bitmap?)
                    .setBigContentTitle(title)
                    .setSummaryText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .setFullScreenIntent(fullScreenPending, true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setTimeoutAfter(30_000)

        if (loveBitmap != null) {
            builder.setLargeIcon(loveBitmap)
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            runCatching { context.startActivity(fullScreenIntent) }
        }
    }

    private fun loveBitmap(context: Context): Bitmap? =
        runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_love_heart_full)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.img_love_heart)
        }.getOrNull()

    private fun wakeScreen(context: Context) {
        val power = context.getSystemService(PowerManager::class.java) ?: return
        @Suppress("DEPRECATION")
        val wake = power.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
            "ping:love"
        )
        runCatching {
            wake.acquire(3_000)
            wake.release()
        }
    }
}
