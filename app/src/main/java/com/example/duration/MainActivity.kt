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
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.duration.ui.theme.DurationTheme
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.duration.constant.Global
import com.example.duration.logManager.LogManager
import com.example.duration.service.AccessibilityService
import com.example.duration.sharedPreferences.AttendancePrefs
import com.example.duration.ui.composable.AccessibilitySwitch
import com.example.duration.ui.composable.AttendanceRecord
import com.example.duration.ui.composable.LogView

class MainActivity : ComponentActivity() {
    // 无障碍是否开启的标志
    private val isEnabled = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 每次启动应用时，检查是否需要结算昨天的加班
        AttendancePrefs.accumulateOvertimeIfNewDay(this)


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
                                    openAccessibilitySettings(ctx)
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))


                            // 打卡记录显示
                            AttendanceRecord()

                            Spacer(modifier = Modifier.height(16.dp))

                            // 日志输出框
                            LogView()

                            Spacer(modifier = Modifier.height(16.dp))

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到前台时刷新状态
        isEnabled.value = isAccessibilityServiceEnabled(this, AccessibilityService::class.java)

        // 检查是否需要结算昨天的加班
        AttendancePrefs.accumulateOvertimeIfNewDay(this)
    }
}

/** 检测是否开启 */
fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
    // 获取系统中所有开启了无障碍的app的包名
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    // 创建冒号分隔器
    val colonSplitter = TextUtils.SimpleStringSplitter(':')

    // 以冒号为分隔
    colonSplitter.setString(enabledServices)

    // 拼接Duration的完整包名
    val serviceName = ComponentName(context, service).flattenToString()

    // 遍历无障碍列表中是否有Duration的包名
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(serviceName, ignoreCase = true)) {  // 忽略大小写比较是否相同字符串
            Log.d(Global.LOG_TAG,"无障碍已开启 Success")
            LogManager.add("无障碍已开启 Success")
            return true
        }
    }

    Log.d(Global.LOG_TAG,"无障碍未开启 False")
    LogManager.add("无障碍未开启 False")
    return false
}

/** 打开无障碍设置 */
fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}