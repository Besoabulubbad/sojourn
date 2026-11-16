package com.abulubad.sojourn.core.designsystem.components.rating

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.model.Rating

/** Values the Rating card fixes that are not design tokens. */
object SojournRatingDefaults {

    /** Fewer reviews than this and the rating is hidden (N-04). */
    const val MINIMUM_REVIEWS: Int = 3

    /** The star is 14 dp in the Rating card, smaller than the `icon-sm` token, so that it sits inside body-sm text. */
    val StarSize: Dp = 14.dp

    /** Whether a rating is shown at all (N-04). */
    fun isShown(rating: Rating): Boolean = rating.count >= MINIMUM_REVIEWS
}
