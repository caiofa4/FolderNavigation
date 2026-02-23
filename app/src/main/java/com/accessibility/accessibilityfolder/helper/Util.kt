package com.accessibility.accessibilityfolder.helper

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.accessibility.accessibilityfolder.TAG
import kotlin.math.min

object Util {

    fun getFolderList(): List<String> {
        return mutableListOf("FolderA", "SubfolderB", "TargetFile.pdf")
    }

    fun getBestDeviceName(context: Context): String {
        val deviceName = Settings.Global.getString(
            context.contentResolver,
            Settings.Global.DEVICE_NAME
        )

        return deviceName ?: "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun snapshotVisibleTexts(root: AccessibilityNodeInfo): Set<String> {
        val result = mutableSetOf<String>()

        fun traverse(node: AccessibilityNodeInfo) {
            node.text?.toString()?.let { result.add(it) }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { traverse(it) }
            }
        }

        traverse(root)
        return result
    }

    fun printList(list: List<String>) {
        var text = ""
        for (item in list) {
            text += "$item, "
        }
        Log.i(TAG, text)
    }

    fun areFolderListsEqual(list1: List<String>, list2: List<String>): Boolean {
        val min = min(list1.size, list2.size)
        if (min == 0) return false
        return list1[0] == list2[0]
    }
}