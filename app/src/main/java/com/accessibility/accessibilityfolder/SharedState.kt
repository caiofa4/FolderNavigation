package com.accessibility.accessibilityfolder

import android.view.accessibility.AccessibilityNodeInfo

object SharedState {
    var isRunning = false
    var screenNode: AccessibilityNodeInfo? = null

    var previousFileList: MutableList<String> = arrayListOf()
    var currentFileList: MutableList<String> = arrayListOf()
}