package com.example.alarm

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class AlarmItem(
    val id: Int = System.currentTimeMillis().toInt(),
    val isEnabled: Boolean = true,
    val hour: Int = 8,
    val minute: Int = 0,
    val days: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val linkedZikrTitle: String = "أذكار الصباح",
    val linkedZikrId: Int = -1,
    val sound: String = "soft_bell.wav",
    val vibration: Boolean = true,
    val volume: Float = 0.8f,
    val repeatMode: String = "كل يوم",
    val autoOpen: Boolean = false,
    val createdTime: Long = System.currentTimeMillis(),
    val lastFiredTime: Long = 0L,
    val nextTriggerTime: Long = 0L
)

object AlarmRepository {
    private const val PREFS_NAME = "alarms_prefs_v2"
    private val _alarmsFlow = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarmsFlow: StateFlow<List<AlarmItem>> = _alarmsFlow.asStateFlow()

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString("alarms_json", "[]") ?: "[]"
        val alarmsList = mutableListOf<AlarmItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val daysArray = obj.optJSONArray("days")
                val daysSet = mutableSetOf<Int>()
                if (daysArray != null) {
                    for (j in 0 until daysArray.length()) {
                        daysSet.add(daysArray.getInt(j))
                    }
                } else {
                    daysSet.addAll(setOf(1, 2, 3, 4, 5, 6, 7))
                }
                
                alarmsList.add(
                    AlarmItem(
                        id = obj.optInt("id", System.currentTimeMillis().toInt()),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        hour = obj.optInt("hour", 8),
                        minute = obj.optInt("minute", 0),
                        days = daysSet,
                        linkedZikrTitle = obj.optString("linkedZikrTitle", "أذكار الصباح"),
                        linkedZikrId = obj.optInt("linkedZikrId", -1),
                        sound = obj.optString("sound", "soft_bell.wav"),
                        vibration = obj.optBoolean("vibration", true),
                        volume = obj.optDouble("volume", 0.8).toFloat(),
                        repeatMode = obj.optString("repeatMode", "كل يوم"),
                        autoOpen = obj.optBoolean("autoOpen", false),
                        createdTime = obj.optLong("createdTime", System.currentTimeMillis()),
                        lastFiredTime = obj.optLong("lastFiredTime", 0L),
                        nextTriggerTime = obj.optLong("nextTriggerTime", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _alarmsFlow.value = alarmsList
        alarmsList.forEach { AlarmScheduler.scheduleOrCancel(context, it) }
    }

    private fun save(context: Context, alarms: List<AlarmItem>) {
        android.util.Log.d("HISN_ALARM", "TRACE: AlarmData.save() called. Total alarms: ${alarms.size}")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        alarms.forEach { alarm ->
            val obj = JSONObject()
            obj.put("id", alarm.id)
            obj.put("isEnabled", alarm.isEnabled)
            obj.put("hour", alarm.hour)
            obj.put("minute", alarm.minute)
            val daysArray = JSONArray()
            alarm.days.forEach { daysArray.put(it) }
            obj.put("days", daysArray)
            obj.put("linkedZikrTitle", alarm.linkedZikrTitle)
            obj.put("linkedZikrId", alarm.linkedZikrId)
            obj.put("sound", alarm.sound)
            obj.put("vibration", alarm.vibration)
            obj.put("volume", alarm.volume.toDouble())
            obj.put("repeatMode", alarm.repeatMode)
            obj.put("autoOpen", alarm.autoOpen)
            obj.put("createdTime", alarm.createdTime)
            obj.put("lastFiredTime", alarm.lastFiredTime)
            obj.put("nextTriggerTime", alarm.nextTriggerTime)
            jsonArray.put(obj)
        }
        prefs.edit {
            putString("alarms_json", jsonArray.toString())
        }
        android.util.Log.d("HISN_ALARM", "TRACE: Saved JSON to SharedPreferences.")
        _alarmsFlow.value = alarms
        alarms.forEach { 
            android.util.Log.d("HISN_ALARM", "TRACE: Calling AlarmScheduler.scheduleOrCancel for alarmId: ${it.id}")
            AlarmScheduler.scheduleOrCancel(context, it) 
        }
    }

    fun addAlarm(context: Context, alarm: AlarmItem) {
        android.util.Log.d("HISN_ALARM", "TRACE: AlarmRepository.addAlarm() called for alarmId: ${alarm.id}")
        val currentList = _alarmsFlow.value.toMutableList()
        currentList.add(alarm)
        save(context, currentList)
    }

    fun updateAlarm(context: Context, alarm: AlarmItem) {
        android.util.Log.d("HISN_ALARM", "TRACE: AlarmRepository.updateAlarm() called for alarmId: ${alarm.id}")
        val currentList = _alarmsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == alarm.id }
        if (index != -1) {
            currentList[index] = alarm
            save(context, currentList)
        } else {
            android.util.Log.d("HISN_ALARM", "TRACE: AlarmRepository.updateAlarm() failed. Alarm not found.")
        }
    }

    fun deleteAlarm(context: Context, alarmId: Int) {
        val currentList = _alarmsFlow.value.toMutableList()
        val alarm = currentList.find { it.id == alarmId }
        currentList.removeAll { it.id == alarmId }
        save(context, currentList)
        alarm?.let { AlarmScheduler.cancel(context, it) }
    }
}
