package com.abulubad.sojourn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.localization.AppLocaleController
import com.abulubad.sojourn.core.platform.AccessibilitySettings
import org.koin.compose.koinInject

/**
 * The root of the shared UI on both platforms: the theme in the guest's chosen language (which also sets the
 * layout direction, L-03), the network image loader, and the tabbed navigation host. Koin must be started first
 * (`initKoin`, called by `SojournApplication` on Android and `iOSApp` on iOS).
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    val localeController = koinInject<AppLocaleController>()
    val accessibility = koinInject<AccessibilitySettings>()
    val locale by localeController.locale.collectAsStateWithLifecycle()
    ProvideImageLoader()
    SojournTheme(locale = locale, reduceMotion = accessibility.reduceMotion()) {
        SojournNavHost(modifier)
    }
}
