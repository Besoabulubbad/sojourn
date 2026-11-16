package com.abulubad.sojourn.core.designsystem.components.segmented

import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes

/** Measurements from the SegmentedControl card. */
object SojournSegmentedControlDefaults {
    /** Inset of the sunken track around the segments. */
    val TrackPadding = 3.dp

    /** Gap between segments. */
    val SegmentGap = 2.dp

    /**
     * The card's segments are 40 dp; 42 dp makes the whole control `touch-min` tall, so every segment has a
     * 48 dp target without growing the control.
     */
    val SegmentMinHeight = SojournSizes.touchMin - TrackPadding * 2

    /** Two to five options; more, or labels that do not fit, use chips. */
    val OptionCount = 2..5
}
