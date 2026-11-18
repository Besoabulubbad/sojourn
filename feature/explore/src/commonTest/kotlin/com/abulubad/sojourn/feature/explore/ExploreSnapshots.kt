package com.abulubad.sojourn.feature.explore

import com.abulubad.sojourn.core.testing.SnapshotCase

/** Every Explore state (I-05), rendered on Android and iOS; content in the full matrix (font 200 %, 320 dp). */
val exploreSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("explore_content", fullMatrix = true) { ExploreSamples.Screen(ExploreSamples.content) },
    SnapshotCase("explore_first_run") { ExploreSamples.Screen(ExploreSamples.firstRun) },
    SnapshotCase("explore_offline_cached") { ExploreSamples.Screen(ExploreSamples.offlineCached) },
    SnapshotCase("explore_offline_empty") { ExploreSamples.Screen(ExploreSamples.offlineEmpty) },
    SnapshotCase("explore_error") { ExploreSamples.Screen(ExploreSamples.error) },
)
