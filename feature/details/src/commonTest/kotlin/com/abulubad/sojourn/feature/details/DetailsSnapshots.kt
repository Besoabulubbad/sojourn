package com.abulubad.sojourn.feature.details

import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * Every Details state (I-05, ScreenStates) and the card's three frames, rendered on Android and iOS: the top with
 * dates in the full matrix, the middle scrolled, and the end without dates.
 */
val detailsSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("details_top", fullMatrix = true) { DetailsSamples.Screen(DetailsSamples.withDates) },
    SnapshotCase("details_middle") { DetailsSamples.Screen(DetailsSamples.withDates, fraction = 0.5f) },
    SnapshotCase("details_end_no_dates") { DetailsSamples.Screen(DetailsSamples.withoutDates, fraction = 1f) },
    SnapshotCase("details_translated") { DetailsSamples.Screen(DetailsSamples.translated, fraction = 0.75f) },
    SnapshotCase("details_loading") { DetailsSamples.Screen(DetailsSamples.loading) },
    SnapshotCase("details_gone") { DetailsSamples.Screen(DetailsSamples.gone) },
    SnapshotCase("details_offline") { DetailsSamples.Screen(DetailsSamples.offline) },
    SnapshotCase("details_choose_dates") { DetailsSamples.Screen(DetailsSamples.choosingDates) },
    SnapshotCase("details_price_details") { DetailsSamples.Screen(DetailsSamples.priceDetails) },
    SnapshotCase("gallery") { DetailsSamples.Gallery() },
)
