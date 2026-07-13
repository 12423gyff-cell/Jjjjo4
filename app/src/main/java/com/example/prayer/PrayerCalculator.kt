package com.example.prayer

import java.util.Calendar
import java.util.Locale
import kotlin.math.*

object PrayerCalculator {
    // Premium mathematical calculations for high precision fallback times
    fun calculateFallbackTimes(date: Calendar, latitude: Double, longitude: Double, timezone: Double): FallbackTimes {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)

        // 1. Julian Date
        val JD = getJulianDate(year, month, day)
        val D = JD - 2451545.0

        // 2. Solar Parameters
        val g = fixAngle(357.529 + 0.98560028 * D)
        val q = fixAngle(280.459 + 0.98564736 * D)
        val L = fixAngle(q + 1.915 * sin(g * Math.PI / 180.0) + 0.020 * sin(2.0 * g * Math.PI / 180.0))

        val e = 23.439 - 0.00000036 * D
        val declination = asin(sin(e * Math.PI / 180.0) * sin(L * Math.PI / 180.0)) // in radians

        var RA = atan2(cos(e * Math.PI / 180.0) * sin(L * Math.PI / 180.0), cos(L * Math.PI / 180.0)) / (Math.PI / 180.0) / 15.0
        RA = fixHour(RA)

        val EqT = q / 15.0 - RA

        // 3. Mid Day (Dhuhr)
        val dhuhrLocal = 12.0 + timezone - longitude / 15.0 - EqT

        // 4. Fajr Angle (18.0 degrees angle corresponds roughly to Karachi / Umm Al Qura Standard)
        val fajrAngle = 18.0
        val fajrHourAngle = try {
            acos((-sin(fajrAngle * Math.PI / 180.0) - sin(latitude * Math.PI / 180.0) * sin(declination)) / (cos(latitude * Math.PI / 180.0) * cos(declination))) * 180.0 / Math.PI / 15.0
        } catch (e: Exception) {
            5.0 // fallback
        }
        val fajrLocal = dhuhrLocal - fajrHourAngle

        // 5. Sunrise (0.833 degrees angle for atmospheric refraction)
        val sunriseAngle = 0.833
        val sunriseHourAngle = try {
            acos((-sin(sunriseAngle * Math.PI / 180.0) - sin(latitude * Math.PI / 180.0) * sin(declination)) / (cos(latitude * Math.PI / 180.0) * cos(declination))) * 180.0 / Math.PI / 15.0
        } catch (e: Exception) {
            6.0 // fallback
        }
        val sunriseLocal = dhuhrLocal - sunriseHourAngle

        // 6. Asr (Standard Shafi/Hanafi method)
        val t = abs(latitude - declination * 180.0 / Math.PI)
        val zenithAsr = atan(1.0 + tan(t * Math.PI / 180.0))
        val asrAltitude = Math.PI / 2.0 - zenithAsr
        val asrHourAngle = try {
            acos((sin(asrAltitude) - sin(latitude * Math.PI / 180.0) * sin(declination)) / (cos(latitude * Math.PI / 180.0) * cos(declination))) * 180.0 / Math.PI / 15.0
        } catch (e: Exception) {
            3.5 // fallback
        }
        val asrLocal = dhuhrLocal + asrHourAngle

        // 7. Maghrib (Sunset, same angle as sunrise)
        val maghribLocal = dhuhrLocal + sunriseHourAngle

        // 8. Isha (18.0 degrees angle)
        val ishaAngle = 18.0
        val ishaHourAngle = try {
            acos((-sin(ishaAngle * Math.PI / 180.0) - sin(latitude * Math.PI / 180.0) * sin(declination)) / (cos(latitude * Math.PI / 180.0) * cos(declination))) * 180.0 / Math.PI / 15.0
        } catch (e: Exception) {
            7.5 // fallback
        }
        val ishaLocal = dhuhrLocal + ishaHourAngle

        // 9. Midnight & Last Third calculations (Sunset to Fajr length based)
        val nightLength = (24.0 - maghribLocal + fajrLocal) % 24.0
        val midnightLocal = (maghribLocal + nightLength / 2.0) % 24.0
        val lastThirdLocal = (maghribLocal + nightLength * (2.0 / 3.0)) % 24.0

        return FallbackTimes(
            fajr = formatTime(fajrLocal),
            sunrise = formatTime(sunriseLocal),
            dhuhr = formatTime(dhuhrLocal),
            asr = formatTime(asrLocal),
            maghrib = formatTime(maghribLocal),
            isha = formatTime(ishaLocal),
            midnight = formatTime(midnightLocal),
            lastThird = formatTime(lastThirdLocal)
        )
    }

    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val A = (y / 100.0).toInt()
        val B = 2 - A + (A / 4.0).toInt()
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + B - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixHour(h: Double): Double {
        var hour = h - 24.0 * floor(h / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    private fun formatTime(hours: Double): String {
        var h = hours
        if (h.isNaN() || h.isInfinite()) {
            return "12:00"
        }
        while (h < 0) h += 24.0
        while (h >= 24) h -= 24.0
        val totalMinutes = (h * 60.0).roundToInt()
        val hr = (totalMinutes / 60) % 24
        val mn = totalMinutes % 60
        return String.format(Locale.US, "%02d:%02d", hr, mn)
    }
}

data class FallbackTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val midnight: String,
    val lastThird: String
)
