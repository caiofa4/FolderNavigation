package com.accessibility.accessibilityfolder.model

import android.net.Uri
import androidx.documentfile.provider.DocumentFile

data class FolderPickerState(
    val folderUri: Uri? = null,
    val files: List<DocumentFile> = emptyList(),
    val selectedFile: DocumentFile? = null
)