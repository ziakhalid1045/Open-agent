package com.eshort.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.eshort.app.ui.theme.EShortDarkCard
import com.eshort.app.ui.theme.EShortDarkGray
import com.eshort.app.ui.theme.EShortDarkSurface
import com.eshort.app.ui.theme.EShortMediumGray
import com.eshort.app.ui.theme.EShortPrimary
import com.eshort.app.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val user = profileState.user

    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }

    LaunchedEffect(currentUserId) {
        profileViewModel.loadProfile(currentUserId, currentUserId)
    }

    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            profileViewModel.clearUpdateSuccess()
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        profileViewModel.updateProfile(displayName, username, bio)
                    },
                    enabled = !profileState.isUpdating
                ) {
                    if (profileState.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EShortPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, null, tint = EShortPrimary)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = EShortDarkSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            androidx.compose.foundation.layout.Box {
                AsyncImage(
                    model = user?.avatarUrl?.ifEmpty { null },
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(EShortDarkGray),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EShortPrimary)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ProfileTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Display Name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio",
                maxLines = 4,
                modifier = Modifier.height(120.dp)
            )

            if (profileState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(profileState.error ?: "", color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = EShortMediumGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = EShortPrimary,
                unfocusedBorderColor = EShortDarkGray,
                cursorColor = EShortPrimary,
                focusedContainerColor = EShortDarkCard,
                unfocusedContainerColor = EShortDarkCard
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier.fillMaxWidth(),
            maxLines = maxLines
        )
    }
}
