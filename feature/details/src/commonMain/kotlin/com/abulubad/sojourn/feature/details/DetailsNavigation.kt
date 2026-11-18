package com.abulubad.sojourn.feature.details

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.GalleryKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.StayDetailsKey

/** Details and its full-screen gallery, pushed on whichever tab opened the stay. */
fun EntryProviderScope<NavKey>.detailsEntries(navigator: Navigator) {
    entry<StayDetailsKey> { key -> DetailsRoute(key, navigator) }
    entry<GalleryKey> { key -> GalleryRoute(key, navigator) }
}
