package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.model.SettingsManager
import com.example.ui.HisnAlMuslimApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var crashDump by mutableStateOf<String?>(null)
    private var currentIntent by mutableStateOf<android.content.Intent?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent = intent
        
        if (savedInstanceState == null) {
            val dao = com.example.model.AppDatabase.getDatabase(this).zikrDao()
            lifecycleScope.launch {
                dao.clearAllProgress()
            }
        }
        
        enableEdgeToEdge()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            val stackTrace = exception.stackTraceToString()
            runOnUiThread {
                crashDump = stackTrace
            }
            defaultHandler?.uncaughtException(thread, exception)
        }

        val settingsManager = SettingsManager.getInstance(this)

        setContent {
            val themeMode by settingsManager.themeMode.collectAsState()
            val textSizeMultiplier by settingsManager.textSizeMultiplier.collectAsState()
            val accentColorIndex by settingsManager.appAccentColorIndex.collectAsState()
            
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(
                darkTheme = isDark,
                textSizeMultiplier = textSizeMultiplier,
                accentColorIndex = accentColorIndex
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (crashDump != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red.copy(alpha = 0.2f))
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp)
                            ) {
                                Text("CRASH DETECTED", color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(crashDump ?: "", color = Color.White)
                            }
                        } else {
                            HisnAlMuslimApp(intent = currentIntent, onIntentHandled = {
                                intent.data = null
                                currentIntent = null
                            })
                        }
                    }
                }
            }
        }
    }
}
