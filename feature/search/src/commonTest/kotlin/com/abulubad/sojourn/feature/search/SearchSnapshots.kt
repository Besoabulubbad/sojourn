package com.abulubad.sojourn.feature.search

import com.abulubad.sojourn.core.testing.SnapshotCase

/** Each step and every Where state (I-05), rendered on Android and iOS; each step in the full matrix. */
val searchSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("search_where", fullMatrix = true) { SearchSamples.Screen(SearchSamples.where) },
    SnapshotCase("search_where_loading") { SearchSamples.Screen(SearchSamples.whereLoading) },
    SnapshotCase("search_where_offline_cached") { SearchSamples.Screen(SearchSamples.whereOfflineCached) },
    SnapshotCase("search_where_offline_empty") { SearchSamples.Screen(SearchSamples.whereOfflineEmpty) },
    SnapshotCase("search_when_dates", fullMatrix = true) { SearchSamples.Screen(SearchSamples.whenDates) },
    SnapshotCase("search_when_no_dates") { SearchSamples.Screen(SearchSamples.whenNoDates) },
    SnapshotCase("search_when_flexible", fullMatrix = true) { SearchSamples.Screen(SearchSamples.whenFlexible) },
    SnapshotCase("search_who_any_time", fullMatrix = true) { SearchSamples.Screen(SearchSamples.whoAnyTime) },
)
