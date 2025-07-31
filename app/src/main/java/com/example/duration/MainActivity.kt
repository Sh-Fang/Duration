// MainActivity.kt
package com.example.duration

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.duration.ui.screen.DurationTrackerScreen
import com.example.duration.ui.theme.DurationTheme
import com.example.duration.viewmodel.DurationViewModel
import com.example.duration.work.WorkManagerHelper

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Schedule reset works
        WorkManagerHelper.scheduleResetWorks(this)
        
        setContent {
            DurationTheme {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        val viewModel: DurationViewModel = viewModel()
                        DurationTrackerScreen(viewModel)
                    }
                }
            }
        }
    }
}
