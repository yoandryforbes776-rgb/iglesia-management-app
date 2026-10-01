package com.iglesiaflow.gestion.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.iglesiaflow.gestion.core.config.AppSettings
import com.iglesiaflow.gestion.domain.model.ThemeMode

/**
 * Tema Material 3 con soporte de color dinámico (Material You) y de paleta
 * personalizada definida por la iglesia en tiempo de ejecución.
 */
@Composable
fun IglesiaFlowTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val darkTheme = when (settings.themeMode) {
        ThemeMode.CLARO -> false
        ThemeMode.OSCURO -> true
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        settings.useDynamicColor && supportsDynamic ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> ColorSchemeFactory.dark(
            Color(settings.primaryColorArgb.toInt()),
            Color(settings.secondaryColorArgb.toInt())
        )
        else -> ColorSchemeFactory.light(
            Color(settings.primaryColorArgb.toInt()),
            Color(settings.secondaryColorArgb.toInt())
        )
    }

    // Capa "iOS": fondo agrupado gris, tarjetas blancas (o negras en oscuro) y
    // separadores finos, manteniendo el color de marca elegido por la iglesia.
    val iosScheme = if (darkTheme) {
        colorScheme.copy(
            background = IosGroupedBackgroundDark,
            onBackground = IosLabelDark,
            surface = IosCardDark,
            onSurface = IosLabelDark,
            surfaceVariant = IosCardElevatedDark,
            onSurfaceVariant = IosGray,
            surfaceContainerLowest = IosGroupedBackgroundDark,
            surfaceContainerLow = IosCardDark,
            surfaceContainer = IosCardDark,
            surfaceContainerHigh = IosCardElevatedDark,
            surfaceContainerHighest = IosCardElevatedDark,
            outline = IosGray,
            outlineVariant = IosSeparatorDark,
            error = IosRed
        )
    } else {
        colorScheme.copy(
            background = IosGroupedBackground,
            onBackground = IosLabel,
            surface = IosCard,
            onSurface = IosLabel,
            surfaceVariant = IosCard,
            onSurfaceVariant = IosGray,
            surfaceContainerLowest = IosCard,
            surfaceContainerLow = IosCard,
            surfaceContainer = IosCard,
            surfaceContainerHigh = IosGroupedBackground,
            surfaceContainerHighest = IosGroupedBackground,
            outline = IosGray,
            outlineVariant = IosSeparator,
            error = IosRed
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = iosScheme.background.toArgb()
            window.navigationBarColor = iosScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = iosScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
