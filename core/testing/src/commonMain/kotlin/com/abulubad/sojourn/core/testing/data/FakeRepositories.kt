package com.abulubad.sojourn.core.testing.data

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.data.BookingRepository
import com.abulubad.sojourn.core.data.CityRepository
import com.abulubad.sojourn.core.data.PreferencesRepository
import com.abulubad.sojourn.core.data.PricingRepository
import com.abulubad.sojourn.core.data.SavedRepository
import com.abulubad.sojourn.core.data.SearchRepository
import com.abulubad.sojourn.core.data.StayRepository
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Instant

/*
 * In-memory stand-ins for the `core:data` repositories, for use case and ViewModel tests. Each separates what is
 * cached (what the database would hold) from what the network would return, so tests drive the offline-first states:
 * set [FakeStayRepository.refreshResult] to `AppError.Offline` and the cache stays as it was.
 */

private val OK: AppResult<Unit> = AppResult.Success(Unit)

class FakeCityRepository(var remote: List<City> = emptyList(), cached: List<City> = emptyList()) : CityRepository {
    val cache = MutableStateFlow(cached)
    var refreshResult: AppResult<Unit> = OK

    /** How long a refresh takes (virtual time under `runTest`), so a test can see the state while it runs. */
    var latency: Duration = Duration.ZERO
    var refreshCalls = 0
        private set

    override fun observeCities(): Flow<List<City>> = cache
    override fun observeCity(id: CityId): Flow<City?> = cache.map { list -> list.find { it.id == id } }
    override suspend fun refresh(force: Boolean): AppResult<Unit> {
        refreshCalls++
        delay(latency)
        if (refreshResult.isSuccess) cache.value = remote
        return refreshResult
    }
}

class FakeStayRepository(var remote: List<Stay> = emptyList(), cached: List<Stay> = emptyList()) : StayRepository {
    val cache = MutableStateFlow(cached)
    val blocked = MutableStateFlow<Map<StayId, List<DateRange>>>(emptyMap())
    var refreshResult: AppResult<Unit> = OK

    /** How long a refresh takes (virtual time under `runTest`). */
    var latency: Duration = Duration.ZERO
    var refreshCalls = 0
        private set

    override fun observeStays(cityId: CityId?): Flow<List<Stay>> =
        cache.map { list -> list.filter { cityId == null || it.cityId == cityId } }
    override fun observeStays(ids: Set<StayId>): Flow<List<Stay>> = cache.map { list -> list.filter { it.id in ids } }
    override fun observeStay(id: StayId): Flow<Stay?> = cache.map { list -> list.find { it.id == id } }
    override fun observeBlockedDates(id: StayId): Flow<List<DateRange>> = blocked.map { it[id].orEmpty() }
    override suspend fun hasCached(cityId: CityId?): Boolean = cache.value.any { cityId == null || it.cityId == cityId }

    override suspend fun refresh(cityId: CityId?, force: Boolean): AppResult<Unit> {
        refreshCalls++
        delay(latency)
        if (refreshResult.isSuccess) {
            cache.update { old -> old.filterNot { cityId == null || it.cityId == cityId } + remote.inScope(cityId) }
        }
        return refreshResult
    }

    override suspend fun refreshStay(id: StayId, force: Boolean): AppResult<Unit> {
        delay(latency)
        if (!refreshResult.isSuccess) return refreshResult
        val fresh = remote.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound(id.value))
        cache.update { old -> old.filterNot { it.id == id } + fresh }
        return OK
    }

    private fun List<Stay>.inScope(cityId: CityId?) = filter { cityId == null || it.cityId == cityId }
}

/** Prices with [quoteFor]. When [offline], it returns the stored quote for the same request, as the real one does. */
class FakePricingRepository(
    var quoteFor: (StayId, DateRange, Guests) -> AppResult<Quote> = { id, _, _ ->
        AppResult.Failure(AppError.NotFound(id.value))
    },
) : PricingRepository {
    val stored = MutableStateFlow<Map<QuoteId, Quote>>(emptyMap())
    var offline = false
    var quoteCalls = 0
        private set

    override suspend fun quote(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote> {
        quoteCalls++
        if (offline) {
            val cached = stored.value.values.find { it.stayId == stayId && it.dates == dates && it.guests == guests }
            return cached?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.Offline)
        }
        return quoteFor(stayId, dates, guests).also { result ->
            if (result is AppResult.Success) stored.update { it + (result.value.id to result.value) }
        }
    }

    override fun observeQuote(id: QuoteId): Flow<Quote?> = stored.map { it[id] }
}

/** Books any quote in [issued] (the API's memory); a quote not there is `NotFound`, as after an expiry. */
class FakeBookingRepository(private val createdAt: Instant = Instant.parse("2026-03-01T09:00:00Z")) :
    BookingRepository {
    val bookings = MutableStateFlow<List<Booking>>(emptyList())
    val issued = mutableMapOf<QuoteId, Quote>()
    var offline = false

    override fun observeBookings(): Flow<List<Booking>> =
        bookings.map { list -> list.sortedByDescending { it.dates.start } }
    override fun observeBooking(id: BookingId): Flow<Booking?> = bookings.map { list -> list.find { it.id == id } }

    override suspend fun book(quoteId: QuoteId): AppResult<Booking> {
        if (offline) return AppResult.Failure(AppError.Offline)
        val quote = issued[quoteId] ?: return AppResult.Failure(AppError.NotFound(quoteId.value))
        val booking = Booking(
            id = BookingId("b-${quoteId.value}"),
            reference = "SJ-${bookings.value.size + 1}",
            stayId = quote.stayId,
            quoteId = quote.id,
            dates = quote.dates,
            guests = quote.guests,
            total = quote.total,
            status = BookingStatus.Upcoming,
            createdAt = createdAt,
        )
        bookings.update { it + booking }
        return AppResult.Success(booking)
    }
}

class FakeSavedRepository(saved: List<StayId> = emptyList()) : SavedRepository {
    val ids = MutableStateFlow(saved)

    override fun observeSavedIds(): Flow<List<StayId>> = ids
    override suspend fun setSaved(id: StayId, saved: Boolean) {
        ids.update { old -> if (saved) listOf(id) + (old - id) else old - id }
    }
    override suspend fun toggle(id: StayId): Boolean = (id !in ids.value).also { setSaved(id, it) }
}

class FakeSearchRepository : SearchRepository {
    val recent = MutableStateFlow<List<SearchQuery>>(emptyList())

    override fun observeRecent(limit: Int): Flow<List<SearchQuery>> = recent.map { it.take(limit) }
    override suspend fun record(query: SearchQuery) {
        recent.update { old -> (listOf(query) + (old - query)).take(SearchRepository.KEEP) }
    }
    override suspend fun clear() {
        recent.value = emptyList()
    }
}

class FakePreferencesRepository(initial: Filters = Filters.None) : PreferencesRepository {
    private val state = MutableStateFlow(initial)
    override val filters: StateFlow<Filters> = state.asStateFlow()
    override fun setFilters(filters: Filters) {
        state.value = filters
    }
}
