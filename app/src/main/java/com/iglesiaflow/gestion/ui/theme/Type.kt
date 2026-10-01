package com.iglesiaflow.gestion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()
private val sans = FontFamily.Default

/**
 * Escala tipográfica inspirada en iOS (San Francisco): títulos grandes y
 * compactos, cuerpo de 17 sp y pies de 13 sp, con interletraje negativo.
 */
val AppTypography = Typography(
    displaySmall = base.displaySmall.copy(
        fontFamily = sans,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.6).sp
    ),
    headlineLarge = base.headlineLarge.copy(
        fontFamily = sans,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = base.headlineMedium.copy(
        fontFamily = sans,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = base.headlineSmall.copy(
        fontFamily = sans,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = base.titleLarge.copy(
        fontFamily = sans,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp
    ),
    titleMedium = base.titleMedium.copy(
        fontFamily = sans,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp
    ),
    titleSmall = base.titleSmall.copy(
        fontFamily = sans,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontFamily = sans,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = sans,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = sans,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = base.labelLarge.copy(
        fontFamily = sans,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp
    ),
    labelMedium = base.labelMedium.copy(fontFamily = sans, fontSize = 13.sp),
    labelSmall = base.labelSmall.copy(
        fontFamily = sans,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )
)
