package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.model.ZikrData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisnAlMuslimApp(intent: android.content.Intent? = null, onIntentHandled: () -> Unit = {}) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val categories by ZikrData.categoriesFlow.collectAsState()
    val error by ZikrData.errorFlow.collectAsState()
    
    val settingsManager = remember { com.example.model.SettingsManager.getInstance(context) }
    val appBackgroundId by settingsManager.appBackgroundId.collectAsState()

    LaunchedEffect(intent, categories) {
        if (intent?.data != null && categories.isNotEmpty()) {
            try {
                val uriStr = intent.data.toString()
                if (uriStr.startsWith("hisn://category/")) {
                    val categoryIdStr = uriStr.substringAfter("hisn://category/").substringBefore("?")
                    val categoryId = categoryIdStr.toIntOrNull()
                    val fromAlarm = uriStr.contains("fromAlarm=true")
                    
                    if (categoryId != null) {
                        navController.navigate("categories") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                        navController.navigate("category/$categoryId?fromAlarm=$fromAlarm") {
                            launchSingleTop = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onIntentHandled()
        }
    }

    LaunchedEffect(Unit) {
        ZikrData.loadData(context)
    }

    if (error != null) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Red.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.lazy.LazyColumn { item { androidx.compose.material3.Text(error ?: "", color = androidx.compose.ui.graphics.Color.White) } }
        }
        return
    }

    Crossfade(targetState = categories.isNotEmpty(), label = "AppLoad") { isLoaded ->
        if (isLoaded) {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    if (currentRoute == "categories" || currentRoute == "favorites" || currentRoute == "settings") {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            color = Color(0xFF1A1A1A).copy(alpha = 0.95f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            shadowElevation = 24.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val items = listOf(
                                    Triple("categories", "الرئيسية", Icons.Default.Home),
                                    Triple("favorites", "المفضلة", Icons.Default.Favorite),
                                    Triple("settings", "الإعدادات", Icons.Default.Settings)
                                )
                                
                                items.forEach { (route, label, icon) ->
                                    val isSelected = currentRoute == route
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                navController.navigate(route) {
                                                    popUpTo("categories") { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.foundation.layout.Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = label,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = Color.White.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // Global persistent wallpaper background (retained in both Light & Dark modes)
                    Image(
                        painter = painterResource(id = appBackgroundId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Adaptive Readability Overlay (Subtle neutral dark scrim to preserve original background colors and shapes in their true form)
                    val overlayColor = Color.Black.copy(alpha = 0.15f)
                    Box(modifier = Modifier.fillMaxSize().background(overlayColor))
                    
                    // Main navigation content
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            LaunchedEffect(categories) {
                                if (categories.isNotEmpty()) {
                                    kotlinx.coroutines.delay(2000)
                                    navController.navigate("categories") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = androidx.compose.ui.res.painterResource(id = appBackgroundId),
                                    contentDescription = "Splash Background",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo_1782755246317),
                                    contentDescription = "App Logo",
                                    modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(1f),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        composable("categories") {
                            CategoryListScreen(
                                categories = categories,
                                onCategoryClick = { categoryId ->
                                    navController.navigate("category/$categoryId")
                                },
                                onPrayerClick = { navController.navigate("prayer") },
                                onFavoritesClick = { navController.navigate("favorites") },
                                onAlarmClick = { navController.navigate("alarm") },
                                onQuranClick = { surahName, text ->
                                    val route = if (text != null) {
                                        "quran/${android.net.Uri.encode(surahName)}?text=${android.net.Uri.encode(text)}"
                                    } else {
                                        "quran/${android.net.Uri.encode(surahName)}"
                                    }
                                    navController.navigate(route)
                                }
                            )
                        }
                        composable("alarm") {
                            AzkarAlarmScreen(
                                categories = categories,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "category/{categoryId}?fromAlarm={fromAlarm}",
                            deepLinks = listOf(
                                androidx.navigation.navDeepLink { uriPattern = "hisn://category/{categoryId}?fromAlarm={fromAlarm}" }
                            )
                        ) { backStackEntry ->
                            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull()
                            val fromAlarm = backStackEntry.arguments?.getString("fromAlarm")?.toBoolean() ?: false
                            val category = categories.find { it.id == categoryId }

                            if (categories.isNotEmpty()) {
                                if (category != null) {
                                    ZikrListScreen(
                                        category = category,
                                        fromAlarm = fromAlarm,
                                        onBackClick = { navController.popBackStack() },
                                        onQuranClick = { surahName, text ->
                                            val route = if (text != null) {
                                                "quran/${android.net.Uri.encode(surahName)}?text=${android.net.Uri.encode(text)}"
                                            } else {
                                                "quran/${android.net.Uri.encode(surahName)}"
                                            }
                                            navController.navigate(route)
                                        }
                                    )
                                } else {
                                    LaunchedEffect(Unit) {
                                        android.widget.Toast.makeText(context, "الباب غير موجود", android.widget.Toast.LENGTH_SHORT).show()
                                        navController.navigate("categories") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                        }
                                    }
                                }
                            } else {
                                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                        composable("prayer") {
                            PrayerTimesScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable("favorites") {
                            FavoritesScreen(
                                categories = categories,
                                onBackClick = { navController.popBackStack() },
                                onQuranClick = { surahName, text ->
                                    val route = if (text != null) {
                                        "quran/${android.net.Uri.encode(surahName)}?text=${android.net.Uri.encode(text)}"
                                    } else {
                                        "quran/${android.net.Uri.encode(surahName)}"
                                    }
                                    navController.navigate(route)
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(onBackClick = { navController.popBackStack() })
                        }
                        composable(
                            route = "quran/{surahName}?text={text}",
                            arguments = listOf(
                                androidx.navigation.navArgument("surahName") { type = androidx.navigation.NavType.StringType },
                                androidx.navigation.navArgument("text") { 
                                    type = androidx.navigation.NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val surahName = backStackEntry.arguments?.getString("surahName") ?: "سورة"
                            val text = backStackEntry.arguments?.getString("text")
                            QuranReadingScreen(
                                title = surahName,
                                customText = text,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    } else {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Background Pattern
                Image(
                    painter = painterResource(id = com.example.R.drawable.home_bg_1782755258565),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f
                )
                
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                    androidx.compose.material3.Text(
                        text = "جاري التحميل...", 
                        color = MaterialTheme.colorScheme.secondary, 
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("سيتم إضافة $title قريباً", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

