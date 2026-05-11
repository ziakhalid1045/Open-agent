package io.eshort.user.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.eshort.user.core.auth.AuthViewModel
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Graphite
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Mist
import io.eshort.user.presentation.theme.Snow
import io.eshort.user.presentation.theme.Steel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Snow)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Settings",
            color = Snow,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        SettingsItem(Icons.Default.Notifications, "Notifications", "Manage push notifications") {}
        SettingsItem(Icons.Default.PrivacyTip, "Privacy", "Account privacy settings") {}
        SettingsItem(Icons.Default.Shield, "Security", "Password & login") {}
        SettingsItem(Icons.Default.Info, "About", "App version & info") {}

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Coral.copy(alpha = 0.1f))
                .clickable {
                    authViewModel.signOut()
                    onLoggedOut()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                null,
                tint = Coral,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                "Log Out",
                color = Coral,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "eShort v1.0.0",
            color = Ash,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 20.dp)
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Mist, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Snow, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(subtitle, color = Ash, fontSize = 12.sp)
        }
    }
}
