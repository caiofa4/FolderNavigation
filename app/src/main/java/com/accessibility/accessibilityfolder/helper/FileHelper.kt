package com.accessibility.accessibilityfolder.helper

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

object FileHelper {
    fun getSelectedFilename(context: Context, uri: Uri): String {
        val selectedFile = copyUriToCache(context, uri)
        val path = selectedFile.absolutePath
        return path.split("/").last()
    }

    fun copyUriToCache(context: Context, uri: Uri): File {
        val fileName = getFileName(context, uri)
        val file = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown"

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(0)
            }
        }

        return name
    }
}