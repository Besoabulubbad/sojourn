package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.data.PricingRepository
import com.abulubad.sojourn.core.data.SavedRepository
import com.abulubad.sojourn.core.data.StayRepository
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.SavedEntry
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Flips a stay's heart; returns the new state, so the pop plays only when it becomes saved. A new save keeps the
 * search it was made in ([query]'s dates and guests), so the Saved tab can show that search's total. Works offline.
 */
class ToggleSaved(private val saved: SavedRepository) {
    suspend operator fun invoke(id: StayId, query: SearchQuery? = null): Boolean =
        if (query == null) saved.toggle(id) else saved.toggle(id, query.dates, query.guests)
}

/** Un-saves a stay from the Saved tab and returns what was saved, so "Undo" can put it back ([RestoreSaved]). */
class RemoveSaved(private val saved: SavedRepository) {
    suspend operator fun invoke(id: StayId): SavedEntry? = saved.remove(id)
}

/** "Undo": the stay returns to the Saved tab where it was, with its search. */
class RestoreSaved(private val saved: SavedRepository) {
    suspend operator fun invoke(entry: SavedEntry) = saved.restore(entry)
}

/** A card on the Saved tab: the stay as a result, and the search it was saved from. */
data class SavedStay(val result: StayResult, val entry: SavedEntry)

/**
 * The Saved tab (ScreenSaved card): saved stays, the most recently saved first, local and so available offline. A
 * stay no longer in the catalogue is left out.
 *
 * - A card keeps the dates of the search it was saved from: those dates are priced like Results (the quote's all-in
 *   total, cached for offline, P-02). Until the quote arrives, and when there are no dates, the dates have passed or
 *   the stay can no longer take them, the card shows the monthly "from" price (P-03).
 */
class ObserveSaved(
    private val saved: SavedRepository,
    private val stays: StayRepository,
    private val pricing: PricingRepository,
    private val clock: AppClock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<SavedStay>> = channelFlow {
        val quotes = MutableStateFlow<Map<SavedEntry, Quote?>>(emptyMap())
        launch {
            saved.observeSaved().collectLatest { entries ->
                entries.filter { it.isPriceable() && it !in quotes.value }.forEach { entry ->
                    val dates = entry.dates ?: return@forEach
                    val quote = (pricing.quote(entry.stayId, dates, entry.guests) as? AppResult.Success)?.value
                    quotes.update { it + (entry to quote) }
                }
            }
        }
        val cards = saved.observeSaved().flatMapLatest { entries ->
            stays.observeStays(entries.mapTo(HashSet()) { it.stayId }).map { cached -> entries to cached }
        }
        combine(cards, quotes) { (entries, cached), quoted ->
            val byId = cached.associateBy { it.id }
            entries.mapNotNull { entry ->
                val stay = byId[entry.stayId] ?: return@mapNotNull null
                val quote = quoted[entry]?.takeIf { entry.isPriceable() }
                SavedStay(
                    result = StayResult(
                        stay = stay,
                        price = quote?.display ?: StayRules.fromPrice(stay, PriceBasis.Month),
                        rating = StayRules.visibleRating(stay.rating),
                        saved = true,
                        pricedAt = quote?.pricedAt,
                    ),
                    entry = entry,
                )
            }
        }.collect { send(it) }
    }

    /** Dates still ahead can be priced; past ones show the "from" price. */
    private fun SavedEntry.isPriceable(): Boolean = dates?.let { it.start >= clock.today() } ?: false
}
