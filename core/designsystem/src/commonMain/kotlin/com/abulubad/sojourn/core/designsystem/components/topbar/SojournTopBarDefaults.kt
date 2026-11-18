package com.abulubad.sojourn.core.designsystem.components.topbar

import androidx.compose.ui.unit.dp

/** Measurements from the TopBar card. */
object SojournTopBarDefaults {
    /** Scroll distance over which a bar over a photo fills in: solid before any text reaches it. */
    val SolidAfter = 48.dp

    /** The fraction at which the floating buttons become plain and the title appears. */
    const val SOLID_SWITCH = 0.5f

    /** One segment of [SojournStepTopBar]'s progress bar. */
    val StepHeight = 4.dp
}
