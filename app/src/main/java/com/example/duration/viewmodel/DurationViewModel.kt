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
        
        // Daily reset
        if (DurationUtils.shouldResetDaily(today, lastResetDate)) {
            newData = newData.copy(
                todayCycles = 0,
                lastRecordedTodayCycles = 0, // Reset last recorded cycles for the new day
                lastResetDate = today
            )
            shouldSave = true
        }
        
        // Weekly reset
        if (DurationUtils.shouldResetWeekly(today, lastResetDate)) {
            newData = newData.copy(
                weekCycles = 0
            )
            // If daily reset didn't happen on the same day as weekly reset,
            // ensure lastResetDate is updated for weekly logic.
            // This also ensures lastResetDate is set if it was initially null and shouldResetDaily might not have set it
            // (though shouldResetDaily should handle null lastResetDate and set it to today).
            if (newData.lastResetDate == null || !newData.lastResetDate.isEqual(today)) {
                 newData = newData.copy(lastResetDate = today)
            }
            shouldSave = true
        }
        
        // Monthly reset
        if (DurationUtils.shouldResetMonthly(today, lastResetDate)) {
            newData = newData.copy(
                monthCycles = 0
            )
            // Similar to weekly, ensure lastResetDate is updated.
            if (newData.lastResetDate == null || !newData.lastResetDate.isEqual(today)) {
                 newData = newData.copy(lastResetDate = today)
            }
            shouldSave = true
        }
        
        if (shouldSave) {
            _state.value = newData
            saveData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIn() {
        resetIfNeeded() // Call this first to ensure state is up-to-date for the current day

        val now = LocalDateTime.now()
        val today = LocalDate.now() // `today` is consistent with what resetIfNeeded uses

        val currentTodayCycles = DurationUtils.calculateCycles(today, now)
        
        // _state.value reflects changes from resetIfNeeded (if any).
        // For example, lastRecordedTodayCycles would be 0 if a daily reset just occurred.
        // And lastResetDate would be 'today'.
        val previousRecordedTodayCycles = _state.value.lastRecordedTodayCycles

        // Calculate newly added cycles.
        // If it's a new day (resetIfNeeded made lastRecordedTodayCycles = 0), cyclesToAdd = currentTodayCycles.
        // If it's a subsequent check-in on the same day, cyclesToAdd = currentTodayCycles - previousRecordedTodayCycles.
        val cyclesToAdd = currentTodayCycles - previousRecordedTodayCycles

        _state.value = _state.value.copy(
            lastCheckIn = now,
            todayCycles = currentTodayCycles, // This is the total for today
            weekCycles = _state.value.weekCycles + cyclesToAdd.coerceAtLeast(0), // Add only the new cycles
            monthCycles = _state.value.monthCycles + cyclesToAdd.coerceAtLeast(0), // Add only the new cycles
            lastRecordedTodayCycles = currentTodayCycles // Store current total for today for the next calculation
        )

        saveData()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun clearAllRecords() {
        _state.value = DurationData( // Reset to default/empty state
            lastCheckIn = null,
            todayCycles = 0,
            weekCycles = 0,
            monthCycles = 0,
            lastResetDate = null, // Set to null for a complete clear
            lastRecordedTodayCycles = 0
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
