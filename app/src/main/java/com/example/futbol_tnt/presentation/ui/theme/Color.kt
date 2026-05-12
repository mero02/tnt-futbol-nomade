package com.example.futbol_tnt.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Paleta fija — 7 colores (fuente: docs/design-system.md)
// =============================================================================

val Verde = Color(0xFF22C55E)         // CTA primario, FAB, badges activos, online
val VerdeOscuro = Color(0xFF0F5132)   // Header de perfil, contraste sobre verde
val Blanco = Color(0xFFFFFFFF)        // Background, surface, texto sobre verde/negro
val Negro = Color(0xFF111827)         // Texto principal, bottom nav, headlines
val GrisTexto = Color(0xFF6B7280)     // Texto secundario, placeholders, iconos inactivos
val GrisBorde = Color(0xFFE5E7EB)     // Bordes, separadores, fondo de input
val Rojo = Color(0xFFEF4444)          // Solo crítico: rechazar, urgente, login

// =============================================================================
// Aliases de compatibilidad — mapean nombres legacy a la paleta de 7 colores.
// Permiten que las screens existentes sigan compilando sin tocarlas.
// Para código nuevo: usar los 7 tokens de arriba directamente.
// =============================================================================

val BluePrimary = Verde
val BluePrimaryDark = VerdeOscuro
val BluePrimaryLight = Verde

val AccentGreen = Verde
val AccentGreenDark = VerdeOscuro
val AccentOrange = Rojo

val SurfaceGray = Blanco
val SurfaceGrayDark = Negro
val CardBackground = Blanco
val CardBackgroundDark = Negro

val EstadoConfirmada = Verde
val EstadoPendiente = GrisTexto
val EstadoCancelada = Rojo

val Gray50 = Blanco
val Gray100 = GrisBorde
val Gray200 = GrisBorde
val Gray300 = GrisBorde
val Gray400 = GrisTexto
val Gray500 = GrisTexto
val Gray600 = GrisTexto
val Gray700 = Negro
val Gray800 = Negro
val Gray900 = Negro

val DarkBackground = Negro
val DarkSurface = Negro
