package com.artemis.mgrsnav.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Forest = Color(0xFF1B5E3B)
val ForestDark = Color(0xFF0B1A12)
val Sand = Color(0xFFC4A35A)
val SandDim = Color(0xFF8A7340)
val Fog = Color(0xFFE8E2D4)
val Alert = Color(0xFFC45C26)
val Good = Color(0xFF3D9A5F)

private val DarkColors = darkColorScheme(
    primary = Sand,
    onPrimary = ForestDark,
    secondary = Forest,
    onSecondary = Fog,
    background = ForestDark,
    onBackground = Fog,
    surface = Color(0xFF12261A),
    onSurface = Fog,
    error = Alert
)

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Fog,
    secondary = SandDim,
    onSecondary = ForestDark,
    background = Fog,
    onBackground = ForestDark,
    surface = Color(0xFFF5F0E6),
    onSurface = ForestDark,
    error = Alert
)

@Composable
fun MeridianTheme(
    darkTheme: Boolean = true, // field default: dark for night ops
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme || isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
