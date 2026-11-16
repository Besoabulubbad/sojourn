package com.abulubad.sojourn.core.localization

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import java.util.Locale

/**
 * Android, following the Compose Multiplatform "Manage local resource environment" guide:
 *
 * 1. `Locale.setDefault` — Compose Multiplatform resources (1.12) resolve `stringResource` from Compose's
 *    `Locale.current`, which reads the JVM default locale, not the composition's `Configuration`. Without this the
 *    Arabic UI shows English strings (caught by the Price screenshot test).
 * 2. A copy of the current [Configuration] and a context created from it, so platform resource lookups agree.
 * 3. `key(locale)` so everything below recomposes and re-resolves its resources when the language changes.
 */
@Composable
actual fun ProvideAppLocale(locale: AppLocale, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val configuration = remember(locale, baseConfiguration) {
        val javaLocale = Locale.forLanguageTag(locale.tag)
        Locale.setDefault(javaLocale)
        Configuration(baseConfiguration).apply {
            setLocales(LocaleList(javaLocale))
            setLayoutDirection(javaLocale)
        }
    }
    val context = remember(baseContext, configuration) { baseContext.createConfigurationContext(configuration) }
    CompositionLocalProvider(
        LocalAppLocale provides locale,
        LocalConfiguration provides configuration,
        LocalContext provides context,
        LocalResources provides context.resources,
        LocalLayoutDirection provides locale.layoutDirection,
    ) {
        key(locale) { content() }
    }
}
