package com.iglesiaflow.gestion.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de estilo iOS: azul del sistema como color de marca y grises
 * "agrupados" para fondos y tarjetas.
 */
val SeedPrimary = Color(0xFF007AFF)
val SeedSecondary = Color(0xFF5856D6)

// --- Colores del sistema iOS ---
val IosBlue = Color(0xFF007AFF)
val IosGreen = Color(0xFF34C759)
val IosRed = Color(0xFFFF3B30)
val IosOrange = Color(0xFFFF9500)
val IosTeal = Color(0xFF30B0C7)
val IosPurple = Color(0xFFAF52DE)
val IosPink = Color(0xFFFF2D55)
val IosGray = Color(0xFF8E8E93)

// Fondos claros
val IosGroupedBackground = Color(0xFFF2F2F7)
val IosCard = Color(0xFFFFFFFF)
val IosSeparator = Color(0xFFD1D1D6)
val IosLabel = Color(0xFF1C1C1E)

// Fondos oscuros
val IosGroupedBackgroundDark = Color(0xFF000000)
val IosCardDark = Color(0xFF1C1C1E)
val IosCardElevatedDark = Color(0xFF2C2C2E)
val IosSeparatorDark = Color(0xFF38383A)
val IosLabelDark = Color(0xFFF2F2F7)

val StatusPositive = IosGreen
val StatusWarning = IosOrange
val StatusNegative = IosRed
val StatusInfo = IosBlue

val ChartPalette = listOf(
    IosBlue,
    IosGreen,
    IosOrange,
    IosPurple,
    IosTeal,
    IosPink,
    Color(0xFFFFCC00)
)

/** Semillas configurables desde Administración → Apariencia (ARGB). */
val SeedPalette = listOf(
    0xFF007AFFL, 0xFF5856D6L, 0xFF34C759L, 0xFFFF9500L,
    0xFFFF3B30L, 0xFFAF52DEL, 0xFF30B0C7L, 0xFFFF2D55L,
    0xFF8E8E93L, 0xFF00C7BEL
)
