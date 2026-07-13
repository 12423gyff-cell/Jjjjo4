package com.example.prayer

sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(
        val timings: CachedPrayerTimes,
        val countdownState: CountdownState,
        val currentTime: String,
        val selectedCityNameAr: String,
        val isOfflineMode: Boolean = false
    ) : PrayerUiState()
    data class Error(val message: String) : PrayerUiState()
}
