package com.abulubad.sojourn.feature.filters

import com.abulubad.sojourn.core.testing.SnapshotCase

/** Every filter-sheet state, rendered on Android and iOS; the card's state in the full matrix. */
val filtersSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("filters_card", fullMatrix = true) { FiltersSamples.Screen(FiltersSamples.card) },
    SnapshotCase("filters_undated") { FiltersSamples.Screen(FiltersSamples.undated) },
    SnapshotCase("filters_no_match") { FiltersSamples.Screen(FiltersSamples.noMatch) },
    SnapshotCase("filters_counting") { FiltersSamples.Screen(FiltersSamples.counting) },
)
