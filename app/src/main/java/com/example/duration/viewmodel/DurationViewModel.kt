// viewmodel/DurationViewModel.kt
package com.example.duration.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import com.example.duration.model.DurationData
import com.example.duration.model.DurationUtils

@RequiresApi(Build.VERSION_CODES.O)
class DurationViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DurationData())
    val state = _state.asStateFlow()

    init {
        loadSavedData()
        resetIfNeeded()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadSavedData() {
        val savedData = DurationData.fromPreferences(getApplication())
        _state.value = savedData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun resetIfNeeded() {
        val today = LocalDate.now()
        val currentState = _state.value
        val lastResetDate = currentState.lastResetDate
        
        var shouldSave = false
        var newData = currentState.copy()
        
        // 每日重置
        if (DurationUtils.shouldResetDaily(today, lastResetDate)) {
            newData = newData.copy(
                todayCycles = 0,
                lastResetDate = today
            )
            shouldSave = true
        }
        
        // 每周重置
        if (DurationUtils.shouldResetWeekly(today, lastResetDate)) {
            newData = newData.copy(
                weekCycles = 0
            )
            shouldSave = true
        }
        
        // 每月重置
        if (DurationUtils.shouldResetMonthly(today, lastResetDate)) {
            newData = newData.copy(
                monthCycles = 0
            )
            shouldSave = true
        }
        
        if (shouldSave) {
            _state.value = newData
            saveData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIn() {
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val cycles = DurationUtils.calculateCycles(today, now)

        _state.value = _state.value.copy(
            lastCheckIn = now,
            todayCycles = cycles,
            weekCycles = _state.value.weekCycles + cycles,
            monthCycles = _state.value.monthCycles + cycles
        )
        
        saveData()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData() {
        viewModelScope.launch {
            DurationData.saveToPreferences(getApplication(), _state.value)
        }
    }
}
