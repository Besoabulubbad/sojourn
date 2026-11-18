package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId

/**
 * The network seam (ADR 0007). Only `core:data` calls it, to refresh the database; screens never wait on it.
 * Every call returns an [AppResult]: no connection is `AppError.Offline`, a server error `AppError.Remote`.
 *
 * Implementations: [com.abulubad.sojourn.core.network.local.LocalSojournApi] (the default: the bundled catalogue
 * behind simulated latency and failures) and [com.abulubad.sojourn.core.network.ktor.KtorSojournApi] (the same
 * catalogue fetched over HTTP from the docs site).
 */
interface SojournApi {

    /** Every city in the catalogue, with its stay count and monthly "from" price. */
    suspend fun cities(): AppResult<List<City>>

    /** Stays, all of them or one city's. */
    suspend fun stays(cityId: CityId? = null): AppResult<List<Stay>>

    suspend fun stay(id: StayId): AppResult<Stay>

    /** Ranges a stay is already booked for; the date picker strikes them through. */
    suspend fun blockedDates(id: StayId): AppResult<List<DateRange>>

    /** A stay's latest reviews, newest first; empty when it has none. */
    suspend fun reviews(id: StayId): AppResult<List<Review>>

    /**
     * Prices [dates] for [guests]. The breakdown's lines sum to the total by construction (P-05), and the quote id
     * travels with the stay so every surface shows the same total (P-02).
     */
    suspend fun quote(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote>

    /** Books a quote this API issued. */
    suspend fun book(quoteId: QuoteId): AppResult<Booking>

    /** Cancels a booking; its nights are free again. */
    suspend fun cancel(id: BookingId): AppResult<Unit>
}
