package com.abulubad.sojourn.core.designsystem.components.bottomnav

import androidx.compose.runtime.Immutable
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon

/**
 * One destination in a [SojournBottomNav]. [value] is the caller's key (a navigation key in the app); [label]
 * comes from string resources ("Explore").
 */
@Immutable
class SojournNavTab<T>(val value: T, val icon: SojournIcon, val label: String)
