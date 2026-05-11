package com.eshort.admin.ui.screens.users

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.eshort.admin.data.model.AdminUser
import com.eshort.admin.ui.theme.AdminCard
import com.eshort.admin.ui.theme.AdminDanger
import com.eshort.admin.ui.theme.AdminPrimary
import com.eshort.admin.ui.theme.AdminSuccess
import com.eshort.admin.ui.theme.AdminTextSecondary
import com.eshort.admin.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var showBanDialog by remember { mutableStateOf<AdminUser?>(null) }
    var banReason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("User Management", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "${state.users.size} users",
                        color = AdminTextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.users) { user ->
                UserCard(
                    user = user,
                    onBan = { showBanDialog = user },
                    onUnban = { viewModel.unbanUser(user.uid) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    showBanDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showBanDialog = null },
            title = { Text("Ban ${user.username}?", color = Color.White) },
            text = {
                Column {
                    Text("This will prevent the user from accessing eShort.", color = AdminTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = banReason,
                        onValueChange = { banReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.banUser(user.uid, banReason)
                        showBanDialog = null
                        banReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminDanger)
                ) {
                    Text("Ban User")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = null }) {
                    Text("Cancel", color = AdminTextSecondary)
                }
            },
            containerColor = AdminCard
        )
    }
}

@Composable
fun UserCard(
    user: AdminUser,
    onBan: () -> Unit,
    onUnban: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdminCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatarUrl.ifEmpty { null },
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.username,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Verified,
                            null,
                            tint = AdminPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (user.isBanned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Block,
                            null,
                            tint = AdminDanger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(user.email, color = AdminTextSecondary, fontSize = 13.sp)
                Row {
                    Text(
                        "${user.followerCount} followers",
                        color = AdminTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        " · ${user.videoCount} videos",
                        color = AdminTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = AdminTextSecondary)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (user.isBanned) {
                        DropdownMenuItem(
                            text = { Text("Unban User") },
                            onClick = { onUnban(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = AdminSuccess) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Ban User", color = AdminDanger) },
                            onClick = { onBan(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Block, null, tint = AdminDanger) }
                        )
                    }
                }
            }
        }
    }
}
