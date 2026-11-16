package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournColors
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournType

/**
 * Material 3 sees the tokens through this bridge, so Material internals (ripples, text selection, dialogs)
 * used inside `core:designsystem` agree with the Sojourn palette. Components never read `MaterialTheme`
 * for design decisions; they read `SojournTheme`.
 */
internal fun SojournColors.toMaterialColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandTint,
        onPrimaryContainer = brandInk,
        secondary = accentInk,
        onSecondary = onAccent,
        secondaryContainer = accentTint,
        onSecondaryContainer = ink,
        tertiary = save,
        background = bg,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = inkMuted,
        surfaceContainer = surface,
        surfaceContainerLow = bg,
        surfaceContainerHigh = surfaceSunken,
        outline = borderControl,
        outlineVariant = hairline,
        error = danger,
        onError = onDanger,
        errorContainer = dangerTint,
        onErrorContainer = danger,
        scrim = scrim,
    )
}

internal fun SojournType.toMaterialTypography(): Typography = Typography(
    displayLarge = display,
    displayMedium = display,
    displaySmall = titleLg,
    headlineLarge = titleLg,
    headlineMedium = titleMd,
    headlineSmall = titleSm,
    titleLarge = titleLg,
    titleMedium = titleMd,
    titleSmall = titleSm,
    bodyLarge = bodyLg,
    bodyMedium = body,
    bodySmall = bodySm,
    labelLarge = label,
    labelMedium = label,
    labelSmall = labelSm,
)

internal fun sojournMaterialShapes(): Shapes = Shapes(
    extraSmall = SojournShapes.xs,
    small = SojournShapes.sm,
    medium = SojournShapes.md,
    large = SojournShapes.lg,
    extraLarge = SojournShapes.xl,
)
