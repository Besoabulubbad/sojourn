package com.abulubad.sojourn.core.designsystem.components.skeleton

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Values the Skeleton card fixes that are not design tokens. */
object SojournSkeletonDefaults {
    /** One full pulse (dim, bright, dim). Stops under reduce motion. */
    const val PULSE_MILLIS: Int = 1400

    /** The stay-card skeleton: a 16:10 photo block at list width and three text lines (Skeleton card). */
    val PhotoHeight: Dp = 176.dp
    val LineHeightSmall: Dp = 14.dp
    val LineHeightLarge: Dp = 18.dp
    const val FIRST_LINE_FRACTION: Float = 0.45f
    const val SECOND_LINE_FRACTION: Float = 0.8f
    const val THIRD_LINE_FRACTION: Float = 0.6f
}
