package com.example.duration

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duration.ui.theme.DurationTheme
import java.time.*
import java.time.temporal.WeekFields
import java.util.*

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DurationTheme {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        DurationTrackerScreen()
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DurationTrackerScreen() {
    val today = LocalDate.now()

    var lastCheckIn by remember { mutableStateOf<LocalDateTime?>(null) }
    var todayCycles by remember { mutableIntStateOf(0) }
    var weekCycles by remember { mutableIntStateOf(0) }
    var monthCycles by remember { mutableIntStateOf(0) }

    fun calculateCycles(): Int {
        val now = LocalDateTime.now()
        val start = LocalDateTime.of(today, LocalTime.of(19, 30))
        return if (now.isAfter(start)) {
            (Duration.between(start, now).toMinutes() / 30).toInt()
        } else 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "打卡周期统计",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            StatCard(title = "今日周期", value = todayCycles)
            Spacer(modifier = Modifier.height(12.dp))
            StatCard(title = "本周周期", value = weekCycles)
            Spacer(modifier = Modifier.height(12.dp))
            StatCard(title = "本月周期", value = monthCycles)

            Spacer(modifier = Modifier.height(16.dp))

            lastCheckIn?.let {
                Text(
                    text = "上次打卡：${it.toLocalTime().withSecond(0).withNano(0)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = {
                lastCheckIn = LocalDateTime.now()
                val cycles = calculateCycles()
                todayCycles = cycles
                weekCycles = cycles
                monthCycles = cycles
            },
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
