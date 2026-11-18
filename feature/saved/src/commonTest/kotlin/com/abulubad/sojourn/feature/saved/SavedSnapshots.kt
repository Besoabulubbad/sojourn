package com.abulubad.sojourn.feature.saved

import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * The Saved tab's frames (ScreenSaved card) on Android and iOS: the list in the full matrix (a dated save with its
 * total, an undated one with its "from" price), the list with "Undo" showing, empty, and loading.
 */
val savedSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("saved_list", fullMatrix = true) { SavedSamples.Screen(SavedSamples.stays) },
    SnapshotCase("saved_removed") { SavedSamples.Screen(SavedSamples.stays, removing = true) },
    SnapshotCase("saved_empty", fullMatrix = true) { SavedSamples.Screen(SavedSamples.empty) },
    SnapshotCase("saved_loading") { SavedSamples.Screen(SavedSamples.loading) },
)
