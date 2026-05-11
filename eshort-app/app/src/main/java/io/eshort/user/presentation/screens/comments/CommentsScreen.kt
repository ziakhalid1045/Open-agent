package io.eshort.user.presentation.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.eshort.user.core.util.formatCount
import io.eshort.user.domain.model.Comment
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Charcoal
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Graphite
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Mist
import io.eshort.user.presentation.theme.Slate
import io.eshort.user.presentation.theme.Snow
import io.eshort.user.presentation.theme.Steel

@Composable
fun CommentsScreen(
    videoId: String,
    onBack: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(videoId) { viewModel.loadComments(videoId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Snow)
            }
            Text(
                "Comments",
                color = Snow,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${comments.size}",
                color = Mist,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Steel)
        )

        // Comments list
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Coral)
            }
        } else if (comments.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No comments yet", color = Snow, fontSize = 16.sp)
                    Text("Start the conversation!", color = Ash, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(comments) { comment -> CommentItem(comment) }
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Charcoal)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Add a comment...", color = Ash) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Snow,
                    unfocusedTextColor = Snow,
                    focusedContainerColor = Graphite,
                    unfocusedContainerColor = Graphite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Coral
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.postComment(videoId, input.trim())
                        input = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Coral)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Snow, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        AsyncImage(
            model = comment.authorPhoto.ifEmpty { null },
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Slate),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "@${comment.authorName}",
                color = Mist,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(comment.text, color = Snow.copy(alpha = 0.9f), fontSize = 14.sp)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(Icons.Default.FavoriteBorder, null, tint = Ash, modifier = Modifier.size(18.dp))
            if (comment.likes > 0) {
                Text(comment.likes.formatCount(), color = Ash, fontSize = 11.sp)
            }
        }
    }
}
