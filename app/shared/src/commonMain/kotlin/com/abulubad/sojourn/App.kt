package com.abulubad.sojourn

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.domain.ObserveThemeMode
import com.abulubad.sojourn.core.localization.AppLocaleController
import com.abulubad.sojourn.core.platform.AccessibilitySettings
import org.koin.compose.koinInject

/**
 * The root of the shared UI on both platforms: the theme in the guest's chosen language (which also sets the
 * layout direction, L-03) and appearance (System, Light or Dark), the network image loader, and the tabbed
 * navigation host. Koin must be started first (`initKoin`, called by `SojournApplication` on Android and `iOSApp`
 * on iOS).
 *
 * [onDarkTheme] tells the platform shell whether the app draws dark, so the status and navigation bar icons stay
 * legible when the guest's choice differs from the device's.
 */
@Composable
fun App(modifier: Modifier = Modifier, onDarkTheme: (Boolean) -> Unit = {}) {
    val localeController = koinInject<AppLocaleController>()
    val accessibility = koinInject<AccessibilitySettings>()
    val observeThemeMode = koinInject<ObserveThemeMode>()
    val locale by localeController.locale.collectAsStateWithLifecycle()
    val themeMode by observeThemeMode().collectAsStateWithLifecycle()
    val dark = themeMode.isDark(deviceIsDark = isSystemInDarkTheme())
    LaunchedEffect(dark) { onDarkTheme(dark) }
    // Above the theme, which keys its content by language: a language change keeps the tab and back stacks.
    val navState = rememberSojournNavState()
    ProvideImageLoader()
    SojournTheme(locale = locale, darkTheme = dark, reduceMotion = accessibility.reduceMotion()) {
        SojournNavHost(modifier, navState)
    }
}
