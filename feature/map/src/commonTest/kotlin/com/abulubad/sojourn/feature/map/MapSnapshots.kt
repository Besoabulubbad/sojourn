package com.abulubad.sojourn.feature.map

import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * Every map state (I-05, ScreenStates) over the stand-in map (ADR 0017), on Android and iOS: the card's frame in the
 * full matrix, browsing with "Search this area", an area with no stays, no matches, loading, offline and an error.
 */
val mapSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("map_selected", fullMatrix = true) { MapSamples.Screen(MapSamples.selected) },
    SnapshotCase("map_moved") { MapSamples.Screen(MapSamples.moved) },
    SnapshotCase("map_area_empty") { MapSamples.Screen(MapSamples.areaEmpty) },
    SnapshotCase("map_empty") { MapSamples.Screen(MapSamples.empty) },
    SnapshotCase("map_loading") { MapSamples.Screen(MapSamples.loading) },
    SnapshotCase("map_offline") { MapSamples.Screen(MapSamples.offline) },
    SnapshotCase("map_error") { MapSamples.Screen(MapSamples.error) },
)
