package com.abulubad.sojourn.feature.results

import com.abulubad.sojourn.core.domain.SearchResults
import com.abulubad.sojourn.core.domain.StayResult
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/**
 * Results' state: the [query] from the search flow, the guest's [filters] (persisted, shared with the filter
 * sheet), and the [results] offline-first. [pulling] is true only while a refresh the guest pulled for is running.
 * Offline, [staleHours] says how old each dated price is once it is over an hour (ScreenStates card).
 */
data class ResultsUiState(
    val query: SearchQuery,
    val filters: Filters = Filters.None,
    val results: ScreenState<SearchResults> = ScreenState.Loading,
    val pulling: Boolean = false,
    val sortOpen: Boolean = false,
    val staleHours: Map<StayId, Long> = emptyMap(),
) : UiState

/**
 * The card's quick-filter chips, the same in every language, each toggling one filter. Studio is the sheet's
 * Bedrooms "Studio": turning it on replaces another bedroom choice, turning it off leaves any.
 */
enum class QuickFilter {
    Studio,
    Workspace,
    Washer,
    FreeCancellation,
    ;

    fun isOn(filters: Filters): Boolean = when (this) {
        Studio -> filters.bedrooms == BedroomChoice.Studio
        Workspace -> Amenity.Workspace in filters.amenities
        Washer -> Amenity.Washer in filters.amenities
        FreeCancellation -> filters.freeCancellation
    }

    fun toggle(filters: Filters): Filters = when (this) {
        Studio -> filters.copy(bedrooms = BedroomChoice.Studio.takeUnless { filters.bedrooms == it })
        Workspace -> filters.copy(amenities = filters.amenities.toggled(Amenity.Workspace))
        Washer -> filters.copy(amenities = filters.amenities.toggled(Amenity.Washer))
        FreeCancellation -> filters.copy(freeCancellation = !filters.freeCancellation)
    }
}

private fun Set<Amenity>.toggled(amenity: Amenity): Set<Amenity> =
    if (amenity in this) this - amenity else this + amenity

/** What the guest did on Results. */
sealed interface ResultsIntent : UiIntent {
    data object BackTapped : ResultsIntent
    data object SearchTapped : ResultsIntent
    data object FiltersTapped : ResultsIntent
    data class QuickFilterTapped(val filter: QuickFilter) : ResultsIntent
    data object ClearFiltersTapped : ResultsIntent
    data object SortTapped : ResultsIntent
    data class SortSelected(val sort: SortOrder) : ResultsIntent
    data object SortDismissed : ResultsIntent
    data class StayTapped(val result: StayResult) : ResultsIntent
    data class SaveTapped(val id: StayId) : ResultsIntent
    data object MapTapped : ResultsIntent
    data object Pulled : ResultsIntent
    data object RetryTapped : ResultsIntent
}

/** Results' one-off outputs. */
sealed interface ResultsEffect : UiEffect {
    data object Back : ResultsEffect
    data class Navigate(val key: SojournNavKey) : ResultsEffect
}
