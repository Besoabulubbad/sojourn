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
 * Android: a copy of the current [Configuration] with the app locale, provided together with a context created
 * from it, so Compose Multiplatform resources and any platform resource lookups resolve the app language.
 * Follows the pattern in the Compose Multiplatform "resource environment" guide.
 */
@Composable
actual fun ProvideAppLocale(locale: AppLocale, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val configuration = remember(locale, baseConfiguration) {
        val javaLocale = Locale.forLanguageTag(locale.tag)
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
