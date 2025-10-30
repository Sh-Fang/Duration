package com.example.duration.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.duration.constant.Global
import com.example.duration.logManager.LogManager
import com.example.duration.store.AttendanceStore
import com.example.duration.store.DataRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch


object TextDetectorService {
    private var lastCheckedPage: String? = null
    private var isTitleTextShow: Boolean = false
    private var isTargetTextShow: Boolean = false

    // 处理窗口文字检测
    fun handleWindowChange(service: AccessibilityService, event: AccessibilityEvent) {
        val currentPage = "${event.packageName}_${event.className}"

        if (currentPage == lastCheckedPage) {
            // 同一页面，已经检测过，不重复
            return
        }

        lastCheckedPage = currentPage

        // 每次检查初始化
        isTitleTextShow = false
        isTargetTextShow = false

        // 选取可用根节点：优先 rootInActiveWindow，其次 event.source
        val root: AccessibilityNodeInfo? = service.rootInActiveWindow ?: event.source
        if (root == null) {
            LogManager.add("root/source 为空，无法遍历")
            return
        }

        // 递归检查
        if (isTextExist(root)){
            Log.d("TextDetectorService","打卡记录成功")
            LogManager.add("打卡记录成功")
            // 如果找到，更新全局状态
            val now = System.currentTimeMillis()
            DataRepository.updateEarliestClockIn(now)
            DataRepository.updateLatestClockIn(now)
            // 持久化（DataStore）
            kotlinx.coroutines.GlobalScope.launch {
                AttendanceStore.saveFirstTime(service.applicationContext, now)
                AttendanceStore.saveLastTime(service.applicationContext, now)
            }
        }
    }

    // 检测文字
    private fun isTextExist(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        // 同时检查 text 与 contentDescription（两个目标可能在不同节点），比较前进行空白归一化
        val normalize: (String?) -> String? = { s ->
            s?.replace("\u00A0", " ") // 不换行空格
                ?.replace("\\s+".toRegex(), "") // 去除全部空白
        }
        val textStr = normalize(node.text?.toString())
        val descStr = normalize(node.contentDescription?.toString())
        val titleKey = normalize(Global.TITLE_TEXT)
        val targetKey = normalize(Global.TARGET_TEXT)

        if ((textStr?.contains(titleKey ?: "") == true) ||
            (descStr?.contains(titleKey ?: "") == true)) {
            isTitleTextShow = true
        }

        if ((textStr?.contains(targetKey ?: "") == true) ||
            (descStr?.contains(targetKey ?: "") == true)) {
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