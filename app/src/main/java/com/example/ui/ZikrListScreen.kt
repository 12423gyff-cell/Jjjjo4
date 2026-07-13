package com.example.ui
import androidx.compose.ui.graphics.luminance

import androidx.compose.foundation.border
import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.example.model.Category
import com.example.model.Zikr
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
fun formatQuranicText(zekrText: String): String {
    var result = zekrText

    // Replacing Surahs in standard Zikrs with correctly formatted and numbered variants
    result = result.replace(
        "﴿قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ* لَمْ يَلِدْ وَلَمْ يُولَدْ* وَلَمْ يَكُن لَّهُ كُفُواً أَحَدٌ﴾",
        "﴿ قُلْ هُوَ اللَّهُ أَحَدٌ ﴿١﴾ اللَّهُ الصَّمَدُ ﴿٢﴾ لَمْ يَلِدْ وَلَمْ يُولَدْ ﴿٣﴾ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ﴿٤﴾ ﴾"
    )
    result = result.replace(
        "﴿قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ* مِن شَرِّ مَا خَلَقَ* وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ* وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ* وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ﴾",
        "﴿ قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ﴿١﴾ مِن شَرِّ مَا خَلَقَ ﴿٢﴾ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ﴿٣﴾ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ﴿٤﴾ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴿٥﴾ ﴾"
    )
    result = result.replace(
        "﴿قُلْ أَعُوذُ بِرَبِّ النَّاسِ* مَلِكِ النَّاسِ* إِلَهِ النَّاسِ* مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ* الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ* مِنَ الْجِنَّةِ وَ النَّاسِ﴾",
        "﴿ قُلْ أَعُوذُ بِرَبِّ النَّاسِ ﴿١﴾ مَلِكِ النَّاسِ ﴿٢﴾ إِلَهِ النَّاسِ ﴿٣﴾ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ﴿٤﴾ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ﴿٥﴾ مِنَ الْجِنَّةِ وَالنَّاسِ ﴿٦﴾ ﴾"
    )
    result = result.replace(
        "﴿اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْـــــــــــدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾",
        "﴿ اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ ﴿٢٥٥﴾ ﴾"
    )
    result = result.replace(
        "﴿آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ كُلٌّ آمَنَ بِاللَّهِ وَمَلآئِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لاَ نُفَرِّقُ بَيْنَ أَحَدٍ مِّن رُّسُلِهِ وَقَالُواْ سَمِعْنَا وَأَطَعْنَا غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ* لاَ يُكَلِّفُ اللَّهُ نَفْساً إِلاَّ وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ رَبَّنَا لاَ تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا رَبَّنَا وَلاَ تَحْمِلْ عَلَيْنَا إِصْراً كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا رَبَّنَا وَلاَ تُحَمِّلْنَا مَا لاَ طَاقَةَ لَنَا بِهِ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَآ أَنتَ مَوْلاَنَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ﴾",
        "﴿ آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِّن رُّسُلِهِ وَقَالُوا سَمِعْنَا وَأَطَعْنَا غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ ﴿٢٨٥﴾ لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا أَنتَ مَوْلَانَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ ﴿٢٨٦﴾ ﴾"
    )

    // Expand Surah Al Mulk and As-Sajdah if it only says "Read..."
    if (result.contains("يقرأ (سورة الم السجدة)، و (سورة تبارك الذي بيده الملك).")) {
        result = result.replace(
            "يقرأ (سورة الم السجدة)، و (سورة تبارك الذي بيده الملك).",
            "سورة السجدة:\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\n" + getFullQuranicText("السجدة") + "\n\nسورة الملك:\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\n" + getFullQuranicText("الملك")
        )
    }

    // Convert any western numbers inside ﴿ ﴾ to Arabic numerals
    val regex = Regex("﴿\\s*(\\d+)\\s*﴾")
    result = regex.replace(result) { matchResult ->
        "﴿${convertToArabicNumerals(matchResult.groupValues[1])}﴾"
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZikrListScreen(
    category: Category,
    fromAlarm: Boolean = false,
    onBackClick: () -> Unit,
    onQuranClick: (String, String?) -> Unit = { _, _ -> },
    viewModel: ZikrViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as Application)
    )
) {
    BackHandler {
        viewModel.ttsManager.stop()
        onBackClick()
    }
    val context = LocalContext.current
    val settingsManager = remember { com.example.model.SettingsManager.getInstance(context) }
    val textSizeMultiplier by settingsManager.textSizeMultiplier.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll states
    var isAutoScrolling by remember { mutableStateOf(false) }
    var scrollSpeed by remember { mutableIntStateOf(1) } // 1: slow, 2: medium, 3: fast
    
    // TTS states
    val isPlaying by viewModel.ttsManager.isPlaying.collectAsState()
    val currentZikrId by viewModel.ttsManager.currentZikrId.collectAsState()
    var isPlayingAll by remember { mutableStateOf(false) }
    var playAllCurrentIndex by remember { mutableIntStateOf(0) }
    var playAllRepeatCount by remember { mutableIntStateOf(0) }
    
    // Progress state
    val progressList by viewModel.progressFlow.collectAsState()
    val favoritesList by viewModel.favoritesFlow.collectAsState()
    
    // Scroll to first zikr if opened from alarm
    LaunchedEffect(fromAlarm) {
        if (fromAlarm && category.azkar.isNotEmpty()) {
            delay(100) // Small delay to let UI compose
            listState.animateScrollToItem(1)
        }
    }
    
    // Auto-scroll effect
    LaunchedEffect(isAutoScrolling, scrollSpeed) {
        if (isAutoScrolling) {
            while (true) {
                val speed = when(scrollSpeed) {
                    1 -> 1.5f
                    2 -> 3.2f
                    3 -> 5.5f
                    4 -> 8.5f
                    5 -> 13.0f
                    else -> 3.2f
                }
                listState.scrollBy(speed)
                delay(16)
            }
        }
    }

    // Play All Effect
    LaunchedEffect(isPlayingAll, isPlaying) {
        if (isPlayingAll && !isPlaying) {
            if (playAllCurrentIndex < category.azkar.size) {
                val currentZikr = category.azkar[playAllCurrentIndex]
                val targetCount = if (currentZikr.count > 0) currentZikr.count else 1
                if (playAllRepeatCount < targetCount) {
                    playAllRepeatCount++
                    viewModel.ttsManager.play(currentZikr.text, currentZikr.id)
                    listState.animateScrollToItem(playAllCurrentIndex)
                } else {
                    playAllCurrentIndex++
                    playAllRepeatCount = 0
                    if (playAllCurrentIndex < category.azkar.size) {
                        val nextZikr = category.azkar[playAllCurrentIndex]
                        playAllRepeatCount++
                        viewModel.ttsManager.play(nextZikr.text, nextZikr.id)
                        listState.animateScrollToItem(playAllCurrentIndex)
                    } else {
                        isPlayingAll = false
                    }
                }
            } else {
                isPlayingAll = false
            }
        }
    }

    // Sync speed with TTSManager
    LaunchedEffect(scrollSpeed) {
        val ttsSpeed = when(scrollSpeed) {
            1 -> 0.9f
            2 -> 1.15f
            3 -> 1.4f
            4 -> 1.65f
            5 -> 1.9f
            else -> 1.3f
        }
        viewModel.ttsManager.setSpeechRate(ttsSpeed)
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        if (event.changes.any { it.pressed } && isAutoScrolling) {
                            isAutoScrolling = false
                        }
                    }
                }
            }
    ) {
        val scenicBg = getScenicBgForCategory(category.title)
        val appBgColor = Color(0xFF10202B) // Deep dark slate blue that matches our premium theme
        
        val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            if (isDarkTheme) {
                // Scenic illustration header
                Image(
                    painter = painterResource(id = scenicBg),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Luxury gradient overlay that blends the top scenic background with the solid color below
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),  // Top shadow for title visibility
                                    Color.Transparent,               // Center shine
                                    appBgColor.copy(alpha = 0.7f),   // Transition blend
                                    appBgColor                       // Solid blend
                                )
                            )
                        )
                )
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = category.title, 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                                fontSize = 28.sp,
                                fontFamily = com.example.ui.theme.ArefRuqaaFontFamily
                            ), 
                            color = MaterialTheme.colorScheme.onBackground 
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            viewModel.ttsManager.stop()
                            onBackClick() 
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الرجوع",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                        scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A3140) // Premium Blue Whale primary color
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Play/Pause Auto-scroll
                        IconButton(
                            onClick = { isAutoScrolling = !isAutoScrolling },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isAutoScrolling) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "تشغيل التمرير التلقائي",
                                tint = if (isAutoScrolling) Color(0xFF1A3140) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Middle: Speed selection (1x to 5x) - ALWAYS visible and ultra fast response!
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(1, 2, 3, 4, 5).forEach { speed ->
                                val isSelected = scrollSpeed == speed
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.05f)
                                        )
                                        .clickable { scrollSpeed = speed },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${speed}x",
                                        color = if (isSelected) Color(0xFF1A3140) else Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Right: Fast Manual scrolling down/up
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        listState.animateScrollBy(-500f)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "تحريك لأعلى",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        listState.animateScrollBy(500f)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "تحريك لأسفل",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, 
                    end = 16.dp, 
                    top = paddingValues.calculateTopPadding() + 8.dp, 
                    bottom = paddingValues.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    // Audio & Speed Controls Bar
                    Surface(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { 
                                    isPlayingAll = !isPlayingAll
                                    if (isPlayingAll) {
                                        if (currentZikrId != null) {
                                            val startIdx = category.azkar.indexOfFirst { it.id == currentZikrId }
                                            playAllCurrentIndex = if (startIdx != -1) startIdx else 0
                                        } else {
                                            playAllCurrentIndex = 0
                                        }
                                        playAllRepeatCount = 0
                                        if (isPlaying) {
                                            viewModel.ttsManager.stop()
                                        }
                                    } else {
                                        viewModel.ttsManager.stop()
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isPlayingAll) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل الكل",
                                        tint = if (isPlayingAll) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                IconButton(onClick = { 
                                    viewModel.ttsManager.stop()
                                    isPlayingAll = false
                                    val currentIdx = category.azkar.indexOfFirst { it.id == currentZikrId }
                                    if (currentIdx > 0) {
                                        val prev = category.azkar[currentIdx - 1]
                                        viewModel.ttsManager.play(prev.text, prev.id)
                                        coroutineScope.launch { listState.animateScrollToItem(currentIdx - 1) }
                                    }
                                }) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "السابق", tint = MaterialTheme.colorScheme.onBackground)
                                }
                                IconButton(onClick = { 
                                    isPlayingAll = false
                                    if (isPlaying) viewModel.ttsManager.stop() else {
                                        val current = category.azkar.find { it.id == currentZikrId } ?: category.azkar.firstOrNull()
                                        if (current != null) viewModel.ttsManager.play(current.text, current.id)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "تشغيل الحالي",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                IconButton(onClick = { 
                                    viewModel.ttsManager.stop()
                                    isPlayingAll = false
                                    val currentIdx = category.azkar.indexOfFirst { it.id == currentZikrId }
                                    if (currentIdx != -1 && currentIdx < category.azkar.size - 1) {
                                        val next = category.azkar[currentIdx + 1]
                                        viewModel.ttsManager.play(next.text, next.id)
                                        coroutineScope.launch { listState.animateScrollToItem(currentIdx + 1) }
                                    }
                                }) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "التالي", tint = MaterialTheme.colorScheme.onBackground)
                                }
                                IconButton(onClick = { 
                                    val current = category.azkar.find { it.id == currentZikrId }
                                    if (current != null) {
                                        isPlayingAll = false
                                        viewModel.ttsManager.play(current.text, current.id)
                                    }
                                }) {
                                    Icon(Icons.Default.Replay, contentDescription = "إعادة", tint = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Speed Controls
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "سرعة الحركة والقراءة:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(1, 2, 3).forEach { speed ->
                                        val isSelected = scrollSpeed == speed
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp, 28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                                .clickable { scrollSpeed = speed },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${speed}x",
                                                color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                itemsIndexed(category.azkar) { index, zikr ->
                    val savedProgress = progressList.find { it.zikrId == zikr.id }?.count ?: 0
                    ZikrLuxuryCard(
                        zikr = zikr,
                        categoryTitle = category.title,
                        savedProgress = savedProgress,
                        isCurrentlyPlaying = currentZikrId == zikr.id,
                        isFavorite = favoritesList.any { it.zikrId == zikr.id },
                        onProgressUpdate = { newCount ->
                            viewModel.saveProgress(zikr.id, newCount)
                        },
                        onPlayClick = {
                            isPlayingAll = false
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

@Composable
fun ZikrLuxuryCard(
    zikr: Zikr,
    textSizeMultiplier: Float = 1.0f, 
    categoryTitle: String,
    savedProgress: Int, 
    isCurrentlyPlaying: Boolean,
    isFavorite: Boolean,
    onProgressUpdate: (Int) -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onQuranClick: (String, String?) -> Unit = { _, _ -> }
) {
    var currentCount by remember(savedProgress) { mutableIntStateOf(savedProgress) }
    val isCompleted = currentCount >= zikr.count
    val view = LocalView.current
    var isExpanded by remember { mutableStateOf(false) }

    val isLongText = zikr.text.length > 250
    

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("zikr_card_${zikr.id}"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isCurrentlyPlaying) 2.dp else 1.dp, 
            when {
                isCurrentlyPlaying -> MaterialTheme.colorScheme.secondary
                isCompleted -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f) 
                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable(enabled = !isCompleted) {
                    if (currentCount < zikr.count) {
                        currentCount++
                        onProgressUpdate(currentCount)
                        view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    }
                }
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val context = androidx.compose.ui.platform.LocalContext.current
                IconButton(onClick = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(zikr.text))
                    android.widget.Toast.makeText(context, "تم النسخ", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, zikr.text)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "مشاركة الذكر"))
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "حفظ",
                        tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Audio Indicator
            if (isCurrentlyPlaying) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "جاري التشغيل",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            val instructions = zikr.instructions ?: ""
            val zekrText = zikr.text ?: ""
            
            // Analyze the text to identify associated Quranic Surahs / Verses
            val surahNamesList = remember(zekrText, instructions) {
                val list = mutableListOf<String>()
                if (instructions.contains("سورة الإخلاص") || zekrText.contains("قُلْ هُوَ اللَّهُ أَحَدٌ")) {
                    list.add("سورة الإخلاص")
                }
                if (instructions.contains("سورة الفلق") || zekrText.contains("قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ")) {
                    list.add("سورة الفلق")
                }
                if (instructions.contains("سورة الناس") || zekrText.contains("قُلْ أَعُوذُ بِرَبِّ النَّاسِ")) {
                    list.add("سورة الناس")
                }
                if (zekrText.contains("اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ") || zekrText.contains("الْقَيُّومُ")) {
                    list.add("آية الكرسي")
                }
                if (zekrText.contains("آمَنَ الرَّسُولُ")) {
                    list.add("خواتيم سورة البقرة")
                }
                if (instructions.contains("سورة الملك") || zekrText.contains("تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ") || zekrText.contains("بِيَدِهِ الْمُلْكُ")) {
                    list.add("سورة الملك")
                }
                if (zekrText.contains("تَنْزِيلَ السَّجْدَة") || zekrText.contains("السَّجْدَة")) {
                    list.add("سورة السجدة")
                }
                
                if (list.isEmpty()) {
                    if (instructions.contains("سورة")) {
                        list.add(instructions.replace("[", "").replace("]", "").replace(".", "").trim())
                    } else if (zekrText.contains("﴿") || instructions.contains("آية")) {
                        val name = if (instructions.isNotEmpty() && !instructions.contains("مرة") && !instructions.contains("يفعل")) {
                            instructions.replace("[", "").replace("]", "").replace(".", "").trim()
                        } else {
                            "آية قرآنية"
                        }
                        list.add(name)
                    }
                }
                list
            }

            val formattedZekrText = remember(zikr.text) { formatQuranicText(zikr.text ?: "") }
            val isQuran = remember(formattedZekrText, surahNamesList) {
                surahNamesList.isNotEmpty() || formattedZekrText.contains("﴿")
            }

            if (isQuran) {
                if (surahNamesList.isNotEmpty()) {
                    surahNamesList.forEach { name ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clickable { onQuranClick(name, formattedZekrText) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.quran_surah_frame),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "انقر لفتح وضعية المصحف 📖",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable { onQuranClick("آية قرآنية كريمة", formattedZekrText) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.quran_surah_frame),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "آية قرآنية كريمة",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "انقر لفتح وضعية المصحف 📖",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                QuranicTextRenderer(
                    text = formattedZekrText,
                    fontSize = 28f * textSizeMultiplier,
                    baseColor = if (isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground, // Milk for maximum readability
                    maxLines = if (isExpanded || !isLongText) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            } else {
                // Show small circular category icon instead of empty space
                ZikrCategoryIcon(
                    title = categoryTitle,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = formattedZekrText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize * textSizeMultiplier,
                        lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * textSizeMultiplier
                    ),
                    maxLines = if (isExpanded || !isLongText) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCompleted) 
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) 
                    else 
                        MaterialTheme.colorScheme.onBackground
                )
            }

            if (isLongText) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "عرض أقل" else "قراءة المزيد", color = MaterialTheme.colorScheme.secondary)
                }
            }

            if (!zikr.instructions.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = zikr.instructions,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Play / Counter Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPlayClick) {
                    Icon(
                        imageVector = if (isCurrentlyPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل الصوت",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Counter
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (zikr.count > 0) {
                        val progress by animateFloatAsState(
                            targetValue = if (zikr.count > 0) currentCount.toFloat() / zikr.count.toFloat() else 1f,
                            label = "progress"
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                            strokeWidth = 6.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )
                            .border(1.dp, if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(targetState = currentCount, label = "counter") { count ->
                            if (count >= zikr.count && zikr.count > 0) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "تم",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                val remaining = if (zikr.count > 0) zikr.count - count else 1
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$remaining",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    if (zikr.count > 1) {
                                        Text(
                                            text = "من ${zikr.count}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Add a dummy icon to balance the row
                IconButton(onClick = {
                    currentCount = 0
                    onProgressUpdate(0)
                }, enabled = currentCount > 0) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة التعيين",
                        tint = if (currentCount > 0) MaterialTheme.colorScheme.secondary else Color.Transparent
                    )
                }
            }
        }
    }
}

