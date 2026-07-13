package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val currentTime = System.currentTimeMillis()
        
        Log.d("HISN_ALARM", "7. AlarmReceiver received alarm: $alarmId at $currentTime")

        if (alarmId != -1) {
            val lastFired = AlarmState.getLastFiredTime(context, alarmId)
            // Prevent duplicate firing within 10 seconds (10,000 ms)
            if (currentTime - lastFired < 10_000L) {
                Log.d("HISN_ALARM", "Duplicate alarm prevented for $alarmId (fired recently)")
                return
            }
            AlarmState.setLastFiredTime(context, alarmId, currentTime)
        }

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtras(intent)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(intent.getStringExtra("zikr_title") ?: "تنبيه الأذكار")
            .setContentText("حان موعد الذكر")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(alarmId, notification)
        Log.d("HISN_ALARM", "10. Notification shown for alarmId: $alarmId")

        // Try starting the activity directly as well
        try {
            context.startActivity(alarmIntent)
            Log.d("HISN_ALARM", "Requested AlarmActivity start from AlarmReceiver")
        } catch (e: Exception) {
            Log.e("HISN_ALARM", "Failed to start AlarmActivity: ${e.message}")
            e.printStackTrace()
        }

        // Re-schedule next alarm
        if (alarmId != -1) {
            AlarmRepository.load(context)
            val alarm = AlarmRepository.alarmsFlow.value.find { it.id == alarmId }
            if (alarm != null) {
                val updatedAlarm = alarm.copy(lastFiredTime = currentTime)
                
                if (alarm.repeatMode == "مرة واحدة" || alarm.repeatMode == "Once" || alarm.repeatMode == "TEST") {
                    Log.d("HISN_ALARM", "Alarm is one-time, disabling alarmId: $alarmId")
                    AlarmRepository.updateAlarm(context, updatedAlarm.copy(isEnabled = false))
                } else {
                    Log.d("HISN_ALARM", "12. Alarm rescheduled for repeating alarmId: $alarmId")
                    AlarmRepository.updateAlarm(context, updatedAlarm)
                    // The save() method in updateAlarm automatically calls AlarmScheduler.scheduleOrCancel
                }
            } else {
                Log.e("HISN_ALARM", "Could not find alarm $alarmId in database to reschedule")
            }
        }
    }
}
