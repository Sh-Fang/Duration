package com.example.duration.service

import android.content.Context
import com.example.duration.logManager.LogManager
import com.example.duration.store.AttendancePrefs
import com.example.duration.viewmodel.AttendanceViewModel

object RecordService {
    fun saveRecord() {
        LogManager.add("保存记录")
    }

    fun getRecord() {
        LogManager.add("获取记录")
    }
}