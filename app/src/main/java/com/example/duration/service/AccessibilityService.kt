package com.example.duration.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.duration.constant.Global
import com.example.duration.logManager.LogManager

class AccessibilityService: AccessibilityService() {

    override fun onServiceConnected() {
        LogManager.add("AccessibilityService 已连接")
        Log.d(Global.LOG_TAG,"AccessibilityService 已连接")
    }

    // 监听无障碍事件
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // 延迟十几毫秒后再抓树，避免过早拿到空树
            val svc = this
            val captured = AccessibilityEvent.obtain(event)
            mainExecutor.execute {
                svc.mainLooper.queue.addIdleHandler {
                    // 再次轻微延时（~16ms）
                    svc.mainExecutor.execute {
                        TextDetectorService.handleWindowChange(svc, captured)
                        captured.recycle()
                    }
                    false
                }
            }
        }
    }

    override fun onInterrupt() {
        LogManager.add("AccessibilityService 被中断")
        Log.d(Global.LOG_TAG,"AccessibilityService 被中断")
    }

    /** 检测是否开启 */
    fun isAccessibilityServiceEnabled(context: Context, service: Class<out com.example.duration.service.AccessibilityService>): Boolean {
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

}