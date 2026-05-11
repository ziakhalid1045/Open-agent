package io.eshort.user.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.google.firebase.auth.FirebaseAuth
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Graphite
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Mist
import io.eshort.user.presentation.theme.Slate
import io.eshort.user.presentation.theme.Snow

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            displayName = it.displayName
            username = it.username
            bio = it.bio
        }
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModel.loadProfile(uid)
    }

    LaunchedEffect(isSaved) {
        if (isSaved) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Header
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Snow)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Edit Profile",
            color = Snow,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Avatar
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            AsyncImage(
                model = profile?.photoUrl?.ifEmpty { null },
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Slate),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Coral)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Snow, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FieldLabel("Display Name")
        EditField(value = displayName, onValueChange = { displayName = it }, placeholder = "Your name")

        Spacer(modifier = Modifier.height(20.dp))

        FieldLabel("Username")
        EditField(value = username, onValueChange = { username = it }, placeholder = "username")

        Spacer(modifier = Modifier.height(20.dp))

        FieldLabel("Bio")
        TextField(
            value = bio,
            onValueChange = { bio = it },
            placeholder = { Text("Tell us about yourself...", color = Ash) },
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
                .height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = { viewModel.saveProfile(displayName, username, bio) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Coral),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Snow,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FieldLabel(label: String) {
    Text(
        label,
        color = Mist,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun EditField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Ash) },
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
}
