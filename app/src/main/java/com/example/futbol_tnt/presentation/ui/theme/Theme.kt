package com.example.futbol_tnt.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
    outline = GrisBorde,
    outlineVariant = GrisBorde,
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
    outline = GrisTexto,
    outlineVariant = GrisTexto,
)

@Composable
fun FutbolTNTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
