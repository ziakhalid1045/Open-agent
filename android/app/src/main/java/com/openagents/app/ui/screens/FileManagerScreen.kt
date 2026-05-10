package com.openagents.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openagents.app.data.api.FileInfo
import com.openagents.app.di.AppModule
import kotlinx.coroutines.launch

@Composable
fun FileManagerScreen(token: String) {
    val repository = AppModule.repository
    val scope = rememberCoroutineScope()
    var files by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
    var currentDir by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var workspaceSize by remember { mutableStateOf("") }
    val dirStack = remember { mutableStateListOf<String>() }

    fun loadFiles(dir: String = currentDir) {
        scope.launch {
            isLoading = true
            val result = repository.listFiles(token, dir)
            result.onSuccess { r -> files = r.files }
            val wsResult = repository.getWorkspaceInfo(token)
            wsResult.onSuccess { ws -> workspaceSize = "${ws.total_size_mb} MB" }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadFiles() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "File Manager",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = workspaceSize,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Breadcrumb
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dirStack.isNotEmpty()) {
                IconButton(onClick = {
                    val prev = if (dirStack.size > 1) dirStack[dirStack.size - 2] else ""
                    dirStack.removeLastOrNull()
                    currentDir = prev
                    loadFiles(prev)
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                text = "/" + currentDir,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            IconButton(onClick = { loadFiles() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // File list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(files) { file ->
                FileItem(
                    file = file,
                    onClick = {
                        if (file.is_dir) {
                            dirStack.add(currentDir)
                            currentDir = file.path
                            loadFiles(file.path)
                        }
                    }
                )
            }

            if (files.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No files",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(file: FileInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.is_dir) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = null,
                tint = if (file.is_dir)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = file.name, fontWeight = FontWeight.Medium)
                if (!file.is_dir) {
                    Text(
                        text = formatFileSize(file.size),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
