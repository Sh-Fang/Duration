package com.example.duration.ui.composable

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AccessibilitySwitch(
    isEnabled: MutableState<Boolean>,
    context: Context,
    openAccessibilitySettings: (Context) -> Unit
) {
    // 开关
    Switch(
        checked = isEnabled.value,
        onCheckedChange = { checked ->
            openAccessibilitySettings(context)
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            uncheckedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF4CAF50),
            uncheckedTrackColor = Color(0xFFF44336)
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 状态文本
    Text("无障碍是否开启: ${if (isEnabled.value) "已开启" else "未开启"}")
}