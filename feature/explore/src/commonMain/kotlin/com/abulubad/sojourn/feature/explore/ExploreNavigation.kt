package com.abulubad.sojourn.feature.explore

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.ExploreKey
import com.abulubad.sojourn.core.navigation.Navigator

/** Explore's destinations: the Explore tab's root. */
fun EntryProviderScope<NavKey>.exploreEntries(navigator: Navigator) {
    entry<ExploreKey> { ExploreRoute(navigator) }
}
