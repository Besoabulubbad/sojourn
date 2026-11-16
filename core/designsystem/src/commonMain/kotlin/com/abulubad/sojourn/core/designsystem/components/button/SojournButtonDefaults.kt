package com.abulubad.sojourn.core.designsystem.components.button

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Values the Button card fixes that are not design tokens. */
object SojournButtonDefaults {
    /** The loading spinner: 18 dp ring, 2 dp stroke (Button card). */
    val SpinnerSize: Dp = 18.dp
    val SpinnerStroke: Dp = 2.dp

    /** One spinner revolution. A progress indicator, not a transition, so it is not a motion token. */
    const val SPINNER_REVOLUTION_MILLIS: Int = 800

    /** "Darken 8 %" while pressed (motion.md). */
    const val PRESSED_OVERLAY_ALPHA: Float = 0.08f
}
