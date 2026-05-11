package io.eshort.user.presentation.screens.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import io.eshort.user.domain.model.VideoPlatform
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Charcoal
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Graphite
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Mint
import io.eshort.user.presentation.theme.Mist
import io.eshort.user.presentation.theme.Slate
import io.eshort.user.presentation.theme.Snow
import io.eshort.user.presentation.theme.Steel
import io.eshort.user.presentation.theme.Violet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(
    viewModel: CreateViewModel = hiltViewModel()
) {
    val state by viewModel.createState.collectAsState()
    var videoUrl by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    val detectedPlatform = if (videoUrl.isNotBlank()) VideoPlatform.detect(videoUrl) else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Create Post",
            color = Snow,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            "Paste a video link to share",
            color = Ash,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // URL input
        Text("Video Link", color = Mist, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            placeholder = { Text("https://...", color = Ash) },
            leadingIcon = { Icon(Icons.Default.Link, null, tint = Coral) },
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.getText()?.text?.let { videoUrl = it }
                }) {
                    Icon(Icons.Default.ContentPaste, null, tint = Mist)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Snow,
                unfocusedTextColor = Snow,
                focusedContainerColor = Graphite,
                unfocusedContainerColor = Graphite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Coral
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Platform detection
        AnimatedVisibility(visible = detectedPlatform != null, enter = fadeIn()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Mint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Detected: ${detectedPlatform?.label ?: ""}",
                    color = Mint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Caption
        Text("Caption", color = Mist, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = caption,
            onValueChange = { caption = it },
            placeholder = { Text("Write a caption...", color = Ash) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Snow,
                unfocusedTextColor = Snow,
                focusedContainerColor = Graphite,
                unfocusedContainerColor = Graphite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Coral
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tags
        Text("Tags", color = Mist, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = tagsInput,
            onValueChange = { tagsInput = it },
            placeholder = { Text("funny, dance, viral (comma separated)", color = Ash) },
            leadingIcon = { Icon(Icons.Default.Tag, null, tint = Violet) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Snow,
                unfocusedTextColor = Snow,
                focusedContainerColor = Graphite,
                unfocusedContainerColor = Graphite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Coral
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Publish button
        Button(
            onClick = {
                val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                viewModel.publish(videoUrl, caption, tags)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Coral),
            enabled = videoUrl.isNotBlank() && !state.isPublishing
        ) {
            if (state.isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Snow,
                    strokeWidth = 2.dp
                )
            } else if (state.isSuccess) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Snow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Published!", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Publish, null, tint = Snow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(state.error.orEmpty(), color = Coral, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Supported platforms
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Charcoal)
                .border(1.dp, Steel, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Supported Platforms",
                    color = Snow,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TikTok", "YouTube Shorts", "Instagram Reels", "Facebook Reels").forEach { name ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(Graphite, Slate))
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(name, color = Mist, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
