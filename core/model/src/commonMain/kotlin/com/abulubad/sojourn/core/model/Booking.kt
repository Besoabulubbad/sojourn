package com.abulubad.sojourn.core.model

import kotlin.time.Instant

/** Where a booking is in its life. Localized by id (S-05). */
enum class BookingStatus(val id: String) {
    Upcoming("upcoming"),
    Active("active"),
    Completed("completed"),
    Cancelled("cancelled"),
}

/**
 * A confirmed reservation. [reference] is shown as written and never mirrored (brand book); [total] is the quote's
 * all-in total at the time of booking.
 */
data class Booking(
    val id: BookingId,
    val reference: String,
    val stayId: StayId,
    val quoteId: QuoteId,
    val dates: DateRange,
    val guests: Guests,
    val total: Money,
    val status: BookingStatus,
    val createdAt: Instant,
) {
    init {
        require(reference.isNotBlank()) { "A booking has a reference" }
    }
}
