package com.example.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TTSManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentZikrId = MutableStateFlow<Int?>(null)
    val currentZikrId: StateFlow<Int?> = _currentZikrId

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ar"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Arabic not supported, fallback or ignore
            } else {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isPlaying.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentZikrId.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentZikrId.value = null
                    }
                })
            }
        }
    }

    private var speechRate = 1.0f

    fun setSpeechRate(rate: Float) {
        speechRate = rate
        if (isInitialized) {
            tts?.setSpeechRate(rate)
        }
    }

    fun play(text: String, zikrId: Int) {
        if (!isInitialized) return
        stop()
        _currentZikrId.value = zikrId
        tts?.setSpeechRate(speechRate)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Zikr_$zikrId")
    }

    fun stop() {
        if (!isInitialized) return
        tts?.stop()
        _isPlaying.value = false
        _currentZikrId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
