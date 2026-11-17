package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.data.PricingRepository
import com.abulubad.sojourn.core.data.SavedRepository
import com.abulubad.sojourn.core.data.StayRepository
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One card in the results: the stay, the price it shows, its rating if shown (N-04), and the heart. */
data class StayResult(val stay: Stay, val price: PriceDisplay, val rating: Rating?, val saved: Boolean)

/**
 * The results for a query. Every card's price is on [basis] (P-04): `Stay` (all-in totals from quotes) with dates,
 * `Night` or `Month` ("from" prices, P-03) without. The filter sheet's price range must use the same basis.
 */
data class SearchResults(val stays: List<StayResult>, val basis: PriceBasis)

/**
 * Results for the Where / When / Who of [SearchQuery] and the guest's [Filters], offline-first (ADR 0007).
 *
 * - Without dates every matching stay shows its "from" price on the query's basis.
 * - With dates each matching stay is quoted. A stay the dates do not work for (minimum stay, capacity, already
 *   booked) is left out; cards appear as their quotes arrive, and each carries its quote id so every later surface
 *   shows the same total (P-02).
 * - A price range applies only when its basis matches the cards' (P-04), and only to prices in its currency.
 */
class SearchStays(
    private val stays: StayRepository,
    private val pricing: PricingRepository,
    private val saved: SavedRepository,
) {
    operator fun invoke(query: SearchQuery, filters: Filters, force: Boolean = false): Flow<Loadable<SearchResults>> {
        val basis = StayRules.basisFor(query)
        val cached = offlineFirst(stays.observeStays(query.cityId), { it.isNotEmpty() }) {
            stays.refresh(query.cityId, force)
        }
        val dates = query.dates ?: return combine(cached, saved.observeSavedIds()) { loadable, savedIds ->
            loadable.toResults(basis, filters) { stay -> StayRules.fromPrice(stay, basis).takeIf { stay.fits(query) } }
                .withSaved(savedIds)
        }
        return quoted(cached, query, dates, filters)
    }

    private fun quoted(
        cached: Flow<Loadable<List<Stay>>>,
        query: SearchQuery,
        dates: DateRange,
        filters: Filters,
    ): Flow<Loadable<SearchResults>> = channelFlow {
        val quotes = MutableStateFlow<Map<StayId, AppResult<Quote>>>(emptyMap())
        launch {
            // Quote the candidates once per distinct set; a refresh that returns the same stays costs nothing.
            cached.map { loadable -> loadable.data?.filter { it.fits(query) && it.matches(filters) }?.map { it.id } }
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { ids ->
                    ids.filterNot { it in quotes.value }.forEach { quote(it, dates, query.guests, quotes) }
                }
        }
        combine(cached, quotes, saved.observeSavedIds()) { loadable, quoted, savedIds ->
            val candidates = loadable.data?.filter { it.fits(query) && it.matches(filters) }.orEmpty()
            val quoting = candidates.any { it.id !in quoted }
            val offline = quoted.values.any { (it as? AppResult.Failure)?.error == AppError.Offline }
            val results = loadable.toResults(PriceBasis.Stay, filters) { stay ->
                (quoted[stay.id] as? AppResult.Success)?.value?.display
            }.withSaved(savedIds)
            results.copy(
                // Still loading while a candidate has no quote yet: an empty list now is not an answer.
                data = results.data?.takeUnless { it.stays.isEmpty() && quoting },
                refreshing = results.refreshing || quoting,
                error = results.error ?: AppError.Offline.takeIf { offline },
            )
        }.collect { send(it) }
    }

    private suspend fun quote(
        id: StayId,
        dates: DateRange,
        guests: Guests,
        into: MutableStateFlow<Map<StayId, AppResult<Quote>>>,
    ) {
        val result = pricing.quote(id, dates, guests)
        into.update { it + (id to result) }
    }

    private fun Loadable<List<Stay>>.toResults(
        basis: PriceBasis,
        filters: Filters,
        priceOf: (Stay) -> PriceDisplay?,
    ): Loadable<SearchResults> {
        val results = data?.mapNotNull { stay ->
            val price = priceOf(stay) ?: return@mapNotNull null
            StayResult(stay, price, StayRules.visibleRating(stay.rating), saved = false)
        }?.filter { it.stay.matches(filters) && it.price.inRange(filters, basis) }?.sortedWith(filters.sort.comparator)
        return Loadable(results?.let { SearchResults(it, basis) }, refreshing, error)
    }

    private fun Loadable<SearchResults>.withSaved(savedIds: List<StayId>): Loadable<SearchResults> {
        val ids = savedIds.toSet()
        return copy(data = data?.let { it.copy(stays = it.stays.map { r -> r.copy(saved = r.stay.id in ids) }) })
    }
}

/** Capacity and city: what a stay must meet before its price is even considered. */
private fun Stay.fits(query: SearchQuery): Boolean =
    query.guests.total <= facts.sleeps && (query.cityId == null || cityId == query.cityId)

/** The filters that do not involve price. */
private fun Stay.matches(filters: Filters): Boolean =
    amenities.containsAll(filters.amenities) &&
        facts.bedrooms >= (filters.minBedrooms ?: 0) &&
        (!filters.instantBook || instantBook)

/** P-04: a range on another basis does not apply; one in another currency excludes what it cannot compare. */
private fun PriceDisplay.inRange(filters: Filters, basis: PriceBasis): Boolean {
    val range = filters.priceRange ?: return true
    if (filters.basis != basis) return true
    return amount.currency == range.min.currency && amount in range
}

private val SortOrder.comparator: Comparator<StayResult>
    get() = when (this) {
        SortOrder.Recommended -> compareBy { it.stay.id.value }
        SortOrder.PriceLowToHigh -> compareBy<StayResult> { it.price.amount.currency }.thenBy { it.price.amount.minor }
        SortOrder.PriceHighToLow ->
            compareBy<StayResult> { it.price.amount.currency }.thenByDescending { it.price.amount.minor }
        // Hidden ratings (N-04) rank after every shown one.
        SortOrder.Rating -> compareByDescending<StayResult> { it.rating?.score ?: -1.0 }
            .thenByDescending { it.rating?.count ?: 0 }
    }
