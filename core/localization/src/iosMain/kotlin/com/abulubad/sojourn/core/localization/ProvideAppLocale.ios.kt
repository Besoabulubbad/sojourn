package com.abulubad.sojourn.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import platform.Foundation.NSUserDefaults

/**
 * iOS, following the Compose Multiplatform "Manage local resource environment" guide: Compose's `Locale.current`
 * (which `stringResource` resolves from) reads the preferred languages, so the app's `AppleLanguages` default is set
 * to the in-app language before anything below composes; `key(locale)` re-resolves resources on a change. Direction
 * and [LocalAppLocale] are provided here too.
 */
@Composable
actual fun ProvideAppLocale(locale: AppLocale, content: @Composable () -> Unit) {
    remember(locale) {
        NSUserDefaults.standardUserDefaults.setObject(listOf(locale.tag), forKey = APPLE_LANGUAGES)
    }
    CompositionLocalProvider(
        LocalAppLocale provides locale,
        LocalLayoutDirection provides locale.layoutDirection,
    ) {
        key(locale) { content() }
    }
}

private const val APPLE_LANGUAGES = "AppleLanguages"
