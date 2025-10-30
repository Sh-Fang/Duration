package com.example.duration.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.duration.store.AttendanceStore
import com.example.duration.store.DataRepository
import kotlinx.coroutines.launch

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = getApplication()

    // 最早打卡时间
    private val _firstTime = mutableStateOf<String?>(null)
    val firstTime: State<String?> get() = _firstTime

    // 最晚打卡时间
    private val _lastTime = mutableStateOf<String?>(null)
    val lastTime: State<String?> get() = _lastTime

    init {
        // 初始化时读取持久化数据
        refreshTimes()
    }

    // 保存最早打卡时间
    fun saveFirstTime(timestamp: Long) {
        val formatted = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        _firstTime.value = formatted               // 立即刷新 UI
        DataRepository.updateEarliestClockIn(timestamp) // 更新全局状态
        // 异步落盘
        viewModelScope.launch {
            AttendanceStore.saveFirstTime(appContext, timestamp)
        }
    }

    // 保存最晚打卡时间
    fun saveLastTime(timestamp: Long) {
        val formatted = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        _lastTime.value = formatted
        DataRepository.updateLatestClockIn(timestamp)
        viewModelScope.launch {
            AttendanceStore.saveLastTime(appContext, timestamp)
        }
    }

    // 刷新状态（从持久化读取）
    fun refreshTimes() {
        viewModelScope.launch {
            _firstTime.value = AttendanceStore.getFirstTimeString(appContext)
            _lastTime.value = AttendanceStore.getLastTimeString(appContext)
        }
    }
}