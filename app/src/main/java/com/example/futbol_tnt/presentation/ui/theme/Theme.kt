package com.example.futbol_tnt.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Verde,
    onPrimary = Blanco,
    primaryContainer = Verde,
    onPrimaryContainer = Blanco,
    secondary = VerdeOscuro,
    onSecondary = Blanco,
    secondaryContainer = VerdeOscuro,
    onSecondaryContainer = Blanco,
    tertiary = Rojo,
    onTertiary = Blanco,
    error = Rojo,
    onError = Blanco,
    errorContainer = Rojo,
    onErrorContainer = Blanco,
    background = Blanco,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = GrisBorde,
    onSurfaceVariant = GrisTexto,
    // Surface containers (Material3 1.2+): se mantienen 100% blancos para evitar
    // que los cards reciban tonal-tint verde via surfaceTint.
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Blanco,
    surfaceContainer = Blanco,
    surfaceContainerHigh = Blanco,
    surfaceContainerHighest = Blanco,
    surfaceTint = Color.Transparent,
    outline = GrisBorde,
    outlineVariant = GrisBorde,
    scrim = Negro,
    inverseSurface = Negro,
    inverseOnSurface = Blanco,
    inversePrimary = Verde,
)

private val DarkColorScheme = darkColorScheme(
    primary = Verde,
    onPrimary = Negro,
    primaryContainer = VerdeOscuro,
    onPrimaryContainer = Blanco,
    secondary = Verde,
    onSecondary = Negro,
    tertiary = Rojo,
    onTertiary = Blanco,
    error = Rojo,
    onError = Blanco,
    errorContainer = Rojo,
    onErrorContainer = Blanco,
    background = Negro,
    onBackground = Blanco,
    surface = Negro,
    onSurface = Blanco,
    surfaceVariant = GrisTexto,
    onSurfaceVariant = Blanco,
    surfaceContainerLowest = Negro,
    surfaceContainerLow = Negro,
    surfaceContainer = Negro,
    surfaceContainerHigh = Negro,
    surfaceContainerHighest = Negro,
    surfaceTint = Color.Transparent,
    outline = GrisTexto,
    outlineVariant = GrisTexto,
    scrim = Negro,
    inverseSurface = Blanco,
    inverseOnSurface = Negro,
    inversePrimary = Verde,
)

@Composable
fun FutbolTNTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}