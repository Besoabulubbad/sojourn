package com.abulubad.sojourn.core.designsystem.components.sheet

import androidx.compose.ui.unit.dp

/** Measurements from the Sheet card. */
object SojournSheetDefaults {
    /** The peek detent's visible height. */
    val PeekHeight = 96.dp

    /** The drag handle. */
    val HandleWidth = 36.dp
    val HandleHeight = 4.dp

    /** A fling faster than this goes to the next detent in its direction instead of the nearest one. */
    val FlingThreshold = 125.dp
}
