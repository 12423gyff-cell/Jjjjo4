package com.example.prayer

import android.content.Context
import android.util.Log
import com.example.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PrayerRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val prayerDao = database.prayerTimingDao()

    suspend fun getPrayerTimes(
        calendar: Calendar,
        cityName: String,
        cityNameAr: String,
        countryName: String,
        latitude: Double,
        longitude: Double,
        timezone: Double
    ): CachedPrayerTimes = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = dateFormat.format(calendar.time)
        val dateKey = "${dateStr}_${cityName}"

        // 1. Try loading from local Room Database Cache first
        try {
            val cached = prayerDao.getTiming(dateKey)
            if (cached != null) {
                Log.d("PrayerRepository", "Loaded prayer times from Room cache for $dateKey")
                return@withContext CachedPrayerTimes(
                    hijriDate = cached.hijriDate,
                    gregorianDate = cached.gregorianDate,
                    dayName = cached.dayName,
                    fajr = cached.fajr,
                    sunrise = cached.sunrise,
                    dhuhr = cached.dhuhr,
                    asr = cached.asr,
                    maghrib = cached.maghrib,
                    isha = cached.isha,
                    midnight = cached.midnight,
                    lastThird = cached.lastThird,
                    isFromCache = true
                )
            }
        } catch (e: Exception) {
            Log.e("PrayerRepository", "Failed to retrieve from local cache", e)
        }

        // 2. Fetch from AlAdhan API if not in Cache
        try {
            Log.d("PrayerRepository", "Fetching prayer times from AlAdhan API for $cityName ($countryName)")
            val response = RetrofitClient.alAdhanApi.getTimingsByCity(
                city = cityName,
                country = countryName,
                method = 1, // Karachi (1) matching ihadis
                school = 0, // Shafi (0)
                timezone = "Asia/Aden"
            )

            if (response.code == 200) {
                val timings = response.data.timings
                val dateInfo = response.data.date
                
                // Format Hijri Calendar text
                val hijri = dateInfo.hijri
                val hijriStr = "${hijri.weekday.ar ?: hijri.weekday.en} ${hijri.day} ${hijri.month.ar ?: hijri.month.en} ${hijri.year} هـ"
                
                // Format Gregorian Calendar text
                val greg = dateInfo.gregorian
                val gregStr = "${greg.day} ${greg.month.en} ${greg.year}"
                val dayNameAr = hijri.weekday.ar ?: hijri.weekday.en

                // Clean timestamps
                val cleanFajr = timings.Fajr.substringBefore(" ")
                val cleanSunrise = timings.Sunrise.substringBefore(" ")
                val cleanDhuhr = timings.Dhuhr.substringBefore(" ")
                val cleanAsr = timings.Asr.substringBefore(" ")
                val cleanMaghrib = timings.Maghrib.substringBefore(" ")
                val cleanIsha = timings.Isha.substringBefore(" ")
                val cleanMidnight = timings.Midnight.substringBefore(" ")
                val cleanLastThird = timings.Lastthird.substringBefore(" ")

                val entity = PrayerTimingEntity(
                    dateKey = dateKey,
                    city = cityName,
                    country = countryName,
                    hijriDate = hijriStr,
                    gregorianDate = gregStr,
                    dayName = dayNameAr,
                    fajr = cleanFajr,
                    sunrise = cleanSunrise,
                    dhuhr = cleanDhuhr,
                    asr = cleanAsr,
                    maghrib = cleanMaghrib,
                    isha = cleanIsha,
                    midnight = cleanMidnight,
                    lastThird = cleanLastThird,
                    timestamp = System.currentTimeMillis()
                )

                // Cache timing locally in Room
                try {
                    prayerDao.insertTiming(entity)
                    Log.d("PrayerRepository", "Saved downloaded timings to Room cache for $dateKey")
                } catch (e: Exception) {
                    Log.e("PrayerRepository", "Failed to cache timings locally", e)
                }

                // Delete extremely old caches
                try {
                    val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    prayerDao.deleteOldTimings(oneMonthAgo)
                } catch (e: Exception) {
                    Log.e("PrayerRepository", "Cleanup error", e)
                }

                return@withContext CachedPrayerTimes(
                    hijriDate = hijriStr,
                    gregorianDate = gregStr,
                    dayName = dayNameAr,
                    fajr = cleanFajr,
                    sunrise = cleanSunrise,
                    dhuhr = cleanDhuhr,
                    asr = cleanAsr,
                    maghrib = cleanMaghrib,
                    isha = cleanIsha,
                    midnight = cleanMidnight,
                    lastThird = cleanLastThird,
                    isFromCache = false
                )
            }
        } catch (e: Exception) {
            Log.e("PrayerRepository", "API execution failure", e)
        }

        // 3. Fallback to offline premium calculation as a final safety measure
        Log.w("PrayerRepository", "Local cache and API failed. Using mathematical fallback model.")
        val fallback = PrayerCalculator.calculateFallbackTimes(calendar, latitude, longitude, timezone)
        
        val gregStrAr = SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(calendar.time)
        val dayOfWeekAr = SimpleDateFormat("EEEE", Locale("ar")).format(calendar.time)
        
        val hijriStrAr = try {
            val islamicCal = android.icu.util.IslamicCalendar()
            islamicCal.time = calendar.time
            val hDay = islamicCal.get(android.icu.util.IslamicCalendar.DAY_OF_MONTH)
            val hMonth = islamicCal.get(android.icu.util.IslamicCalendar.MONTH)
            val hYear = islamicCal.get(android.icu.util.IslamicCalendar.YEAR)
            val hijriMonths = listOf(
                "محرم", "صفر", "ربيع الأول", "ربيع الآخر", 
                "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", 
                "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
            )
            val monthName = hijriMonths.getOrElse(hMonth) { "محرم" }
            "$dayOfWeekAr $hDay $monthName $hYear هـ"
        } catch (e: Exception) {
            "$dayOfWeekAr 25 محرم 1448 هـ"
        }

        return@withContext CachedPrayerTimes(
            hijriDate = hijriStrAr,
            gregorianDate = gregStrAr,
            dayName = dayOfWeekAr,
            fajr = fallback.fajr,
            sunrise = fallback.sunrise,
            dhuhr = fallback.dhuhr,
            asr = fallback.asr,
            maghrib = fallback.maghrib,
            isha = fallback.isha,
            midnight = fallback.midnight,
            lastThird = fallback.lastThird,
            isFromCache = false
        )
    }
}

data class CachedPrayerTimes(
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
    val isFromCache: Boolean
)
