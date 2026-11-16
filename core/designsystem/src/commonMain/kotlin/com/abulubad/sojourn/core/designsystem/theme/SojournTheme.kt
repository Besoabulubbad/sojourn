package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.abulubad.sojourn.core.designsystem.tokens.SojournColors
import com.abulubad.sojourn.core.designsystem.tokens.SojournElevation
import com.abulubad.sojourn.core.designsystem.tokens.SojournType
import com.abulubad.sojourn.core.designsystem.tokens.darkSojournColors
import com.abulubad.sojourn.core.designsystem.tokens.darkSojournElevation
import com.abulubad.sojourn.core.designsystem.tokens.lightSojournColors
import com.abulubad.sojourn.core.designsystem.tokens.lightSojournElevation
import com.abulubad.sojourn.core.designsystem.tokens.sojournType
import com.abulubad.sojourn.core.localization.AppLocale
import com.abulubad.sojourn.core.localization.ProvideAppLocale

val LocalSojournColors = staticCompositionLocalOf<SojournColors> { error("No SojournTheme in this composition") }
val LocalSojournType = staticCompositionLocalOf<SojournType> { error("No SojournTheme in this composition") }
val LocalSojournElevation = staticCompositionLocalOf<SojournElevation> { error("No SojournTheme in this composition") }

/** True when the OS "reduce motion" setting is on; every transition helper reads it (M-03). */
val LocalReduceMotion = staticCompositionLocalOf { false }

private val LocalSojournIsDark = staticCompositionLocalOf { false }

/**
 * Wraps the app (ADR 0008). Provides the colour, type and elevation tokens for the active theme, the locale
 * environment (language, layout direction, resources — L-01), and the reduce-motion flag. A `MaterialTheme`
 * bridged from the same tokens sits underneath so Material components used *inside* `core:designsystem` match;
 * nothing outside this module sees Material.
 *
 * @param locale the in-app language, from `AppLocaleController` or a preview variant; never the device locale.
 * @param darkTheme dark is a full theme, not an inversion (brand book); defaults to the system setting.
 * @param reduceMotion the platform accessibility setting, from `core:platform`.
 */
@Composable
fun SojournTheme(
    locale: AppLocale,
    darkTheme: Boolean = isSystemInDarkTheme(),
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = remember(darkTheme) { if (darkTheme) darkSojournColors() else lightSojournColors() }
    val elevation = remember(darkTheme) { if (darkTheme) darkSojournElevation() else lightSojournElevation() }
    val display = SojournFonts.alexandria()
    val text = SojournFonts.readexPro()
    val type = remember(display, text) { sojournType(display = display, text = text) }

    ProvideAppLocale(locale) {
        CompositionLocalProvider(
            LocalSojournColors provides colors,
            LocalSojournType provides type,
            LocalSojournElevation provides elevation,
            LocalSojournIsDark provides darkTheme,
            LocalReduceMotion provides reduceMotion,
        ) {
            MaterialTheme(
                colorScheme = colors.toMaterialColorScheme(darkTheme),
                typography = type.toMaterialTypography(),
                shapes = sojournMaterialShapes(),
                content = content,
            )
        }
    }
}

/** Accessors for the current theme: `SojournTheme.colors.brand`, `SojournTheme.type.price`. */
object SojournTheme {
    val colors: SojournColors
        @Composable @ReadOnlyComposable
        get() = LocalSojournColors.current

    val type: SojournType
        @Composable @ReadOnlyComposable
        get() = LocalSojournType.current

    val elevation: SojournElevation
        @Composable @ReadOnlyComposable
        get() = LocalSojournElevation.current

    val isDark: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalSojournIsDark.current
}
