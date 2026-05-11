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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.eshort.admin.ui.theme.AdminAsh
import io.eshort.admin.ui.theme.AdminCharcoal
import io.eshort.admin.ui.theme.AdminCoral
import io.eshort.admin.ui.theme.AdminJet
import io.eshort.admin.ui.theme.AdminMint
import io.eshort.admin.ui.theme.AdminMist
import io.eshort.admin.ui.theme.AdminRose
import io.eshort.admin.ui.theme.AdminSlate
import io.eshort.admin.ui.theme.AdminSnow
import io.eshort.admin.viewmodel.AdminViewModel

@Composable
fun UsersScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminJet)
            .statusBarsPadding()
    ) {
        Text(
            "User Management",
            color = AdminSnow,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        Text(
            "${users.size} users",
            color = AdminAsh,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCharcoal)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = user.photoUrl.ifEmpty { null },
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AdminSlate),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "@${user.username}",
                                    color = AdminSnow,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        null,
                                        tint = AdminCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                "${user.followerCount} followers · ${user.postsCount} posts",
                                color = AdminMist,
                                fontSize = 12.sp
                            )
                            if (user.isBanned) {
                                Text("BANNED", color = AdminRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Verify button
                        IconButton(onClick = { viewModel.verifyUser(user.uid, !user.isVerified) }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = if (user.isVerified) AdminMint else AdminAsh,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Ban button
                        IconButton(onClick = { viewModel.toggleBan(user.uid, !user.isBanned) }) {
                            Icon(
                                Icons.Default.Block,
                                null,
                                tint = if (user.isBanned) AdminRose else AdminAsh,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
