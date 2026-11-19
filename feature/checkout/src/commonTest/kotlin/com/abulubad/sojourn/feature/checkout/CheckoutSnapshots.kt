package com.abulubad.sojourn.feature.checkout

import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * Every checkout state (I-05, ScreenStates), the card's frame in the full matrix, each way a payment can fail, the
 * in-place editors, and the confirmation. Rendered on Android and iOS.
 */
val checkoutSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("checkout_ready", fullMatrix = true) { CheckoutSamples.Screen(CheckoutSamples.ready) },
    SnapshotCase("checkout_repriced") { CheckoutSamples.Screen(CheckoutSamples.repriced, fraction = 1f) },
    SnapshotCase("checkout_paying") { CheckoutSamples.Screen(CheckoutSamples.paying) },
    SnapshotCase("checkout_wallet") { CheckoutSamples.Screen(CheckoutSamples.wallet) },
    SnapshotCase("checkout_payment_failed") { CheckoutSamples.Screen(CheckoutSamples.paymentFailed) },
    SnapshotCase("checkout_price_changed") { CheckoutSamples.Screen(CheckoutSamples.priceChanged, fraction = 1f) },
    SnapshotCase("checkout_loading") { CheckoutSamples.Screen(CheckoutSamples.loading) },
    SnapshotCase("checkout_gone") { CheckoutSamples.Screen(CheckoutSamples.gone) },
    SnapshotCase("checkout_offline") { CheckoutSamples.Screen(CheckoutSamples.offline) },
    SnapshotCase("checkout_edit_dates") { CheckoutSamples.Screen(CheckoutSamples.editingDates) },
    SnapshotCase("checkout_edit_guests") { CheckoutSamples.Screen(CheckoutSamples.editingGuests) },
    SnapshotCase("booking_confirmed", fullMatrix = true) { CheckoutSamples.Confirmation() },
)
