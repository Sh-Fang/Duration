package com.example.duration.sharedPreferences

import android.annotation.SuppressLint
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import java.util.Calendar

object AttendancePrefs {
    private const val PREF_NAME = "attendance_prefs"
    private const val KEY_FIRST = "first_time"
    private const val KEY_LAST = "last_time"
    private const val KEY_TOTAL_OVERTIME = "total_overtime" // 累计加班时长，单位：分钟
    private const val KEY_LAST_DATE = "last_date"           // 上次记录的日期，用来判断是否换天

    private val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val sdfDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    fun saveFirstTime(context: Context, timestamp: Long) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = sp.getLong(KEY_FIRST, 0L)
        if (current == 0L || timestamp < current) {
            sp.edit().putLong(KEY_FIRST, timestamp).apply()
        }
    }

    fun saveLastTime(context: Context, timestamp: Long) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = sp.getLong(KEY_LAST, 0L)
        if (timestamp > current) {
            sp.edit { putLong(KEY_LAST, timestamp) }
        }
    }

    fun getFirstTime(context: Context): String {
        val ts = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_FIRST, 0L)
        return if (ts > 0) sdfTime.format(Date(ts)) else "--:--"
    }

    fun getLastTime(context: Context): String {
        val ts = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST, 0L)
        return if (ts > 0) sdfTime.format(Date(ts)) else "--:--"
    }

    // 计算今日加班时长（分钟）
    // 计算今日加班时长（半小时为一个周期）
    private fun calcTodayOvertimeMinutes(context: Context): Int {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val last = sp.getLong(KEY_LAST, 0L)
        if (last == 0L) return 0

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 19)
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val nineteenThirty = calendar.timeInMillis

        if (last <= nineteenThirty) return 0

        val diffMinutes = ((last - nineteenThirty) / (1000 * 60)).toInt()
        // 按 30 分钟为单位取整
        return (diffMinutes / 30) * 30
    }

    fun getTodayOvertime(context: Context): String {
        val minutes = calcTodayOvertimeMinutes(context)
        val h = minutes / 60
        val m = minutes % 60
        return "${h}小时${m}分"
    }

    // 累计加班时长（每天只算一次）
    fun accumulateOvertimeIfNewDay(context: Context) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val today = sdfDate.format(Date())
        val lastDate = sp.getString(KEY_LAST_DATE, "")

        if (today != lastDate) {
            val todayMinutes = calcTodayOvertimeMinutes(context)
            val total = sp.getInt(KEY_TOTAL_OVERTIME, 0)
            sp.edit {
                putInt(KEY_TOTAL_OVERTIME, total + todayMinutes)
                    .putString(KEY_LAST_DATE, today)
                    .remove(KEY_FIRST) // 新的一天清空
                    .remove(KEY_LAST)
            }
        }
    }

    fun getTotalOvertime(context: Context): String {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val minutes = sp.getInt(KEY_TOTAL_OVERTIME, 0)
        val h = minutes / 60
        val m = minutes % 60
        return "${h}小时${m}分"
    }
}
