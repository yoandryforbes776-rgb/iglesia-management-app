package com.iglesiaflow.gestion.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Genera esquemas de color Material 3 a partir de los colores primario y
 * secundario elegidos por la iglesia, sin necesidad de recompilar la app.
 */
object ColorSchemeFactory {

    fun light(primary: Color, secondary: Color): ColorScheme {
        val tertiary = primary.rotateHue(40f)
        return lightColorScheme(
            primary = primary.tone(0.42f),
            onPrimary = Color.White,
            primaryContainer = primary.tone(0.90f),
            onPrimaryContainer = primary.tone(0.14f),
            secondary = secondary.tone(0.42f),
            onSecondary = Color.White,
            secondaryContainer = secondary.tone(0.90f),
            onSecondaryContainer = secondary.tone(0.14f),
            tertiary = tertiary.tone(0.42f),
            onTertiary = Color.White,
            tertiaryContainer = tertiary.tone(0.90f),
            onTertiaryContainer = tertiary.tone(0.14f),
            background = primary.tone(0.985f),
            onBackground = Color(0xFF1B1B21),
            surface = primary.tone(0.985f),
            onSurface = Color(0xFF1B1B21),
            surfaceVariant = secondary.tone(0.92f),
            onSurfaceVariant = secondary.tone(0.30f),
            outline = secondary.tone(0.50f),
            outlineVariant = secondary.tone(0.80f),
            error = Color(0xFFB3261E),
            onError = Color.White,
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        )
    }

    fun dark(primary: Color, secondary: Color): ColorScheme {
        val tertiary = primary.rotateHue(40f)
        return darkColorScheme(
            primary = primary.tone(0.80f),
            onPrimary = primary.tone(0.20f),
            primaryContainer = primary.tone(0.30f),
            onPrimaryContainer = primary.tone(0.90f),
            secondary = secondary.tone(0.80f),
            onSecondary = secondary.tone(0.20f),
            secondaryContainer = secondary.tone(0.30f),
            onSecondaryContainer = secondary.tone(0.90f),
            tertiary = tertiary.tone(0.80f),
            onTertiary = tertiary.tone(0.20f),
            tertiaryContainer = tertiary.tone(0.30f),
            onTertiaryContainer = tertiary.tone(0.90f),
            background = Color(0xFF121318),
            onBackground = Color(0xFFE3E1E9),
            surface = Color(0xFF121318),
            onSurface = Color(0xFFE3E1E9),
            surfaceVariant = secondary.tone(0.26f),
            onSurfaceVariant = secondary.tone(0.82f),
            outline = secondary.tone(0.60f),
            outlineVariant = secondary.tone(0.35f),
            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            errorContainer = Color(0xFF8C1D18),
            onErrorContainer = Color(0xFFF9DEDC)
        )
    }

    /** Reemplaza la luminosidad manteniendo el matiz y la saturación. */
    private fun Color.tone(lightness: Float): Color {
        val (h, s) = hueSaturation()
        return hslToColor(h, s.coerceAtLeast(0.12f), lightness.coerceIn(0f, 1f))
    }

    private fun Color.rotateHue(degrees: Float): Color {
        val (h, s) = hueSaturation()
        val lightness = luminanceApprox()
        return hslToColor((h + degrees) % 360f, s, lightness)
    }

    private fun Color.luminanceApprox(): Float {
        val maxValue = max(red, max(green, blue))
        val minValue = min(red, min(green, blue))
        return (maxValue + minValue) / 2f
    }

    private fun Color.hueSaturation(): Pair<Float, Float> {
        val maxValue = max(red, max(green, blue))
        val minValue = min(red, min(green, blue))
        val delta = maxValue - minValue
        val lightness = (maxValue + minValue) / 2f
        val hue = when {
            delta == 0f -> 0f
            maxValue == red -> 60f * (((green - blue) / delta) % 6f)
            maxValue == green -> 60f * (((blue - red) / delta) + 2f)
            else -> 60f * (((red - green) / delta) + 4f)
        }
        val saturation = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * lightness - 1f)).coerceAtLeast(0.0001f)
        return ((hue + 360f) % 360f) to saturation.coerceIn(0f, 1f)
    }

    private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
        val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
        val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
        val m = lightness - c / 2f
        val (r, g, b) = when {
            hue < 60f -> Triple(c, x, 0f)
            hue < 120f -> Triple(x, c, 0f)
            hue < 180f -> Triple(0f, c, x)
            hue < 240f -> Triple(0f, x, c)
            hue < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color((r + m).coerceIn(0f, 1f), (g + m).coerceIn(0f, 1f), (b + m).coerceIn(0f, 1f))
    }
}
