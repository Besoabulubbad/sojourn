package com.abulubad.sojourn.feature.search

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.SearchKey

/** The search flow's destination: one entry for all three steps, opened from Explore. */
fun EntryProviderScope<NavKey>.searchEntries(navigator: Navigator) {
    entry<SearchKey> { key -> SearchRoute(key, navigator) }
}
