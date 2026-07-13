package com.example.prayer

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "prayer_timings_cache")
data class PrayerTimingEntity(
    @PrimaryKey
    val dateKey: String, // format: "yyyy-MM-dd_cityName"
    val city: String,
    val country: String,
    val hijriDate: String,
    val gregorianDate: String,
    val dayName: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val midnight: String,
    val lastThird: String,
    val timestamp: Long
)

@Dao
interface PrayerTimingDao {
    @Query("SELECT * FROM prayer_timings_cache WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getTiming(dateKey: String): PrayerTimingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiming(timing: PrayerTimingEntity)

    @Query("DELETE FROM prayer_timings_cache WHERE timestamp < :expiryTimestamp")
    suspend fun deleteOldTimings(expiryTimestamp: Long)
}
