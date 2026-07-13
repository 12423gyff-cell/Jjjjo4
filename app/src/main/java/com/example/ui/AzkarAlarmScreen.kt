package com.example.ui

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.alarm.AlarmItem
import com.example.alarm.AlarmRepository
import kotlinx.coroutines.launch
import java.util.*
import androidx.activity.compose.BackHandler

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.R

val GoldAccent: Color
    @Composable
    get() = MaterialTheme.colorScheme.secondary
val DeepBlue = Color.Black
val PlumCard = Color(0xFF202020)
val WarmWhite = Color.White

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Amiri")
val fallbackFontName = GoogleFont("Noto Naskh Arabic")

val ArabicFont = FontFamily(
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fallbackFontName, fontProvider = provider, weight = FontWeight.SemiBold)
)


fun formatRemainingTime(triggerTime: Long): String {
    val diff = triggerTime - System.currentTimeMillis()
    if (diff <= 0) return "الآن"
    val days = diff / (1000 * 60 * 60 * 24)
    val hours = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60
    
    val parts = mutableListOf<String>()
    if (days > 0) parts.add("$days يوم")
    if (hours > 0) parts.add("$hours ساعة")
    if (minutes > 0) parts.add("$minutes دقيقة")
    if (parts.isEmpty()) parts.add("أقل من دقيقة")
    
    return "بعد " + parts.joinToString(" و")
}


val soundDisplayNames = mapOf(
    "soft_bell.wav" to "جرس هادئ",
    "deep_chime.wav" to "رنين عميق",
    "double_beep.wav" to "تنبيه رقمي",
    "sound1.wav" to "أنشودة 1",
    "sound2.wav" to "أنشودة 2",
    "default_ringtone.mp3" to "افتراضي"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarAlarmScreen(categories: List<com.example.model.Category>, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AlarmRepository.load(context)
    }
    val alarms by AlarmRepository.alarmsFlow.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<AlarmItem?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("منبه الأذكار", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 24.sp)
                        Text("إدارة التذكيرات الخاصة بك", fontSize = 12.sp, color = WarmWhite.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = WarmWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    alarmToEdit = null
                    showCreateDialog = true 
                },
                containerColor = GoldAccent,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = GoldAccent, ambientColor = GoldAccent)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منبه", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { isEnabled ->
                        AlarmRepository.updateAlarm(context, alarm.copy(isEnabled = isEnabled))
                    },
                    onClick = {
                        alarmToEdit = alarm
                        showCreateDialog = true
                    },
                    onDelete = {
                        AlarmRepository.deleteAlarm(context, alarm.id)
                    },
                    onDuplicate = {
                        AlarmRepository.addAlarm(context, alarm.copy(id = System.currentTimeMillis().toInt()))
                        scope.launch {
                            snackbarHostState.showSnackbar("تم نسخ المنبه بنجاح")
                        }
                    }
                )
            }
            item {
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            add(Calendar.SECOND, 10)
                        }
                        val testAlarm = AlarmItem(
                            id = System.currentTimeMillis().toInt(),
                            isEnabled = true,
                            hour = calendar.get(Calendar.HOUR_OF_DAY),
                            minute = calendar.get(Calendar.MINUTE),
                            days = setOf(),
                            linkedZikrTitle = "تجربة المنبه",
                            sound = "soft_bell.wav",
                            vibration = true,
                            volume = 1.0f,
                            repeatMode = "TEST",
                            autoOpen = false
                        )
                        AlarmRepository.addAlarm(context, testAlarm)
                        scope.launch {
                            snackbarHostState.showSnackbar("سيتم تنشيط المنبه بعد 10 ثوانٍ")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlumCard, contentColor = GoldAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("تجربة المنبه (بعد 10 ثوانٍ)", fontWeight = FontWeight.SemiBold)
                }
            }
            if (alarms.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا يوجد منبهات حالياً", color = WarmWhite.copy(alpha = 0.5f), fontSize = 18.sp)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAlarmDialog(
            initialAlarm = alarmToEdit,
            categories = categories,
            onDismiss = { showCreateDialog = false },
            onSave = { newAlarm ->
                if (alarmToEdit == null) {
                    AlarmRepository.addAlarm(context, newAlarm)
                    scope.launch {
                        snackbarHostState.showSnackbar("تم حفظ المنبه بنجاح")
                    }
                } else {
                    AlarmRepository.updateAlarm(context, newAlarm)
                    scope.launch {
                        snackbarHostState.showSnackbar("تم تحديث المنبه بنجاح")
                    }
                }
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun AlarmCard(alarm: AlarmItem, onToggle: (Boolean) -> Unit, onClick: () -> Unit, onDelete: () -> Unit, onDuplicate: () -> Unit) {
    val amPm = if (alarm.hour < 12) "صباحاً" else "مساءً"
    val displayHour = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
    val displayMinute = String.format(Locale.US, "%02d", alarm.minute)
    
    val remainingTime = remember(alarm.hour, alarm.minute, alarm.days, alarm.repeatMode, alarm.isEnabled) {
        if (alarm.isEnabled) {
            val triggerTime = com.example.alarm.AlarmScheduler.getNextTriggerTime(alarm)
            formatRemainingTime(triggerTime)
        } else ""
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) PlumCard else PlumCard.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$displayHour:$displayMinute",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) WarmWhite else WarmWhite.copy(alpha = 0.5f),
                        
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = amPm,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) GoldAccent else GoldAccent.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 10.dp),
                        
                    )
                }
                
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (alarm.isEnabled) DeepBlue else Color.Gray,
                        checkedTrackColor = GoldAccent,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = PlumCard.copy(alpha = 0.8f)
                    )
                )
            }
            
            if (alarm.isEnabled) {
                Text(
                    text = "المنبه سيعمل: $remainingTime",
                    color = GoldAccent.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                    
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            HorizontalDivider(color = WarmWhite.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Book, 
                            contentDescription = null, 
                            tint = if (alarm.isEnabled) GoldAccent else GoldAccent.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = alarm.linkedZikrTitle,
                            color = if (alarm.isEnabled) WarmWhite else WarmWhite.copy(alpha = 0.5f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val displaySound = when(alarm.sound) {
                        "soft_bell.wav" -> "جرس هادئ"
                        "deep_chime.wav" -> "رنين عميق"
                        "double_beep.wav" -> "تنبيه رقمي"
                        "sound1.wav" -> "أنشودة 1"
                        "sound2.wav" -> "أنشودة 2"
                        else -> alarm.sound
                    }
                    Text(
                        text = "${alarm.repeatMode} • $displaySound",
                        color = WarmWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        
                    )
                }
                
                Row {
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = WarmWhite.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444).copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmDialog(initialAlarm: AlarmItem?, categories: List<com.example.model.Category>, onDismiss: () -> Unit, onSave: (AlarmItem) -> Unit) {
    val context = LocalContext.current
    
    var hour by remember { mutableStateOf(initialAlarm?.hour ?: 8) }
    var minute by remember { mutableStateOf(initialAlarm?.minute ?: 0) }
    var selectedDays by remember { mutableStateOf(initialAlarm?.days ?: setOf(1, 2, 3, 4, 5, 6, 7)) }
    var linkedZikrTitle by remember { mutableStateOf(initialAlarm?.linkedZikrTitle ?: "أذكار الصباح") }
    var sound by remember { mutableStateOf(initialAlarm?.sound ?: "soft_bell.wav") }
    var vibration by remember { mutableStateOf(initialAlarm?.vibration ?: true) }
    var volume by remember { mutableStateOf(initialAlarm?.volume ?: 0.8f) }
    var repeatMode by remember { mutableStateOf(initialAlarm?.repeatMode ?: "كل يوم") }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        DisposableEffect(Unit) {
            val dialog = android.app.TimePickerDialog(
                context,
                { _, h, m ->
                    hour = h
                    minute = m
                    showTimePicker = false
                },
                hour,
                minute,
                false
            )
            dialog.setOnDismissListener {
                showTimePicker = false
            }
            dialog.show()

            onDispose {
                dialog.dismiss()
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = DeepBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        if (initialAlarm == null) "إضافة منبه جديد" else "تعديل المنبه",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        
                    )
                }

                // Time Picker
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showTimePicker = true
                            },
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = PlumCard)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val amPm = if (hour < 12) "صباحاً" else "مساءً"
                            val displayHour = if (hour % 12 == 0) 12 else hour % 12
                            val displayMinute = String.format(Locale.US, "%02d", minute)
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$displayHour:$displayMinute",
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = amPm,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    
                                )
                            }
                        }
                    }
                }

                // Repeat Mode
                item {
                    Text("التكرار", color = GoldAccent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val repeatOptions = listOf("مرة واحدة", "كل يوم", "أيام العمل", "اختيار أيام")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(repeatOptions) { option ->
                            val isSelected = option == repeatMode
                            Surface(
                                modifier = Modifier.clickable {
                                    repeatMode = option
                                    if (option == "مرة واحدة") {
                                        selectedDays = setOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
                                    } else if (option == "كل يوم") {
                                        selectedDays = setOf(1, 2, 3, 4, 5, 6, 7)
                                    } else if (option == "أيام العمل") {
                                        selectedDays = setOf(2, 3, 4, 5, 6) // Mon-Fri
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) GoldAccent else PlumCard
                            ) {
                                Text(
                                    text = option,
                                    color = if (isSelected) PlumCard else WarmWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    
                                )
                            }
                        }
                    }
                }

                // Zikr Selection
                item {
                    var showZikrDropdown by remember { mutableStateOf(false) }
                    val zikrOptions = listOf("أذكار الصباح", "أذكار المساء", "أذكار النوم", "أذكار الصلاة", "دعاء الركوب", "دعاء السفر", "ذكر مخصص")
                    
                    Text("الذكر المرتبط", color = GoldAccent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedCard(
                        onClick = { showZikrDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = PlumCard),
                        border = CardDefaults.outlinedCardBorder(false),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(linkedZikrTitle, color = WarmWhite)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GoldAccent)
                        }
                    }
                    
                    if (showZikrDropdown) {
                        Dialog(onDismissRequest = { showZikrDropdown = false }) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = PlumCard)
                            ) {
                                LazyColumn(modifier = Modifier.padding(16.dp)) {
                                    item { Text("اختر الذكر", color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
                                    items(zikrOptions) { option ->
                                        Text(
                                            text = soundDisplayNames[option] ?: option,
                                            color = WarmWhite,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    linkedZikrTitle = option
                                                    showZikrDropdown = false
                                                }
                                                .padding(vertical = 12.dp),
                                            
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sound Selection
                item {
                    var showSoundDropdown by remember { mutableStateOf(false) }
                    var isPlaying by remember { mutableStateOf(false) }
                    val soundOptions = remember {
                        try {
                            context.assets.list("audio")?.filter { it.endsWith(".mp3") || it.endsWith(".wav") || it.endsWith(".ogg") } ?: listOf("soft_bell.wav", "deep_chime.wav", "double_beep.wav")
                        } catch (e: Exception) {
                            listOf("soft_bell.wav", "deep_chime.wav", "double_beep.wav")
                        }
                    }

                    
                    Text("الصوت", color = GoldAccent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedCard(
                        onClick = { showSoundDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = PlumCard),
                        border = CardDefaults.outlinedCardBorder(false),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(soundDisplayNames[sound] ?: sound, color = WarmWhite)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    if (isPlaying) {
                                        isPlaying = false
                                    } else {
                                        isPlaying = true
                                        try {
                                            val afd = context.assets.openFd("audio/$sound")
                                            val player = android.media.MediaPlayer()
                                            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                            player.prepare()
                                            player.start()
                                            player.setOnCompletionListener { 
                                                isPlaying = false
                                                it.release() 
                                            }
                                        } catch (e: Exception) {
                                            try {
                                                val fallbackAfd = context.assets.openFd("audio/default_alarm.mp3")
                                                val player = android.media.MediaPlayer()
                                                player.setDataSource(fallbackAfd.fileDescriptor, fallbackAfd.startOffset, fallbackAfd.length)
                                                player.prepare()
                                                player.start()
                                                player.setOnCompletionListener { 
                                                    isPlaying = false
                                                    it.release() 
                                                }
                                            } catch (fallbackEx: Exception) {
                                                try {
                                                    val defaultRingtoneUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                                                    val player = android.media.MediaPlayer()
                                                    player.setDataSource(context, defaultRingtoneUri)
                                                    player.prepare()
                                                    player.start()
                                                    player.setOnCompletionListener { 
                                                        isPlaying = false
                                                        it.release() 
                                                    }
                                                } catch (e2: Exception) {
                                                    isPlaying = false
                                                }
                                            }
                                        }
                                    }
                                }) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = "Preview Sound",
                                        tint = GoldAccent
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GoldAccent)
                            }
                        }
                    }
                    
                    if (showSoundDropdown) {
                        Dialog(onDismissRequest = { showSoundDropdown = false }) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = PlumCard)
                            ) {
                                LazyColumn(modifier = Modifier.padding(16.dp)) {
                                    item { Text("اختر الصوت", color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
                                    items(soundOptions) { option ->
                                        Text(
                                            text = soundDisplayNames[option] ?: option,
                                            color = WarmWhite,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    sound = option
                                                    showSoundDropdown = false
                                                }
                                                .padding(vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Extra Settings
                item {
                    Text("إعدادات إضافية", color = GoldAccent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = PlumCard)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الاهتزاز", color = WarmWhite)
                                Switch(
                                    checked = vibration,
                                    onCheckedChange = { vibration = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = GoldAccent, checkedThumbColor = DeepBlue)
                                )
                            }
                            HorizontalDivider(color = WarmWhite.copy(alpha = 0.1f))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("مستوى الصوت", color = WarmWhite)
                                Slider(
                                    value = volume,
                                    onValueChange = { volume = it },
                                    modifier = Modifier.width(130.dp),
                                    colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmWhite)
                        ) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                val searchTitle = when (linkedZikrTitle) {
                                    "أذكار الصلاة" -> "الأذكار بعد السلام من الصلاة"
                                    "أذكار النوم" -> "أذكار النوم"
                                    else -> linkedZikrTitle
                                }
                                var resolvedCategory = categories.find { it.title == searchTitle }
                                if (resolvedCategory == null && linkedZikrTitle == "أذكار النوم") {
                                    resolvedCategory = categories.find { it.title == "أذكار الاستيقاظ من النوم" }
                                }
                                
                                val newAlarmItem = AlarmItem(
                                        id = initialAlarm?.id ?: System.currentTimeMillis().toInt(),
                                        isEnabled = true,
                                        hour = hour,
                                        minute = minute,
                                        days = selectedDays,
                                        linkedZikrTitle = linkedZikrTitle,
                                        linkedZikrId = resolvedCategory?.id ?: -1,
                                        sound = sound,
                                        vibration = vibration,
                                        volume = volume,
                                        repeatMode = repeatMode,
                                        autoOpen = false
                                    )
                                android.util.Log.d("HISN_ALARM", "TRACE: User presses SAVE. Created AlarmItem: id=${newAlarmItem.id}, hour=${newAlarmItem.hour}, min=${newAlarmItem.minute}, repeatMode=${newAlarmItem.repeatMode}, days=${newAlarmItem.days}")
                                onSave(newAlarmItem)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            Text("حفظ", color = PlumCard, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
