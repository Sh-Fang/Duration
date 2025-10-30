// MainActivity.kt
package com.example.duration

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.duration.ui.theme.DurationTheme
import android.content.Intent
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.duration.constant.Global
import com.example.duration.logManager.LogManager
import com.example.duration.service.AccessibilityService
import androidx.lifecycle.lifecycleScope
import com.example.duration.store.AttendanceStore
import com.example.duration.ui.composable.AccessibilitySwitch
import com.example.duration.ui.composable.AttendanceRecord
import com.example.duration.ui.composable.LogView

class MainActivity : ComponentActivity() {
    // 无障碍是否开启的标志
    private val isEnabled = mutableStateOf(false)
    val service = AccessibilityService()

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
                        val context = LocalContext.current

                        Column (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Center,      // 垂直居中
                            horizontalAlignment = Alignment.CenterHorizontally // 水平居中
                        ){

                            // 无障碍开关
                            AccessibilitySwitch(
                                isEnabled = isEnabled,
                                context = context,
                                // 回调函数，打开无障碍设置
                                openAccessibilitySettings = { ctx ->
                                    service.openAccessibilitySettings(ctx)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))


                            // 打卡记录显示
                            AttendanceRecord()
                            Spacer(modifier = Modifier.height(16.dp))

                            // 日志输出框
                            LogView()


                        }

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到前台时刷新状态
        isEnabled.value = service.isAccessibilityServiceEnabled(this, AccessibilityService::class.java)

        // 检查是否需要结算昨天的加班（DataStore）
        lifecycleScope.launchWhenResumed {
            AttendanceStore.accumulateOvertimeIfNewDay(this@MainActivity)
        }
    }
}