package com.abulubad.sojourn.core.localization

import androidx.compose.runtime.Composable

/**
 * Makes [locale] the locale of everything inside [content]: [LocalAppLocale], `LocalLayoutDirection`, and the
 * resource environment that `stringResource` / `pluralStringResource` read.
 *
 * - **Android:** overrides the composition's `Configuration` and `Context` so Compose Multiplatform resources resolve
 *   `values-ar` while the device stays in any language (the four device/app pairs, L-03).
 * - **iOS:** resources follow the app's `AppleLanguages` override set by [PlatformLocaleSync]; this only sets the
 *   direction and [LocalAppLocale].
 *
 * `SojournTheme` calls this; screens never do.
 */
@Composable
expect fun ProvideAppLocale(locale: AppLocale, content: @Composable () -> Unit)
