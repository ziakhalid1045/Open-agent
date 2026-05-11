package com.eshort.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat

val EShortBlack = Color(0xFF000000)
val EShortDarkSurface = Color(0xFF0D0D0D)
val EShortDarkCard = Color(0xFF1A1A1A)
val EShortDarkGray = Color(0xFF2A2A2A)
val EShortMediumGray = Color(0xFF666666)
val EShortLightGray = Color(0xFFAAAAAA)
val EShortWhite = Color(0xFFFFFFFF)
val EShortPrimary = Color(0xFFFF2D55)
val EShortPrimaryDark = Color(0xFFE0264A)
val EShortAccent = Color(0xFF00F5FF)
val EShortGradientStart = Color(0xFFFF2D55)
val EShortGradientEnd = Color(0xFFFF6B35)
val EShortSuccess = Color(0xFF34C759)
val EShortWarning = Color(0xFFFFCC00)
val EShortError = Color(0xFFFF3B30)
val EShortGlass = Color(0x33FFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = EShortPrimary,
    onPrimary = EShortWhite,
    primaryContainer = EShortPrimaryDark,
    secondary = EShortAccent,
    onSecondary = EShortBlack,
    background = EShortBlack,
    onBackground = EShortWhite,
    surface = EShortDarkSurface,
    onSurface = EShortWhite,
    surfaceVariant = EShortDarkCard,
    onSurfaceVariant = EShortLightGray,
    outline = EShortDarkGray,
    error = EShortError,
    onError = EShortWhite,
)

val EShortTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    ),
)

@Composable
fun EShortTheme(content: @Composable () -> Unit) {
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
        colorScheme = DarkColorScheme,
        typography = EShortTypography,
        content = content
    )
}
