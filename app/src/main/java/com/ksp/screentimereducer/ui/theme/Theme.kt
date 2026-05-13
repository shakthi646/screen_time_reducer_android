package com.ksp.screentimereducer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = DeepIndigo,
    onPrimary = PaperWhite,
    primaryContainer = SoftPurpleLight,
    onPrimaryContainer = DeepIndigo,
    secondary = SoftPurple,
    onSecondary = MidnightBlue,
    secondaryContainer = CalmLavender,
    onSecondaryContainer = DeepIndigo,
    tertiary = GentleMint,
    onTertiary = MidnightBlue,
    background = PaperWhite,
    onBackground = InkBlack,
    surface = Color(0xFFFFFFFF),
    onSurface = InkBlack,
    surfaceVariant = CalmLavender,
    onSurfaceVariant = DeepIndigo,
    outline = MutedGrey,
)

private val DarkColors = darkColorScheme(
    primary = SoftPurple,
    onPrimary = MidnightBlue,
    primaryContainer = DeepIndigo,
    onPrimaryContainer = SoftPurpleLight,
    secondary = SoftPurpleLight,
    onSecondary = MidnightBlue,
    secondaryContainer = DeepIndigoBright,
    onSecondaryContainer = SoftPurpleLight,
    tertiary = GentleMint,
    onTertiary = MidnightBlue,
    background = MidnightBlueDeep,
    onBackground = PaperWhite,
    surface = MidnightBlue,
    onSurface = PaperWhite,
    surfaceVariant = Color(0xFF1A1E48),
    onSurfaceVariant = SoftPurpleLight,
    outline = Color(0xFF4F4F75),
)

@Composable
fun ScreenTimeReducerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScreenTimeTypography,
        shapes = ScreenTimeShapes,
        content = content
    )
}
