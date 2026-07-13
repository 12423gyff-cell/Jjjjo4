package com.example.ui

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.example.model.Category
import com.example.model.Zikr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    categories: List<Category>,
    onBackClick: () -> Unit,
    onQuranClick: (String, String?) -> Unit = { _, _ -> },
    viewModel: ZikrViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as Application)
    )
) {
    BackHandler(onBack = onBackClick)
    val favoritesList by viewModel.favoritesFlow.collectAsState()
    val progressList by viewModel.progressFlow.collectAsState()
    val context = LocalContext.current
    val settingsManager = remember { com.example.model.SettingsManager.getInstance(context) }
    val textSizeMultiplier by settingsManager.textSizeMultiplier.collectAsState()
    val isPlaying by viewModel.ttsManager.isPlaying.collectAsState()
    val currentZikrId by viewModel.ttsManager.currentZikrId.collectAsState()

    // Find all favorited azkar with their parent category title
    val favoritedItems = remember(favoritesList, categories) {
        val list = mutableListOf<Pair<Zikr, String>>()
        val favoriteIds = favoritesList.map { it.zikrId }.toSet()
        for (category in categories) {
            for (zikr in category.azkar) {
                if (favoriteIds.contains(zikr.id)) {
                    list.add(Pair(zikr, category.title))
                }
            }
        }
        list
    }

    val primaryColor = MaterialTheme.colorScheme.primary // Blue Whale
    val secondaryColor = MaterialTheme.colorScheme.secondary // Dynamic secondary accent color
    val surfaceColor = MaterialTheme.colorScheme.background // Deep slate blue background
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "المفضلة",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = secondaryColor,
                    navigationIconContentColor = secondaryColor
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
        ) {
            if (favoritedItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = secondaryColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "قائمتك المفضلة فارغة حالياً",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "احفظ الأذكار والآيات التي تقرؤها بكثرة لتجدها هنا بسرعة وسهولة.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoritedItems, key = { it.first.id }) { (zikr, categoryTitle) ->
                        val savedProgress = progressList.find { it.zikrId == zikr.id }?.count ?: 0
                        
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Category Badge Header
                            Surface(
                                color = primaryColor,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = categoryTitle,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = com.example.ui.theme.ArefRuqaaFontFamily,
                                        fontSize = 20.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                                    ),
                                    color = secondaryColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            ZikrLuxuryCard(
                                zikr = zikr,
                                textSizeMultiplier = textSizeMultiplier,
                                categoryTitle = categoryTitle,
                                savedProgress = savedProgress,
                                isCurrentlyPlaying = currentZikrId == zikr.id,
                                isFavorite = true,
                                onProgressUpdate = { newCount ->
                                    viewModel.saveProgress(zikr.id, newCount)
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
            }
        }
    }
}
