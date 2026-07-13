package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SettingsManager
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: ZikrViewModel = viewModel()
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    
    val themeMode by settingsManager.themeMode.collectAsState()
    val textSizeMultiplier by settingsManager.textSizeMultiplier.collectAsState()
    val appBackgroundId by settingsManager.appBackgroundId.collectAsState()
    val accentColorIndex by settingsManager.appAccentColorIndex.collectAsState()
    
    BackHandler(onBack = onBackClick)
    Scaffold(
        containerColor = Color.Black.copy(alpha = 0.6f),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الإعدادات", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // About App Section with Logo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(100.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo_1782755246317),
                        contentDescription = "App Logo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                val textGradientBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = when (accentColorIndex) {
                        1 -> listOf(Color(0xFF60A5FA), Color(0xFF1D4ED8), Color(0xFF1E3A8A)) // Blue wave shades
                        2 -> listOf(Color(0xFF93C5FD), Color(0xFF3B82F6), Color(0xFF1D4ED8)) // Light Blue wave shades
                        3 -> listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF7F1D1D)) // Red wave shades
                        4 -> listOf(Color(0xFF4ADE80), Color(0xFF16A34A), Color(0xFF14532D)) // Green wave shades
                        5 -> listOf(Color(0xFF9CA3AF), Color(0xFF4B5563), Color(0xFF1F2937)) // Gray wave shades
                        else -> listOf(Color(0xFFFFF0D3), Color(0xFFFFDE95), Color(0xFFB45309)) // Gold wave shades
                    }
                )
                Text(
                    text = "حِصْنُ المُسْلِمِ",
                    style = MaterialTheme.typography.displaySmall.copy(
                        brush = textGradientBrush
                    ),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "أدعية وأذكار من الكتاب والسنة",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "الإصدار 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            SettingsSection("قسم الشكل") {
                SettingsRow("الوضع الليلي", isSelected = themeMode == 2) { settingsManager.setThemeMode(2) }
                SettingsRow("الوضع الفاتح", isSelected = themeMode == 1) { settingsManager.setThemeMode(1) }
                SettingsRow("تلقائي", isSelected = themeMode == 0) { settingsManager.setThemeMode(0) }
            }

            SettingsSection("خلفية التطبيق") {
                val bgs = listOf(
                    "الخلفية الأساسية" to com.example.R.drawable.home_bg_1782755258565,
                    "الخلفية الخضراء الفاخرة" to com.example.R.drawable.bg_green_calligraphy_1783794161261,
                    "خلفية الحجر العتيق" to com.example.R.drawable.bg_vintage_gray_1783794178923,
                    "الخط العربي الدافئ" to com.example.R.drawable.bg_brown_brush_1783794190746,
                    "السجاد الإسلامي الفاخر" to com.example.R.drawable.bg_blue_carpet_1783794200379,
                    "الزخرفة الحمراء المهيبة" to com.example.R.drawable.bg_red_mandala_1783794216804,
                    "الخط الذهبي الدائري" to com.example.R.drawable.bg_dark_gold_1783794238721,
                    "البلاط الأندلسي الفاخر" to com.example.R.drawable.bg_blue_gold_tiles_1783794980161
                )
                bgs.forEach { (name, resId) ->
                    SettingsRow(name, isSelected = appBackgroundId == resId) {
                        settingsManager.setAppBackgroundId(resId)
                    }
                }
            }

            SettingsSection("ألوان التطبيق") {
                val colors = listOf(
                    "الذهبي الفاخر" to 0,
                    "الأزرق الداكن" to 1,
                    "الأزرق الفاتح" to 2,
                    "الأحمر الملكي" to 3,
                    "الأخضر الإسلامي" to 4,
                    "الرمادي الفولاذي" to 5
                )
                colors.forEach { (name, index) ->
                    SettingsRow(name, isSelected = accentColorIndex == index) {
                        settingsManager.setAppAccentColorIndex(index)
                    }
                }
            }

            SettingsSection("حجم النص") {
                SettingsRow("كبير جدا", isSelected = textSizeMultiplier == 1.4f) { settingsManager.setTextSizeMultiplier(1.4f) }
                SettingsRow("كبير", isSelected = textSizeMultiplier == 1.2f) { settingsManager.setTextSizeMultiplier(1.2f) }
                SettingsRow("افتراضي", isSelected = textSizeMultiplier == 1.0f) { settingsManager.setTextSizeMultiplier(1.0f) }
            }
            
            SettingsSection("إدارة البيانات") {
                SettingsRow("تصفير العدادات", isSelected = false) {
                    viewModel.resetCounters()
                    Toast.makeText(context, "تم تصفير جميع العدادات", Toast.LENGTH_SHORT).show()
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // For bottom nav
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.5f), // semi-transparent black background card for supreme readability
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsRow(title: String, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}
