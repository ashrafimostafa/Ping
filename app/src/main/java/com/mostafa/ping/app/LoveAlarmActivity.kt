package com.mostafa.ping.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mostafa.ping.app.ui.screens.IncomingLoveOverlay
import com.mostafa.ping.app.ui.theme.PingTheme

/**
 * Full-screen “I love you” shown over the lock screen when a ping arrives.
 */
class LoveAlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val message = intent.getStringExtra(EXTRA_MESSAGE)
            ?: getString(R.string.love_notification_body)

        enableEdgeToEdge()
        setContent {
            PingTheme {
                IncomingLoveOverlay(
                    message = message,
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_FROM = "fromCode"
    }
}
