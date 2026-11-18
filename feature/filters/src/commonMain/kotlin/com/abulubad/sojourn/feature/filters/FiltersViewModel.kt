package com.abulubad.sojourn.feature.filters

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.domain.ObserveFilters
import com.abulubad.sojourn.core.domain.PreviewFilters
import com.abulubad.sojourn.core.domain.UpdateFilters
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.toQuery
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * The filter sheet (ScreenFilters card) over the results for [key]. The guest edits a draft of the persisted
 * filters; the sheet counts what the draft keeps, 300 ms after the last change, with the same search Results runs, so
 * "Show 14 stays" shows 14. Only that button applies the draft; closing the sheet any other way discards it.
 */
@OptIn(FlowPreview::class)
class FiltersViewModel(
    key: FiltersKey,
    observeFilters: ObserveFilters,
    private val updateFilters: UpdateFilters,
    private val previewFilters: PreviewFilters,
) : SojournViewModel<FiltersUiState, FiltersIntent, FiltersEffect>(
    FiltersUiState(query = key.args.toQuery(), draft = observeFilters().value),
) {

    init {
        viewModelScope.launch {
            val drafts = state.map { it.draft }.distinctUntilChanged()
            // The opening draft is counted at once; each change after it once the guest pauses.
            merge(drafts.take(1), drafts.drop(1).debounce(COUNT_AFTER)).collectLatest { draft ->
                setState { copy(counting = true) }
                previewFilters(currentState.query, draft).collect { loadable ->
                    // A count is shown once settled: a half-quoted search would count up under the guest's eyes.
                    if (loadable.refreshing) return@collect
                    val found = loadable.data
                    setState {
                        copy(preview = found ?: preview, prices = found?.prices ?: prices, counting = false)
                    }
                }
            }
        }
    }

    override fun handle(intent: FiltersIntent) {
        when (intent) {
            is FiltersIntent.PriceChanged -> {
                val basis = currentState.preview?.basis ?: return
                // The full bounds filter nothing: store no range rather than one that excludes tomorrow's prices.
                val range = intent.range.takeUnless { it == currentState.prices?.bounds }
                edit { copy(priceRange = range, basis = basis) }
            }
            is FiltersIntent.BedroomsSelected -> edit { copy(bedrooms = intent.choice) }
            is FiltersIntent.BedsChanged -> edit { copy(minBeds = intent.beds.takeIf { it > 1 }) }
            is FiltersIntent.BathroomsChanged -> edit { copy(minBathrooms = intent.bathrooms.takeIf { it > 1 }) }
            is FiltersIntent.AmenityTapped -> edit {
                val amenity = intent.amenity
                copy(amenities = if (amenity in amenities) amenities - amenity else amenities + amenity)
            }
            FiltersIntent.FreeCancellationTapped -> edit { copy(freeCancellation = !freeCancellation) }
            FiltersIntent.InstantBookTapped -> edit { copy(instantBook = !instantBook) }
            // Clears the sheet but keeps it open; the sort is Results' order, not a filter.
            FiltersIntent.ClearAllTapped -> edit { Filters(sort = sort) }
            FiltersIntent.ApplyTapped -> {
                if (currentState.apply == ApplyButton.NoMatch) return
                updateFilters(currentState.draft)
                sendEffect(FiltersEffect.Close)
            }
            FiltersIntent.Dismissed -> sendEffect(FiltersEffect.Back)
        }
    }

    private fun edit(change: Filters.() -> Filters) = setState { copy(draft = draft.change()) }

    private companion object {
        /** The ScreenFilters card: the count updates 300 ms after the last change. */
        val COUNT_AFTER = 300.milliseconds
    }
}
