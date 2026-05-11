package io.eshort.user.presentation.screens.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import io.eshort.user.core.auth.AuthViewModel
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Snow
import io.eshort.user.presentation.theme.Steel
import io.eshort.user.presentation.theme.Violet

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onSignedIn: () -> Unit
) {
    val state by authViewModel.authState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                authViewModel.handleSignInResult(account)
            } catch (_: ApiException) { }
        }
    }

    LaunchedEffect(state.user) {
        if (state.user != null) onSignedIn()
    }

    val pulse = rememberInfiniteTransition(label = "bg")
    val bgOffset by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
    ) {
        // Animated glow blobs
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .blur(80.dp)
                .background(
                    Coral.copy(alpha = 0.15f + bgOffset * 0.1f),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .blur(80.dp)
                .background(
                    Violet.copy(alpha = 0.12f + bgOffset * 0.08f),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Coral, Violet)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = Snow,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                "eShort",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Snow
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Discover short videos from\naround the world",
                color = Ash,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Google Sign-In Button
            Button(
                onClick = {
                    val intent = authViewModel.googleSignInClient.signInIntent
                    launcher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Snow),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Jet,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "G",
                            color = Coral,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Continue with Google",
                            color = Jet,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    state.error.orEmpty(),
                    color = Coral,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                color = Ash.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
