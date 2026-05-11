package io.eshort.user.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Cyan
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Violet
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val glowRadius by pulseTransition.animateFloat(
        initialValue = 60f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, tween(500))
        delay(1200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet),
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        Box(
            modifier = Modifier
                .size(glowRadius.dp)
                .blur(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Coral.copy(alpha = 0.5f),
                            Violet.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(logoScale.value)
        ) {
            // Logo icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Coral, Violet)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "e",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "eShort",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Text(
                "Watch. Create. Connect.",
                fontSize = 14.sp,
                color = Cyan.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}
