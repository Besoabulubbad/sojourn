package com.abulubad.sojourn.core.domain

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module: every use case, created per use (they hold no state). Needs `dataModule` and an `AppClock`. */
val domainModule: Module = module {
    factoryOf(::GetCities)
    factoryOf(::SearchStays)
    factoryOf(::PreviewFilters)
    factoryOf(::GetStayDetails)
    factoryOf(::GetQuote)
    factoryOf(::ObserveQuote)
    factoryOf(::CreateBooking)
    factoryOf(::ListBookings)
    factoryOf(::GetBooking)
    factoryOf(::CancelBooking)
    factoryOf(::ObserveArrivingStay)
    factoryOf(::ToggleSaved)
    factoryOf(::ObserveSaved)
    factoryOf(::RemoveSaved)
    factoryOf(::RestoreSaved)
    factoryOf(::ObserveFilters)
    factoryOf(::UpdateFilters)
    factoryOf(::ObserveRecentSearches)
    factoryOf(::RecordSearch)
}
