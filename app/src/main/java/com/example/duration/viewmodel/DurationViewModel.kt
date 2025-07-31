// viewmodel/DurationViewModel.kt
package com.example.duration.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import com.example.duration.model.DurationData
import com.example.duration.model.DurationUtils

class DurationViewModel : ViewModel() {

    private val _state = MutableStateFlow(DurationData())
    val state = _state.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIn() {
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val cycles = DurationUtils.calculateCycles(today, now)

        _state.value = _state.value.copy(
            lastCheckIn = now,
            todayCycles = cycles,
            weekCycles = cycles,
            monthCycles = cycles
        )
    }
}
