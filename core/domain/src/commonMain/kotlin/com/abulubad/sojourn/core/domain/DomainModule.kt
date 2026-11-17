package com.abulubad.sojourn.core.domain

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module: every use case, created per use (they hold no state). Needs `dataModule` and an `AppClock`. */
val domainModule: Module = module {
    factoryOf(::GetCities)
    factoryOf(::SearchStays)
    factoryOf(::GetStayDetails)
    factoryOf(::GetQuote)
    factoryOf(::ObserveQuote)
    factoryOf(::CreateBooking)
    factoryOf(::ListBookings)
    factoryOf(::ToggleSaved)
    factoryOf(::ObserveSaved)
    factoryOf(::ObserveFilters)
    factoryOf(::UpdateFilters)
    factoryOf(::ObserveRecentSearches)
    factoryOf(::RecordSearch)
}
