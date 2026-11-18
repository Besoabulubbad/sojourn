package com.abulubad.sojourn.feature.search

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.RecordSearch
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.SearchStep
import com.abulubad.sojourn.core.navigation.toArgs
import com.abulubad.sojourn.core.navigation.toQuery
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/**
 * The full-screen search flow (ScreenSearchFlow card): Where, When, Who in one back-stack entry, so the steps slide
 * inside the screen and the progress bar fills (motion.md). [key] says where to start and what is already chosen:
 * Explore's stay-length shortcuts open it on Where with Flexible pre-set.
 *
 * Back goes one step back and closes the flow from Where. "Search" records the search for Explore's "Continue" row,
 * then hands over to the results.
 */
class SearchViewModel(
    key: SearchKey,
    private val getCities: GetCities,
    private val recordSearch: RecordSearch,
    clock: AppClock,
) : SojournViewModel<SearchUiState, SearchIntent, SearchEffect>(initialState(key, clock.today())) {

    private var citiesJob: Job? = null
    private var searching = false

    init {
        loadCities(force = false)
    }

    override fun handle(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.CityTapped -> setState { copy(cityId = intent.id, step = SearchStep.When) }
            is SearchIntent.ModeSelected -> setState { copy(mode = intent.mode) }
            is SearchIntent.MonthShown -> setState { copy(month = intent.month) }
            is SearchIntent.DatesSelected -> setState { copy(dates = intent.dates) }
            is SearchIntent.LengthSelected -> setState { copy(length = intent.length) }
            is SearchIntent.StartMonthSelected -> setState { copy(startMonth = intent.month) }
            SearchIntent.NextTapped -> setState {
                if (canContinueFromWhen) copy(anyTime = false, step = SearchStep.Who) else this
            }
            SearchIntent.AnyTimeTapped -> setState { copy(anyTime = true, step = SearchStep.Who) }
            is SearchIntent.AdultsChanged, is SearchIntent.ChildrenChanged -> guests(intent)
            SearchIntent.SearchTapped -> search()
            SearchIntent.BackTapped -> back()
            SearchIntent.RetryTapped -> loadCities(force = true)
        }
    }

    private fun guests(intent: SearchIntent) = setState {
        when (intent) {
            is SearchIntent.AdultsChanged ->
                copy(guests = guests.copy(adults = intent.adults.coerceIn(SearchUiState.Adults)))
            is SearchIntent.ChildrenChanged ->
                copy(guests = guests.copy(children = intent.children.coerceIn(SearchUiState.Children)))
            else -> this
        }
    }

    private fun back() {
        when (currentState.step) {
            SearchStep.Where -> sendEffect(SearchEffect.Close)
            SearchStep.When -> setState { copy(step = SearchStep.Where) }
            SearchStep.Who -> setState { copy(step = SearchStep.When) }
        }
    }

    private fun search() {
        if (searching) return
        searching = true
        val query = currentState.query
        viewModelScope.launch {
            // Recorded before leaving: the flow's scope ends once the results replace it.
            recordSearch(query)
            sendEffect(SearchEffect.ShowResults(ResultsKey(query.toArgs())))
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
}

/** The flow as [key] opens it: its step, and the choices its arguments already carry. */
internal fun initialState(key: SearchKey, today: LocalDate): SearchUiState {
    val query = key.args.toQuery()
    val thisMonth = today.yearMonth
    val startMonths = List(SearchUiState.START_MONTHS) { thisMonth.plus(it, DateTimeUnit.MONTH) }
    val flexible = query.dates == null && query.length != null
    return SearchUiState(
        step = key.step,
        today = today,
        cityId = query.cityId,
        mode = if (flexible) WhenMode.Flexible else WhenMode.Dates,
        dates = query.dates,
        month = query.dates?.start?.yearMonth ?: thisMonth,
        length = query.length ?: StayLength.Month,
        startMonth = query.startMonth?.takeIf { it in startMonths } ?: thisMonth,
        startMonths = startMonths,
        guests = Guests(
            adults = query.guests.adults.coerceIn(SearchUiState.Adults),
            children = query.guests.children.coerceIn(SearchUiState.Children),
        ),
    )
}
