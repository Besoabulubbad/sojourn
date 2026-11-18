package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.Loadable
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.map
import com.abulubad.sojourn.core.data.CityRepository
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
import com.abulubad.sojourn.core.model.StayLength
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.time.Instant

/**
 * One card in the results: the stay, the price it shows, its rating if shown (N-04), and the heart.
 * [distanceMetres] is from the city's centre (the distance sort); [pricedAt] is when a dated price was quoted, so an
 * offline card can say how old it is.
 */
data class StayResult(
    val stay: Stay,
    val price: PriceDisplay,
    val rating: Rating?,
    val saved: Boolean,
    val distanceMetres: Int? = null,
    val pricedAt: Instant? = null,
)

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
 * - A flexible search with a start month ("1 month, starting in March") keeps the stays with a free run of that
 *   length starting in that month ([StayRules.hasFreeRun]). Each stay's booked dates are fetched first, so cards
 *   appear as they are checked; a stay that cannot be checked (offline, nothing cached) stays in, under the offline
 *   banner.
 * - A price range applies only when its basis matches the cards' (P-04), and only to prices in its currency.
 * - "Free cancellation" keeps stays whose free-cancellation window has not closed; Bedrooms is an exact count
 *   ("Studio", 1, 2) or three and more.
 * - Results are sorted last, by [Filters.sort]; distance is from the stay's city centre to its area's centre.
 */
class SearchStays(
    private val stays: StayRepository,
    private val pricing: PricingRepository,
    private val saved: SavedRepository,
    private val cities: CityRepository,
    private val clock: AppClock,
) {
    operator fun invoke(query: SearchQuery, filters: Filters, force: Boolean = false): Flow<Loadable<SearchResults>> =
        combine(unsorted(query, filters, force), cities.observeCities()) { loadable, known ->
            val centres = known.associate { it.id to it.location }
            loadable.map { results ->
                val placed = results.stays.map { result ->
                    val centre = centres[result.stay.cityId] ?: return@map result
                    result.copy(distanceMetres = StayRules.distanceMetres(centre, result.stay.location.center))
                }
                results.copy(stays = placed.sortedWith(filters.sort.comparator))
            }
        }

    private fun unsorted(query: SearchQuery, filters: Filters, force: Boolean): Flow<Loadable<SearchResults>> {
        val basis = StayRules.basisFor(query)
        val cached = offlineFirst(stays.observeStays(query.cityId), { it.isNotEmpty() }) {
            stays.refresh(query.cityId, force)
        }
        val length = query.length
        val month = query.startMonth
        if (query.dates == null && length != null && month != null) {
            return flexible(cached, query, filters, Flexible(length, month))
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
            cached.map { loadable ->
                loadable.data?.filter { it.fits(query) && it.matches(filters, clock.today()) }?.map { it.id }
            }
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { ids ->
                    ids.filterNot { it in quotes.value }.forEach { quote(it, dates, query.guests, quotes) }
                }
        }
        combine(cached, quotes, saved.observeSavedIds()) { loadable, quoted, savedIds ->
            val candidates = loadable.data?.filter { it.fits(query) && it.matches(filters, clock.today()) }.orEmpty()
            val quoting = candidates.any { it.id !in quoted }
            val offline = quoted.values.any { (it as? AppResult.Failure)?.error == AppError.Offline }
            val results = loadable.toResults(PriceBasis.Stay, filters) { stay ->
                (quoted[stay.id] as? AppResult.Success)?.value?.display
            }.withSaved(savedIds).map { found ->
                found.copy(
                    stays = found.stays.map { result ->
                        result.copy(pricedAt = (quoted[result.stay.id] as? AppResult.Success)?.value?.pricedAt)
                    },
                )
            }
            results.copy(
                // Still loading while a candidate has no quote yet: an empty list now is not an answer.
                data = results.data?.takeUnless { it.stays.isEmpty() && quoting },
                refreshing = results.refreshing || quoting,
                error = results.error ?: AppError.Offline.takeIf { offline },
            )
        }.collect { send(it) }
    }

    private fun flexible(
        cached: Flow<Loadable<List<Stay>>>,
        query: SearchQuery,
        filters: Filters,
        flexible: Flexible,
    ): Flow<Loadable<SearchResults>> = channelFlow {
        val basis = StayRules.basisFor(query)
        val checks = MutableStateFlow<Map<StayId, Check>>(emptyMap())
        launch {
            cached.map { loadable -> loadable.data?.filter { it.fits(query) && it.matches(filters, clock.today()) } }
                .filterNotNull()
                .distinctUntilChanged { old, new -> old.map { it.id } == new.map { it.id } }
                .collectLatest { candidates ->
                    candidates.filterNot { it.id in checks.value }.forEach { check(it, flexible, checks) }
                }
        }
        combine(cached, checks, saved.observeSavedIds()) { loadable, checked, savedIds ->
            val candidates = loadable.data?.filter { it.fits(query) && it.matches(filters, clock.today()) }.orEmpty()
            val checking = candidates.any { it.id !in checked }
            val offline = checked.values.any { it.error == AppError.Offline }
            val results = loadable.toResults(basis, filters) { stay ->
                StayRules.fromPrice(stay, basis).takeIf { stay.fits(query) && checked[stay.id]?.free == true }
            }.withSaved(savedIds)
            results.copy(
                data = results.data?.takeUnless { it.stays.isEmpty() && checking },
                refreshing = results.refreshing || checking,
                error = results.error ?: AppError.Offline.takeIf { offline },
            )
        }.collect { send(it) }
    }

    /** Fetches [stay]'s booked dates, then judges the run on whatever is cached: none cached counts as free. */
    private suspend fun check(stay: Stay, flexible: Flexible, into: MutableStateFlow<Map<StayId, Check>>) {
        val refreshed = stays.refreshStay(stay.id)
        val blocked = stays.observeBlockedDates(stay.id).first()
        val free = StayRules.hasFreeRun(stay, flexible.length, flexible.month, blocked, clock.today())
        into.update { it + (stay.id to Check(free, refreshed.errorOrNull())) }
    }

    private class Flexible(val length: StayLength, val month: YearMonth)

    private data class Check(val free: Boolean, val error: AppError?)

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
        }?.filter { it.stay.matches(filters, clock.today()) && StayRules.inPriceRange(it.price, filters, basis) }
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
private fun Stay.matches(filters: Filters, today: LocalDate): Boolean =
    amenities.containsAll(filters.amenities) &&
        (filters.bedrooms?.matches(facts.bedrooms) ?: true) &&
        facts.beds >= (filters.minBeds ?: 0) &&
        facts.bathrooms >= (filters.minBathrooms ?: 0) &&
        (!filters.instantBook || instantBook) &&
        (!filters.freeCancellation || StayRules.hasFreeCancellation(this, today))

private val SortOrder.comparator: Comparator<StayResult>
    get() = when (this) {
        SortOrder.Recommended -> compareBy { it.stay.id.value }
        SortOrder.PriceLowToHigh -> compareBy<StayResult> { it.price.amount.currency }.thenBy { it.price.amount.minor }
        // Hidden ratings (N-04) rank after every shown one.
        SortOrder.Rating -> compareByDescending<StayResult> { it.rating?.score ?: -1.0 }
            .thenByDescending { it.rating?.count ?: 0 }
        // A stay whose city is not known yet ranks last.
        SortOrder.DistanceToCentre -> compareBy<StayResult> { it.distanceMetres ?: Int.MAX_VALUE }
            .thenBy { it.stay.id.value }
    }
