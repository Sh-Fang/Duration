// model/DurationData.kt
package com.example.duration.model

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class DurationData(
    val lastCheckIn: LocalDateTime? = null,
    val todayCycles: Int = 0,
    val weekCycles: Int = 0,
    val monthCycles: Int = 0,
    val lastResetDate: LocalDate? = null,
    val lastRecordedTodayCycles: Int = 0 // New field
) {
    companion object {
        private const val PREFS_NAME = "DurationData"
        private const val KEY_LAST_CHECK_IN = "last_check_in"
        private const val KEY_TODAY_CYCLES = "today_cycles"
        private const val KEY_WEEK_CYCLES = "week_cycles"
        private const val KEY_MONTH_CYCLES = "month_cycles"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_LAST_RECORDED_TODAY_CYCLES = "last_recorded_today_cycles" // New key
        
        @RequiresApi(Build.VERSION_CODES.O)
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        @RequiresApi(Build.VERSION_CODES.O)
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromPreferences(context: Context): DurationData {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            val lastCheckInStr = prefs.getString(KEY_LAST_CHECK_IN, null)
            val lastCheckIn = if (lastCheckInStr != null) {
                LocalDateTime.parse(lastCheckInStr, formatter)
            } else null
            
            val lastResetDateStr = prefs.getString(KEY_LAST_RESET_DATE, null)
            val lastResetDate = if (lastResetDateStr != null) {
                LocalDate.parse(lastResetDateStr, dateFormatter)
            } else null
            
            return DurationData(
                lastCheckIn = lastCheckIn,
                todayCycles = prefs.getInt(KEY_TODAY_CYCLES, 0),
                weekCycles = prefs.getInt(KEY_WEEK_CYCLES, 0),
                monthCycles = prefs.getInt(KEY_MONTH_CYCLES, 0),
                lastResetDate = lastResetDate,
                lastRecordedTodayCycles = prefs.getInt(KEY_LAST_RECORDED_TODAY_CYCLES, 0) // Read new field
            )
        }
        
        @RequiresApi(Build.VERSION_CODES.O)
        fun saveToPreferences(context: Context, data: DurationData) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(prefs.edit()) {
                data.lastCheckIn?.let {
                    putString(KEY_LAST_CHECK_IN, it.format(formatter))
                } ?: run {
                    remove(KEY_LAST_CHECK_IN)
                }
                
                putInt(KEY_TODAY_CYCLES, data.todayCycles)
                putInt(KEY_WEEK_CYCLES, data.weekCycles)
                putInt(KEY_MONTH_CYCLES, data.monthCycles)
                putInt(KEY_LAST_RECORDED_TODAY_CYCLES, data.lastRecordedTodayCycles) // Save new field
                
                data.lastResetDate?.let {
                    putString(KEY_LAST_RESET_DATE, it.format(dateFormatter))
                } ?: run {
                    remove(KEY_LAST_RESET_DATE)
                }
                
                apply()
            }
        }
    }
}

object DurationUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateCycles(today: LocalDate, now: LocalDateTime = LocalDateTime.now()): Int {
        val start = LocalDateTime.of(today, LocalTime.of(19, 30))
        return if (now.isAfter(start)) {
            (Duration.between(start, now).toMinutes() / 30).toInt()
        } else 0
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun shouldResetDaily(currentDate: LocalDate, lastResetDate: LocalDate?): Boolean {
        return lastResetDate == null || !currentDate.isEqual(lastResetDate)
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun shouldResetWeekly(currentDate: LocalDate, lastResetDate: LocalDate?): Boolean {
        if (lastResetDate == null) return true
        val currentWeek = currentDate.dayOfYear / 7
        val lastResetWeek = lastResetDate.dayOfYear / 7
        return currentDate.year > lastResetDate.year || currentWeek > lastResetWeek
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun shouldResetMonthly(currentDate: LocalDate, lastResetDate: LocalDate?): Boolean {
        return lastResetDate == null || currentDate.month != lastResetDate.month || currentDate.year != lastResetDate.year
    }
}
