package com.example.duration.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.duration.sharedPreferences.AttendancePrefs

class AttendanceViewModel(private val context: Context) : ViewModel() {
    // Compose 可观察的状态
    private val _firstTime = mutableStateOf(AttendancePrefs.getFirstTime(context))
    val firstTime: State<String> get() = _firstTime

    private val _lastTime = mutableStateOf(AttendancePrefs.getLastTime(context))
    val lastTime: State<String> get() = _lastTime

    // 保存最早打卡时间
    fun saveFirstTime(timestamp: Long) {
        AttendancePrefs.saveFirstTime(context, timestamp)
        _firstTime.value = AttendancePrefs.getFirstTime(context) // 更新 UI
    }

    // 保存最晚打卡时间
    fun saveLastTime(timestamp: Long) {
        AttendancePrefs.saveLastTime(context, timestamp)
        _lastTime.value = AttendancePrefs.getLastTime(context) // 更新 UI
    }

    // 可选：刷新状态（从持久化读取）
    fun refreshTimes() {
        _firstTime.value = AttendancePrefs.getFirstTime(context)
        _lastTime.value = AttendancePrefs.getLastTime(context)
    }
}