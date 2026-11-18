package com.abulubad.sojourn.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.data.local.lineRows
import com.abulubad.sojourn.core.data.local.toModel
import com.abulubad.sojourn.core.data.local.toRow
import com.abulubad.sojourn.core.database.SojournDatabase
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.network.SojournApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Quotes: priced stays for chosen dates. Every quote the API issues is stored, and every surface that shows a total
 * (card, pin, booking bar, checkout, pay button) reads it back by [QuoteId], so they all show the same number (P-02).
 * The breakdown's lines sum to its total by construction, on the way in and on the way back out (P-05). Each quote is
 * stamped with the time it was priced ([Quote.pricedAt]) as it is stored.
 */
interface PricingRepository {

    /**
     * Prices [dates] for [guests] at [stayId] and stores the quote. Offline, the last quote for the same request is
     * returned if there is one, so a stay already priced keeps its total; otherwise the failure is returned as is
     * (`Validation` for a rule such as the minimum stay, `NotFound` for a stay that is gone).
     */
    suspend fun quote(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote>

    /** A stored quote, or null when it was never issued on this device. */
    fun observeQuote(id: QuoteId): Flow<Quote?>
}

internal class OfflineFirstPricingRepository(
    private val db: SojournDatabase,
    private val api: SojournApi,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : PricingRepository {

    private val queries get() = db.quoteQueries

    override suspend fun quote(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote> =
        when (val result = api.quote(stayId, dates, guests)) {
            is AppResult.Success -> {
                val priced = result.value.copy(pricedAt = clock.now())
                withContext(dispatchers.io) { store(priced) }
                AppResult.Success(priced)
            }
            is AppResult.Failure -> {
                val cached = if (result.error == AppError.Offline) cached(stayId, dates, guests) else null
                cached?.let { AppResult.Success(it) } ?: result
            }
        }

    override fun observeQuote(id: QuoteId): Flow<Quote?> =
        queries.byId(id.value).asFlow().mapToOneOrNull(dispatchers.io).map { row ->
            row?.toModel(queries.linesFor(row.id).executeAsList())
        }

    private fun store(quote: Quote) = db.transaction {
        queries.upsert(quote.toRow())
        queries.clearLines(quote.id.value)
        quote.lineRows().forEach(queries::insertLine)
    }

    private suspend fun cached(stayId: StayId, dates: DateRange, guests: Guests): Quote? = withContext(dispatchers.io) {
        queries.byRequest(
            stay_id = stayId.value,
            start_date = dates.start.toString(),
            end_date = dates.end.toString(),
            adults = guests.adults.toLong(),
            children = guests.children.toLong(),
        ).executeAsOneOrNull()?.let { row -> row.toModel(queries.linesFor(row.id).executeAsList()) }
    }
}
