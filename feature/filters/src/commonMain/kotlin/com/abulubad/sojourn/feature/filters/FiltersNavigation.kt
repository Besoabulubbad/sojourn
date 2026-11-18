package com.abulubad.sojourn.feature.filters

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.SojournScenes

/** The filter sheet's destination: an overlay over Results, which stays visible under the scrim. */
fun EntryProviderScope<NavKey>.filtersEntries(navigator: Navigator) {
    entry<FiltersKey>(metadata = SojournScenes.overlay()) { key -> FiltersRoute(key, navigator) }
}
