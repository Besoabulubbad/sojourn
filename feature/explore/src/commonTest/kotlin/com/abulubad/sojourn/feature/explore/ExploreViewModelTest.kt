package com.abulubad.sojourn.feature.explore

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.ListBookings
import com.abulubad.sojourn.core.domain.ObserveArrivingStay
import com.abulubad.sojourn.core.domain.ObserveRecentSearches
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchArgs
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.SearchStep
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.QuoteFixtures
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeBookingRepository
import com.abulubad.sojourn.core.testing.data.FakeCityRepository
import com.abulubad.sojourn.core.testing.data.FakeSearchRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class ExploreViewModelTest {

    private val cities = FakeCityRepository(remote = listOf(StayFixtures.london))
    private val searches = FakeSearchRepository()
    private val bookings = FakeBookingRepository()
    private val stays = FakeStayRepository(cached = listOf(StayFixtures.canalStudio))

    /** Two days before the March fortnight (12 March): the stay leads Explore from now. */
    private val arriving = FixedAppClock(Instant.parse("2026-03-10T09:00:00Z"))

    private fun viewModel(clock: FixedAppClock = arriving) = ExploreViewModel(
        GetCities(cities),
        ObserveRecentSearches(searches),
        ObserveArrivingStay(ListBookings(bookings, stays, clock), clock),
    )

    private suspend fun TestScope.settled(vm: ExploreViewModel): ExploreUiState {
        advanceUntilIdle()
        return vm.state.value
    }

    @Test
    fun `first run shows the cities and no continue row`() = runSojournTest {
        val state = settled(viewModel())
        assertEquals(ScreenState.Content(listOf(StayFixtures.london)), state.cities)
        assertNull(state.resume)
    }

    @Test
    fun `offline with cached cities keeps them under the banner and retry refreshes`() = runSojournTest {
        cities.cache.value = listOf(StayFixtures.london)
        cities.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel()
        assertEquals(ScreenState.Offline(listOf(StayFixtures.london)), settled(vm).cities)
        cities.refreshResult = AppResult.Success(Unit)
        vm.onIntent(ExploreIntent.RetryTapped)
        assertEquals(ScreenState.Content(listOf(StayFixtures.london)), settled(vm).cities)
        assertEquals(2, cities.refreshCalls)
    }

    @Test
    fun `the last search is the continue row with its city photo`() = runSojournTest {
        val query = SearchQuery(cityId = CityId("london"), dates = QuoteFixtures.marchFortnight, guests = Guests(2))
        searches.record(query)
        val resume = assertIs<ExploreResume.LastSearch>(settled(viewModel()).resume)
        assertEquals(query, resume.query)
        assertEquals(StayFixtures.london.photo, resume.photo)
    }

    @Test
    fun `a stay more than two days away leaves the last search in place`() = runSojournTest {
        searches.record(SearchQuery(cityId = CityId("london")))
        bookings.issued[QuoteFixtures.canalStudioFortnight.id] = QuoteFixtures.canalStudioFortnight
        bookings.book(QuoteFixtures.canalStudioFortnight.id)
        assertIs<ExploreResume.LastSearch>(settled(viewModel(clock = FixedAppClock())).resume)
    }

    @Test
    fun `a stay two days away replaces the last search`() = runSojournTest {
        searches.record(SearchQuery(cityId = CityId("london")))
        bookings.issued[QuoteFixtures.canalStudioFortnight.id] = QuoteFixtures.canalStudioFortnight
        bookings.book(QuoteFixtures.canalStudioFortnight.id)
        val resume = assertIs<ExploreResume.NextStay>(settled(viewModel()).resume)
        assertEquals(StayFixtures.canalStudio.cityId, resume.cityId)
        assertEquals(StayFixtures.canalStudio.photos.first(), resume.photo)
    }

    @Test
    fun `every tap navigates where the card says`() = runSojournTest {
        val query = SearchQuery(cityId = CityId("london"), dates = QuoteFixtures.marchFortnight, guests = Guests(2))
        searches.record(query)
        val vm = viewModel()
        settled(vm)
        vm.effects.test {
            vm.onIntent(ExploreIntent.SearchTapped)
            assertEquals(ExploreEffect.Navigate(SearchKey()), awaitItem())
            vm.onIntent(ExploreIntent.LengthTapped(StayLength.Month))
            assertEquals(ExploreEffect.Navigate(SearchKey(args = SearchArgs(length = "Month"))), awaitItem())
            vm.onIntent(ExploreIntent.CityTapped(CityId("paris")))
            assertEquals(ExploreEffect.Navigate(ResultsKey(SearchArgs(cityId = "paris"))), awaitItem())
            vm.onIntent(ExploreIntent.SeeAllCitiesTapped)
            assertEquals(ExploreEffect.Navigate(SearchKey(step = SearchStep.Where)), awaitItem())
            vm.onIntent(ExploreIntent.ResumeTapped)
            val args = SearchArgs(cityId = "london", checkIn = "2026-03-12", checkOut = "2026-03-27", adults = 2)
            assertEquals(ExploreEffect.Navigate(ResultsKey(args)), awaitItem())
        }
    }

    @Test
    fun `the next stay opens its booking`() = runSojournTest {
        bookings.issued[QuoteFixtures.canalStudioFortnight.id] = QuoteFixtures.canalStudioFortnight
        val booking = (bookings.book(QuoteFixtures.canalStudioFortnight.id) as AppResult.Success).value
        val vm = viewModel()
        settled(vm)
        vm.effects.test {
            vm.onIntent(ExploreIntent.ResumeTapped)
            assertEquals(ExploreEffect.Navigate(BookingDetailsKey(booking.id.value)), awaitItem())
        }
    }
}
