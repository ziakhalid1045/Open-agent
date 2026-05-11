package io.eshort.admin.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.eshort.admin.ui.theme.AdminAmber
import io.eshort.admin.ui.theme.AdminAsh
import io.eshort.admin.ui.theme.AdminCharcoal
import io.eshort.admin.ui.theme.AdminCoral
import io.eshort.admin.ui.theme.AdminCyan
import io.eshort.admin.ui.theme.AdminGraphite
import io.eshort.admin.ui.theme.AdminJet
import io.eshort.admin.ui.theme.AdminMint
import io.eshort.admin.ui.theme.AdminRose
import io.eshort.admin.ui.theme.AdminSnow
import io.eshort.admin.ui.theme.AdminViolet
import io.eshort.admin.viewmodel.AdminViewModel

@Composable
fun DashboardScreen(viewModel: AdminViewModel) {
    val stats by viewModel.stats.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadStats() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminJet)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Admin Dashboard", color = AdminSnow, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("eShort Platform Overview", color = AdminAsh, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                StatCard(
                    icon = Icons.Default.People,
                    label = "Total Users",
                    value = "${stats.totalUsers}",
                    gradientStart = AdminCyan.copy(alpha = 0.3f),
                    gradientEnd = AdminCyan.copy(alpha = 0.1f),
                    iconTint = AdminCyan
                )
            }
            item {
                StatCard(
                    icon = Icons.Default.VideoLibrary,
                    label = "Total Videos",
                    value = "${stats.totalVideos}",
                    gradientStart = AdminViolet.copy(alpha = 0.3f),
                    gradientEnd = AdminViolet.copy(alpha = 0.1f),
                    iconTint = AdminViolet
                )
            }
            item {
                StatCard(
                    icon = Icons.Default.Report,
                    label = "Pending Reports",
                    value = "${stats.pendingReports}",
                    gradientStart = AdminAmber.copy(alpha = 0.3f),
                    gradientEnd = AdminAmber.copy(alpha = 0.1f),
                    iconTint = AdminAmber
                )
            }
            item {
                StatCard(
                    icon = Icons.Default.Block,
                    label = "Banned Users",
                    value = "${stats.bannedUsers}",
                    gradientStart = AdminRose.copy(alpha = 0.3f),
                    gradientEnd = AdminRose.copy(alpha = 0.1f),
                    iconTint = AdminRose
                )
            }
            item {
                StatCard(
                    icon = Icons.Default.Visibility,
                    label = "Total Views",
                    value = formatViews(stats.totalViews),
                    gradientStart = AdminMint.copy(alpha = 0.3f),
                    gradientEnd = AdminMint.copy(alpha = 0.1f),
                    iconTint = AdminMint
                )
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    gradientStart: Color,
    gradientEnd: Color,
    iconTint: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCharcoal)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(gradientStart, gradientEnd)))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
                    }
                }
                Column {
                    Text(value, color = AdminSnow, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text(label, color = AdminSnow.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }
        }
    }
}

fun formatViews(views: Long): String = when {
    views >= 1_000_000_000 -> "${views / 1_000_000_000}B"
    views >= 1_000_000 -> "${views / 1_000_000}M"
    views >= 1_000 -> "${views / 1_000}K"
    else -> "$views"
}
