package com.example.alarm

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import com.example.MainActivity
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.icu.util.IslamicCalendar

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("HISN_ALARM", "8. AlarmActivity launched for alarmId: ${intent.getIntExtra("alarm_id", -1)}")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val zikrTitle = intent.getStringExtra("zikr_title") ?: "أذكار الصباح"
        val linkedZikrId = intent.getIntExtra("linked_zikr_id", -1)
        val displayZikrTitle = when (zikrTitle) {
            "أذكار الصباح" -> "☀️ أذكار الصباح"
            "أذكار المساء" -> "🌙 أذكار المساء"
            "أذكار النوم" -> "🛏 أذكار النوم"
            "أذكار الصلاة" -> "🕌 أذكار الصلاة"
            else -> zikrTitle
        }
        val sound = intent.getStringExtra("sound") ?: "morning.mp3"
        val useVibration = intent.getBooleanExtra("vibration", true)
        val volume = intent.getFloatExtra("volume", 0.8f)
        val autoOpen = intent.getBooleanExtra("auto_open", false)

        if (useVibration) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
            }
        }

        try {
            val afd = assets.openFd("audio/$sound")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                setVolume(volume, volume)
                isLooping = true
                prepare()
                android.util.Log.d("HISN_ALARM", "9. MediaPlayer started with asset: $sound")
                start()
            }
        } catch (e: Exception) {
            android.util.Log.e("HISN_ALARM", "Failed to start main MediaPlayer: ${e.message}")
            e.printStackTrace()
            // Try fallback
            try {
                val fallbackAfd = assets.openFd("audio/default_alarm.mp3")
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(fallbackAfd.fileDescriptor, fallbackAfd.startOffset, fallbackAfd.length)
                    setVolume(volume, volume)
                    isLooping = true
                    prepare()
                    android.util.Log.d("HISN_ALARM", "9. MediaPlayer started with fallback asset")
                    start()
                }
            } catch (fallbackEx: Exception) {
                android.util.Log.e("HISN_ALARM", "Failed to start fallback MediaPlayer: ${fallbackEx.message}")
                fallbackEx.printStackTrace()
                // Use default system ringtone
                try {
                    val defaultRingtoneUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(this@AlarmActivity, defaultRingtoneUri)
                        setVolume(volume, volume)
                        isLooping = true
                        prepare()
                        android.util.Log.d("HISN_ALARM", "9. MediaPlayer started with system default ringtone")
                        start()
                    }
                } catch (e2: Exception) {
                    android.util.Log.e("HISN_ALARM", "Failed to start default system ringtone: ${e2.message}")
                    e2.printStackTrace()
                }
            }
        }

        setContent {
            var currentTime by remember { mutableStateOf("") }
            var amPm by remember { mutableStateOf("") }
            var dayStr by remember { mutableStateOf("") }
            var hijriDateStr by remember { mutableStateOf("") }
            var gregDateStr by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
                val gregorianFormat = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
                val dayFormat = SimpleDateFormat("EEEE", Locale("ar"))
                
                while (true) {
                    val date = Date()
                    val cal = java.util.Calendar.getInstance()
                    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                    val minute = cal.get(java.util.Calendar.MINUTE)
                    val displayHour = if (hour % 12 == 0) 12 else hour % 12
                    val displayMinute = String.format(java.util.Locale.US, "%02d", minute)
                    currentTime = "$displayHour:$displayMinute"
                    amPm = if (hour < 12) "صباحاً" else "مساءً"
                    
                    gregDateStr = gregorianFormat.format(date)
                    dayStr = dayFormat.format(date)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val islamicCal = IslamicCalendar()
                        val day = islamicCal.get(IslamicCalendar.DAY_OF_MONTH)
                        val month = islamicCal.get(IslamicCalendar.MONTH)
                        val year = islamicCal.get(IslamicCalendar.YEAR)
                        val hijriMonths = arrayOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
                        val monthName = if (month in 0..11) hijriMonths[month] else ""
                        hijriDateStr = "$day $monthName $year"
                    }
                    
                    delay(1000)
                }
            }

            MyApplicationTheme {
                var isVisible by remember { mutableStateOf(false) }
                var isDismissing by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()
                
                LaunchedEffect(Unit) {
                    isVisible = true
                }
                
                val alphaAnim by animateFloatAsState(
                    targetValue = if (isVisible && !isDismissing) 1f else 0f,
                    animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
                    label = "alpha"
                )
                
                val scaleAnim by animateFloatAsState(
                    targetValue = if (isVisible) (if (isDismissing) 0.9f else 1f) else 0.8f,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                    label = "scale"
                )
                
                val cardOffsetAnim by animateDpAsState(
                    targetValue = if (isVisible && !isDismissing) 0.dp else 40.dp,
                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                    label = "cardOffset"
                )
                
                val buttonsOffsetAnim by animateDpAsState(
                    targetValue = if (isVisible && !isDismissing) 0.dp else 60.dp,
                    animationSpec = tween(durationMillis = 450, delayMillis = 50, easing = FastOutSlowInEasing),
                    label = "buttonsOffset"
                )

                val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
                val translationY by infiniteTransition.animateFloat(
                    initialValue = -50f, targetValue = 50f,
                    animationSpec = infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "light_anim"
                )
                
                val DeepBlack = Color(0xFF050505)
                val DarkNavy = Color(0xFF0A1128)
                val SoftGold = Color(0xFFD4AF37)
                
                var isHandled by remember { mutableStateOf(false) }
                
                fun onAction(open: Boolean) {
                    if (isHandled) return
                    isHandled = true
                    isDismissing = true
                    coroutineScope.launch {
                        delay(350)
                        stopAlarm()
                        if (open) {
                            val mainIntent = Intent(this@AlarmActivity, MainActivity::class.java).apply {
                                action = Intent.ACTION_VIEW
                                data = android.net.Uri.parse("hisn://category/$linkedZikrId?fromAlarm=true")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(mainIntent)
                        }
                        finish()
                        overridePendingTransition(0, 0)
                    }
                }

                Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(DarkNavy, DeepBlack),
                                center = Offset(size.width / 2, size.height / 3),
                                radius = size.height * 0.8f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(SoftGold.copy(alpha = 0.08f), Color.Transparent),
                                radius = size.width * 0.8f
                            ),
                            center = Offset(size.width * 0.2f, size.height * 0.1f + translationY)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.15f), Color.Transparent),
                                radius = size.width * 0.9f
                            ),
                            center = Offset(size.width * 0.8f, size.height * 0.8f - translationY)
                        )
                    }
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))
                    
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer {
                                alpha = alphaAnim
                                scaleX = scaleAnim
                                scaleY = scaleAnim
                            }
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = currentTime,
                                    fontSize = 100.sp,
                                    fontWeight = FontWeight.Light,
                                    color = Color(0xFFF8FAFC),
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.White.copy(alpha = 0.15f),
                                            blurRadius = 40f
                                        )
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = amPm,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftGold,
                                    modifier = Modifier.padding(bottom = 20.dp),
                                    fontFamily = com.example.ui.ArabicFont
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val dateText = buildString {
                                append(dayStr)
                                if (hijriDateStr.isNotEmpty()) {
                                    append(" • ")
                                    append(hijriDateStr)
                                }
                                append(" • ")
                                append(gregDateStr)
                            }
                            
                            Text(
                                text = dateText,
                                fontSize = 16.sp,
                                color = Color(0xFF9CA3AF),
                                fontFamily = com.example.ui.ArabicFont,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(64.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = cardOffsetAnim)
                                .graphicsLayer {
                                    alpha = if (isDismissing) 0f else alphaAnim
                                },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.15f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                                    ))
                                    .padding(vertical = 32.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = displayZikrTitle,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC),
                                    fontFamily = com.example.ui.ArabicFont,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "حان وقت هذا الذكر",
                                    fontSize = 16.sp,
                                    color = SoftGold.copy(alpha = 0.9f),
                                    fontFamily = com.example.ui.ArabicFont
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = buttonsOffsetAnim)
                                .graphicsLayer {
                                    alpha = alphaAnim
                                },
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onAction(false) },
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White.copy(alpha = 0.9f),
                                    containerColor = Color.White.copy(alpha = 0.03f)
                                ),
                                modifier = Modifier.weight(1f).height(64.dp)
                            ) {
                                Text("إيقاف", fontSize = 20.sp, fontFamily = com.example.ui.ArabicFont, fontWeight = FontWeight.Medium)
                            }
                            
                            Button(
                                onClick = { onAction(true) },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftGold,
                                    contentColor = DeepBlack
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 8.dp,
                                    pressedElevation = 2.dp
                                ),
                                modifier = Modifier.weight(1f).height(64.dp)
                            ) {
                                Text("فتح الذكر", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = com.example.ui.ArabicFont)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopAlarm() {
        val alarmId = intent.getIntExtra("alarm_id", 0)
        android.util.Log.d("HISN_ALARM", "11. Alarm stopped for alarmId: $alarmId")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(alarmId)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtras(this@AlarmActivity.intent)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            alarmId,
            alarmIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
