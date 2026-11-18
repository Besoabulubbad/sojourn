package com.abulubad.sojourn.feature.results

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.Navigator
import com.abulubad.sojourn.core.navigation.ResultsKey

/** Results' destination, in the Explore tab: opened by a search, a city on Explore, or "Continue your search". */
fun EntryProviderScope<NavKey>.resultsEntries(navigator: Navigator) {
    entry<ResultsKey> { key -> ResultsRoute(key, navigator) }
}
