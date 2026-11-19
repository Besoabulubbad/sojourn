package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.data.PreferencesRepository
import com.abulubad.sojourn.core.data.SearchRepository
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** The filter sheet's current state; it survives a restart. */
class ObserveFilters(private val preferences: PreferencesRepository) {
    operator fun invoke(): StateFlow<Filters> = preferences.filters
}

/** Applies the guest's filters. The price range's basis must be the one the results show (P-04). */
class UpdateFilters(private val preferences: PreferencesRepository) {
    operator fun invoke(filters: Filters) = preferences.setFilters(filters)
}

/** The search flow's "Recent" list, newest first. */
class ObserveRecentSearches(private val searches: SearchRepository) {
    operator fun invoke(limit: Int = SearchRepository.DEFAULT_LIMIT): Flow<List<SearchQuery>> =
        searches.observeRecent(limit)
}

/** Remembers a search when the guest runs it; a repeat moves to the top. */
class RecordSearch(private val searches: SearchRepository) {
    suspend operator fun invoke(query: SearchQuery) = searches.record(query)
}

/** The app's appearance (ScreenProfile card): System, Light or Dark. The theme follows it at once. */
class ObserveThemeMode(private val preferences: PreferencesRepository) {
    operator fun invoke(): StateFlow<ThemeMode> = preferences.themeMode
}

/** Chooses the app's appearance; it survives a restart. */
class SetThemeMode(private val preferences: PreferencesRepository) {
    operator fun invoke(mode: ThemeMode) = preferences.setThemeMode(mode)
}
