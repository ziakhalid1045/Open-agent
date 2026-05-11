package io.eshort.user.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.eshort.user.core.util.formatCount
import io.eshort.user.domain.model.ShortVideo
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
fun ProfileScreen(
    uid: String,
    isSelf: Boolean,
    onBack: () -> Unit,
    onNavigateToChat: (conversationId: String, otherUid: String) -> Unit,
    onEditProfile: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val videos by viewModel.userVideos.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()

    LaunchedEffect(uid) { viewModel.loadProfile(uid) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item(span = { GridItemSpan(3) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSelf) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Snow)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (isSelf) {
                        IconButton(onClick = { onSettings?.invoke() }) {
                            Icon(Icons.Default.Settings, null, tint = Snow)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Avatar
                Box {
                    AsyncImage(
                        model = profile?.photoUrl?.ifEmpty { null },
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Slate)
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(Coral, Color(0xFF7C4DFF))),
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                    if (profile?.isVerified == true) {
                        Icon(
                            Icons.Default.Verified,
                            null,
                            tint = Coral,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .background(Jet, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    profile?.displayName.orEmpty().ifEmpty { profile?.username.orEmpty() },
                    color = Snow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "@${profile?.username.orEmpty()}",
                    color = Mist,
                    fontSize = 14.sp
                )

                if (profile?.bio?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        profile?.bio.orEmpty(),
                        color = Snow.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(profile?.postsCount?.formatCount() ?: "0", "Videos")
                    StatItem(profile?.followerCount?.formatCount() ?: "0", "Followers")
                    StatItem(profile?.followingCount?.formatCount() ?: "0", "Following")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                if (isSelf) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { onEditProfile?.invoke() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Steel)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Snow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", color = Snow, fontSize = 13.sp)
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.toggleFollow(uid) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Graphite else Coral
                            )
                        ) {
                            Text(
                                if (isFollowing) "Following" else "Follow",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.startChat(uid) { convoId ->
                                    onNavigateToChat(convoId, uid)
                                }
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Steel)
                        ) {
                            Text("Message", color = Snow, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Steel)
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Video grid
        items(videos) { video ->
            VideoGridItem(video = video)
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = Snow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Ash, fontSize = 12.sp)
    }
}

@Composable
fun VideoGridItem(video: ShortVideo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(4.dp))
            .background(Charcoal)
    ) {
        AsyncImage(
            model = video.thumbnailUrl.ifEmpty { null },
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Snow, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(video.views.formatCount(), color = Snow, fontSize = 11.sp)
        }
    }
}
