package com.example.duration.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.duration.store.AttendancePrefs

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = getApplication()

    // 最早打卡时间
    private val _firstTime = mutableStateOf(AttendancePrefs.getFirstTime(appContext))
    val firstTime: State<String> get() = _firstTime

    // 最晚打卡时间
    private val _lastTime = mutableStateOf(AttendancePrefs.getLastTime(appContext))
    val lastTime: State<String> get() = _lastTime

    // 保存最早打卡时间
//    fun saveFirstTime(timestamp: Long) {
//        val formatted = AttendancePrefs.formatTime(timestamp)
//        _firstTime.value = formatted             // 立即刷新 UI
//        AttendancePrefs.saveFirstTime(appContext, timestamp) // 异步落盘
//    }
//
//    // 保存最晚打卡时间
//    fun saveLastTime(timestamp: Long) {
//        val formatted = AttendancePrefs.formatTime(timestamp)
//        _lastTime.value = formatted;
//        AttendancePrefs.saveLastTime(appContext, timestamp)
//    }

    // 刷新状态（从持久化读取）
    fun refreshTimes() {
        _firstTime.value = AttendancePrefs.getFirstTime(appContext)
        _lastTime.value = AttendancePrefs.getLastTime(appContext)
    }
}