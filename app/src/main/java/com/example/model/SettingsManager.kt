package com.example.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.R

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0)) // 0=Auto, 1=Light, 2=Dark
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _textSizeMultiplier = MutableStateFlow(prefs.getFloat("text_size_multiplier", 1.0f))
    val textSizeMultiplier: StateFlow<Float> = _textSizeMultiplier.asStateFlow()

    private val _appBackgroundId = MutableStateFlow(prefs.getInt("app_background_id", R.drawable.home_bg_1782755258565))
    val appBackgroundId: StateFlow<Int> = _appBackgroundId.asStateFlow()

    private val _appAccentColorIndex = MutableStateFlow(prefs.getInt("app_accent_color_index", 0)) // 0=Gold, 1=Dark Blue, 2=Light Blue, 3=Red, 4=Green, 5=Gray
    val appAccentColorIndex: StateFlow<Int> = _appAccentColorIndex.asStateFlow()

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setTextSizeMultiplier(multiplier: Float) {
        prefs.edit().putFloat("text_size_multiplier", multiplier).apply()
        _textSizeMultiplier.value = multiplier
    }

    fun setAppBackgroundId(resId: Int) {
        prefs.edit().putInt("app_background_id", resId).apply()
        _appBackgroundId.value = resId
    }

    fun setAppAccentColorIndex(index: Int) {
        prefs.edit().putInt("app_accent_color_index", index).apply()
        _appAccentColorIndex.value = index
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
