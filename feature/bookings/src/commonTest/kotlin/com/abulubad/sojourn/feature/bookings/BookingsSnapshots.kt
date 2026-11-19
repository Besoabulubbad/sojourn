package com.abulubad.sojourn.feature.bookings

import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * The Bookings tab and a booking's page on Android and iOS: the card's frame (Upcoming) in the full matrix, Past,
 * empty, the check-in guide before arrival and on arrival day, the booking page, cancelling, and a cancelled booking.
 */
val bookingsSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("bookings_upcoming", fullMatrix = true) { BookingsSamples.Tab(BookingsSamples.upcoming) },
    SnapshotCase("bookings_past") { BookingsSamples.Tab(BookingsSamples.past) },
    SnapshotCase("bookings_empty") { BookingsSamples.Tab(BookingsSamples.empty) },
    SnapshotCase("bookings_guide_before") { BookingsSamples.Tab(BookingsSamples.guideBefore) },
    SnapshotCase("bookings_guide_arrival") { BookingsSamples.Tab(BookingsSamples.guideOnArrival) },
    SnapshotCase("bookings_guide_full") { BookingsSamples.GuideOpen(BookingsSamples.guideOnArrival) },
    SnapshotCase("booking_details", fullMatrix = true) { BookingsSamples.Details(BookingsSamples.details) },
    SnapshotCase("booking_cancel") { BookingsSamples.Details(BookingsSamples.cancelling) },
    SnapshotCase("booking_cancelled") { BookingsSamples.Details(BookingsSamples.cancelledDetails) },
)
