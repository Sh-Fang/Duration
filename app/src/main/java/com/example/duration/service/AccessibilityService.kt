package com.example.duration.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.duration.constant.Global
import com.example.duration.logManager.LogManager
import com.example.duration.sharedPreferences.AttendancePrefs
import java.text.SimpleDateFormat
import java.util.Locale

class AccessibilityService: AccessibilityService() {
    private var lastCheckedPage: String? = null


    private var isTitleTextShow: Boolean = false
    private var isTargetTextShow: Boolean = false

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
            LogManager.add("检测到${Global.TITLE_TEXT}")
            isTitleTextShow = true
        }

        // 遍历节点树
        if (node.text?.contains(Global.TARGET_TEXT) == true) {
            LogManager.add("检测到${Global.TARGET_TEXT}")
            isTargetTextShow = true
        }

        if (isTargetTextShow && isTitleTextShow){
            LogManager.add("同时检测到${Global.TITLE_TEXT}和${Global.TARGET_TEXT}")
            return true
        }

        for (i in 0 until node.childCount) {
            if (checkScreenText(node.getChild(i))) {
                return true
            }
        }

        return false
    }

    // 写入记录
    private fun recordOccurrenceTime() {
        // 当前时间
        val now = System.currentTimeMillis()

        LogManager.add("记录打卡时间")

        AttendancePrefs.saveFirstTime(this, now)
        AttendancePrefs.saveLastTime(this, now)
    }


    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

}