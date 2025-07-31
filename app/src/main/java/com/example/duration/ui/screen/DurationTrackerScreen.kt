// ui/screen/DurationTrackerScreen.kt
package com.example.duration.ui.screen

import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.duration.viewmodel.DurationViewModel
import java.time.LocalDateTime

@RequiresApi(26)
@Composable
fun DurationTrackerScreen(viewModel: DurationViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "打卡周期统计",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            StatCard(title = "今日周期", value = state.todayCycles)
            Spacer(modifier = Modifier.height(12.dp))
            StatCard(title = "本周周期", value = state.weekCycles)
            Spacer(modifier = Modifier.height(12.dp))
            StatCard(title = "本月周期", value = state.monthCycles)

            Spacer(modifier = Modifier.height(16.dp))

            state.lastCheckIn?.let {
                Text(
                    text = "上次打卡：${it.toLocalTime().withSecond(0).withNano(0)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = { viewModel.checkIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("立即打卡", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun StatCard(title: String, value: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$value 周期",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
