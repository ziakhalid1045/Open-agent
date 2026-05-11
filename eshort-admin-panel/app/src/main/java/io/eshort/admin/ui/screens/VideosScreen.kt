package io.eshort.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.eshort.admin.ui.theme.AdminAmber
import io.eshort.admin.ui.theme.AdminAsh
import io.eshort.admin.ui.theme.AdminCharcoal
import io.eshort.admin.ui.theme.AdminJet
import io.eshort.admin.ui.theme.AdminMint
import io.eshort.admin.ui.theme.AdminMist
import io.eshort.admin.ui.theme.AdminRose
import io.eshort.admin.ui.theme.AdminSnow
import io.eshort.admin.viewmodel.AdminViewModel

@Composable
fun VideosScreen(viewModel: AdminViewModel) {
    val videos by viewModel.videos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminJet)
            .statusBarsPadding()
    ) {
        Text(
            "Video Moderation",
            color = AdminSnow,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        Text(
            "${videos.size} videos",
            color = AdminAsh,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(videos) { video ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCharcoal)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    video.caption.ifEmpty { "No caption" },
                                    color = AdminSnow,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "by @${video.authorName} · ${video.platform}",
                                    color = AdminMist,
                                    fontSize = 12.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusColor = when (video.status) {
                                        "approved" -> AdminMint
                                        "rejected" -> AdminRose
                                        else -> AdminAmber
                                    }
                                    Text(
                                        video.status.uppercase(),
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${formatViews(video.views)} views · ${video.likes} likes",
                                        color = AdminAsh,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Approve
                            IconButton(onClick = { viewModel.updateVideoStatus(video.id, "approved") }) {
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = AdminMint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            // Reject
                            IconButton(onClick = { viewModel.updateVideoStatus(video.id, "rejected") }) {
                                Icon(
                                    Icons.Default.RemoveCircle, null,
                                    tint = AdminAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            // Delete
                            IconButton(onClick = { viewModel.deleteVideo(video.id) }) {
                                Icon(
                                    Icons.Default.Delete, null,
                                    tint = AdminRose,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
