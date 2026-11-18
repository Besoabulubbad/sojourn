package com.abulubad.sojourn.feature.results

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.Loadable
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
import kotlin.time.Duration.Companion.hours

/**
 * Results (ScreenResults card): the stays for the search in [key], offline-first, under the guest's filters. The
 * quick-filter chips and the sort write the persisted filters, so the filter sheet and its count badge agree.
 * Every price on a card is the one later surfaces show: a dated card carries its quote id to Details (P-02).
 */
class ResultsViewModel(
    private val key: ResultsKey,
    private val searchStays: SearchStays,
    observeFilters: ObserveFilters,
    private val updateFilters: UpdateFilters,
    private val toggleSaved: ToggleSaved,
    private val clock: AppClock,
) : SojournViewModel<ResultsUiState, ResultsIntent, ResultsEffect>(ResultsUiState(key.args.toQuery())) {

    /**
     * The persisted filters, read directly when a chip or the sort changes them: the copy in state arrives a moment
     * later, and two quick taps built on it would lose the first.
     */
    private val filters = observeFilters()

    /** Bumped by pull to refresh and retry; each bump runs the search again past the refresh policy. */
    private val refreshes = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            combine(filters, refreshes) { filters, refresh -> filters to refresh }.collectLatest { (filters, refresh) ->
                setState { copy(filters = filters) }
                searchStays(currentState.query, filters, force = refresh > 0).collect { loadable -> show(loadable) }
            }
        }
    }

    override fun handle(intent: ResultsIntent) {
        when (intent) {
            ResultsIntent.BackTapped -> sendEffect(ResultsEffect.Back)
            ResultsIntent.SearchTapped -> navigate(SearchKey(args = key.args))
            ResultsIntent.FiltersTapped -> navigate(FiltersKey)
            is ResultsIntent.QuickFilterTapped -> updateFilters(intent.filter.toggle(filters.value))
            ResultsIntent.ClearFiltersTapped -> updateFilters(Filters(sort = filters.value.sort))
            ResultsIntent.SortTapped -> setState { copy(sortOpen = true) }
            is ResultsIntent.SortSelected -> {
                setState { copy(sortOpen = false) }
                updateFilters(filters.value.copy(sort = intent.sort))
            }
            ResultsIntent.SortDismissed -> setState { copy(sortOpen = false) }
            is ResultsIntent.StayTapped -> {
                val quoteId = (intent.result.price as? PriceDisplay.Total)?.quoteId?.value
                navigate(StayDetailsKey(intent.result.stay.id.value, quoteId))
            }
            is ResultsIntent.SaveTapped -> viewModelScope.launch { toggleSaved(intent.id) }
            ResultsIntent.MapTapped -> navigate(MapKey(key.args))
            ResultsIntent.Pulled -> {
                setState { copy(pulling = true) }
                refreshes.value++
            }
            ResultsIntent.RetryTapped -> refreshes.value++
        }
    }

    private fun show(loadable: Loadable<SearchResults>) {
        val now = clock.now()
        // Offline, a cached dated price older than an hour says how old it is.
        val stale = if (loadable.error == AppError.Offline) {
            loadable.data?.stays.orEmpty().mapNotNull { result ->
                val age = result.pricedAt?.let { now - it } ?: return@mapNotNull null
                if (age >= STALE_AFTER) result.stay.id to age.inWholeHours else null
            }.toMap()
        } else {
            emptyMap<StayId, Long>()
        }
        setState {
            copy(
                results = ScreenState.from(loadable) { it.stays.isEmpty() },
                pulling = pulling && loadable.refreshing,
                staleHours = stale,
            )
        }
    }

    private fun navigate(key: SojournNavKey) = sendEffect(ResultsEffect.Navigate(key))

    private companion object {
        val STALE_AFTER = 1.hours
    }
}
