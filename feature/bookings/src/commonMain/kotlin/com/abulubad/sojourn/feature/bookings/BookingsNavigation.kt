package com.abulubad.sojourn.feature.bookings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.navigation.BookingsKey
import com.abulubad.sojourn.core.navigation.Navigator

/** The Bookings tab's root, and a booking's page on whichever tab opened it. */
fun EntryProviderScope<NavKey>.bookingsEntries(navigator: Navigator) {
    entry<BookingsKey> { BookingsRoute(navigator) }
    entry<BookingDetailsKey> { key -> BookingDetailsRoute(key, navigator) }
}
