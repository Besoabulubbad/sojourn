package com.abulubad.sojourn.feature.map

import com.abulubad.sojourn.core.domain.SearchResults
import com.abulubad.sojourn.core.domain.StayResult
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.GeoBounds
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState

/**
 * The map's state: the same search, filters and [results] as Results, offline-first, and what the guest did on the
 * map. [cityCenter] is where the map opens before the stays are known. [selected] is the stay whose card is
 * raised; [seen] are stays whose card has been raised before (their pins dim). [area] is the box "Search this area"
 * last searched; [movedTo] is where the guest has panned or zoomed since, which is what shows the pill.
 */
data class MapUiState(
    val query: SearchQuery,
    val cityCenter: GeoPoint? = null,
    val filters: Filters = Filters.None,
    val results: ScreenState<SearchResults> = ScreenState.Loading,
    val selected: StayId? = null,
    val seen: Set<StayId> = emptySet(),
    val area: GeoBounds? = null,
    val movedTo: GeoBounds? = null,
) : UiState {

    /** Every stay the search found, from content or the offline cache. */
    val found: List<StayResult>
        get() = when (val shown = results) {
            is ScreenState.Content -> shown.data.stays
            is ScreenState.Offline -> shown.cached?.stays.orEmpty()
            else -> emptyList()
        }

    /** The stays on the map: those found, inside the searched area when there is one. */
    val stays: List<StayResult>
        get() = area?.let { box -> found.filter { it.stay.location.center in box } } ?: found

    /** The stay whose card is raised, while it is on the map. */
    val selectedStay: StayResult?
        get() = selected?.let { id -> stays.firstOrNull { it.stay.id == id } }

    /** "Search this area" shows once the guest has moved the map, and only then (ScreenMap card). */
    val showSearchArea: Boolean
        get() = movedTo != null
}

/** What the guest did on the map. */
sealed interface MapIntent : UiIntent {
    data object BackTapped : MapIntent
    data object SearchTapped : MapIntent
    data object FiltersTapped : MapIntent
    data object ListTapped : MapIntent
    data class PinTapped(val id: StayId) : MapIntent

    /** The card pager settled on [id]: a swipe moves the selection to the next pin. */
    data class CardShown(val id: StayId) : MapIntent
    data object MapTapped : MapIntent
    data class GuestMoved(val bounds: GeoBounds) : MapIntent
    data object SearchAreaTapped : MapIntent

    /** Back to every stay the search found, after an area with none. */
    data object ShowAllTapped : MapIntent
    data class StayTapped(val result: StayResult) : MapIntent
    data class SaveTapped(val id: StayId) : MapIntent
    data object ClearFiltersTapped : MapIntent
    data object RetryTapped : MapIntent
}

/** The map's one-off outputs. */
sealed interface MapEffect : UiEffect {
    data object Back : MapEffect
    data class Navigate(val key: SojournNavKey) : MapEffect

    /** The list for the same search, in the map's place, so list and map never stack up. */
    data class ShowList(val key: SojournNavKey) : MapEffect
}
