package com.abulubad.sojourn.core.designsystem.components.booking

import com.abulubad.sojourn.core.designsystem.components.bookingbar.SojournBookingBarSamples
import com.abulubad.sojourn.core.designsystem.components.breakdown.SojournPriceBreakdownSamples
import com.abulubad.sojourn.core.designsystem.components.reminder.SojournReminderCardSamples
import com.abulubad.sojourn.core.designsystem.components.wallet.SojournWalletSheetSamples
import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * The Booking group: BookingBar, PriceBreakdown, the demo WalletSheet and the ReminderCard, each in the full matrix.
 */
val bookingSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("booking_bar_states", fullMatrix = true) { SojournBookingBarSamples.States() },
    SnapshotCase("price_breakdown_quote", fullMatrix = true) { SojournPriceBreakdownSamples.Quote() },
    SnapshotCase("wallet_sheet", fullMatrix = true) { SojournWalletSheetSamples.Pay() },
    SnapshotCase("reminder_card", fullMatrix = true) { SojournReminderCardSamples.States() },
)
