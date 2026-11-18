package com.abulubad.sojourn.feature.map

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.ObserveFilters
import com.abulubad.sojourn.core.domain.SearchResults
import com.abulubad.sojourn.core.domain.SearchStays
import com.abulubad.sojourn.core.domain.ToggleSaved
import com.abulubad.sojourn.core.domain.UpdateFilters
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.MapKey
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.core.navigation.toQuery
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * The map (ScreenMap card) for the search in [key]: the same stays, filters and prices as Results, so a pin shows the
 * total its card shows (P-02). Selecting a pin raises its card; swiping the card selects the next pin. "Search this
 * area" appears after the guest pans or zooms, and searching it keeps only the stays in view. The camera itself is
 * the screen's: this keeps what the guest chose, not where the map looks.
 */
class MapViewModel(
    private val key: MapKey,
    private val searchStays: SearchStays,
    getCities: GetCities,
    observeFilters: ObserveFilters,
    private val updateFilters: UpdateFilters,
    private val toggleSaved: ToggleSaved,
) : SojournViewModel<MapUiState, MapIntent, MapEffect>(
    MapUiState(query = key.args.toQuery(), selected = key.stayId?.let(::StayId)),
) {

    private val filters = observeFilters()

    /** Bumped by "Try again"; each bump runs the search again past the refresh policy. */
    private val refreshes = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            getCities().collect { loadable ->
                val city = loadable.data?.firstOrNull { it.id == currentState.query.cityId } ?: return@collect
                setState { copy(cityCenter = city.location) }
            }
        }
        viewModelScope.launch {
            combine(filters, refreshes) { filters, refresh -> filters to refresh }.collectLatest { (filters, refresh) ->
                setState { copy(filters = filters) }
                searchStays(currentState.query, filters, force = refresh > 0).collect(::show)
            }
        }
    }

    override fun handle(intent: MapIntent) {
        when (intent) {
            MapIntent.BackTapped -> sendEffect(MapEffect.Back)
            MapIntent.SearchTapped -> navigate(SearchKey(args = key.args))
            MapIntent.FiltersTapped -> navigate(FiltersKey(key.args))
            MapIntent.ListTapped -> sendEffect(MapEffect.ShowList(ResultsKey(key.args)))
            is MapIntent.PinTapped -> select(intent.id)
            is MapIntent.CardShown -> select(intent.id)
            MapIntent.MapTapped -> setState { copy(selected = null, seen = seenWith(selected)) }
            is MapIntent.GuestMoved -> setState { copy(movedTo = intent.bounds) }
            MapIntent.SearchAreaTapped -> searchArea()
            MapIntent.ShowAllTapped -> setState { copy(area = null, movedTo = null) }
            is MapIntent.StayTapped,
            is MapIntent.SaveTapped,
            MapIntent.ClearFiltersTapped,
            MapIntent.RetryTapped,
            -> handleResults(intent)
        }
    }

    /** The intents about the stays themselves: a card, its heart, and the ways out of an empty or failed search. */
    private fun handleResults(intent: MapIntent) {
        when (intent) {
            is MapIntent.StayTapped -> {
                val quoteId = (intent.result.price as? PriceDisplay.Total)?.quoteId?.value
                navigate(StayDetailsKey(intent.result.stay.id.value, quoteId, key.args))
            }
            is MapIntent.SaveTapped -> viewModelScope.launch { toggleSaved(intent.id, currentState.query) }
            MapIntent.ClearFiltersTapped -> updateFilters(Filters(sort = currentState.filters.sort))
            MapIntent.RetryTapped -> refreshes.value++
            else -> Unit
        }
    }

    /** Raises [id]'s card; the stay it replaces counts as seen. */
    private fun select(id: StayId) = setState {
        if (selected == id) this else copy(selected = id, seen = seenWith(selected))
    }

    private fun searchArea() = setState {
        val box = movedTo ?: return@setState this
        val stillShown = selected?.takeIf { id -> found.any { it.stay.id == id && it.stay.location.center in box } }
        copy(area = box, movedTo = null, selected = stillShown)
    }

    private fun MapUiState.seenWith(id: StayId?): Set<StayId> = if (id == null) seen else seen + id

    private fun show(loadable: Loadable<SearchResults>) =
        setState { copy(results = ScreenState.from(loadable) { it.stays.isEmpty() }) }

    private fun navigate(key: SojournNavKey) = sendEffect(MapEffect.Navigate(key))
}
