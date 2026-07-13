package com.example.prayer

import java.text.SimpleDateFormat
import java.util.*

enum class PrayerState {
    COUNTDOWN, // Counting down to next prayer
    IQAMAH,    // Iqamah countdown is running
    PRAYING    // Showing "أقيمت الصلاة"
}

data class PrayerTimeInfo(
    val name: String,
    val timeStr: String,
    val calendar: Calendar,
    val hasIqamah: Boolean
)

data class CountdownState(
    val currentPrayerName: String,
    val nextPrayerName: String,
    val state: PrayerState,
    val countdownText: String
)

object CountdownManager {
    fun calculateCountdownState(
        currentTimeMillis: Long,
        timings: CachedPrayerTimes,
        iqamahMinutesMap: Map<String, Int> // e.g. "الفجر" -> 20, "الظهر" -> 15...
    ): CountdownState {
        val nowCal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val datePrefix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nowCal.time)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

        // Parse today's times
        fun parseTime(name: String, timeStr: String, hasIqamah: Boolean): PrayerTimeInfo {
            val safeTimeStr = timeStr.replace("٠", "0").replace("١", "1")
                .replace("٢", "2").replace("٣", "3").replace("٤", "4")
                .replace("٥", "5").replace("٦", "6").replace("٧", "7")
                .replace("٨", "8").replace("٩", "9")
            
            val date = try {
                format.parse("$datePrefix $safeTimeStr")
            } catch (e: Exception) {
                Date() // Fallback
            }
            val cal = Calendar.getInstance().apply { time = date }
            return PrayerTimeInfo(name, safeTimeStr, cal, hasIqamah)
        }

        val todayPrayers = listOf(
            parseTime("الفجر", timings.fajr, true),
            parseTime("الشروق", timings.sunrise, false),
            parseTime("الظهر", timings.dhuhr, true),
            parseTime("العصر", timings.asr, true),
            parseTime("المغرب", timings.maghrib, true),
            parseTime("العشاء", timings.isha, true)
        )

        // Check if now is before today's Fajr
        val firstPrayer = todayPrayers[0]
        
        if (currentTimeMillis < firstPrayer.calendar.timeInMillis) {
            val diffMs = firstPrayer.calendar.timeInMillis - currentTimeMillis
            return CountdownState(
                currentPrayerName = "العشاء",
                nextPrayerName = "الفجر",
                state = PrayerState.COUNTDOWN,
                countdownText = formatMillis(diffMs)
            )
        }

        // Find the active prayer segment
        var activeIndex = -1
        for (i in todayPrayers.indices) {
            if (currentTimeMillis >= todayPrayers[i].calendar.timeInMillis) {
                activeIndex = i
            }
        }

        val activePrayer = todayPrayers[activeIndex]
        
        val nextPrayer = if (activeIndex < todayPrayers.size - 1) {
            todayPrayers[activeIndex + 1]
        } else {
            // Tomorrow's Fajr
            val tomorrowCal = Calendar.getInstance().apply {
                timeInMillis = currentTimeMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tomorrowCal.time)
            
            val safeFajr = timings.fajr.replace("٠", "0").replace("١", "1")
                .replace("٢", "2").replace("٣", "3").replace("٤", "4")
                .replace("٥", "5").replace("٦", "6").replace("٧", "7")
                .replace("٨", "8").replace("٩", "9")
                
            val tomorrowFajrDate = try {
                format.parse("$tomorrowPrefix $safeFajr")
            } catch (e: Exception) {
                Date()
            }
            val tomorrowFajrCal = Calendar.getInstance().apply { time = tomorrowFajrDate }
            PrayerTimeInfo("الفجر", safeFajr, tomorrowFajrCal, true)
        }

        // If the active prayer has Iqamah, check Iqamah state
        if (activePrayer.hasIqamah) {
            val iqamahDurationMs = (iqamahMinutesMap[activePrayer.name] ?: 15) * 60 * 1000
            val iqamahEndTimeMs = activePrayer.calendar.timeInMillis + iqamahDurationMs

            if (currentTimeMillis < iqamahEndTimeMs) {
                // Currently in Iqamah ticking state
                val diffMs = iqamahEndTimeMs - currentTimeMillis
                return CountdownState(
                    currentPrayerName = activePrayer.name,
                    nextPrayerName = nextPrayer.name,
                    state = PrayerState.IQAMAH,
                    countdownText = formatMinutesSeconds(diffMs)
                )
            } else {
                // Post-Iqamah, check if we show "أقيمت الصلاة"
                val prayingEndMs = iqamahEndTimeMs + (30L * 60 * 1000)
                if (currentTimeMillis < prayingEndMs && currentTimeMillis < nextPrayer.calendar.timeInMillis) {
                    return CountdownState(
                        currentPrayerName = activePrayer.name,
                        nextPrayerName = nextPrayer.name,
                        state = PrayerState.PRAYING,
                        countdownText = "أقيمت الصلاة"
                    )
                }
            }
        }

        // Standard countdown to next prayer
        val diffMs = nextPrayer.calendar.timeInMillis - currentTimeMillis
        return CountdownState(
            currentPrayerName = activePrayer.name,
            nextPrayerName = nextPrayer.name,
            state = PrayerState.COUNTDOWN,
            countdownText = formatMillis(diffMs)
        )
    }

    private fun formatMillis(millis: Long): String {
        val totalSecs = millis / 1000
        val hr = totalSecs / 3600
        val mn = (totalSecs % 3600) / 60
        val sc = totalSecs % 60
        return String.format("%02d:%02d:%02d", hr, mn, sc)
    }

    private fun formatMinutesSeconds(millis: Long): String {
        val totalSecs = millis / 1000
        val mn = totalSecs / 60
        val sc = totalSecs % 60
        return String.format("%02d:%02d", mn, sc)
    }
}
