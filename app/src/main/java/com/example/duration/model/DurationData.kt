// model/DurationData.kt
package com.example.duration.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DurationData(
    val lastCheckIn: LocalDateTime? = null,
    val todayCycles: Int = 0,
    val weekCycles: Int = 0,
    val monthCycles: Int = 0
)

object DurationUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateCycles(today: LocalDate, now: LocalDateTime = LocalDateTime.now()): Int {
        val start = LocalDateTime.of(today, LocalTime.of(19, 30))
        return if (now.isAfter(start)) {
            (Duration.between(start, now).toMinutes() / 30).toInt()
        } else 0
    }
}
