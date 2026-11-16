package com.abulubad.sojourn.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalLayoutDirection

/**
 * iOS: direction and [LocalAppLocale] are set here; string resources follow the `AppleLanguages` override that
 * [PlatformLocaleSync] writes when the language changes (ADR 0009).
 */
@Composable
actual fun ProvideAppLocale(locale: AppLocale, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppLocale provides locale,
        LocalLayoutDirection provides locale.layoutDirection,
    ) {
        key(locale) { content() }
    }
}
