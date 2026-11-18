package com.abulubad.sojourn.feature.checkout

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.BookingConfirmedKey
import com.abulubad.sojourn.core.navigation.CheckoutKey
import com.abulubad.sojourn.core.navigation.Navigator

/** Checkout and its confirmation, pushed on whichever tab opened the stay. */
fun EntryProviderScope<NavKey>.checkoutEntries(navigator: Navigator) {
    entry<CheckoutKey> { key -> CheckoutRoute(key, navigator) }
    entry<BookingConfirmedKey> { key -> ConfirmationRoute(key, navigator) }
}
