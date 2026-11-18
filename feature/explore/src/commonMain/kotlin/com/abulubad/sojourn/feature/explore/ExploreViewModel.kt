package com.abulubad.sojourn.feature.explore

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.ListBookings
import com.abulubad.sojourn.core.domain.ObserveRecentSearches
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchArgs
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.SearchStep
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.navigation.toArgs
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import com.abulubad.sojourn.core.ui.dataOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Explore (ScreenExplore card): cities offline-first, and the "Continue" row from the next upcoming stay or the
 * last search. Every tap is a navigation effect; the search bar and the shortcuts open the guided search flow.
 */
class ExploreViewModel(
    private val getCities: GetCities,
    observeRecentSearches: ObserveRecentSearches,
    listBookings: ListBookings,
) : SojournViewModel<ExploreUiState, ExploreIntent, ExploreEffect>(ExploreUiState()) {

    private var citiesJob: Job? = null

    init {
        loadCities(force = false)
        viewModelScope.launch {
            val cities = state.map { it.cities.dataOrNull.orEmpty() }.distinctUntilChanged()
            combine(observeRecentSearches(limit = 1), listBookings(), cities) { recent, bookings, known ->
                val next = bookings.firstOrNull {
                    it.booking.status == BookingStatus.Upcoming || it.booking.status == BookingStatus.Active
                }
                when {
                    next != null -> ExploreResume.NextStay(next.booking, next.stay?.cityId, next.stay?.photos?.first())
                    recent.isNotEmpty() -> recent.first().let { ExploreResume.LastSearch(it, known.photoOf(it)) }
                    else -> null
                }
            }.collect { resume -> setState { copy(resume = resume) } }
        }
    }

    override fun handle(intent: ExploreIntent) {
        when (intent) {
            ExploreIntent.SearchTapped -> navigate(SearchKey())
            is ExploreIntent.LengthTapped -> navigate(SearchKey(args = SearchArgs(length = intent.length.name)))
            is ExploreIntent.CityTapped -> navigate(ResultsKey(SearchArgs(cityId = intent.id.value)))
            ExploreIntent.SeeAllCitiesTapped -> navigate(SearchKey(step = SearchStep.Where))
            ExploreIntent.ResumeTapped -> when (val resume = currentState.resume) {
                is ExploreResume.LastSearch -> navigate(ResultsKey(resume.query.toArgs()))
                is ExploreResume.NextStay -> navigate(BookingDetailsKey(resume.booking.id.value))
                null -> Unit
            }
            ExploreIntent.RetryTapped -> loadCities(force = true)
        }
    }

    private fun loadCities(force: Boolean) {
        citiesJob?.cancel()
        citiesJob = viewModelScope.launch {
            getCities(force).collect { loadable ->
                setState { copy(cities = ScreenState.from(loadable) { it.isEmpty() }) }
            }
        }
    }

    private fun navigate(key: SojournNavKey) =
        sendEffect(ExploreEffect.Navigate(key))
}

private fun List<City>.photoOf(query: SearchQuery) = query.cityId?.let { id -> find { it.id == id }?.photo }
