package com.example.duration.logManager

import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogManager {
    // Compose 可观察的日志列表
    private val _logs = mutableStateListOf<String>()
    val logs: List<String> get() = _logs

    // 添加日志
    fun add(message: String) {
        val timestamp = System.currentTimeMillis()
        _logs.add("${formatTime(timestamp)} ➜ $message")
    }

    // 清空日志
    fun clear() {
        _logs.clear()
    }

    // 格式化时间
    private fun formatTime(ms: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(ms))
    }
}