package com.accessibility.accessibilityfolder

import com.accessibility.accessibilityfolder.AppPackageNames.documentUI

object AppPackageNames {
    const val documentUI = "com.google.android.documentsui"
    const val chatGPT = "com.openai.chatgpt"
}

object ViewIds {
    const val DIR_LIST = "$documentUI:id/dir_list"
    const val MENU_CONTAINER = "$documentUI:id/container_roots"
    const val SELECT_FOLDER_BUTTON = "android:id/button1"
    const val SELECT_FOLDER_BUTTON_2 = "android:id/button2"
    const val ROOT_FOLDER_BUTTON_TITLE = "android:id/title"
//    const val ROOT_FOLDER_BUTTON_TITLE = "android:id/title"
}

const val TAG = "TESTTAG"
const val BACK_ATTEMPTS_LIMIT = 10