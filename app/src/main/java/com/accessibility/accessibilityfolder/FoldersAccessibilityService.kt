package com.accessibility.accessibilityfolder

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.accessibility.accessibilityfolder.SharedState.screenNode
import kotlinx.coroutines.channels.Channel

class FoldersAccessibilityService : AccessibilityService() {
    private val scrollEventChannel = Channel<Unit>(Channel.CONFLATED)

    companion object {
        var instance: FoldersAccessibilityService? = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        screenNode = rootInActiveWindow

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            scrollEventChannel.trySend(Unit)
        }
    }

    override fun onInterrupt() {
        Log.d("YoutubeAccessibility", "Service interrupted")
    }

    override fun onServiceConnected() {
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
        Log.d("YoutubeAccessibility", "Service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun pressBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun pressNode(node: AccessibilityNodeInfo?) {
        node?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: run {
            Log.d(TAG, "node is null")
        }
    }

//    private fun launchPoCApp() {
//        Log.d(TAG, "launchPoCApp")
//        val intent = packageManager
//            .getLaunchIntentForPackage("com.accessibility.accessibilityfolder")
//            ?.apply {
//                addFlags(
//                    Intent.FLAG_ACTIVITY_NEW_TASK or
//                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
//                )
//            }
//
//        if (intent != null) {
//            startActivity(intent)
//        }
//    }

} 