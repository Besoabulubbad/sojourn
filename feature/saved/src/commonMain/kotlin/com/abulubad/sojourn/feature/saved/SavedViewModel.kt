package com.abulubad.sojourn.feature.saved

import androidx.lifecycle.viewModelScope
import com.abulubad.sojourn.core.domain.ObserveSaved
import com.abulubad.sojourn.core.domain.RemoveSaved
import com.abulubad.sojourn.core.domain.RestoreSaved
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.SavedEntry
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.core.navigation.toArgs
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournViewModel
import kotlinx.coroutines.launch

/**
 * The Saved tab (ScreenSaved card): the guest's shortlist, from the device, so it works offline. A card keeps the
 * search it was saved from: tapping it opens Details for those dates and guests, with the quote the card shows.
 * Removing a stay takes it off at once; "Undo" puts it back where it was, with its search.
 */
class SavedViewModel(
    observeSaved: ObserveSaved,
    private val removeSaved: RemoveSaved,
    private val restoreSaved: RestoreSaved,
) : SojournViewModel<SavedUiState, SavedIntent, SavedEffect>(SavedUiState()) {

    /** The last stay removed, for "Undo" while its snackbar shows. */
    private var removed: SavedEntry? = null

    init {
        viewModelScope.launch {
            observeSaved().collect { stays ->
                setState { copy(saved = ScreenState.fromData(stays) { it.isEmpty() }) }
            }
        }
    }

    override fun handle(intent: SavedIntent) {
        when (intent) {
            is SavedIntent.StayTapped -> {
                val stay = intent.stay
                val quoteId = (stay.result.price as? PriceDisplay.Total)?.quoteId?.value
                val search = SearchQuery(
                    cityId = stay.result.stay.cityId,
                    dates = stay.entry.dates,
                    guests = stay.entry.guests,
                )
                sendEffect(SavedEffect.Navigate(StayDetailsKey(stay.result.stay.id.value, quoteId, search.toArgs())))
            }
            is SavedIntent.RemoveTapped -> viewModelScope.launch {
                removed = removeSaved(intent.id) ?: return@launch
                sendEffect(SavedEffect.ShowRemoved)
            }
            SavedIntent.UndoTapped -> {
                val entry = removed ?: return
                removed = null
                viewModelScope.launch { restoreSaved(entry) }
            }
            SavedIntent.ExploreTapped -> sendEffect(SavedEffect.ShowExplore)
        }
    }
}
