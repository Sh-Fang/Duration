package com.example.duration

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class PageTextDetector {
    fun detectText(event: AccessibilityEvent, targetPkg: String, targetText: String): Boolean {
        if (event.packageName != targetPkg) return false

        val node = event.source ?: return false
        return nodeHasText(node, targetText)
    }

    private fun nodeHasText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.text?.contains(text) == true) return true
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null && nodeHasText(child, text)) return true
        }
        return false
    }
}