package com.openagents.app.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openagents.app.di.AppModule
import kotlinx.coroutines.launch

@Composable
fun BrowserScreen(token: String) {
    val repository = AppModule.repository
    val scope = rememberCoroutineScope()
    var urlInput by remember { mutableStateOf("") }
    var screenshotBase64 by remember { mutableStateOf<String?>(null) }
    var currentUrl by remember { mutableStateOf("about:blank") }
    var currentTitle by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = "Remote Browser",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // URL Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter URL...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        val result = repository.navigate(token, urlInput)
                        result.onSuccess { r ->
                            currentUrl = r.url ?: urlInput
                            currentTitle = r.title ?: ""
                            // Take screenshot
                            val ssResult = repository.screenshot(token)
                            ssResult.onSuccess { ss -> screenshotBase64 = ss.base64 }
                        }
                        result.onFailure { e -> error = e.message }
                        isLoading = false
                    }
                },
                enabled = !isLoading
            ) {
                Text("Go")
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = {
                scope.launch {
                    repository.navigate(token, "javascript:history.back()")
                    val ssResult = repository.screenshot(token)
                    ssResult.onSuccess { ss -> screenshotBase64 = ss.base64 }
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = {
                scope.launch {
                    repository.navigate(token, "javascript:history.forward()")
                    val ssResult = repository.screenshot(token)
                    ssResult.onSuccess { ss -> screenshotBase64 = ss.base64 }
                }
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
            }
            IconButton(onClick = {
                scope.launch {
                    isLoading = true
                    val ssResult = repository.screenshot(token)
                    ssResult.onSuccess { ss -> screenshotBase64 = ss.base64 }
                    isLoading = false
                }
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }

            Text(
                text = currentTitle.ifBlank { currentUrl },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                maxLines = 1,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Loading
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Error
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
                fontSize = 12.sp
            )
        }

        // Screenshot view
        screenshotBase64?.let { b64 ->
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Browser view",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Navigate to a URL to see the browser view",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
