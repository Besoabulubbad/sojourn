package com.abulubad.sojourn.feature.bookings

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin module: [BookingsViewModel], and [BookingDetailsViewModel] created with its key as a parameter. */
val bookingsModule: Module = module {
    viewModelOf(::BookingsViewModel)
    viewModelOf(::BookingDetailsViewModel)
}
