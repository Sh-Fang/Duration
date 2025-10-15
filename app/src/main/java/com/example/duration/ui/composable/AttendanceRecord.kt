package com.example.duration.ui.composable

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.duration.viewmodel.AttendanceViewModel
import com.example.duration.viewmodel.AttendanceViewModelFactory

@Composable
fun AttendanceRecord(
    modifier: Modifier = Modifier
) {
    // 获取 Application Context
    val application = LocalContext.current.applicationContext as Application

    // 使用安全的 Factory 创建 ViewModel
    val viewModel: AttendanceViewModel = viewModel(
        factory = AttendanceViewModelFactory(application)
    )

    val lifecycleOwner = LocalLifecycleOwner.current

    // 当生命周期进入 RESUME 时自动刷新
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshTimes()
        }
    }

    val firstTime by viewModel.firstTime
    val lastTime by viewModel.lastTime

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Text("最早打卡时间: $firstTime")
        Text("最晚打卡时间: $lastTime")
    }
}