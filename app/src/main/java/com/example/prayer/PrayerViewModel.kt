package com.example.prayer

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CityInfo(
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val countryEn: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double
)

data class CountryInfo(
    val nameAr: String,
    val nameEn: String
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val repository = PrayerRepository(context)
    private val prefs = context.getSharedPreferences("prayer_settings", Context.MODE_PRIVATE)

    val allCities = listOf(
        // Yemen
        CityInfo("عدن", "Aden", "اليمن", "Yemen", 12.77957, 45.03852, 3.0),
        CityInfo("صنعاء", "Sana'a", "اليمن", "Yemen", 15.3694, 44.1910, 3.0),
        CityInfo("تعز", "Taiz", "اليمن", "Yemen", 13.5794, 44.0206, 3.0),
        CityInfo("المكلا", "Mukalla", "اليمن", "Yemen", 14.5417, 49.1250, 3.0),
        CityInfo("الحديدة", "Hodeidah", "اليمن", "Yemen", 14.7978, 42.9544, 3.0),
        CityInfo("إب", "Ibb", "اليمن", "Yemen", 13.9722, 44.1792, 3.0),
        CityInfo("ذمار", "Dhamar", "اليمن", "Yemen", 14.5427, 44.4011, 3.0),
        CityInfo("مأرب", "Marib", "اليمن", "Yemen", 15.4619, 45.3253, 3.0),

        // Saudi Arabia
        CityInfo("مكة المكرمة", "Makkah", "المملكة العربية السعودية", "Saudi Arabia", 21.4225, 39.8262, 3.0),
        CityInfo("المدينة المنورة", "Madinah", "المملكة العربية السعودية", "Saudi Arabia", 24.4672, 39.6111, 3.0),
        CityInfo("الرياض", "Riyadh", "المملكة العربية السعودية", "Saudi Arabia", 24.7136, 46.6753, 3.0),
        CityInfo("جدة", "Jeddah", "المملكة العربية السعودية", "Saudi Arabia", 21.5433, 39.1728, 3.0),
        CityInfo("الدمام", "Dammam", "المملكة العربية السعودية", "Saudi Arabia", 26.4207, 50.0888, 3.0),
        CityInfo("أبها", "Abha", "المملكة العربية السعودية", "Saudi Arabia", 18.2164, 42.5053, 3.0),
        CityInfo("تبوك", "Tabuk", "المملكة العربية السعودية", "Saudi Arabia", 28.3998, 36.5668, 3.0),
        CityInfo("بريدة", "Buraydah", "المملكة العربية السعودية", "Saudi Arabia", 26.3260, 43.9750, 3.0),

        // Egypt
        CityInfo("القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, 3.0),
        CityInfo("الإسكندرية", "Alexandria", "مصر", "Egypt", 31.2001, 29.9187, 3.0),
        CityInfo("الجيزة", "Giza", "مصر", "Egypt", 30.0131, 31.2089, 3.0),
        CityInfo("المنصورة", "Mansoura", "مصر", "Egypt", 31.0413, 31.3785, 3.0),
        CityInfo("أسوان", "Aswan", "مصر", "Egypt", 24.0889, 32.8998, 3.0),
        CityInfo("الأقصر", "Luxor", "مصر", "Egypt", 25.6872, 32.6396, 3.0),

        // UAE
        CityInfo("دبي", "Dubai", "الإمارات العربية المتحدة", "United Arab Emirates", 25.2048, 55.2708, 4.0),
        CityInfo("أبوظبي", "Abu Dhabi", "الإمارات العربية المتحدة", "United Arab Emirates", 24.4539, 54.3773, 4.0),
        CityInfo("الشارقة", "Sharjah", "الإمارات العربية المتحدة", "United Arab Emirates", 25.3463, 55.4209, 4.0),
        CityInfo("العين", "Al Ain", "الإمارات العربية المتحدة", "United Arab Emirates", 24.1302, 55.8023, 4.0),

        // Kuwait
        CityInfo("الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, 3.0),
        CityInfo("الجهراء", "Al Jahra", "الكويت", "Kuwait", 29.3375, 47.6581, 3.0),

        // Qatar
        CityInfo("الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, 3.0),
        CityInfo("الريان", "Al Rayyan", "قطر", "Qatar", 25.2917, 51.4244, 3.0)
    )

    val countries = listOf(
        CountryInfo("اليمن", "Yemen"),
        CountryInfo("المملكة العربية السعودية", "Saudi Arabia"),
        CountryInfo("مصر", "Egypt"),
        CountryInfo("الإمارات العربية المتحدة", "United Arab Emirates"),
        CountryInfo("الكويت", "Kuwait"),
        CountryInfo("قطر", "Qatar")
    )

    private val _selectedCity = MutableStateFlow(loadSelectedCity())
    val selectedCity: StateFlow<CityInfo> = _selectedCity.asStateFlow()

    private val _selectedCountry = MutableStateFlow(loadSelectedCountry())
    val selectedCountry: StateFlow<CountryInfo> = _selectedCountry.asStateFlow()

    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val _iqamahDurations = MutableStateFlow(loadIqamahDurations())
    val iqamahDurations: StateFlow<Map<String, Int>> = _iqamahDurations.asStateFlow()

    private val _useGps = MutableStateFlow(prefs.getBoolean("use_gps", false))
    val useGps: StateFlow<Boolean> = _useGps.asStateFlow()

    private val _gpsCoordinates = MutableStateFlow<Pair<Double, Double>?>(
        if (prefs.getBoolean("use_gps", false)) {
            Pair(
                prefs.getFloat("gps_lat", 12.77957f).toDouble(),
                prefs.getFloat("gps_lon", 45.03852f).toDouble()
            )
        } else null
    )
    val gpsCoordinates: StateFlow<Pair<Double, Double>?> = _gpsCoordinates.asStateFlow()

    private var tickerJob: Job? = null
    private var cachedTimings: CachedPrayerTimes? = null
    private var lastLoadedDayOfYear: Int = -1

    init {
        loadTimingsForSelectedCity()
        startTicker()
    }

    fun selectCity(city: CityInfo) {
        viewModelScope.launch {
            _selectedCity.value = city
            saveSelectedCity(city)
            _useGps.value = false
            _gpsCoordinates.value = null
            prefs.edit().putBoolean("use_gps", false).apply()
            loadTimingsForSelectedCity()
        }
    }

    fun enableGps(lat: Double, lon: Double) {
        val oldLat = prefs.getFloat("gps_lat", 0f).toDouble()
        val oldLon = prefs.getFloat("gps_lon", 0f).toDouble()
        val isFirstTime = !prefs.getBoolean("has_saved_gps", false)
        val isSignificantChange = Math.abs(oldLat - lat) > 0.01 || Math.abs(oldLon - lon) > 0.01
        
        prefs.edit()
            .putBoolean("use_gps", true)
            .putBoolean("has_saved_gps", true)
            .putFloat("gps_lat", lat.toFloat())
            .putFloat("gps_lon", lon.toFloat())
            .apply()
            
        _useGps.value = true
        _gpsCoordinates.value = Pair(lat, lon)
        
        if (isFirstTime || isSignificantChange || _uiState.value !is PrayerUiState.Success) {
            loadTimingsForSelectedCity()
        }
    }

    fun hasSavedGps(): Boolean {
        return prefs.getBoolean("has_saved_gps", false)
    }

    fun disableGps() {
        prefs.edit().putBoolean("use_gps", false).apply()
        _useGps.value = false
        _gpsCoordinates.value = null
        loadTimingsForSelectedCity()
    }

    fun updateIqamahDuration(prayerName: String, minutes: Int) {
        _iqamahDurations.value = _iqamahDurations.value.toMutableMap().apply {
            put(prayerName, minutes)
        }
        prefs.edit().putInt("iqamah_$prayerName", minutes).apply()
    }

    fun refresh() {
        loadTimingsForSelectedCity()
    }

    private fun loadTimingsForSelectedCity() {
        viewModelScope.launch {
            _uiState.value = PrayerUiState.Loading
            try {
                val cal = Calendar.getInstance()
                val timings = if (_useGps.value) {
                    val lat = prefs.getFloat("gps_lat", 12.77957f).toDouble()
                    val lon = prefs.getFloat("gps_lon", 45.03852f).toDouble()
                    val tz = TimeZone.getDefault().rawOffset / 3600000.0
                    repository.getPrayerTimes(
                        calendar = cal,
                        cityName = "GPS Location",
                        cityNameAr = "موقع GPS تلقائي",
                        countryName = "GPS",
                        latitude = lat,
                        longitude = lon,
                        timezone = tz
                    )
                } else {
                    val city = _selectedCity.value
                    repository.getPrayerTimes(
                        calendar = cal,
                        cityName = city.nameEn,
                        cityNameAr = city.nameAr,
                        countryName = city.countryEn,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        timezone = city.timezone
                    )
                }
                cachedTimings = timings
                lastLoadedDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                updateStateWithTime(System.currentTimeMillis())
            } catch (e: Exception) {
                _uiState.value = PrayerUiState.Error("فشل تحميل المواقيت: ${e.localizedMessage}")
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                
                // Automatic daily reload at midnight
                val cal = Calendar.getInstance()
                if (cachedTimings != null && cal.get(Calendar.DAY_OF_YEAR) != lastLoadedDayOfYear) {
                    loadTimingsForSelectedCity()
                } else {
                    updateStateWithTime(now)
                }
                delay(1000)
            }
        }
    }

    private fun updateStateWithTime(currentTimeMillis: Long) {
        val timings = cachedTimings ?: return
        val currentCity = _selectedCity.value
        
        // Format current time with Arabic digits
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale("ar"))
        val currentTimeStr = timeFormat.format(Date(currentTimeMillis))

        val countdown = CountdownManager.calculateCountdownState(
            currentTimeMillis = currentTimeMillis,
            timings = timings,
            iqamahMinutesMap = _iqamahDurations.value
        )

        _uiState.value = PrayerUiState.Success(
            timings = timings,
            countdownState = countdown,
            currentTime = currentTimeStr,
            selectedCityNameAr = if (_useGps.value) "موقع GPS تلقائي" else currentCity.nameAr,
            isOfflineMode = timings.isFromCache
        )
    }

    fun selectCountry(country: CountryInfo) {
        viewModelScope.launch {
            _selectedCountry.value = country
            val countryCities = allCities.filter { it.countryEn == country.nameEn }
            if (countryCities.isNotEmpty()) {
                selectCity(countryCities[0])
            }
        }
    }

    private fun loadSelectedCity(): CityInfo {
        val nameEn = prefs.getString("city_en", "Aden") ?: "Aden"
        val found = allCities.find { it.nameEn == nameEn }
        return found ?: allCities[0]
    }

    private fun loadSelectedCountry(): CountryInfo {
        val city = _selectedCity.value
        return countries.find { it.nameEn == city.countryEn } ?: countries[0]
    }

    private fun saveSelectedCity(city: CityInfo) {
        prefs.edit().putString("city_en", city.nameEn).apply()
        // Synchronize selected country
        val country = countries.find { it.nameEn == city.countryEn }
        if (country != null) {
            _selectedCountry.value = country
        }
    }

    private fun loadIqamahDurations(): Map<String, Int> {
        return mapOf(
            "الفجر" to prefs.getInt("iqamah_الفجر", 20),
            "الظهر" to prefs.getInt("iqamah_الظهر", 15),
            "العصر" to prefs.getInt("iqamah_العصر", 15),
            "المغرب" to prefs.getInt("iqamah_المغرب", 10),
            "العشاء" to prefs.getInt("iqamah_العشاء", 15)
        )
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
