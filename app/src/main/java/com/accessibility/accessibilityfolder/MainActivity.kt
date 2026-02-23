package com.accessibility.accessibilityfolder

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.accessibility.accessibilityfolder.model.FolderPickerState
import com.accessibility.accessibilityfolder.ui.theme.AccessibilityFolderTheme
import androidx.core.net.toUri
import com.accessibility.accessibilityfolder.AppPackageNames.chatGPT
import com.accessibility.accessibilityfolder.SharedState.isRunning
import com.accessibility.accessibilityfolder.helper.FileHelper.getSelectedFilename

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessibilityFolderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val selectedFileState = remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Enable Accessibility Permission")
        }

        FilePickerButton(selectedFileState)

        Text(
            text = selectedFileState.value.ifBlank { "No file selected" },
            modifier = Modifier
                .padding(top = 32.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )


//        FolderPickerExample()
//
//        FolderFilePickerScreen()
    }
}

@Composable
fun FilePickerButton(selectedFileState: MutableState<String>) {
    val context = LocalContext.current
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let {
                // Example: read file content
//                val file = copyUriToCache(context, uri)
                selectedFileState.value = getSelectedFilename(context, uri)
//                Log.d(TAG, "file path: ${file.path}")
//                Log.d(TAG, "file absolutePath: ${file.absolutePath}")
                val text = readTextFromUri(context, it)
                Log.d(TAG, "Content: $text")
            }
        }

    Button(
        onClick = {
            Log.d("TESTTAG", "***************************")
            Log.d("TESTTAG", "Start Navigation")
            openChatGptApp(context)
//            isRunning = true
            RunGPTAutomation.run(context)

            // Open file picker
//            filePickerLauncher.launch(arrayOf("*/*"))
        }
    ) {
        Text("Open ChatGPT")
    }
}

@Composable
fun FolderPickerExample() {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            Log.d("FOLDER_PICKER", "Folder: $uri")
        }

    Button(onClick = { launcher.launch(null) }) {
        Text("Pick folder")
    }
}

@Composable
fun FolderFilePickerScreen() {
    val context = LocalContext.current

    var state by remember {
        mutableStateOf(FolderPickerState())
    }

    val rootUri = "content://com.android.externalstorage.documents/document/primary:".toUri()

    // 📂 Folder picker
    val folderPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult

            // Persist permission
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val files = listFilesInFolder(context, uri)

            state = state.copy(
                folderUri = uri,
                files = files,
                selectedFile = null
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = {
                Log.d("TESTTAG", "***************************")
                Log.d("TESTTAG", "Start Navigation")
//                isRunning = true
                folderPickerLauncher.launch(rootUri)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pick folder")
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.folderUri?.let {
            Text(
                text = "Folder selected:",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.files) { file: DocumentFile ->
                FileRow(
                    file = file,
                    isSelected = file.uri == state.selectedFile?.uri
                ) {
                    state = state.copy(selectedFile = file)
                    Log.i("TESTTAG", "filename: ${state.selectedFile?.name}")
                }
            }
        }
    }
}

@Composable
fun FileRow(
    file: DocumentFile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = file.name ?: "Unnamed",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun listFilesInFolder(
    context: Context,
    folderUri: Uri
): List<DocumentFile> {

    val tree = DocumentFile.fromTreeUri(context, folderUri)
        ?: return emptyList()

    return tree.listFiles()
        .filter { it.isFile }
        .sortedBy { it.name?.lowercase() }
}

fun readTextFromUri(context: Context, uri: Uri): String {
    return context.contentResolver
        .openInputStream(uri)
        ?.bufferedReader()
        ?.use { it.readText() }
        ?: ""
}

fun openChatGptApp(context: Context) {
    val intent = context.packageManager
        .getLaunchIntentForPackage(chatGPT)

    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } else {
        Toast.makeText(
            context,
            "ChatGPT app is not installed",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AccessibilityFolderTheme {
        MainScreen()
    }
}