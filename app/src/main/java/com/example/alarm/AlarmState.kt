package com.example.alarm

import android.content.Context
import android.content.SharedPreferences

object AlarmState {
    private const val PREFS_NAME = "alarm_state_prefs"
    
    fun setLastFiredTime(context: Context, alarmId: Int, time: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong("last_fired_$alarmId", time).apply()
    }
    
    fun getLastFiredTime(context: Context, alarmId: Int): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("last_fired_$alarmId", 0L)
    }
}
