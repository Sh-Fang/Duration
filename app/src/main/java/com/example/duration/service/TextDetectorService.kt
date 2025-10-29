package com.example.duration.service

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.duration.constant.Global
import com.example.duration.logManager.LogManager
import com.example.duration.store.DataRepository


object TextDetectorService {
    private var lastCheckedPage: String? = null
    private var isTitleTextShow: Boolean = false
    private var isTargetTextShow: Boolean = false

    // 处理窗口文字检测
    fun handleWindowChange(event: AccessibilityEvent) {
        val currentPage = "${event.packageName}_${event.className}"

        if (currentPage == lastCheckedPage) {
            // 同一页面，已经检测过，不重复
            return
        }

        lastCheckedPage = currentPage

        // 每次检查初始化
        isTitleTextShow = false
        isTargetTextShow = false

        // 递归检查
        if (isTextExist(event.source)){
            Log.d("TextDetectorService","打卡记录成功")
            LogManager.add("打卡记录成功")
            // 如果找到，更新全局状态
            DataRepository.updateEarliestClockIn(System.currentTimeMillis())
            DataRepository.updateLatestClockIn(System.currentTimeMillis())
        }
    }

    // 检测文字
    private fun isTextExist(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        // 不能写成一行，因为TARGET_TEXT和TITLE_TEXT可能分布在不同的node中，所以需要分开检测
        if (node.text?.contains(Global.TITLE_TEXT) == true) {
            isTitleTextShow = true
        }

        if (node.text?.contains(Global.TARGET_TEXT) == true) {
            isTargetTextShow = true
        }

        if (isTargetTextShow && isTitleTextShow){
            return true
        }

        // 遍历节点树
        for (i in 0 until node.childCount) {
            if (isTextExist(node.getChild(i))) {
                return true
            }
        }

        return false
    }
}