package com.example.model

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.InputStreamReader

object ZikrData {
    private val _categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    val categoriesFlow: StateFlow<List<Category>> = _categoriesFlow.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()

    fun getImageForCategory(title: String): Int {
        return when {
            title.contains("الصباح") -> com.example.R.drawable.morning_bg
            title.contains("المساء") -> com.example.R.drawable.evening_bg
            title.contains("المسجد") || title.contains("الصلاة") || title.contains("الركوع") || title.contains("السجود") -> com.example.R.drawable.mosque_bg
            title.contains("النوم") || title.contains("الرؤيا") || title.contains("استيقاظ") -> com.example.R.drawable.sleep_bg
            title.contains("الوضوء") || title.contains("الخلاء") || title.contains("ماء") -> com.example.R.drawable.water_bg
            title.contains("طعام") || title.contains("شراب") || title.contains("إفطار") || title.contains("صائم") -> com.example.R.drawable.food_bg
            title.contains("سفر") || title.contains("دخول") || title.contains("ركوب") -> com.example.R.drawable.travel_bg
            title.contains("الهم") || title.contains("الحزن") || title.contains("كرب") || title.contains("المرض") -> com.example.R.drawable.sadness_bg
            title.contains("قرآن") || title.contains("تلاوة") -> com.example.R.drawable.quran_bg
            else -> com.example.R.drawable.home_bg_1782755258565
        }
    }

    fun getIconForCategory(title: String): ImageVector {
        return when {
            title.contains("الصباح") -> Icons.Outlined.WbSunny
            title.contains("المساء") -> Icons.Outlined.NightsStay
            title.contains("النوم") || title.contains("الرؤيا") || title.contains("وحشة") -> Icons.Outlined.Bed
            title.contains("الاستيقاظ") -> Icons.Outlined.WbTwilight
            title.contains("لبس") || title.contains("الثوب") -> Icons.Outlined.Checkroom
            title.contains("الخلاء") || title.contains("الحمام") -> Icons.Outlined.Wash
            title.contains("الوضوء") -> Icons.Outlined.WaterDrop
            title.contains("المنزل") -> Icons.Outlined.Home
            title.contains("المسجد") || title.contains("قنوت") || title.contains("وتر") -> Icons.Outlined.Mosque
            title.contains("الآذان") -> Icons.AutoMirrored.Outlined.VolumeUp
            title.contains("الصلاة") || title.contains("الركوع") || title.contains("السجود") || title.contains("التشهد") -> Icons.Outlined.SelfImprovement
            title.contains("السفر") || title.contains("دخول") || title.contains("الرجوع") || title.contains("ركوب") -> Icons.AutoMirrored.Outlined.DirectionsWalk
            title.contains("الهم") || title.contains("الحزن") || title.contains("الكرب") -> Icons.Outlined.MoodBad
            title.contains("المرض") || title.contains("مريض") -> Icons.Outlined.LocalHospital
            title.contains("الميت") || title.contains("قبر") || title.contains("عزاء") -> Icons.Outlined.AccountBalance
            title.contains("الريح") || title.contains("الرعد") || title.contains("مطر") -> Icons.Outlined.Cloud
            title.contains("طعام") || title.contains("شراب") || title.contains("صائم") || title.contains("إفطار") -> Icons.Outlined.Restaurant
            title.contains("تزوج") -> Icons.Outlined.FamilyRestroom
            title.contains("غضب") -> Icons.Outlined.WarningAmber
            title.contains("قرآن") -> Icons.AutoMirrored.Outlined.MenuBook
            title.contains("استغفار") || title.contains("توبة") || title.contains("تسبيح") -> Icons.Outlined.VolunteerActivism
            else -> Icons.AutoMirrored.Outlined.MenuBook // generic icon
        }
    }

    suspend fun loadData(context: Context) {
        if (_categoriesFlow.value.isNotEmpty()) return

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("hisn.json")
                val isr = InputStreamReader(inputStream, "UTF-8")
                val jsonString = isr.readText()
                isr.close()

                val jsonArray = org.json.JSONArray(jsonString)
                val loadedCategories = mutableListOf<Category>()
                var categoryId = 1

                for (c in 0 until jsonArray.length()) {
                    val categoryObj = jsonArray.getJSONObject(c)
                    val categoryName = categoryObj.getString("title")
                    val array = categoryObj.getJSONArray("azkar")
                    val azkarList = mutableListOf<Zikr>()

                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)

                        var count = 1
                        val countStr = item.optString("count", "1")
                        if (countStr.isNotEmpty()) {
                            try {
                                count = countStr.toInt()
                            } catch (e: Exception) {
                                count = 1
                            }
                        }

                        azkarList.add(
                            Zikr(
                                id = item.optInt("Id", i),
                                text = item.optString("zekr", ""),
                                count = count,
                                instructions = item.optString("reference", "").takeIf { it.isNotBlank() }
                            )
                        )
                    }

                    loadedCategories.add(
                        Category(
                            id = categoryId++,
                            title = categoryName,
                            azkar = azkarList
                        )
                    )
                }
                // Add the special 100-time azkar to Morning and Evening categories
                val extraAzkar = listOf(
                    Zikr(id = 1001, text = "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد، وهو على كل شيء قدير", count = 100, instructions = null),
                    Zikr(id = 1002, text = "سبحان الله وبحمده", count = 100, instructions = null),
                    Zikr(id = 1003, text = "أستغفر الله العظيم وأتوب إليه", count = 100, instructions = null),
                    Zikr(id = 1004, text = "سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر", count = 100, instructions = null)
                )

                val updatedCategories = loadedCategories.map { category ->
                    val sortedAzkar = category.azkar.sortedByDescending { zikr ->
                        // Put Quranic verses first (usually identified by "سورة" or "آية" or particular ID patterns)
                        val isQuran = zikr.text.contains("﴿") || zikr.text.contains("﴾") || 
                                     zikr.text.contains("سورة") || zikr.text.contains("آية") ||
                                     zikr.instructions?.contains("البقرة") == true
                        if (isQuran) 10 else 0
                    }
                    
                    if (category.title.contains("الصباح") || category.title.contains("المساء")) {
                        category.copy(azkar = sortedAzkar + extraAzkar)
                    } else {
                        category.copy(azkar = sortedAzkar)
                    }
                }
                
                _categoriesFlow.value = updatedCategories.sortedBy { it.id }

            } catch (e: Exception) {
                _errorFlow.value = e.stackTraceToString()
                e.printStackTrace()
            }
        }
    }
}
