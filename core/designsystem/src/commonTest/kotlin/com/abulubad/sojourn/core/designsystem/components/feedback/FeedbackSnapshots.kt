package com.abulubad.sojourn.core.designsystem.components.feedback

import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateSamples
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeletonSamples
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarSamples
import com.abulubad.sojourn.core.testing.SnapshotCase

/** The Feedback group: Skeleton, Snackbar with the offline banner, EmptyState; each first case is the default. */
val feedbackSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("skeleton_stay_card", fullMatrix = true) { SojournSkeletonSamples.StayCard() },
    SnapshotCase("skeleton_row") { SojournSkeletonSamples.Row() },
    SnapshotCase("snackbar_and_banner", fullMatrix = true) { SojournSnackbarSamples.SnackbarAndBanner() },
    SnapshotCase("empty_state_no_matches", fullMatrix = true) { SojournEmptyStateSamples.NoMatches() },
)
