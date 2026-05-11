package io.eshort.user.presentation.screens.explore

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.eshort.user.core.util.formatCount
import io.eshort.user.domain.model.ShortVideo
import io.eshort.user.domain.model.UserProfile
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Charcoal
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Graphite
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Mist
import io.eshort.user.presentation.theme.Slate
import io.eshort.user.presentation.theme.Snow

@Composable
fun ExploreScreen(
    onUserClick: (String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val trendingVideos by viewModel.trendingVideos.collectAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadTrending() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding()
    ) {
        // Search bar
        TextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.search(it)
            },
            placeholder = { Text("Search users, videos, tags...", color = Ash) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Ash) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Snow,
                unfocusedTextColor = Snow,
                focusedContainerColor = Graphite,
                unfocusedContainerColor = Graphite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Coral
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true
        )

        if (query.isNotEmpty() && searchResults.users.isNotEmpty()) {
            Text(
                "Users",
                color = Snow,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(searchResults.users) { user ->
                    UserSearchItem(user = user, onClick = { onUserClick(user.uid) })
                }
            }
        }

        // Trending section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.TrendingUp, null, tint = Coral, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Trending", color = Snow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(trendingVideos) { video ->
                TrendingCard(video = video)
            }
        }
    }
}

@Composable
fun UserSearchItem(user: UserProfile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.photoUrl.ifEmpty { null },
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Slate),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                "@${user.username}",
                color = Snow,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                "${user.followerCount.formatCount()} followers",
                color = Mist,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun TrendingCard(video: ShortVideo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = video.thumbnailUrl.ifEmpty { null },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Jet.copy(alpha = 0.7f))
                        )
                    )
            )

            // Play icon
            Icon(
                Icons.Default.PlayArrow,
                null,
                tint = Snow.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
            )

            // View count
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Visibility,
                    null,
                    tint = Snow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    video.views.formatCount(),
                    color = Snow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Author
            Text(
                "@${video.authorName}",
                color = Snow.copy(alpha = 0.8f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}
