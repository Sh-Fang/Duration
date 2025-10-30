package com.example.duration.store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "attendance_prefs")

object AttendanceStore {
    private val KEY_FIRST = longPreferencesKey("first_time")
    private val KEY_LAST = longPreferencesKey("last_time")
    private val KEY_TOTAL_OVERTIME = intPreferencesKey("total_overtime")
    private val KEY_LAST_DATE = stringPreferencesKey("last_date")

    private fun formatTime(timestamp: Long): String {
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdfTime.format(Date(timestamp))
    }

    private fun formatDate(date: Date): String {
        val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdfDate.format(date)
    }

    suspend fun saveFirstTime(context: Context, timestamp: Long) {
        val prefs = context.dataStore.data.first()
        val current = prefs[KEY_FIRST] ?: 0L
        if (current == 0L || timestamp < current) {
            context.dataStore.edit { it[KEY_FIRST] = timestamp }
        }
    }

    suspend fun saveLastTime(context: Context, timestamp: Long) {
        val prefs = context.dataStore.data.first()
        val current = prefs[KEY_LAST] ?: 0L
        if (timestamp > current) {
            context.dataStore.edit { it[KEY_LAST] = timestamp }
        }
    }

    suspend fun getFirstTimeString(context: Context): String {
        val prefs = context.dataStore.data.first()
        val ts = prefs[KEY_FIRST] ?: 0L
        return if (ts > 0) formatTime(ts) else "--:--"
    }

    suspend fun getLastTimeString(context: Context): String {
        val prefs = context.dataStore.data.first()
        val ts = prefs[KEY_LAST] ?: 0L
        return if (ts > 0) formatTime(ts) else "--:--"
    }

    private fun calcTodayOvertimeMinutes(lastTs: Long): Int {
        if (lastTs == 0L) return 0
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 19)
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val nineteenThirty = calendar.timeInMillis
        if (lastTs <= nineteenThirty) return 0
        val diffMinutes = ((lastTs - nineteenThirty) / (1000 * 60)).toInt()
        return (diffMinutes / 30) * 30
    }

    suspend fun getTodayOvertimeString(context: Context): String {
        val prefs = context.dataStore.data.first()
        val last = prefs[KEY_LAST] ?: 0L
        val minutes = calcTodayOvertimeMinutes(last)
        val h = minutes / 60
        val m = minutes % 60
        return "${h}小时${m}分"
    }

    suspend fun accumulateOvertimeIfNewDay(context: Context) {
        val prefs = context.dataStore.data.first()
        val today = formatDate(Date())
        val lastDate = prefs[KEY_LAST_DATE]
        if (today != lastDate) {
            val last = prefs[KEY_LAST] ?: 0L
            val todayMinutes = calcTodayOvertimeMinutes(last)
            val total = prefs[KEY_TOTAL_OVERTIME] ?: 0
            context.dataStore.edit {
                it[KEY_TOTAL_OVERTIME] = total + todayMinutes
                it[KEY_LAST_DATE] = today
                it.remove(KEY_FIRST)
                it.remove(KEY_LAST)
            }
        }
    }

    suspend fun getTotalOvertimeString(context: Context): String {
        val prefs = context.dataStore.data.first()
        val minutes = prefs[KEY_TOTAL_OVERTIME] ?: 0
        val h = minutes / 60
        val m = minutes % 60
        return "${h}小时${m}分"
    }
}


