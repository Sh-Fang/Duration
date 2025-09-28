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
import android.view.accessibility.AccessibilityManager
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    // 无障碍是否开启的标志
    private val isEnabled = mutableStateOf(false)

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



                            Switch(
                                checked = isEnabled.value,
                                // 切换开关时去无障碍设置界面
                                onCheckedChange = { checked ->
                                    openAccessibilitySettings(context)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50),   // 开启时轨道绿色
                                    uncheckedTrackColor = Color(0xFFF44336)  // 关闭时轨道红色
                                )

                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("无障碍是否开启: ${if (isEnabled.value) "已开启" else "未开启"}")

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
            return true
        }
    }
    return false
}

/** 打开无障碍设置 */
fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}
