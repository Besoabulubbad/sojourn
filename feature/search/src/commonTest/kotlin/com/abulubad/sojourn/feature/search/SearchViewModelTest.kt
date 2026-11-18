package com.abulubad.sojourn.feature.search

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.RecordSearch
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchArgs
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.SearchStep
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeCityRepository
import com.abulubad.sojourn.core.testing.data.FakeSearchRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchViewModelTest {

    // The fixed clock reads 1 March 2026.
    private val cities = FakeCityRepository(remote = listOf(StayFixtures.london))
    private val searches = FakeSearchRepository()
    private val march = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))

    private fun viewModel(key: SearchKey = SearchKey()) =
        SearchViewModel(key, GetCities(cities), RecordSearch(searches), FixedAppClock())

    private suspend fun TestScope.settled(vm: SearchViewModel): SearchUiState {
        advanceUntilIdle()
        return vm.state.value
    }

    @Test
    fun `where lists the cities offline-first and retry refreshes`() = runSojournTest {
        cities.cache.value = listOf(StayFixtures.london)
        cities.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel()
        assertEquals(ScreenState.Offline(listOf(StayFixtures.london)), settled(vm).cities)
        cities.refreshResult = AppResult.Success(Unit)
        vm.onIntent(SearchIntent.RetryTapped)
        assertEquals(ScreenState.Content(listOf(StayFixtures.london)), settled(vm).cities)
    }

    @Test
    fun `a city advances to when - dates to who - and search records the search and shows results`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(SearchIntent.CityTapped(CityId("london")))
        assertEquals(SearchStep.When, vm.state.value.step)
        vm.onIntent(SearchIntent.NextTapped)
        assertEquals(SearchStep.When, vm.state.value.step, "Next waits for a range on the Dates tab")
        vm.onIntent(SearchIntent.DatesSelected(march))
        vm.onIntent(SearchIntent.NextTapped)
        assertEquals(SearchStep.Who, vm.state.value.step)
        vm.onIntent(SearchIntent.AdultsChanged(2))
        vm.onIntent(SearchIntent.ChildrenChanged(1))
        vm.effects.test {
            vm.onIntent(SearchIntent.SearchTapped)
            val args = SearchArgs(
                cityId = "london",
                checkIn = "2026-03-12",
                checkOut = "2026-03-27",
                adults = 2,
                children = 1,
            )
            assertEquals(SearchEffect.ShowResults(ResultsKey(args)), awaitItem())
        }
        val expected = SearchQuery(cityId = CityId("london"), dates = march, guests = Guests(adults = 2, children = 1))
        assertEquals(listOf(expected), searches.recent.value)
    }

    @Test
    fun `explore's length shortcut opens flexible - and flexible searches by length and month`() = runSojournTest {
        val vm = viewModel(SearchKey(args = SearchArgs(length = StayLength.Week.name)))
        val opened = settled(vm)
        assertEquals(WhenMode.Flexible, opened.mode)
        assertEquals(StayLength.Week, opened.length)
        assertEquals((3..6).map { YearMonth(2026, it) }, opened.startMonths)
        vm.onIntent(SearchIntent.CityTapped(CityId("london")))
        assertTrue(vm.state.value.canContinueFromWhen)
        vm.onIntent(SearchIntent.StartMonthSelected(YearMonth(2026, 4)))
        vm.onIntent(SearchIntent.NextTapped)
        val query = vm.state.value.query
        val expected = SearchQuery(cityId = CityId("london"), length = StayLength.Week, startMonth = YearMonth(2026, 4))
        assertEquals(expected, query)
    }

    @Test
    fun `any time searches without dates or length - even with a range picked`() = runSojournTest {
        val vm = viewModel(SearchKey(step = SearchStep.When))
        vm.onIntent(SearchIntent.DatesSelected(march))
        vm.onIntent(SearchIntent.AnyTimeTapped)
        val state = settled(vm)
        assertEquals(SearchStep.Who, state.step)
        assertTrue(state.anyTime)
        assertEquals(SearchQuery(), state.query)
        // Back to When and on with Next: the range applies again.
        vm.onIntent(SearchIntent.BackTapped)
        vm.onIntent(SearchIntent.NextTapped)
        assertFalse(vm.state.value.anyTime)
        assertEquals(march, vm.state.value.query.dates)
    }

    @Test
    fun `back goes one step back and closes the flow from where`() = runSojournTest {
        val vm = viewModel(SearchKey(step = SearchStep.Who))
        settled(vm)
        vm.effects.test {
            vm.onIntent(SearchIntent.BackTapped)
            assertEquals(SearchStep.When, vm.state.value.step)
            vm.onIntent(SearchIntent.BackTapped)
            assertEquals(SearchStep.Where, vm.state.value.step)
            vm.onIntent(SearchIntent.BackTapped)
            assertEquals(SearchEffect.Close, awaitItem())
        }
    }

    @Test
    fun `guests stay within the stepper bounds and the key's choices are restored`() = runSojournTest {
        val args = SearchArgs(
            cityId = "paris",
            checkIn = "2026-04-02",
            checkOut = "2026-04-09",
            adults = 40,
            children = 3,
        )
        val vm = viewModel(SearchKey(step = SearchStep.When, args = args))
        val state = settled(vm)
        assertEquals(WhenMode.Dates, state.mode)
        assertEquals(YearMonth(2026, 4), state.month, "the calendar opens on the chosen month")
        assertEquals(Guests(adults = SearchUiState.Adults.last, children = 3), state.guests)
        vm.onIntent(SearchIntent.ChildrenChanged(-1))
        assertEquals(0, vm.state.value.guests.children)
    }
}
