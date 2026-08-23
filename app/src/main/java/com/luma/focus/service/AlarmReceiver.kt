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

class AlarmReceiver:BroadcastReceiver(){
 companion object{const val EXTRA_TASK_TITLE="extra_task_title";const val EXTRA_TASK_ID="extra_task_id";const val EXTRA_SOUND="extra_sound"}
 override fun onReceive(context:Context,intent:Intent){val title=intent.getStringExtra(EXTRA_TASK_TITLE)? :"Task reminder";val id=intent.getStringExtra(EXTRA_TASK_ID)?:title;val sound=intent.getStringExtra(EXTRA_SOUND)?:"Chime";val channel="luma_reminder_${sound.lowercase()}";val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26){val uri=Uri.parse("android.resource://${context.packageName}/${soundRes(sound)}");val ch=NotificationChannel(channel,"Luma $sound reminders",NotificationManager.IMPORTANCE_HIGH);ch.setSound(uri,AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());manager.createNotificationChannel(ch)}
  manager.notify(id.hashCode(),NotificationCompat.Builder(context,channel).setContentTitle("Luma reminder").setContentText(title).setSmallIcon(R.drawable.ic_launcher_foreground).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build())
 }
 private fun soundRes(s:String)=when(s){"Rain"->R.raw.rain;"Soft"->R.raw.soft;"DeepFocus"->R.raw.deepfocus;else->R.raw.chime}
}
object ReminderScheduler{fun schedule(context:Context,taskId:String,title:String,triggerAtMillis:Long,sound:String="Chime"){val am=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;val i=Intent(context,AlarmReceiver::class.java).apply{putExtra(AlarmReceiver.EXTRA_TASK_TITLE,title);putExtra(AlarmReceiver.EXTRA_TASK_ID,taskId);putExtra(AlarmReceiver.EXTRA_SOUND,sound)};val pi=PendingIntent.getBroadcast(context,taskId.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAtMillis,pi)};fun cancel(context:Context,taskId:String){val am=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;val i=Intent(context,AlarmReceiver::class.java);val pi=PendingIntent.getBroadcast(context,taskId.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);am.cancel(pi)}}
