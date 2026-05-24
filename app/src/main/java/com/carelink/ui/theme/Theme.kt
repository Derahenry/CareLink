package com.carelink.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
// ThemeManager — global dark mode toggle
// ─────────────────────────────────────────────────────────────────────────────
object ThemeManager {
    var isDarkMode by mutableStateOf(false)
}


object CareColors {
    // Light mode
    val InkLight        = Color(0xFF2F5468)
    val InkSoftLight    = Color(0xFF4C6B7E)
    val InkMutedLight   = Color(0xFF8B8B85)
    val BgLight         = Color(0xFFECE6D6)
    val SurfaceLight    = Color(0xFFF6F1E1)
    val SurfaceMutedLight = Color(0xFFEBE5D2)
    val BorderLight     = Color(0x1A281400)

    // Dark mode
    val InkDark         = Color(0xFFCAD7DD)
    val InkSoftDark     = Color(0xFFA8B6BD)
    val InkMutedDark    = Color(0xFF7B8389)
    val BgDark          = Color(0xFF141819)
    val SurfaceDark     = Color(0xFF1C2225)
    val SurfaceMutedDark = Color(0xFF23292B)
    val BorderDark      = Color(0x14FFFFFF)

    // Status colors (shared light/dark — warm healthcare palette)
    val Rose        = Color(0xFFA1413A)
    val RoseTint    = Color(0x1AA1413A)
    val Amber       = Color(0xFF94661E)
    val AmberTint   = Color(0x1A94661E)
    val Emerald     = Color(0xFF3F6E54)
    val EmeraldTint = Color(0x1F3F6E54)
    val Iris        = Color(0xFF6B5B8C)
    val IrisTint    = Color(0x1A6B5B8C)
    val Slate       = Color(0xFF5A6B68)
    val SlateTint   = Color(0x1A5A6B68)

    // Dark status colors
    val RoseDark     = Color(0xFFE08B83)
    val AmberDark    = Color(0xFFD8A560)
    val EmeraldDark  = Color(0xFF8FBE9C)
    val IrisDark     = Color(0xFFA89CC8)
    val SlateDark    = Color(0xFFA8B0AE)
}

private val LightColors = lightColorScheme(
    primary          = CareColors.InkLight,
    onPrimary        = CareColors.SurfaceLight,
    secondary        = CareColors.InkSoftLight,
    background       = CareColors.BgLight,
    surface          = CareColors.SurfaceLight,
    surfaceVariant   = CareColors.SurfaceMutedLight,
    onSurface        = CareColors.InkLight,
    onSurfaceVariant = CareColors.InkMutedLight,
    outline          = CareColors.BorderLight,
    error            = CareColors.Rose,
    onBackground     = CareColors.InkLight
)

private val DarkColors = darkColorScheme(
    primary          = CareColors.InkDark,
    onPrimary        = CareColors.BgDark,
    secondary        = CareColors.InkSoftDark,
    background       = CareColors.BgDark,
    surface          = CareColors.SurfaceDark,
    surfaceVariant   = CareColors.SurfaceMutedDark,
    onSurface        = CareColors.InkDark,
    onSurfaceVariant = CareColors.InkMutedDark,
    outline          = CareColors.BorderDark,
    error            = CareColors.RoseDark,
    onBackground     = CareColors.InkDark
)

@Composable
fun CareLinkTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}