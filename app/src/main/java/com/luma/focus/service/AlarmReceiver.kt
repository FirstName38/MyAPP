package com.luma.focus.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.luma.focus.R

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_SOUND = "extra_sound"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task reminder"
        val id = intent.getStringExtra(EXTRA_TASK_ID) ?: title
        val sound = intent.getStringExtra(EXTRA_SOUND) ?: "Chime"
        val channel = "luma_reminder_${sound.lowercase()}"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= 26) {
            val uri = Uri.parse("android.resource://${context.packageName}/${soundRes(sound)}")
            val channelObj = NotificationChannel(
                channel,
                "Luma $sound reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelObj.setSound(
                uri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
            manager.createNotificationChannel(channelObj)
        }

        manager.notify(
            id.hashCode(),
            NotificationCompat.Builder(context, channel)
                .setContentTitle("Luma reminder")
                .setContentText(title)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
    }

    private fun soundRes(sound: String): Int = when (sound) {
        "Rain" -> R.raw.rain
        "Soft" -> R.raw.soft
        "DeepFocus" -> R.raw.deepfocus
        else -> R.raw.chime
    }
}

object ReminderScheduler {
    fun schedule(
        context: Context,
        taskId: String,
        title: String,
        triggerAtMillis: Long,
        sound: String = "Chime"
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_SOUND, sound)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancel(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
