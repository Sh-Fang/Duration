// ui/screen/DurationTrackerScreen.kt
package com.example.duration.ui.screen

import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.duration.viewmodel.DurationViewModel

@RequiresApi(26)
@Composable
fun DurationTrackerScreen(viewModel: DurationViewModel) {
    val state by viewModel.state.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("确认清除") },
            text = { Text("您确定要清除所有记录吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecords()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Content area: Title, Clear Button, Stats, Last Check-in
        Column(
            modifier = Modifier.weight(1f), // This column takes available vertical space
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp), // Adjusted padding
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "打卡周期统计",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { showClearConfirmDialog = true }) { // Show dialog on click
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Clear all records"
                    )
                }
            }

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp) // Added some bottom padding
                )
            }
        }

        // Record Button at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp), // Padding for the button section from screen bottom
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.checkIn() },
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Button takes 80% of width
                    .height(50.dp)      // Reduced height
            ) {
                Text("立即打卡", style = MaterialTheme.typography.titleMedium)
            }
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
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$value 周期",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "%.1f 小时".format(value * 0.5f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
