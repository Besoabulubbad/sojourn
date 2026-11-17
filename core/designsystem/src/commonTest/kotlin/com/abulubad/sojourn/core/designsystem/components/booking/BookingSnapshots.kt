package com.abulubad.sojourn.core.designsystem.components.booking

import com.abulubad.sojourn.core.designsystem.components.bookingbar.SojournBookingBarSamples
import com.abulubad.sojourn.core.designsystem.components.breakdown.SojournPriceBreakdownSamples
import com.abulubad.sojourn.core.testing.SnapshotCase

/** The Booking group: BookingBar and PriceBreakdown, each in the full matrix. */
val bookingSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("booking_bar_states", fullMatrix = true) { SojournBookingBarSamples.States() },
    SnapshotCase("price_breakdown_quote", fullMatrix = true) { SojournPriceBreakdownSamples.Quote() },
)
