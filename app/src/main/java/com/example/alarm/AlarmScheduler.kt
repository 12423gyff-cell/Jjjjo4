package com.example.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.util.Calendar

object AlarmScheduler {
    
    fun getNextTriggerTime(alarmItem: AlarmItem): Long {
        if (alarmItem.repeatMode == "TEST") {
            return System.currentTimeMillis() + 10000
        }
        val nowMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = nowMillis
        calendar.set(Calendar.HOUR_OF_DAY, alarmItem.hour)
        calendar.set(Calendar.MINUTE, alarmItem.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // If the calculated time is before current time + 5 seconds, it's in the past or firing right now.
        if (calendar.timeInMillis <= nowMillis + 5000L) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        if (alarmItem.days.isNotEmpty() && alarmItem.repeatMode != "مرة واحدة" && alarmItem.repeatMode != "Once") {
            var safetyCounter = 0
            while (!alarmItem.days.contains(calendar.get(Calendar.DAY_OF_WEEK)) && safetyCounter < 14) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                safetyCounter++
            }
        }
        
        // Calculate the exact delay and use System.currentTimeMillis() + delay
        // This is exactly the same reliable mechanism used by the Test Alarm
        val delay = calendar.timeInMillis - nowMillis
        return System.currentTimeMillis() + delay
    }

    private fun getPendingIntent(context: Context, alarmItem: AlarmItem): PendingIntent {
        android.util.Log.d("HISN_ALARM", "3. PendingIntent created for alarmId: ${alarmItem.id}")
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // Use an explicit data URI so Android treats every alarm ID as a separate, unique Intent
            data = Uri.parse("alarm://${alarmItem.id}")
            putExtra("alarm_id", alarmItem.id)
            putExtra("zikr_title", alarmItem.linkedZikrTitle)
            putExtra("linked_zikr_id", alarmItem.linkedZikrId)
            putExtra("sound", alarmItem.sound)
            putExtra("vibration", alarmItem.vibration)
            putExtra("volume", alarmItem.volume)
            putExtra("auto_open", alarmItem.autoOpen)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmItem.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleOrCancel(context: Context, alarmItem: AlarmItem) {
        android.util.Log.d("HISN_ALARM", "TRACE: AlarmScheduler.scheduleOrCancel() started for alarmId: ${alarmItem.id}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntent(context, alarmItem)
        android.util.Log.d("HISN_ALARM", "TRACE: PendingIntent generated.")
        alarmManager.cancel(pendingIntent)
        
        if (!alarmItem.isEnabled) {
            android.util.Log.d("HISN_ALARM", "TRACE: Alarm is disabled. Stopped trace.")
            return
        }
        
        android.util.Log.d("HISN_ALARM", "TRACE: Alarm is enabled, calling getNextTriggerTime...")
        val triggerTime = getNextTriggerTime(alarmItem)
        android.util.Log.d("HISN_ALARM", "TRACE: Exact trigger time calculated: $triggerTime")
        
        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            android.util.Log.d("HISN_ALARM", "TRACE: Executing AlarmManager.setAlarmClock() with time: $triggerTime")
        } catch (e: Exception) {
            android.util.Log.e("HISN_ALARM", "Failed to schedule exact alarm: ${e.message}")
            e.printStackTrace()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (fallbackE: Exception) {
                android.util.Log.e("HISN_ALARM", "Fallback scheduling failed: ${fallbackE.message}")
                fallbackE.printStackTrace()
            }
        }
    }

    fun cancel(context: Context, alarmItem: AlarmItem) {
        android.util.Log.d("HISN_ALARM", "Alarm cancelled for alarmId: ${alarmItem.id}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntent(context, alarmItem)
        alarmManager.cancel(pendingIntent)
        // Alarm cancelled successfully
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            return pm.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }
}
