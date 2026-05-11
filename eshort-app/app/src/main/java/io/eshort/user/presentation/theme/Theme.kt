package io.eshort.user.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EShortDarkScheme = darkColorScheme(
    primary = Coral,
    onPrimary = Snow,
    primaryContainer = CoralDark,
    onPrimaryContainer = Snow,
    secondary = Cyan,
    onSecondary = Jet,
    secondaryContainer = CyanDark,
    tertiary = Violet,
    background = Jet,
    onBackground = Snow,
    surface = Onyx,
    onSurface = Snow,
    surfaceVariant = Charcoal,
    onSurfaceVariant = Mist,
    surfaceContainerLowest = Jet,
    surfaceContainerLow = Onyx,
    surfaceContainer = Charcoal,
    surfaceContainerHigh = Graphite,
    surfaceContainerHighest = Slate,
    outline = Steel,
    outlineVariant = Smoke,
    error = Rose,
    onError = Snow,
    inverseSurface = Fog,
    inverseOnSurface = Jet,
    inversePrimary = CoralDark
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
        colorScheme = EShortDarkScheme,
        typography = EShortTypography,
        content = content
    )
}
