package com.eshort.app.ui.screens.upload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.eshort.app.data.model.VideoPlatform
import com.eshort.app.ui.theme.EShortDarkCard
import com.eshort.app.ui.theme.EShortDarkGray
import com.eshort.app.ui.theme.EShortGradientEnd
import com.eshort.app.ui.theme.EShortGradientStart
import com.eshort.app.ui.theme.EShortMediumGray
import com.eshort.app.ui.theme.EShortPrimary
import com.eshort.app.ui.theme.EShortSuccess
import com.eshort.app.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onUploadSuccess: () -> Unit,
    videoViewModel: VideoViewModel = hiltViewModel()
) {
    var videoUrl by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    val uploadState by videoViewModel.uploadState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val detectedPlatform = remember(videoUrl) {
        if (videoUrl.isNotEmpty()) VideoPlatform.detect(videoUrl) else null
    }

    LaunchedEffect(uploadState.isSuccess) {
        if (uploadState.isSuccess) {
            kotlinx.coroutines.delay(1500)
            videoViewModel.resetUploadState()
            onUploadSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Share a Short",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // URL Input
            Column {
                Text(
                    "Video Link",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    placeholder = { Text("Paste TikTok, YouTube Shorts, or Reels link...", color = EShortMediumGray) },
                    leadingIcon = { Icon(Icons.Default.Link, null, tint = EShortPrimary) },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { videoUrl = it }
                        }) {
                            Icon(Icons.Default.ContentPaste, null, tint = EShortMediumGray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EShortPrimary,
                        unfocusedBorderColor = EShortDarkGray,
                        cursorColor = EShortPrimary,
                        focusedContainerColor = EShortDarkCard,
                        unfocusedContainerColor = EShortDarkCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Platform Detection
            AnimatedVisibility(visible = detectedPlatform != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EShortDarkCard)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        null,
                        tint = EShortSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Platform Detected",
                            color = EShortSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            detectedPlatform?.displayName ?: "",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = EShortSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Thumbnail Preview Area
            if (videoUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(EShortDarkCard)
                        .border(1.dp, EShortDarkGray, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Videocam,
                            null,
                            tint = EShortMediumGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Video Preview", color = EShortMediumGray, fontSize = 14.sp)
                    }
                }
            }

            // Caption
            Column {
                Text(
                    "Caption",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Write a caption...", color = EShortMediumGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EShortPrimary,
                        unfocusedBorderColor = EShortDarkGray,
                        cursorColor = EShortPrimary,
                        focusedContainerColor = EShortDarkCard,
                        unfocusedContainerColor = EShortDarkCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )
            }

            // Hashtags
            Column {
                Text(
                    "Hashtags",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hashtags,
                    onValueChange = { hashtags = it },
                    placeholder = { Text("dance, funny, viral (comma-separated)", color = EShortMediumGray) },
                    leadingIcon = { Icon(Icons.Default.Tag, null, tint = EShortPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EShortPrimary,
                        unfocusedBorderColor = EShortDarkGray,
                        cursorColor = EShortPrimary,
                        focusedContainerColor = EShortDarkCard,
                        unfocusedContainerColor = EShortDarkCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Publish Button
            Button(
                onClick = {
                    val hashtagList = hashtags.split(",")
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                    videoViewModel.publishVideo(videoUrl, caption, hashtagList)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EShortPrimary),
                enabled = videoUrl.isNotEmpty() && !uploadState.isUploading
            ) {
                if (uploadState.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else if (uploadState.isSuccess) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Published!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Text(
                        "Publish to eShort",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            // Error
            if (uploadState.error != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Red.copy(alpha = 0.1f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(uploadState.error ?: "", color = Color.Red, fontSize = 14.sp)
                }
            }

            // Supported platforms info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EShortDarkCard)
                    .padding(16.dp)
            ) {
                Text(
                    "Supported Platforms",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                val platforms = listOf(
                    "TikTok" to "tiktok.com",
                    "YouTube Shorts" to "youtube.com/shorts",
                    "Instagram Reels" to "instagram.com/reel",
                    "Facebook Reels" to "facebook.com/reel"
                )
                platforms.forEach { (name, domain) ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EShortPrimary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(name, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(domain, color = EShortMediumGray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
