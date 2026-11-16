package com.abulubad.sojourn.core.designsystem.components.pricerange

import androidx.compose.ui.unit.dp

/** Measurements from the RangeSlider card. */
object SojournPriceRangeDefaults {
    /** The thumb disc; its touch target is the slider's 48 dp height. */
    val ThumbSize = 28.dp
    val TrackHeight = 4.dp
    val HistogramHeight = 44.dp
    val BarGap = 3.dp
    val BarRadius = 2.dp

    /** Fields are slightly tighter than buttons vertically (`6px var(--space-3)` in the card). */
    val FieldVerticalPadding = 6.dp

    /** The shortest bar, so an empty bucket still reads as part of the histogram. */
    internal const val MIN_BAR_FRACTION = 0.06f
}
