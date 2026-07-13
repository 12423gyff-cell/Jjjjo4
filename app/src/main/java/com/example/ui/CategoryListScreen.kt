package com.example.ui
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Category
import com.example.model.Zikr
import com.example.model.ZikrData
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.ui.theme.ArefRuqaaFontFamily

fun String.removeTashkeel(): String {
    return this.replace(Regex("[\u0617-\u061A\u064B-\u0652]"), "")
}

fun String.normalizeArabic(): String {
    var text = this.replace(Regex("[\u0617-\u061A\u064B-\u0652]"), "") // Remove tashkeel
    text = text.replace(Regex("[أإآٱ]"), "ا") // Normalize Alif
    text = text.replace(Regex("ة"), "ه") // Normalize Teh Marbuta
    text = text.replace(Regex("ى"), "ي") // Normalize Alif Maqsura to Yeh
    return text
}

data class SearchResult(
    val category: Category,
    val matchedZikr: Zikr? = null,
    val isTitleMatch: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    categories: List<Category>,
    onCategoryClick: (Int) -> Unit,
    onPrayerClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onQuranClick: (String, String?) -> Unit = { _, _ -> },
    viewModel: ZikrViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }
    var showTasbeehDialog by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.example.alarm.AlarmRepository.load(context)
    }
    val alarms by com.example.alarm.AlarmRepository.alarmsFlow.collectAsState()
    val nextAlarm = remember(alarms) {
        alarms.filter { it.isEnabled }
              .sortedBy { it.hour * 60 + it.minute }
              .firstOrNull()
    }
    
    val progressList by viewModel.progressFlow.collectAsState()
    val favoritesList by viewModel.favoritesFlow.collectAsState()
    val isPlaying by viewModel.ttsManager.isPlaying.collectAsState()
    val currentZikrId by viewModel.ttsManager.currentZikrId.collectAsState()
    
    val settingsManager = remember { com.example.model.SettingsManager.getInstance(context) }
    val appBackgroundId by settingsManager.appBackgroundId.collectAsState()
    val textSizeMultiplier by settingsManager.textSizeMultiplier.collectAsState()
    val accentColorIndex by settingsManager.appAccentColorIndex.collectAsState()
    
    val filters = listOf("الكل", "اليومية", "الصلاة", "منوعات")
    
    val filteredCategories = remember(categories, selectedFilter) {
        when (selectedFilter) {
            "اليومية" -> categories.filter { 
                it.title.contains("صباح") || it.title.contains("مساء") || 
                it.title.contains("استيقاظ") || it.title.contains("نوم") ||
                it.title.contains("يوم")
            }
            "الصلاة" -> categories.filter { 
                it.title.contains("صلاة") || it.title.contains("وضوء") || 
                it.title.contains("أذان") || it.title.contains("مسجد")
            }
            "منوعات" -> categories.filter { 
                !it.title.contains("صباح") && !it.title.contains("مساء") && 
                !it.title.contains("استيقاظ") && !it.title.contains("نوم") &&
                !it.title.contains("صلاة") && !it.title.contains("وضوء") && 
                !it.title.contains("أذان") && !it.title.contains("مسجد")
            }
            else -> categories
        }
    }

    val matchedCategories = remember(searchQuery, filteredCategories) {
        if (searchQuery.isBlank()) return@remember emptyList<Category>()
        val query = searchQuery.normalizeArabic().trim()
        if (query.isEmpty()) return@remember emptyList<Category>()
        filteredCategories.filter { it.title.normalizeArabic().contains(query, ignoreCase = true) }
    }

    val matchedAzkar = remember(searchQuery, filteredCategories) {
        if (searchQuery.isBlank()) return@remember emptyList<Pair<Zikr, Category>>()
        val query = searchQuery.normalizeArabic().trim()
        if (query.isEmpty()) return@remember emptyList<Pair<Zikr, Category>>()
        val list = mutableListOf<Pair<Zikr, Category>>()
        filteredCategories.forEach { category ->
            category.azkar.forEach { zikr ->
                if (zikr.text.normalizeArabic().contains(query, ignoreCase = true)) {
                    list.add(Pair(zikr, category))
                }
            }
        }
        list
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAlarmClick,
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Alarm", modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.header_bg_1782755272017),
                        contentDescription = "Header Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 16.dp, bottom = 16.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "ابحث عن قسم أو ذكر معين...",
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Prayer Times Shortcut
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { onPrayerClick() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10202B)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🕌", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مواقيت الصلاة",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Favorites Shortcut
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { onFavoritesClick() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10202B)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "المفضلة",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                item {
                    if (nextAlarm != null) {
                        val amPm = if (nextAlarm.hour < 12) "AM" else "PM"
                        val displayHour = if (nextAlarm.hour % 12 == 0) 12 else nextAlarm.hour % 12
                        val displayMinute = String.format(java.util.Locale.US, "%02d", nextAlarm.minute)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .clickable(onClick = onAlarmClick),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("المنبه القادم", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
                                    Text(nextAlarm.linkedZikrTitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                                }
                                Text("$displayHour:$displayMinute $amPm", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)
                    ) {
                        items(filters) { filterName ->
                            val isSelected = filterName == selectedFilter
                            Surface(
                                modifier = Modifier.clickable { 
                                    selectedFilter = filterName 
                                    searchQuery = "" 
                                },
                                shape = RoundedCornerShape(25.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = filterName,
                                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 25.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                if (searchQuery.isNotBlank()) {
                    if (matchedCategories.isNotEmpty()) {
                        item {
                            Text(
                                text = "الأقسام المطابقة",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        items(matchedCategories) { category ->
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                CategoryItem(category, onClick = { onCategoryClick(category.id) })
                            }
                        }
                    }

                    if (matchedAzkar.isNotEmpty()) {
                        item {
                            Text(
                                text = "الأذكار المطابقة",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                        items(matchedAzkar, key = { "search_${it.first.id}" }) { (zikr, category) ->
                            val savedProgress = progressList.find { it.zikrId == zikr.id }?.count ?: 0
                            val isFav = favoritesList.any { it.zikrId == zikr.id }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF1A3140),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "الباب: ${category.title}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = ArefRuqaaFontFamily,
                                            fontSize = 20.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                
                                ZikrLuxuryCard(
                                    zikr = zikr,
                                    textSizeMultiplier = textSizeMultiplier,
                                    categoryTitle = category.title,
                                    savedProgress = savedProgress,
                                    isCurrentlyPlaying = currentZikrId == zikr.id && isPlaying,
                                    isFavorite = isFav,
                                    onProgressUpdate = { count ->
                                        viewModel.saveProgress(zikr.id, count)
                                    },
                                    onPlayClick = {
                                        if (currentZikrId == zikr.id && isPlaying) {
                                            viewModel.ttsManager.stop()
                                        } else {
                                            viewModel.ttsManager.play(zikr.text, zikr.id)
                                        }
                                    },
                                    onFavoriteToggle = {
                                        viewModel.toggleFavorite(zikr.id)
                                    },
                                    onQuranClick = onQuranClick
                                )
                            }
                        }
                    }

                    if (matchedCategories.isEmpty() && matchedAzkar.isEmpty()) {
                        item {
                            Text(
                                text = "لا توجد نتائج مطابقة لبحثك",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    items(filteredCategories) { category ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                            CategoryItem(category, onClick = { onCategoryClick(category.id) })
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TasbeehDialog(onDismiss: () -> Unit) {
    var count by remember { mutableIntStateOf(0) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("عداد التسبيح", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Text("$count", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { if (count > 0) count-- }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))) {
                        Text("-", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Button(onClick = { count++ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                        Text("+", color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { count = 0 }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f))) {
                        Text("0", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("إغلاق", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun ActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        }
    }
}


@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    val scenicBg = getScenicBgForCategory(category.title)
    val (engSub, arSub) = getCategorySubtext(category.title)
    val cardBgColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Right side text area (occupies 55% of the card)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1.2f)
                        .background(cardBgColor)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ZikrCategoryIcon(
                                title = category.title,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 24.sp,
                                    fontFamily = ArefRuqaaFontFamily
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        Column {
                            Text(
                                text = engSub,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Light
                                ),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = arSub,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Left side image area with gradient fade overlay (occupies 45% of the card)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.9f)
                ) {
                    Image(
                        painter = painterResource(id = scenicBg),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Smooth gradient fade from right to left to blend the image with the text card background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        cardBgColor.copy(alpha = 0.5f),
                                        cardBgColor
                                    )
                                )
                            )
                    )

                    // Navigation Arrow Overlay on the left edge
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumHeaderBanner(accentColorIndex: Int, modifier: Modifier = Modifier) {
    val gradientColors = when (accentColorIndex) {
        1 -> listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF3B82F6)) // Dark Blue wave shades (Dark -> Medium -> Light)
        2 -> listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6), Color(0xFF93C5FD)) // Light Blue wave shades
        3 -> listOf(Color(0xFF7F1D1D), Color(0xFFDC2626), Color(0xFFF87171)) // Red wave shades
        4 -> listOf(Color(0xFF14532D), Color(0xFF16A34A), Color(0xFF4ADE80)) // Green wave shades
        5 -> listOf(Color(0xFF1F2937), Color(0xFF4B5563), Color(0xFF9CA3AF)) // Gray wave shades
        else -> listOf(Color(0xFFB45309), Color(0xFFFFDE95), Color(0xFFFFF0D3)) // Gold wave shades
    }

    val gradientBrush = Brush.linearGradient(
        colors = gradientColors
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "حِصْنُ المُسْلِمِ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "أدعية وأذكار من الكتاب والسنة",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
    val scenicBg = getScenicBgForCategory(result.category.title)
    val (engSub, arSub) = getCategorySubtext(result.category.title)
    val cardBgColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Right side text area (occupies 55% of the card)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1.2f)
                        .background(cardBgColor)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ZikrCategoryIcon(
                                title = result.category.title,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = result.category.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 24.sp,
                                    fontFamily = ArefRuqaaFontFamily
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        Column {
                            Text(
                                text = engSub,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Light
                                ),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (result.matchedZikr != null) {
                                Text(
                                    text = result.matchedZikr.text,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = arSub,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Left side image area with gradient fade overlay (occupies 45% of the card)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.9f)
                ) {
                    Image(
                        painter = painterResource(id = scenicBg),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Smooth gradient fade from right to left to blend the image with the text card background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        cardBgColor.copy(alpha = 0.5f),
                                        cardBgColor
                                    )
                                )
                            )
                    )

                    // Navigation Arrow Overlay on the left edge
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

