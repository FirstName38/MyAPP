package com.luma.focus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class FocusService : Service() {

    companion object {
        const val CHANNEL_ID = "luma_focus_channel"
        const val NOTIF_ID = 1001
        const val ACTION_START = "com.luma.focus.action.START"
        const val ACTION_STOP = "com.luma.focus.action.STOP"
        const val EXTRA_LABEL = "extra_label"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                val label = intent?.getStringExtra(EXTRA_LABEL) ?: "Focus session"
                startForeground(NOTIF_ID, buildNotification(label))
            }
        }
        return START_STICKY
    }

    private fun buildNotification(label: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Luma — Focusing")
            .setContentText(label)
            .setSmallIcon(com.luma.focus.R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Focus sessions", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
