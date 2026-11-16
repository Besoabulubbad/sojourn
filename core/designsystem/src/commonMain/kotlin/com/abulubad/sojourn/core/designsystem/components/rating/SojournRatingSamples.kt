package com.abulubad.sojourn.core.designsystem.components.rating

import com.abulubad.sojourn.core.model.Rating

/** The Rating card's sample, plus the edge cases. */
object SojournRatingSamples {
    val typical = Rating(score = 4.8, count = 32)
    val perfect = Rating(score = 5.0, count = 1_204)
    val tooFewReviews = Rating(score = 5.0, count = 2)
}
