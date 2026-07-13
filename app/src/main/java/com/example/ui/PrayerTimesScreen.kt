package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.prayer.CachedPrayerTimes
import com.example.prayer.CityInfo
import com.example.prayer.PrayerState
import com.example.prayer.PrayerUiState
import com.example.prayer.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    onBackClick: () -> Unit,
    viewModel: PrayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val iqamahDurations by viewModel.iqamahDurations.collectAsState()
    val useGps by viewModel.useGps.collectAsState()
    val gpsCoords by viewModel.gpsCoordinates.collectAsState()

    var expandedCountryDropdown by remember { mutableStateOf(false) }
    var expandedCityDropdown by remember { mutableStateOf(false) }
    var showIqamahSettingsDialog by remember { mutableStateOf(false) }

    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val hasFinePermission = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasCoarsePermission = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            isFetchingLocation = true
            fetchGpsLocation(
                context = context,
                onLocationResult = { location ->
                    isFetchingLocation = false
                    if (location != null) {
                        viewModel.enableGps(location.latitude, location.longitude)
                        locationError = null
                    } else {
                        val hasSaved = viewModel.hasSavedGps()
                        if (hasSaved) {
                            locationError = "عذراً، تعذر جلب إحداثيات موقعك الحالي عبر GPS. تم استخدام آخر موقع محفوظ."
                        } else {
                            locationError = "عذراً، تعذر جلب إحداثيات موقعك الحالي عبر GPS. تم العودة للوضع اليدوي."
                            viewModel.disableGps()
                        }
                    }
                },
                onError = { errorMsg ->
                    isFetchingLocation = false
                    val hasSaved = viewModel.hasSavedGps()
                    if (hasSaved) {
                        locationError = "$errorMsg. تم استخدام آخر موقع محفوظ."
                    } else {
                        locationError = "$errorMsg. تم العودة للوضع اليدوي."
                        viewModel.disableGps()
                    }
                }
            )
        } else {
            val hasSaved = viewModel.hasSavedGps()
            if (hasSaved) {
                locationError = "تم رفض صلاحية الموقع. تم استخدام آخر موقع محفوظ."
            } else {
                locationError = "تم رفض صلاحية الموقع. تم العودة للوضع اليدوي."
                viewModel.disableGps()
            }
        }
    }

    fun triggerGpsFetch() {
        if (hasFinePermission || hasCoarsePermission) {
            isFetchingLocation = true
            fetchGpsLocation(
                context = context,
                onLocationResult = { location ->
                    isFetchingLocation = false
                    if (location != null) {
                        viewModel.enableGps(location.latitude, location.longitude)
                        locationError = null
                    } else {
                        val hasSaved = viewModel.hasSavedGps()
                        if (hasSaved) {
                            locationError = "عذراً، تعذر جلب إحداثيات موقعك الحالي عبر GPS. تم استخدام آخر موقع محفوظ."
                        } else {
                            locationError = "عذراً، تعذر جلب إحداثيات موقعك الحالي عبر GPS. تم العودة للوضع اليدوي."
                            viewModel.disableGps()
                        }
                    }
                },
                onError = { errorMsg ->
                    isFetchingLocation = false
                    val hasSaved = viewModel.hasSavedGps()
                    if (hasSaved) {
                        locationError = "$errorMsg. تم استخدام آخر موقع محفوظ."
                    } else {
                        locationError = "$errorMsg. تم العودة للوضع اليدوي."
                        viewModel.disableGps()
                    }
                }
            )
        } else {
            launcher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(useGps) {
        if (useGps && gpsCoords == null) {
            triggerGpsFetch()
        }
    }

    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager }
    val locationListener = remember {
        object : android.location.LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                viewModel.enableGps(location.latitude, location.longitude)
                locationError = null
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                locationError = "تم إيقاف خدمات الموقع. يرجى تفعيلها."
                viewModel.disableGps()
            }
        }
    }

    DisposableEffect(useGps, hasFinePermission, hasCoarsePermission) {
        if (useGps && (hasFinePermission || hasCoarsePermission)) {
            try {
                val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
                
                val provider = when {
                    isGpsEnabled -> android.location.LocationManager.GPS_PROVIDER
                    isNetworkEnabled -> android.location.LocationManager.NETWORK_PROVIDER
                    else -> null
                }
                
                if (provider != null) {
                    locationManager.requestLocationUpdates(
                        provider,
                        10000L, // 10 seconds
                        10f,    // 10 meters
                        locationListener,
                        android.os.Looper.getMainLooper()
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("PrayerTimesScreen", "Error starting continuous location updates", e)
            }
        }
        
        onDispose {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: Exception) {
                android.util.Log.e("PrayerTimesScreen", "Error removing location updates", e)
            }
        }
    }

    // Colors - M3 Premium Theme
    val primaryColor = Color(0xFF1A3140) // Blue Whale
    val secondaryColor = MaterialTheme.colorScheme.secondary // Dynamic secondary accent color
    val accentColor = Color(0xFF371931) // Plum
    val milkSurfaceColor = Color(0xFFFFF3E5) // Milk
    val deepDarkBg = Color(0xFF0F1A22) // Luxury dark slate
    val cardBgColor = Color(0xFF162530) // Darker slate card

    // Compass state has been moved inside QiblaCompassCard to prevent high-frequency recompositions of the main screen.

    val qiblaDirection = remember(selectedCity, useGps, gpsCoords) {
        if (useGps && gpsCoords != null) {
            calculateQibla(gpsCoords!!.first, gpsCoords!!.second)
        } else {
            calculateQibla(selectedCity.latitude, selectedCity.longitude)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "مواقيت الصلاة والقبلة",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = secondaryColor)
                    }
                    IconButton(onClick = { showIqamahSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "إعدادات الإقامة", tint = secondaryColor)
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
            when (val state = uiState) {
                is PrayerUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = secondaryColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("جاري تحميل مواقيت الصلاة...", color = Color.White)
                    }
                }
                is PrayerUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                        ) {
                            Text("إعادة المحاولة", color = primaryColor)
                        }
                    }
                }
                is PrayerUiState.Success -> {
                    val timings = state.timings
                    val countdown = state.countdownState

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Background Decorations
                        IslamicScreenDecorations(
                            borderColor = secondaryColor.copy(alpha = 0.15f),
                            ornamentColor = secondaryColor.copy(alpha = 0.4f)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. GPS Location Toggle Card (Premium Islamic Design)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, if (useGps) secondaryColor.copy(alpha = 0.4f) else secondaryColor.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "تحديد موقع GPS",
                                                tint = if (useGps) secondaryColor else Color.White.copy(alpha = 0.6f),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "تحديد الموقع تلقائياً عبر GPS",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = if (useGps) "مفعل ومربوط بالبوصلة ومواقيت الصلاة" else "تحديد الموقع الدقيق للقبلة تلقائياً",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = useGps,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    triggerGpsFetch()
                                                } else {
                                                    viewModel.disableGps()
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = primaryColor,
                                                checkedTrackColor = secondaryColor,
                                                uncheckedThumbColor = Color.LightGray,
                                                uncheckedTrackColor = Color.DarkGray
                                            )
                                        )
                                    }

                                    if (isFetchingLocation) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = secondaryColor
                                            )
                                            Text(
                                                text = "جاري جلب إحداثيات GPS من الأقمار الصناعية...",
                                                fontSize = 11.sp,
                                                color = secondaryColor
                                            )
                                        }
                                    }

                                    if (locationError != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = locationError!!,
                                            fontSize = 11.sp,
                                            color = Color(0xFFEF5350),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (useGps && gpsCoords != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(Color(0xFF81C784), CircleShape)
                                                )
                                                Text(
                                                    text = String.format(Locale.US, "عرض: %.4f° | طول: %.4f°", gpsCoords!!.first, gpsCoords!!.second),
                                                    fontSize = 12.sp,
                                                    color = secondaryColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = "تحديث الإحداثيات 🔄",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryColor,
                                                modifier = Modifier.clickable { triggerGpsFetch() }
                                            )
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(visible = !useGps) {
                                // 1. Selector Bar (Dual Dropdown for Country & City side-by-side)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Country Selector Dropdown
                                    Box(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expandedCountryDropdown = true }
                                                .background(cardBgColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                                .border(0.5.dp, secondaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "تغيير الدولة",
                                                tint = secondaryColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${selectedCountry.nameAr} ▾",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = expandedCountryDropdown,
                                            onDismissRequest = { expandedCountryDropdown = false },
                                            modifier = Modifier.background(primaryColor)
                                        ) {
                                            viewModel.countries.forEach { country ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = country.nameAr,
                                                            color = if (country.nameEn == selectedCountry.nameEn) secondaryColor else Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.selectCountry(country)
                                                        expandedCountryDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // City Selector Dropdown
                                    Box(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expandedCityDropdown = true }
                                                .background(cardBgColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                                .border(0.5.dp, secondaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = "${selectedCity.nameAr} ▾",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = expandedCityDropdown,
                                            onDismissRequest = { expandedCityDropdown = false },
                                            modifier = Modifier.background(primaryColor)
                                        ) {
                                            viewModel.allCities.filter { it.countryEn == selectedCountry.nameEn }.forEach { city ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = city.nameAr,
                                                            color = if (city.nameEn == selectedCity.nameEn) secondaryColor else Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.selectCity(city)
                                                        expandedCityDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Cache Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (state.isOfflineMode) Color(0xFFD32F2F).copy(alpha = 0.15f) else Color(0xFF388E3C).copy(alpha = 0.15f),
                                        border = BorderStroke(
                                            0.5.dp,
                                            if (state.isOfflineMode) Color(0xFFD32F2F).copy(alpha = 0.3f) else Color(0xFF388E3C).copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Text(
                                            text = if (state.isOfflineMode) "محلي" else "متصل",
                                            color = if (state.isOfflineMode) Color(0xFFEF5350) else Color(0xFF81C784),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // 2. High-Fidelity Mosque Digital Board Display
                            MosqueDigitalBoard(
                                timings = timings,
                                countdown = countdown,
                                iqamahDurations = iqamahDurations
                            )

                            // 3. Compass Card (Decoupled to prevent main thread freezing/lagging)
                            QiblaCompassCard(
                                qiblaDirection = qiblaDirection
                            )

                            // 4. Core Supplication Dhikr
                            Text(
                                text = "لا حول ولا قوة إلا بالله",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }

            // 6. Iqamah Duration Settings Dialog
            if (showIqamahSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showIqamahSettingsDialog = false },
                    title = {
                        Text(
                            "إعدادات مدة الإقامة",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val prayers = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
                            prayers.forEach { prayer ->
                                val currentMin = iqamahDurations[prayer] ?: 15
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "صلاة $prayer",
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = "$currentMin دقيقة",
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                if (currentMin > 1) {
                                                    viewModel.updateIqamahDuration(prayer, currentMin - 1)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                        }
                                        Button(
                                            onClick = {
                                                if (currentMin < 60) {
                                                    viewModel.updateIqamahDuration(prayer, currentMin + 1)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                        }
                                    }
                                }
                                Divider(color = Color.Gray.copy(alpha = 0.2f))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showIqamahSettingsDialog = false }
                        ) {
                            Text("حفظ وإغلاق", color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = milkSurfaceColor,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

private fun formatTo12Hour(time24: String): String {
    if (time24 == "—" || !time24.contains(":")) return time24
    return try {
        val parts = time24.trim().split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val suffix = if (hours >= 12) "م" else "ص"
        val displayHours = when {
            hours == 0 -> 12
            hours > 12 -> hours - 12
            else -> hours
        }
        String.format(Locale.getDefault(), "%02d:%02d %s", displayHours, minutes, suffix)
    } catch (e: Exception) {
        time24
    }
}

@Composable
fun rememberCompassHeading(context: Context): State<Float> {
    val heading = remember { mutableStateOf(0f) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager }

    DisposableEffect(context) {
        val rotationSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)

        var hasRotationVector = false

        val listener = object : android.hardware.SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val orientationValues = FloatArray(3)
            val accelerometerReading = FloatArray(3)
            val magnetometerReading = FloatArray(3)
            var lastUpdate = 0L

            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                val now = System.currentTimeMillis()
                if (now - lastUpdate < 32) return // Throttle to ~30fps max
                lastUpdate = now

                if (event.sensor.type == android.hardware.Sensor.TYPE_ROTATION_VECTOR) {
                    android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    android.hardware.SensorManager.getOrientation(rotationMatrix, orientationValues)
                    var azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                    if (azimuth < 0) azimuth += 360f
                    heading.value = azimuth
                    hasRotationVector = true
                } else if (!hasRotationVector) {
                    if (event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                    } else if (event.sensor.type == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                    }

                    val success = android.hardware.SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
                    if (success) {
                        android.hardware.SensorManager.getOrientation(rotationMatrix, orientationValues)
                        var azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                        if (azimuth < 0) azimuth += 360f
                        heading.value = azimuth
                    }
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magneticSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return heading
}

private fun calculateQibla(latitude: Double, longitude: Double): Double {
    val mLat = 21.4225 * PI / 180.0
    val mLon = 39.8262 * PI / 180.0
    val uLat = latitude * PI / 180.0
    val uLon = longitude * PI / 180.0

    val y = sin(mLon - uLon)
    val x = cos(uLat) * tan(mLat) - sin(uLat) * cos(mLon - uLon)
    var qibla = atan2(y, x) * 180.0 / PI
    if (qibla < 0) qibla += 360.0
    return qibla
}

private fun fetchGpsLocation(
    context: Context,
    onLocationResult: (android.location.Location?) -> Unit,
    onError: (String) -> Unit
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
    if (!hasFine && !hasCoarse) {
        onError("لم يتم منح صلاحية الوصول للموقع")
        return
    }

    try {
        val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            onError("يرجى تفعيل ميزة تحديد الموقع (GPS) في الهاتف أولاً")
            return
        }

        var lastKnown: android.location.Location? = null
        if (isGpsEnabled) {
            lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
        }
        if (lastKnown == null && isNetworkEnabled) {
            lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
        }

        // If we have a last known location, return it immediately for instant loading
        if (lastKnown != null) {
            onLocationResult(lastKnown)
            return
        }

        // Otherwise, request a single update and setup a 15-second timeout
        val provider = if (isGpsEnabled) android.location.LocationManager.GPS_PROVIDER else android.location.LocationManager.NETWORK_PROVIDER
        
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        val listener = object : android.location.LocationListener {
            var isFinished = false
            override fun onLocationChanged(location: android.location.Location) {
                if (!isFinished) {
                    isFinished = true
                    handler.removeCallbacksAndMessages(null)
                    onLocationResult(location)
                    try {
                        locationManager.removeUpdates(this)
                    } catch (e: Exception) {}
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val timeoutRunnable = Runnable {
            if (!listener.isFinished) {
                listener.isFinished = true
                try {
                    locationManager.removeUpdates(listener)
                } catch (e: Exception) {}
                onError("انتهت مهلة جلب الموقع من الأقمار الصناعية. يرجى المحاولة في مكان مفتوح.")
            }
        }

        handler.postDelayed(timeoutRunnable, 15000L) // 15 seconds timeout

        locationManager.requestLocationUpdates(
            provider,
            0L,
            0f,
            listener,
            android.os.Looper.getMainLooper()
        )
    } catch (e: Exception) {
        onError("خطأ أثناء جلب الموقع: ${e.localizedMessage}")
    }
}

@Composable
fun IslamicArchContainer(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                val width = size.width
                val height = size.height
                
                val p8 = 8.dp.toPx()
                val p12 = 12.dp.toPx()
                val p16 = 16.dp.toPx()
                val p24 = 24.dp.toPx()
                val strokeOuter = 1.dp.toPx()
                val strokeInner = 0.7f * strokeOuter
                val cs = 12.dp.toPx()
                val strokeCorner = 2.dp.toPx()
                
                // Draw outer decorative line
                drawRect(
                    color = borderColor,
                    topLeft = Offset(p8, p8),
                    size = Size(width - p16, height - p16),
                    style = Stroke(width = strokeOuter)
                )
                
                // Draw inner double border with slight padding
                drawRect(
                    color = borderColor.copy(alpha = 0.5f),
                    topLeft = Offset(p12, p12),
                    size = Size(width - p24, height - p24),
                    style = Stroke(width = strokeInner)
                )
                
                // Draw classic elegant islamic corners
                // Top-Left
                drawLine(borderColor, Offset(p8, p8 + cs), Offset(p8, p8), strokeCorner)
                drawLine(borderColor, Offset(p8, p8), Offset(p8 + cs, p8), strokeCorner)
                
                // Top-Right
                drawLine(borderColor, Offset(width - p8, p8 + cs), Offset(width - p8, p8), strokeCorner)
                drawLine(borderColor, Offset(width - p8, p8), Offset(width - p8 - cs, p8), strokeCorner)
                
                // Bottom-Left
                drawLine(borderColor, Offset(p8, height - p8 - cs), Offset(p8, height - p8), strokeCorner)
                drawLine(borderColor, Offset(p8, height - p8), Offset(p8 + cs, height - p8), strokeCorner)
                
                // Bottom-Right
                drawLine(borderColor, Offset(width - p8, height - p8 - cs), Offset(width - p8, height - p8), strokeCorner)
                drawLine(borderColor, Offset(width - p8, height - p8), Offset(width - p8 - cs, height - p8), strokeCorner)
            },
        content = content
    )
}

private fun calculateIqamahTime(adhanTime24: String, minutes: Int): String {
    if (adhanTime24 == "—" || !adhanTime24.contains(":")) return "—"
    return try {
        val parts = adhanTime24.trim().split(":")
        val hours = parts[0].toInt()
        val mins = parts[1].toInt()
        
        val totalMins = hours * 60 + mins + minutes
        val finalHours = (totalMins / 60) % 24
        val finalMins = totalMins % 60
        
        String.format(Locale.US, "%02d:%02d", finalHours, finalMins)
    } catch (e: Exception) {
        "—"
    }
}

private fun formatTo12HourNoSuffix(time24: String): String {
    if (time24 == "—" || !time24.contains(":")) return time24
    return try {
        val parts = time24.trim().split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val displayHours = when {
            hours == 0 -> 12
            hours > 12 -> hours - 12
            else -> hours
        }
        String.format(Locale.US, "%02d:%02d", displayHours, minutes)
    } catch (e: Exception) {
        time24
    }
}

@Composable
fun IslamicScreenDecorations(
    borderColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
    ornamentColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val margin = 12.dp.toPx()
        val borderW = w - 2 * margin
        val borderH = h - 2 * margin
        val p4 = 4.dp.toPx()
        val p6 = 6.dp.toPx()
        val p12 = 12.dp.toPx()
        val p16 = 16.dp.toPx()
        val p2 = 2.dp.toPx()
        
        // Draw the outer fine border
        drawRect(
            color = borderColor,
            topLeft = Offset(margin, margin),
            size = Size(borderW, borderH),
            style = Stroke(width = 1.5f)
        )
        
        // Draw double line effect
        drawRect(
            color = borderColor.copy(alpha = 0.3f),
            topLeft = Offset(margin + p4, margin + p4),
            size = Size(borderW - 2 * p4, borderH - 2 * p4),
            style = Stroke(width = 0.8f)
        )
        
        // Draw corner ornaments at the four corners
        val oSize = 24.dp.toPx() // size of the ornament
        val strokeW = 2f
        
        // --- TOP-LEFT CORNER ---
        val tlX = margin
        val tlY = margin
        // Diagonal bridge
        drawLine(ornamentColor, Offset(tlX, tlY + oSize), Offset(tlX + oSize, tlY), strokeW)
        // Concentric inner line
        drawLine(ornamentColor, Offset(tlX, tlY + oSize - p6), Offset(tlX + oSize - p6, tlY), strokeW * 0.7f)
        // Small corner star/accent
        drawCircle(ornamentColor, radius = p2, center = Offset(tlX + p6, tlY + p6))
        // Bracket flourishes
        drawArc(
            color = ornamentColor,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(tlX - p4, tlY - p4),
            size = Size(p16, p16),
            style = Stroke(width = strokeW)
        )

        // --- TOP-RIGHT CORNER ---
        val trX = w - margin
        val trY = margin
        drawLine(ornamentColor, Offset(trX, trY + oSize), Offset(trX - oSize, trY), strokeW)
        drawLine(ornamentColor, Offset(trX, trY + oSize - p6), Offset(trX - oSize + p6, trY), strokeW * 0.7f)
        drawCircle(ornamentColor, radius = p2, center = Offset(trX - p6, trY + p6))
        drawArc(
            color = ornamentColor,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(trX - p12, trY - p4),
            size = Size(p16, p16),
            style = Stroke(width = strokeW)
        )

        // --- BOTTOM-LEFT CORNER ---
        val blX = margin
        val blY = h - margin
        drawLine(ornamentColor, Offset(blX, blY - oSize), Offset(blX + oSize, blY), strokeW)
        drawLine(ornamentColor, Offset(blX, blY - oSize + p6), Offset(blX + oSize - p6, blY), strokeW * 0.7f)
        drawCircle(ornamentColor, radius = p2, center = Offset(blX + p6, blY - p6))
        drawArc(
            color = ornamentColor,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(blX - p4, blY - p12),
            size = Size(p16, p16),
            style = Stroke(width = strokeW)
        )

        // --- BOTTOM-RIGHT CORNER ---
        val brX = w - margin
        val brY = h - margin
        drawLine(ornamentColor, Offset(brX, brY - oSize), Offset(brX - oSize, brY), strokeW)
        drawLine(ornamentColor, Offset(brX, brY - oSize + p6), Offset(brX - oSize + p6, brY), strokeW * 0.7f)
        drawCircle(ornamentColor, radius = p2, center = Offset(brX - p6, brY - p6))
        drawArc(
            color = ornamentColor,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(brX - p12, brY - p12),
            size = Size(p16, p16),
            style = Stroke(width = strokeW)
        )
    }
}

@Composable
fun IslamicCrescentEmblem(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
    accentColor: Color = MaterialTheme.colorScheme.secondary
) {
    Box(
        modifier = modifier
            .size(130.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    radius = 240f
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(80.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val p4 = 4.dp.toPx()
            
            // Draw subtle background moon rings
            drawCircle(
                color = accentColor.copy(alpha = 0.05f),
                radius = w * 0.48f,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.1f),
                radius = w * 0.44f,
                style = Stroke(width = 1f)
            )

            // Draw Crescent Moon
            val crescentPath = Path().apply {
                val r = w * 0.38f
                val mx = cx - w * 0.05f
                moveTo(mx, cy - r)
                cubicTo(
                    mx + r * 1.3f, cy - r * 0.8f,
                    mx + r * 1.3f, cy + r * 0.8f,
                    mx, cy + r
                )
                cubicTo(
                    mx + r * 0.65f, cy + r * 0.62f,
                    mx + r * 0.65f, cy - r * 0.62f,
                    mx, cy - r
                )
                close()
            }
            drawPath(
                path = crescentPath,
                color = accentColor.copy(alpha = 0.85f)
            )

            // Draw Mosque Silhouette
            val mosquePath = Path().apply {
                val baseW = w * 0.32f
                val baseH = h * 0.22f
                val bx = cx - baseW / 2
                val by = cy + h * 0.18f
                
                moveTo(bx, by)
                lineTo(bx + baseW, by)
                lineTo(bx + baseW, by - baseH * 0.4f)
                
                val domeCenterX = cx - baseW * 0.08f
                val domeR = baseW * 0.28f
                val domeY = by - baseH * 0.4f
                lineTo(domeCenterX + domeR, domeY)
                cubicTo(
                    domeCenterX + domeR, domeY - domeR * 1.3f,
                    domeCenterX - domeR, domeY - domeR * 1.3f,
                    domeCenterX - domeR, domeY
                )
                lineTo(bx, by - baseH * 0.4f)
                close()
            }
            drawPath(
                path = mosquePath,
                color = accentColor.copy(alpha = 0.35f)
            )
            
            // Draw tall slim minaret
            val minaretPath = Path().apply {
                val minW = w * 0.07f
                val minH = h * 0.45f
                val mx = cx + w * 0.13f
                val my = cy + h * 0.18f
                
                moveTo(mx - minW/2, my)
                lineTo(mx - minW/2, my - minH)
                lineTo(mx - minW, my - minH)
                lineTo(mx - minW, my - minH - p4)
                lineTo(mx + minW, my - minH - p4)
                lineTo(mx + minW, my - minH)
                lineTo(mx + minW/2, my - minH)
                lineTo(mx + minW/2, my - minH - minH * 0.15f)
                lineTo(mx, my - minH - minH * 0.32f)
                lineTo(mx - minW/2, my - minH - minH * 0.15f)
                lineTo(mx - minW/2, my - minH)
                close()
            }
            drawPath(
                path = minaretPath,
                color = accentColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun IslamicVerseCard(
    verse: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.secondary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "﴿",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = verse,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "﴾",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun MosqueDigitalBoard(
    timings: com.example.prayer.CachedPrayerTimes,
    countdown: com.example.prayer.CountdownState,
    iqamahDurations: Map<String, Int>
) {
    val now = Calendar.getInstance()
    // Standard Latin digits clock formatter matching image (01:47:37)
    val clockFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    val liveTimeStr = clockFormat.format(now.time)

    // Dates
    val dayOfWeekAr = when (now.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SATURDAY -> "السبت"
        Calendar.SUNDAY -> "الأحد"
        Calendar.MONDAY -> "الاثنين"
        Calendar.TUESDAY -> "الثلاثاء"
        Calendar.WEDNESDAY -> "الأربعاء"
        Calendar.THURSDAY -> "الخميس"
        Calendar.FRIDAY -> "الجمعة"
        else -> ""
    }
    val monthAr = when (now.get(Calendar.MONTH)) {
        Calendar.JANUARY -> "يناير"
        Calendar.FEBRUARY -> "فبراير"
        Calendar.MARCH -> "مارس"
        Calendar.APRIL -> "أبريل"
        Calendar.MAY -> "مايو"
        Calendar.JUNE -> "يونيو"
        Calendar.JULY -> "يوليو"
        Calendar.AUGUST -> "أغسطس"
        Calendar.SEPTEMBER -> "سبتمبر"
        Calendar.OCTOBER -> "أكتوبر"
        Calendar.NOVEMBER -> "نوفمبر"
        Calendar.DECEMBER -> "ديسمبر"
        else -> ""
    }
    val dateStr = "$dayOfWeekAr $monthAr"
    val dateGregorian = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.time)

    // Colors matching custom color palette
    val boardBgColor = Color(0xFF4C0A13) // Beautiful Deep Crimson Red/Burgundy
    val borderGold = MaterialTheme.colorScheme.secondary // Dynamic secondary accent color
    val adhanPink = Color(0xFFFFA3B1) // Pink rose
    val iqamahCyan = Color(0xFF00FFD1) // Neon turquoise
    val prayerWhite = Color(0xFFFFFFFF)

    // Column Layout with custom arched background
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = boardBgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    
                    // Top corners filled with rich dark green
                    drawRect(color = Color(0xFF13321B))
                    
                    // Arch Path
                    val archPath = Path().apply {
                        val archTopY = 24.dp.toPx()
                        val archCurveStartY = 130.dp.toPx()
                        
                        moveTo(0f, h)
                        lineTo(0f, archCurveStartY)
                        
                        cubicTo(
                            w * 0.1f, archCurveStartY * 0.6f,
                            w * 0.35f, archTopY,
                            w * 0.5f, archTopY
                        )
                        cubicTo(
                            w * 0.65f, archTopY,
                            w * 0.9f, archCurveStartY * 0.6f,
                            w, archCurveStartY
                        )
                        
                        lineTo(w, h)
                        close()
                    }
                    
                    // Fill arch
                    drawPath(path = archPath, color = boardBgColor)
                    
                    // Draw gold border lines
                    drawPath(
                        path = archPath,
                        color = borderGold.copy(alpha = 0.85f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    
                    // Draw some simple lace ornaments in the green corners
                    val p16 = 16.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = p16,
                        center = Offset(p16, p16),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = p16,
                        center = Offset(w - p16, p16),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp), // Padding inside the dome
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Emblem/Logo inside the Dome
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(borderGold.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, borderGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🕌", fontSize = 20.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // 2. Large clock display
                Text(
                    text = liveTimeStr,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Hijri and Gregorian Dates
                Text(
                    text = dateStr,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateGregorian,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Table Header Row (الأذان | الصلاة | الإقامة)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأذان",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = "الصلاة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1.2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "الإقامة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Left
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 5. Prayers Table Rows in exact photo order: Fajr, Dhuhr, Asr, Maghrib, Isha, Sunrise
                val boardPrayers = listOf(
                    Triple("الفجر", timings.fajr, iqamahDurations["الفجر"] ?: 20),
                    Triple("الظهر", timings.dhuhr, iqamahDurations["الظهر"] ?: 15),
                    Triple("العصر", timings.asr, iqamahDurations["العصر"] ?: 15),
                    Triple("المغرب", timings.maghrib, iqamahDurations["المغرب"] ?: 10),
                    Triple("العشاء", timings.isha, iqamahDurations["العشاء"] ?: 15),
                    Triple("الشروق/الضحى", timings.sunrise, 10) // 10 minutes default for duha
                )

                boardPrayers.forEach { (name, timeStr, iqamahMin) ->
                    val isCurrent = countdown.currentPrayerName == name || (name == "الشروق/الضحى" && countdown.currentPrayerName == "الشروق")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrent) Color.White.copy(alpha = 0.06f) else Color.Transparent)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Right column: Adhan time (Pink/Rose)
                        Text(
                            text = formatTo12HourNoSuffix(timeStr),
                            color = adhanPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Right
                        )

                        // Middle column: Prayer name
                        Text(
                            text = name,
                            color = if (isCurrent) borderGold else prayerWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Center
                        )

                        // Left column: Iqamah time (Neon Cyan)
                        val iqamahTime = if (iqamahMin != null) {
                            calculateIqamahTime(timeStr, iqamahMin)
                        } else ""
                        
                        Text(
                            text = if (iqamahTime.isNotEmpty()) formatTo12HourNoSuffix(iqamahTime) else "—",
                            color = iqamahCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Left
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Footer Section with Red Background Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E0409)) // Darker Red Footer Bar
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val adhanCountdownVal = if (countdown.state == com.example.prayer.PrayerState.COUNTDOWN) {
                        countdown.countdownText
                    } else "00:00:00"

                    val iqamahCountdownVal = if (countdown.state == com.example.prayer.PrayerState.IQAMAH) {
                        countdown.countdownText
                    } else "—"

                    val nextPrayerTime = when (countdown.nextPrayerName) {
                        "الفجر" -> timings.fajr
                        "الظهر" -> timings.dhuhr
                        "العصر" -> timings.asr
                        "المغرب" -> timings.maghrib
                        "العشاء" -> timings.isha
                        "الشروق" -> timings.sunrise
                        else -> ""
                    }

                    // Right box: "الأذان بعد"
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "الأذان بعد",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = adhanCountdownVal,
                            color = adhanPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // Middle box: Next Prayer and Time
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = countdown.nextPrayerName,
                            color = borderGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (nextPrayerTime.isNotEmpty()) formatTo12HourNoSuffix(nextPrayerTime) else "—",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // Left box: "الإقامة بعد"
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "الإقامة بعد",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = iqamahCountdownVal,
                            color = iqamahCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QiblaCompassCard(
    qiblaDirection: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawHeading = rememberCompassHeading(context)
    val smoothHeading by animateFloatAsState(
        targetValue = rawHeading.value,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 120f),
        label = "compass_smooth"
    )

    val primaryColor = Color(0xFF1A3140)
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val milkSurfaceColor = Color(0xFFFFF3E5)
    val cardBgColor = Color(0xFF162530)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "القبلة",
                    tint = secondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بوصلة اتجاه القبلة الكهرومغناطيسية",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compass Dial visual block
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(primaryColor.copy(alpha = 0.5f), CircleShape)
                    .border(1.5.dp, secondaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Circular Compass Dial with Degree Markers
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .rotate(-smoothHeading),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(secondaryColor, CircleShape)
                    )

                    // North Indicator (N)
                    Text(
                        text = "ش",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    )

                    // South (S)
                    Text(
                        text = "ج",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )

                    // East (E)
                    Text(
                        text = "ق",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    )

                    // West (W)
                    Text(
                        text = "غ",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp)
                    )
                }

                // Qibla needle pointer pointing to computed Qibla direction relative to North
                val needleRotation = qiblaDirection - smoothHeading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .rotate(needleRotation.toFloat()),
                    contentAlignment = Alignment.Center
                ) {
                    // Elegant Golden Arrow Needle pointing up towards Qibla
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight(0.7f)
                    ) {
                        // Golden Arrowhead pointing up
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .rotate(45f)
                                .background(secondaryColor, RoundedCornerShape(2.dp))
                        )
                        // Needle stalk
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(secondaryColor)
                        )
                        // Base weight
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(secondaryColor, CircleShape)
                        )
                    }

                    // Tiny crescent inside the compass pointing to Qibla
                    Text(
                        text = "🕋",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = String.format(Locale("ar"), "زاوية انحراف القبلة: %.1f°", qiblaDirection),
                fontSize = 13.sp,
                color = milkSurfaceColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "وجه رأس الهاتف باتجاه الكعبة 🕋 للصلاة",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
