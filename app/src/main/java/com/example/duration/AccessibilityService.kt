package com.example.duration

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AccessibilityService: AccessibilityService() {
    private var lastCheckedPage: String? = null


    private var isTitleTextShow: Boolean = false
    private var isTargetTextShow: Boolean = false

    // 定义日期格式
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


    // 监听无障碍事件
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    handleWindowChange(event)
                }
            }
        }
    }

    // 处理窗口文字检测
    private fun handleWindowChange(event: AccessibilityEvent) {
        val currentPage = "${event.packageName}_${event.className}"

        if (currentPage == lastCheckedPage) {
            // 同一页面，已经检测过，不重复
            return
        }

        lastCheckedPage = currentPage

        isTitleTextShow = false
        isTargetTextShow = false

        if (checkScreenText(event.source)){
            recordOccurrenceTime()
        }
    }

    // 检测文字
    private fun checkScreenText(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.text?.contains(Global.TITLE_TEXT) == true) {
            isTitleTextShow = true
        }

        // 遍历节点树
        if (node.text?.contains(Global.TARGET_TEXT) == true) {
            isTargetTextShow = true
        }

        if (isTargetTextShow && isTitleTextShow){
            return true
        }

        for (i in 0 until node.childCount) {
            if (checkScreenText(node.getChild(i))) {
                return true
            }
        }

        return false
    }

    // 遍历节点检测
    private fun recordOccurrenceTime() {
        // 当前时间
        val timestamp = System.currentTimeMillis()
        // 格式化成字符串
        val formattedTime = sdf.format(Date(timestamp))

        Log.d(Global.LOG_TAG, "文字首次出现时间: $formattedTime")

        // 也可以存入 SharedPreferences 或数据库
    }


    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

}