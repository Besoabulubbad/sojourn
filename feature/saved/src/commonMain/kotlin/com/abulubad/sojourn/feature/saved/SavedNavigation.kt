package com.abulubad.sojourn.feature.saved

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.SavedKey

/** The Saved tab's root. A saved stay opens Details on this tab's own stack. */
fun EntryProviderScope<NavKey>.savedEntries(navigator: Navigator) {
    entry<SavedKey> { SavedRoute(navigator) }
}
