package com.example.duration.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
    viewModel: AttendanceViewModel = viewModel(
        factory = AttendanceViewModelFactory(LocalContext.current)
    ),
    modifier: Modifier = Modifier
) {
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