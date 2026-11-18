package com.abulubad.sojourn.feature.results

import com.abulubad.sojourn.core.testing.SnapshotCase

/** Every Results state (I-05, ScreenStates), rendered on Android and iOS; the dated list in the full matrix. */
val resultsSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("results_content", fullMatrix = true) { ResultsSamples.Screen(ResultsSamples.content) },
    SnapshotCase("results_undated") { ResultsSamples.Screen(ResultsSamples.undated) },
    SnapshotCase("results_loading") { ResultsSamples.Screen(ResultsSamples.loading) },
    SnapshotCase("results_offline") { ResultsSamples.Screen(ResultsSamples.offline) },
    SnapshotCase("results_empty_filtered") { ResultsSamples.Screen(ResultsSamples.emptyFiltered) },
    SnapshotCase("results_error") { ResultsSamples.Screen(ResultsSamples.error) },
    SnapshotCase("results_sorting") { ResultsSamples.Screen(ResultsSamples.sorting) },
)
