package com.abulubad.sojourn.feature.map

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.MapKey
import com.abulubad.sojourn.core.navigation.Navigator

/** The map, pushed on whichever tab opened it (Results' "Map", Details' "Show on map"). */
fun EntryProviderScope<NavKey>.mapEntries(navigator: Navigator) {
    entry<MapKey> { key -> MapRoute(key, navigator) }
}
