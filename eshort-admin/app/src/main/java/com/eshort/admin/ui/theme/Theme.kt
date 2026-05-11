package com.eshort.admin.ui.theme

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

val AdminPrimary = Color(0xFF6C5CE7)
val AdminPrimaryDark = Color(0xFF5A4BD6)
val AdminAccent = Color(0xFF00CEC9)
val AdminBackground = Color(0xFF0A0A0F)
val AdminSurface = Color(0xFF12121A)
val AdminCard = Color(0xFF1C1C28)
val AdminBorder = Color(0xFF2A2A3A)
val AdminTextPrimary = Color(0xFFFFFFFF)
val AdminTextSecondary = Color(0xFF8888AA)
val AdminSuccess = Color(0xFF00B894)
val AdminWarning = Color(0xFFFDCB6E)
val AdminDanger = Color(0xFFE17055)
val AdminInfo = Color(0xFF74B9FF)

private val AdminColorScheme = darkColorScheme(
    primary = AdminPrimary,
    onPrimary = Color.White,
    primaryContainer = AdminPrimaryDark,
    secondary = AdminAccent,
    background = AdminBackground,
    onBackground = AdminTextPrimary,
    surface = AdminSurface,
    onSurface = AdminTextPrimary,
    surfaceVariant = AdminCard,
    onSurfaceVariant = AdminTextSecondary,
    outline = AdminBorder,
    error = AdminDanger,
)

val AdminTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
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
        colorScheme = AdminColorScheme,
        typography = AdminTypography,
        content = content
    )
}
