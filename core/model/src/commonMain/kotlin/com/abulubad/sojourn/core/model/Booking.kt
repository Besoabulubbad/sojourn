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
 * all-in total at the time of booking. [keyCode] opens the door on self check-in; the guest sees it only on arrival
 * day (ScreenBookings card). Null for a booking made before key codes existed.
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
    val keyCode: String? = null,
) {
    init {
        require(reference.isNotBlank()) { "A booking has a reference" }
    }
}
