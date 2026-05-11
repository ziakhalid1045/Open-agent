package io.eshort.user.presentation.screens.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import io.eshort.user.core.util.formatCount
import io.eshort.user.domain.model.ShortVideo
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Snow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    onComments: (String) -> Unit,
    onProfile: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val videos by viewModel.feedVideos.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    if (videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Jet),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No videos yet", color = Snow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Be the first to post!", color = Snow.copy(alpha = 0.6f), fontSize = 14.sp)
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { videos.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val video = videos[page]
        VideoPage(
            video = video,
            isActive = pagerState.currentPage == page,
            currentUid = currentUid,
            onLike = { viewModel.toggleLike(video.id) },
            onComment = { onComments(video.id) },
            onShare = { viewModel.shareVideo(video.id) },
            onProfile = { onProfile(video.authorId) },
            onView = { viewModel.recordView(video.id) }
        )
    }
}

@Composable
fun VideoPage(
    video: ShortVideo,
    isActive: Boolean,
    currentUid: String,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onProfile: () -> Unit,
    onView: () -> Unit
) {
    val context = LocalContext.current
    var showHeart by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isLiked = video.likedBy.contains(currentUid)

    val exoPlayer = remember(video.streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.streamUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 1f
            prepare()
        }
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            exoPlayer.play()
            onView()
        } else {
            exoPlayer.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!isLiked) onLike()
                        showHeart = true
                        scope.launch {
                            delay(800)
                            showHeart = false
                        }
                    }
                )
            }
    ) {
        // Video player
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Double-tap heart
        AnimatedVisibility(
            visible = showHeart,
            enter = scaleIn(tween(200)) + fadeIn(),
            exit = scaleOut(tween(400)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                Icons.Filled.Favorite,
                null,
                tint = Coral,
                modifier = Modifier.size(100.dp)
            )
        }

        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Jet.copy(alpha = 0.8f))
                    )
                )
        )

        // Video info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 20.dp, end = 80.dp)
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onProfile() }
            ) {
                AsyncImage(
                    model = video.authorPhoto.ifEmpty { null },
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "@${video.authorName}",
                    color = Snow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                video.caption,
                color = Snow.copy(alpha = 0.9f),
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (video.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    video.tags.joinToString(" ") { "#$it" },
                    color = Snow.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                video.platform,
                color = Snow.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        // Side action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SideButton(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                count = video.likeCount.formatCount(),
                tint = if (isLiked) Coral else Snow,
                onClick = onLike
            )
            SideButton(
                icon = Icons.Filled.ChatBubble,
                count = video.comments.formatCount(),
                tint = Snow,
                onClick = onComment
            )
            SideButton(
                icon = Icons.Filled.Share,
                count = video.shares.formatCount(),
                tint = Snow,
                onClick = onShare
            )
        }
    }
}

@Composable
fun SideButton(
    icon: ImageVector,
    count: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(count, color = Snow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
