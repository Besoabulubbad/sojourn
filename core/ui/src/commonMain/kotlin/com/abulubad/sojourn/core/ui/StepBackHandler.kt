package com.abulubad.sojourn.core.ui

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 * Takes system back (Android back, the iOS edge swipe) while [enabled], for a screen that keeps its own steps: the
 * search flow goes one step back on it, and turns it off on the first step so back closes the flow through the
 * navigator. Previews and screenshot hosts have no dispatcher, so there it does nothing.
 */
@Composable
fun StepBackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (LocalNavigationEventDispatcherOwner.current == null) return
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = enabled,
        onBackCompleted = onBack,
    )
}
