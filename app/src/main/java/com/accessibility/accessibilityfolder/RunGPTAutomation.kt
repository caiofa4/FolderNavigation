package com.accessibility.accessibilityfolder

import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.accessibility.accessibilityfolder.SharedState.currentFileList
import com.accessibility.accessibilityfolder.SharedState.previousFileList
import com.accessibility.accessibilityfolder.SharedState.screenNode
import com.accessibility.accessibilityfolder.SharedState.isRunning
import com.accessibility.accessibilityfolder.ViewIds.MENU_CONTAINER
import com.accessibility.accessibilityfolder.helper.AccessibilityHelper.exitFilePicker
import com.accessibility.accessibilityfolder.helper.AccessibilityHelper.findClickableAncestor
import com.accessibility.accessibilityfolder.helper.AccessibilityHelper.findNodeByContentDescriptionContaining
import com.accessibility.accessibilityfolder.helper.AccessibilityHelper.findNodeByText
import com.accessibility.accessibilityfolder.helper.AccessibilityHelper.findScrollableNode
import com.accessibility.accessibilityfolder.helper.AccessibilityHelper.getValidNode
import com.accessibility.accessibilityfolder.helper.Util.areFolderListsEqual
import com.accessibility.accessibilityfolder.helper.Util.getBestDeviceName
import com.accessibility.accessibilityfolder.helper.Util.getFolderList
import com.accessibility.accessibilityfolder.helper.Util.printList
import com.accessibility.accessibilityfolder.helper.Util.snapshotVisibleTexts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object RunGPTAutomation {
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun run(context: Context) {
        if (isRunning) return
        serviceScope.launch {
            isRunning = true
            Log.d(TAG, "runBlocking")
            val fileNavigationIsOpen = openAttachment()
            if (!fileNavigationIsOpen) return@launch

            val navigationSuccessful = navigateFolders(context)
            if (!navigationSuccessful) return@launch

            val uploadedImageSuccessfully = uploadImage()
            if (!uploadedImageSuccessfully) {
                Log.d(TAG, "Error uploading the image")
                return@launch
            }

            Log.d(TAG, "Image uploaded successfully")
            isRunning = false
        }
    }

    private suspend fun getAttachmentNode(): AccessibilityNodeInfo? {
        var count = 0
        var node: AccessibilityNodeInfo? = null

        while (node == null && count < 10) {
            Log.i(TAG, "count: $count")
            val root = screenNode ?: return null
            node = findNodeByContentDescriptionContaining(root, "Attachment")
            count++
            delay(500)
        }

        return node
    }

    private suspend fun getFilesNode(): AccessibilityNodeInfo? {
        var count = 0
        var node: AccessibilityNodeInfo? = null

        while (node == null && count < 10) {
            val root = screenNode ?: return null
            node = findNodeByText(root, "Files")
            count++
            delay(500L)
        }

        return node
    }

    suspend fun openAttachment(): Boolean {
        val attachmentNode = getAttachmentNode()
        if (attachmentNode == null) {
            Log.i(TAG, "Attachment node not found")
            return false
        }
//        pressFolderIfExists(attachmentNode, 0)
        findClickableAncestor(attachmentNode)

        val filesNode = getFilesNode()
        if (filesNode == null) {
            Log.i(TAG, "Files node not found")
            return false
        }
//        pressFolderIfExists(filesNode, 0)
        findClickableAncestor(filesNode)

        return true
    }

    private fun openMenu(): Boolean {
        val root = screenNode ?: return false
        val menuNode = findNodeByContentDescriptionContaining(root, "Show roots")
        if (menuNode != null) {
            return menuNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            Log.d(TAG, "Didn't find menu button")
        }
        return false
    }

    private fun moveToRootFolder(context: Context): Boolean {
        Log.d(TAG, "moveToRootFolder")
        val root = screenNode ?: return false
        Log.d(TAG, "call getValidNode")
        val menuContainerNode = getValidNode(root, MENU_CONTAINER) ?: return false
        Log.d(TAG, "call getBestDeviceName")
        val deviceName = getBestDeviceName(context)
        val rootFolderTitleNode = findNodeByText(menuContainerNode, deviceName)

        if (rootFolderTitleNode != null) {
            findClickableAncestor(rootFolderTitleNode)
            return true
        } else {
            Log.d(TAG, "Didn't find menu button")
        }
        return false
    }

    private suspend fun navigateFolders(context: Context): Boolean {
        var lastFolderNode: AccessibilityNodeInfo? = null
        val list = getFolderList()

        delay(700)
        val menuIsOpen = openMenu()

        if (!menuIsOpen) {
            Log.d(TAG, "Menu wasn't open, stopping run")
            return false
        }
        Log.d(TAG, "Menu is open")
        delay(1200)
        val rootFolderPressed = moveToRootFolder(context)
        Log.d(TAG, "Root folder is pressed: $rootFolderPressed")

        // Wait for folder navigation screen to open
        delay(1500)
        for (item in list) {
            Log.d(TAG, "***********************************************************")
            Log.d(TAG, "item: $item")
            Log.d(TAG, "***********************************************************")

            lastFolderNode = scrollAndClick(
                item
            )
        }

        if (lastFolderNode == null) {
            exitFilePicker(0)
            return false
        }

        delay(1000)
        return true
    }

    private suspend fun scrollAndClick(
        folderName: String
    ): AccessibilityNodeInfo? {
        repeat(15) { attempt -> // safety limit
            val root = screenNode ?: return null
            Log.d(TAG, "findScrollableNode")
            val scrollable = findScrollableNode(root) // ?: getValidNode(root, DIR_LIST)
            var nodeToPress: AccessibilityNodeInfo? = null
            if (scrollable == null && attempt > 2) {
                Log.d(TAG, "searching node by text")
                nodeToPress = findNodeByText(root, folderName)
            }
            if (scrollable != null) {
                val scrollableClassName = scrollable.className.toString()

                Log.i(TAG, "scrollableClassName: ${scrollableClassName}")

                currentFileList = arrayListOf()
                val folderNode = findNodeByText(scrollable, folderName)
                val snapshotVisibleTexts = snapshotVisibleTexts(scrollable)
                Log.d(TAG, "previousFileList")
                printList(previousFileList)
                Log.d(TAG, "currentFileList")
                printList(currentFileList)
                Log.d(TAG, "snapshotVisibleTexts")
                printList(snapshotVisibleTexts.toList())

                if (currentFileList.isNotEmpty()) {

                    val areFolderListsEqual = areFolderListsEqual(previousFileList, currentFileList)
                    if (areFolderListsEqual) {
                        Log.d(TAG, "didn't update screen")
                    }

                    previousFileList.clear()
                    previousFileList.addAll(currentFileList)

                    if (folderNode != null) {
//                        pressFolderIfExists(folderNode, 0)
                        findClickableAncestor(folderNode)
                        Log.d(TAG, "pressing $folderName")
                        delay(2000)
                        Log.d(TAG, "after delay")
                        return folderNode
                    } else {
                        Log.d(TAG, "didn't find folder/file")
                    }

                    if (!areFolderListsEqual) {
                        Log.d(TAG, "swiping screen")
                        scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        delay(2000)
                    }
                }
            } else if (nodeToPress != null) {
//                pressFolderIfExists(nodeToPress, -2)
                findClickableAncestor(nodeToPress, 6)
                Log.d(TAG, "pressing ${nodeToPress.text}")
                delay(500L)
                return nodeToPress
            } else {
                Log.d(TAG, "scroolable and nodeToPress are null")
                delay(500L)
            }
            Log.d(TAG, "-----------------------------------------------")
        }
        return null
    }

    private suspend fun getSendMessageNode(): AccessibilityNodeInfo? {
        Log.d(TAG, "getSendMessageNode")
        var count = 0
        var node: AccessibilityNodeInfo? = null

        while ((node == null || node.parent == null || node.parent?.isEnabled == false) && count < 20) {
            Log.d(TAG, "finding send message node")
            val root = AccessibilityNodeInfo.obtain(screenNode)
            Log.d(TAG, "root is null: ${root == null}")
            if (root != null) {
                node = findNodeByContentDescriptionContaining(root, "Send Message")
                Log.i(TAG, "node is clickable: ${node?.isClickable}")
                Log.i(TAG, "node is enabled: ${node?.isEnabled}")
                Log.i(TAG, "parent node is clickable: ${node?.parent?.isClickable}")
                Log.i(TAG, "parent node is enabled: ${node?.parent?.isEnabled}")
            }
            count++
            if (node?.parent?.isEnabled == false) {
                delay(1000L)
            }
        }

        Log.d(TAG, "---------------------------------------------------------")
        Log.i(TAG, "node = null: ${node == null}")
        Log.i(TAG, "node.parent?.isEnabled: ${node?.parent?.isEnabled}")
        Log.i(TAG, "count: $count")

        return node
    }

    private suspend fun uploadImage(): Boolean {
        Log.d(TAG, "***********************************************************")
        Log.d(TAG, "Upload Image")
        Log.d(TAG, "***********************************************************")
        val uploadImageNode = getSendMessageNode()
        if (uploadImageNode == null) {
            Log.i(TAG, "Send Message node not found")
            return false
        }
        findClickableAncestor(uploadImageNode)
        return true
    }

}