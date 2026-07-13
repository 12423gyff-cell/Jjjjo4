package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        android.util.Log.d("HISN_ALARM", "BootReceiver triggered with action: $action")
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED) {
            
            // Re-schedule all enabled alarms
            android.util.Log.d("HISN_ALARM", "Rescheduling all enabled alarms after device boot/time change")
            AlarmRepository.load(context)
        }
    }
}
