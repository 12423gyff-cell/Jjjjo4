package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppDatabase
import com.example.model.ZikrProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ZikrViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).zikrDao()
    
    val ttsManager = TTSManager(application)
    
    val progressFlow = dao.getAllProgress().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val favoritesFlow = dao.getAllFavorites().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun saveProgress(zikrId: Int, count: Int) {
        viewModelScope.launch {
            dao.saveProgress(ZikrProgress(zikrId, count))
        }
    }

    fun toggleFavorite(zikrId: Int) {
        viewModelScope.launch {
            val isFav = favoritesFlow.value.any { it.zikrId == zikrId }
            if (isFav) {
                dao.removeFavorite(com.example.model.FavoriteZikr(zikrId))
            } else {
                dao.addFavorite(com.example.model.FavoriteZikr(zikrId))
            }
        }
    }
    
    fun resetCounters() {
        viewModelScope.launch {
            dao.clearAllProgress()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
