package com.abulubad.sojourn

import com.abulubad.sojourn.core.domain.CreateBooking
import com.abulubad.sojourn.core.domain.GetBooking
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.GetQuote
import com.abulubad.sojourn.core.domain.GetStayDetails
import com.abulubad.sojourn.core.domain.ListBookings
import com.abulubad.sojourn.core.domain.ObserveFilters
import com.abulubad.sojourn.core.domain.ObserveQuote
import com.abulubad.sojourn.core.domain.ObserveRecentSearches
import com.abulubad.sojourn.core.domain.ObserveSaved
import com.abulubad.sojourn.core.domain.RecordSearch
import com.abulubad.sojourn.core.domain.SearchStays
import com.abulubad.sojourn.core.domain.ToggleSaved
import com.abulubad.sojourn.core.domain.UpdateFilters
import com.abulubad.sojourn.core.navigation.BookingConfirmedKey
import com.abulubad.sojourn.core.navigation.CheckoutKey
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.GalleryKey
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchArgs
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.feature.checkout.CheckoutViewModel
import com.abulubad.sojourn.feature.checkout.ConfirmationViewModel
import com.abulubad.sojourn.feature.details.DetailsViewModel
import com.abulubad.sojourn.feature.details.GalleryViewModel
import com.abulubad.sojourn.feature.explore.ExploreViewModel
import com.abulubad.sojourn.feature.filters.FiltersViewModel
import com.abulubad.sojourn.feature.results.ResultsViewModel
import com.abulubad.sojourn.feature.search.SearchViewModel
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Builds every use case and ViewModel from the real graph (real database driver, settings and API) on Robolectric.
 * Koin's `verify()` checks declarations; this catches what only creating an instance shows, such as a constructor
 * DSL binding that tries to resolve a parameter meant to keep its default (the first Explore run crashed on one).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournGraphResolvesTest {

    @After
    fun stop() = stopKoin()

    @Test
    fun `every use case and view model can be created`() {
        val koin = initKoin { androidContext(RuntimeEnvironment.getApplication()) }.koin
        koin.get<GetCities>()
        koin.get<SearchStays>()
        koin.get<GetStayDetails>()
        koin.get<GetQuote>()
        koin.get<ObserveQuote>()
        koin.get<CreateBooking>()
        koin.get<ListBookings>()
        koin.get<GetBooking>()
        koin.get<ToggleSaved>()
        koin.get<ObserveSaved>()
        koin.get<ObserveFilters>()
        koin.get<UpdateFilters>()
        koin.get<ObserveRecentSearches>()
        koin.get<RecordSearch>()
        koin.get<ExploreViewModel>()
        koin.get<SearchViewModel> { parametersOf(SearchKey()) }
        koin.get<ResultsViewModel> { parametersOf(ResultsKey(SearchArgs())) }
        koin.get<FiltersViewModel> { parametersOf(FiltersKey(SearchArgs())) }
        koin.get<DetailsViewModel> { parametersOf(StayDetailsKey("london-01")) }
        koin.get<GalleryViewModel> { parametersOf(GalleryKey("london-01")) }
        koin.get<CheckoutViewModel> { parametersOf(CheckoutKey("q-london-01")) }
        koin.get<ConfirmationViewModel> { parametersOf(BookingConfirmedKey("b-1-london-01")) }
    }
}
