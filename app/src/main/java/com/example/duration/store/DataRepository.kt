package com.example.duration.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DataRepository {

    // 最早打卡时间，初始为 null
    private val _earliestClockIn = MutableStateFlow<Long?>(null)
    val earliestClockIn: StateFlow<Long?> get() = _earliestClockIn

    // 最晚打卡时间，初始为 null
    private val _latestClockIn = MutableStateFlow<Long?>(null)
    val latestClockIn: StateFlow<Long?> get() = _latestClockIn

    // Service 或其他模块调用更新最早打卡时间
    fun updateEarliestClockIn(time: Long) {
        _earliestClockIn.value = time
    }

    // Service 或其他模块调用更新最晚打卡时间
    fun updateLatestClockIn(time: Long) {
        _latestClockIn.value = time
    }
}