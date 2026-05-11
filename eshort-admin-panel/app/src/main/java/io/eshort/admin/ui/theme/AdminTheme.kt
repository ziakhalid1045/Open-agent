package io.eshort.admin.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val AdminCoral = Color(0xFFFF3B5C)
val AdminCyan = Color(0xFF00E5FF)
val AdminViolet = Color(0xFF7C4DFF)
val AdminMint = Color(0xFF00D68F)
val AdminAmber = Color(0xFFFFBB33)
val AdminRose = Color(0xFFFF5252)
val AdminJet = Color(0xFF000000)
val AdminOnyx = Color(0xFF0A0A0C)
val AdminCharcoal = Color(0xFF121214)
val AdminGraphite = Color(0xFF1A1A1E)
val AdminSlate = Color(0xFF222228)
val AdminSnow = Color(0xFFFFFFFF)
val AdminMist = Color(0xFFB0B0C0)
val AdminAsh = Color(0xFF70708A)

private val AdminDarkScheme = darkColorScheme(
    primary = AdminCoral,
    onPrimary = AdminSnow,
    secondary = AdminCyan,
    tertiary = AdminViolet,
    background = AdminJet,
    onBackground = AdminSnow,
    surface = AdminOnyx,
    onSurface = AdminSnow,
    surfaceVariant = AdminCharcoal,
    onSurfaceVariant = AdminMist,
    error = AdminRose,
    onError = AdminSnow
)

private val AdminTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp)
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = AdminDarkScheme,
        typography = AdminTypography,
        content = content
    )
}
