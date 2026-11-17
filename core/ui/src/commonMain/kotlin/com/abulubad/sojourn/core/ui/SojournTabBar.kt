package com.abulubad.sojourn.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The app's bottom navigation, provided by `app:shared` and placed by each tab's root screen in its
 * [SojournScaffold]'s `bottomBar` slot. Screens deeper in a tab (Details, Checkout) leave it out, as the design does.
 *
 * Keeping it in the screen's own scaffold means one scaffold per screen measures the bar and owns the bottom inset
 * (I-01, I-03); a shell scaffold around the screens would pad for it twice. Empty by default, so previews and
 * screenshots of a screen alone render without it.
 */
val LocalTabBar: ProvidableCompositionLocal<@Composable () -> Unit> = staticCompositionLocalOf { {} }

/** Places the app's bottom navigation; call it from a tab root's `SojournScaffold(bottomBar = { SojournTabBar() })`. */
@Composable
fun SojournTabBar() {
    LocalTabBar.current()
}
