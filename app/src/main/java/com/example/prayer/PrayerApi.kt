package com.example.prayer

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface AlAdhanApi {
    @GET("v1/timingsByCity")
    suspend fun getTimingsByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 1, // Karachi
        @Query("school") school: Int = 0, // Standard
        @Query("timezone") timezone: String = "Asia/Aden"
    ): AlAdhanResponse
}

data class AlAdhanResponse(
    val code: Int,
    val status: String,
    val data: AlAdhanData
)

data class AlAdhanData(
    val timings: AlAdhanTimings,
    val date: AlAdhanDate
)

data class AlAdhanTimings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Sunset: String,
    val Maghrib: String,
    val Isha: String,
    val Midnight: String,
    val Lastthird: String
)

data class AlAdhanDate(
    val readable: String,
    val timestamp: String,
    val gregorian: AlAdhanGregorian,
    val hijri: AlAdhanHijri
)

data class AlAdhanGregorian(
    val date: String,
    val format: String,
    val day: String,
    val weekday: AlAdhanWeekday,
    val month: AlAdhanMonth,
    val year: String
)

data class AlAdhanHijri(
    val date: String,
    val format: String,
    val day: String,
    val weekday: AlAdhanWeekday,
    val month: AlAdhanMonth,
    val year: String
)

data class AlAdhanWeekday(
    val en: String,
    val ar: String? = null
)

data class AlAdhanMonth(
    val number: Int,
    val en: String,
    val ar: String? = null
)

object RetrofitClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val alAdhanApi: AlAdhanApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AlAdhanApi::class.java)
    }
}
