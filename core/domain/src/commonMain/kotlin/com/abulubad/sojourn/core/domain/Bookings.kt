package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.data.BookingRepository
import com.abulubad.sojourn.core.data.PricingRepository
import com.abulubad.sojourn.core.data.StayRepository
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Stay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Books the quote the guest saw. The booking's total is that quote's (P-02): if the API has forgotten the quote
 * (it expired), the same request is priced again and booked only if the total is unchanged; a changed total comes
 * back as `Validation("price_changed")`, so checkout shows the new price and asks again rather than charging it.
 */
class CreateBooking(private val pricing: PricingRepository, private val bookings: BookingRepository) {
    suspend operator fun invoke(quoteId: QuoteId): AppResult<Booking> {
        val shown = pricing.observeQuote(quoteId).first() ?: return AppResult.Failure(AppError.NotFound(quoteId.value))
        val booked = bookings.book(quoteId)
        if ((booked as? AppResult.Failure)?.error !is AppError.NotFound) return booked
        return when (val fresh = pricing.quote(shown.stayId, shown.dates, shown.guests)) {
            is AppResult.Failure -> fresh
            is AppResult.Success -> if (fresh.value.total == shown.total) {
                bookings.book(fresh.value.id)
            } else {
                AppResult.Failure(AppError.Validation(StayRules.Problem.PRICE_CHANGED))
            }
        }
    }
}

/** A booking on the Bookings tab, with its stay when it is still in the catalogue. */
data class BookingItem(val booking: Booking, val stay: Stay?)

/**
 * The guest's bookings, local and so available offline. Status follows the calendar: upcoming before check-in,
 * active until check-out, completed after; a cancellation stays cancelled. Active first, then upcoming (soonest
 * first), then past ones (latest first).
 */
class ListBookings(
    private val bookings: BookingRepository,
    private val stays: StayRepository,
    private val clock: AppClock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<BookingItem>> = bookings.observeBookings().flatMapLatest { list ->
        stays.observeStays(list.mapTo(HashSet()) { it.stayId }).map { cached ->
            val today = clock.today()
            val byId = cached.associateBy { it.id }
            list.map { it.copy(status = statusOn(it, today)) }
                .sortedWith(order)
                .map { BookingItem(it, byId[it.stayId]) }
        }
    }

    private companion object {
        fun statusOn(booking: Booking, today: LocalDate): BookingStatus = when {
            booking.status == BookingStatus.Cancelled -> BookingStatus.Cancelled
            today < booking.dates.start -> BookingStatus.Upcoming
            today < booking.dates.end -> BookingStatus.Active
            else -> BookingStatus.Completed
        }

        val order: Comparator<Booking> = compareBy<Booking> {
            when (it.status) {
                BookingStatus.Active -> 0
                BookingStatus.Upcoming -> 1
                BookingStatus.Completed, BookingStatus.Cancelled -> 2
            }
        }.thenBy { booking ->
            val day = booking.dates.start.toEpochDays()
            if (booking.status == BookingStatus.Upcoming) day else -day
        }
    }
}
