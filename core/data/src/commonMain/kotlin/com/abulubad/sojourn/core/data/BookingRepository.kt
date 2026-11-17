package com.abulubad.sojourn.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.DispatcherProvider
import com.abulubad.sojourn.core.data.local.toModel
import com.abulubad.sojourn.core.data.local.toRow
import com.abulubad.sojourn.core.database.SojournDatabase
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.network.SojournApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The guest's bookings. Booking needs the network; the bookings themselves live on the device, so the Bookings tab
 * works offline.
 */
interface BookingRepository {

    /** Bookings, latest check-in first. */
    fun observeBookings(): Flow<List<Booking>>

    fun observeBooking(id: BookingId): Flow<Booking?>

    /**
     * Books a quote the API issued and stores the booking. Its total is the quote's (P-02). `NotFound` means the API
     * no longer knows the quote (it expired); the caller re-quotes and asks the guest to confirm a changed total.
     */
    suspend fun book(quoteId: QuoteId): AppResult<Booking>
}

internal class OfflineFirstBookingRepository(
    private val db: SojournDatabase,
    private val api: SojournApi,
    private val dispatchers: DispatcherProvider,
) : BookingRepository {

    private val queries get() = db.bookingQueries

    override fun observeBookings(): Flow<List<Booking>> =
        queries.all().asFlow().mapToList(dispatchers.io).map { rows -> rows.mapNotNull { it.toModel() } }

    override fun observeBooking(id: BookingId): Flow<Booking?> =
        queries.byId(id.value).asFlow().mapToOneOrNull(dispatchers.io).map { it?.toModel() }

    override suspend fun book(quoteId: QuoteId): AppResult<Booking> {
        val result = api.book(quoteId)
        if (result is AppResult.Success) withContext(dispatchers.io) { queries.upsert(result.value.toRow()) }
        return result
    }
}
