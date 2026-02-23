package com.accessibility.accessibilityfolder.helper

import android.text.SpannableString
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.accessibility.accessibilityfolder.BACK_ATTEMPTS_LIMIT
import com.accessibility.accessibilityfolder.FoldersAccessibilityService
import com.accessibility.accessibilityfolder.SharedState.currentFileList
import com.accessibility.accessibilityfolder.SharedState.screenNode
import com.accessibility.accessibilityfolder.TAG
import kotlinx.coroutines.delay

object AccessibilityHelper {

    fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var result: AccessibilityNodeInfo? = null

        if (root.isScrollable) {
            if (root.className == "android.widget.GridView") {
                result = root
            }
            Log.d(TAG, "className: ${root.className}")
        }
//        (root.className == "android.widget.GridView"
////                    || root.className ==  "androidx.recyclerview.widget.RecyclerView"
//                    )
//            ) {
//            result = root
//        }
//        else if (root.isScrollable) {
//            Log.d(TAG, "root isScrollable")
//            Log.d(TAG, "className: ${root.className}")
//        }

        for (i in 0 until root.childCount) {
            root.getChild(i)?.let { child ->
                val deeper = findScrollableNode(child)
                if (deeper != null) {
                    result = deeper
                }
            }
        }

        return result
    }

    fun findNodeByContentDescriptionContaining(
        root: AccessibilityNodeInfo,
        text: String
    ): AccessibilityNodeInfo? {

//        val className = root.className.toString()
//        if (className == "android.widget.TextView" || className == "android.widget.Button") {
//            currentFileList.add(root.text.toString())
////            Log.d(TAG, "node text: ${root.text}")
//        }

        val nodeText = root.contentDescription?.toString()
        if (nodeText != null && nodeText.contains(text, ignoreCase = true)) {
            return root
        }

        for (i in 0 until root.childCount) {
            root.getChild(i)?.let {
                val found = findNodeByContentDescriptionContaining(it, text)
                if (found != null) return found
            }
        }
        return null
    }

    fun findNodeByTextContaining(
        rootNode: AccessibilityNodeInfo,
        contentDescription: String
    ): AccessibilityNodeInfo? {
        val nodeText = rootNode.text

        val className = rootNode.className.toString()
//        Log.d(TAG, "node type: $className")
//        Log.d(TAG, "node id: ${rootNode.viewIdResourceName}")

        if (className == "android.widget.TextView" || className == "android.widget.Button") {
            Log.d(TAG, "node text: $nodeText")
        }

        if (nodeText != null) {
            val nodeTextStr = when (nodeText) {
                is String -> nodeText
                is SpannableString -> nodeText.toString()
                else -> null
            }
            if (nodeTextStr != null && nodeTextStr.contains(contentDescription, ignoreCase = false)) {
                Log.d(TAG, "found node 1")
                return rootNode
            }
        }

        for (i in 0 until rootNode.childCount) {
            val childNode = rootNode.getChild(i)
            if (childNode != null) {
                val result = findNodeByTextContaining(childNode, contentDescription)
                if (result != null) {
                    Log.d(TAG, "found node 2")
                    return result
                }
            }
        }

        return null
    }

    fun findNodeByText(
        root: AccessibilityNodeInfo,
        text: String
    ): AccessibilityNodeInfo? {
        val className = root.className.toString()
        if (className == "android.widget.TextView" || className == "android.widget.Button") {
            if (root.text != null) {
                currentFileList.add(root.text.toString())
            }
        }

        val nodeText = root.text?.toString()
        if (nodeText != null && nodeText.contains(text, ignoreCase = true)) {
            return root
        }

        for (i in 0 until root.childCount) {
            root.getChild(i)?.let {
                val found = findNodeByText(it, text)
                if (found != null) return found
            }
        }
        return null
    }

    fun getValidNode(node: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        Log.d(TAG, "getValidNode")
        val nodeList = node.findAccessibilityNodeInfosByViewId(id)
        if (nodeList.isNotEmpty()) {
            return nodeList.first()
        }
        return null
    }

    fun pressFolderIfExists(node: AccessibilityNodeInfo?, level: Int) {
        if (node != null && node.text != null) {
            Log.d(TAG, "view text: ${node.text}")
        }
        val parent = node?.parent
        val isParentClickable = parent?.isClickable ?: false
        Log.d(TAG, "parent node is clickable: $isParentClickable")
        try {
            if (isParentClickable) {
                parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
            if (level < 2) {
                pressFolderIfExists(parent, level + 1)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun findClickableAncestor(node: AccessibilityNodeInfo?, retries: Int = 4) {
//        val nodes = root.findAccessibilityNodeInfosByText(targetText)
//        for (node in nodes) {
            var current: AccessibilityNodeInfo? = node
            repeat(retries) {
                if (current != null && current?.text != null) {
                    Log.d(TAG, "view text: ${current?.text}")
                }
                Log.d(TAG, "node is clickable: ${current?.isClickable}")

                if (current?.isClickable == true) {
                    FoldersAccessibilityService.instance?.pressNode(current)
                    return
                }
                current = current?.parent
            }
//        }
    }

    suspend fun exitFilePicker(attempt: Int) {
        Log.d(TAG, "exitFilePicker attempt: $attempt")
        if (attempt >= BACK_ATTEMPTS_LIMIT) return

        val root = screenNode
        val pkg = root?.packageName?.toString()

        if (pkg != null && pkg.contains("documentsui")) {
            FoldersAccessibilityService.instance?.pressBack()
            delay(1000)
            exitFilePicker(attempt + 1) // still inside picker → back again
        }
    }

}