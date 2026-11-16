package com.abulubad.sojourn.core.designsystem.components.rating

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * The Rating card's states. The typical rating is the default state and runs the full matrix on Android. The hidden
 * state (fewer than three reviews) has no image; `SojournRatingBehaviourTest` asserts it renders nothing.
 */
val ratingSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("rating_typical", fullMatrix = true) {
        SojournRating(SojournRatingSamples.typical, Modifier.padding(SojournSpacing.space4))
    },
    SnapshotCase("rating_large_count") {
        SojournRating(SojournRatingSamples.perfect, Modifier.padding(SojournSpacing.space4))
    },
    SnapshotCase("rating_without_count") {
        SojournRating(SojournRatingSamples.typical, Modifier.padding(SojournSpacing.space4), showCount = false)
    },
)
