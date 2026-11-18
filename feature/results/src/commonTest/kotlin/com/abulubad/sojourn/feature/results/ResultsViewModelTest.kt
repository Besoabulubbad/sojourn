package com.abulubad.sojourn.feature.results

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.ObserveFilters
import com.abulubad.sojourn.core.domain.SearchStays
import com.abulubad.sojourn.core.domain.ToggleSaved
import com.abulubad.sojourn.core.domain.UpdateFilters
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.MapKey
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchArgs
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.QuoteFixtures
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeCityRepository
import com.abulubad.sojourn.core.testing.data.FakePreferencesRepository
import com.abulubad.sojourn.core.testing.data.FakePricingRepository
import com.abulubad.sojourn.core.testing.data.FakeSavedRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.dataOrNull
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class ResultsViewModelTest {

    // The fixed clock reads 1 March 2026, 09:00 UTC.
    private val clock = FixedAppClock()
    private val studio = StayFixtures.canalStudio
    private val stays = FakeStayRepository(remote = listOf(studio))
    private val pricing = FakePricingRepository { _, _, _ -> AppResult.Success(QuoteFixtures.canalStudioFortnight) }
    private val saved = FakeSavedRepository()
    private val preferences = FakePreferencesRepository()
    private val london = SearchArgs(cityId = "london")

    private fun viewModel(args: SearchArgs = london) = ResultsViewModel(
        ResultsKey(args),
        SearchStays(stays, pricing, saved, FakeCityRepository(cached = listOf(StayFixtures.london)), clock),
        ObserveFilters(preferences),
        UpdateFilters(preferences),
        ToggleSaved(saved),
        clock,
    )

    private suspend fun TestScope.settled(vm: ResultsViewModel): ResultsUiState {
        advanceUntilIdle()
        return vm.state.value
    }

    @Test
    fun `the search runs offline-first and shows the stays`() = runSojournTest {
        val state = settled(viewModel())
        assertEquals(listOf(studio.id), state.results.dataOrNull?.stays?.map { it.stay.id })
        assertFalse(state.pulling)
    }

    @Test
    fun `quick filters and sort write the persisted filters and rerun the search`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(ResultsIntent.QuickFilterTapped(QuickFilter.Studio))
        vm.onIntent(ResultsIntent.QuickFilterTapped(QuickFilter.Washer))
        val chosen = Filters(bedrooms = BedroomChoice.Studio, amenities = setOf(Amenity.Washer))
        assertEquals(chosen, preferences.filters.value)
        vm.onIntent(ResultsIntent.SortTapped)
        assertTrue(vm.state.value.sortOpen)
        vm.onIntent(ResultsIntent.SortSelected(SortOrder.DistanceToCentre))
        val state = settled(vm)
        assertFalse(state.sortOpen)
        assertEquals(SortOrder.DistanceToCentre, state.filters.sort)
        assertEquals(2, state.filters.activeCount)
        // The studio is a studio with a washer: it still matches.
        assertEquals(1, state.results.dataOrNull?.stays?.size)
        vm.onIntent(ResultsIntent.ClearFiltersTapped)
        assertEquals(Filters(sort = SortOrder.DistanceToCentre), preferences.filters.value)
    }

    @Test
    fun `nothing matching is empty and not an error`() = runSojournTest {
        preferences.setFilters(Filters(amenities = setOf(Amenity.Parking)))
        assertEquals(ScreenState.Empty, settled(viewModel()).results)
    }

    @Test
    fun `offline a dated price older than an hour says its age`() = runSojournTest {
        val dates = QuoteFixtures.canalStudioFortnight.dates
        val args = london.copy(checkIn = dates.start.toString(), checkOut = dates.end.toString(), adults = 2)
        val stale = QuoteFixtures.canalStudioFortnight.copy(pricedAt = clock.now() - 3.hours)
        pricing.stored.value = mapOf(stale.id to stale)
        pricing.offline = true
        stays.cache.value = listOf(studio)
        stays.refreshResult = AppResult.Failure(AppError.Offline)
        val state = settled(viewModel(args))
        assertIs<ScreenState.Offline<*>>(state.results)
        assertEquals(mapOf(studio.id to 3L), state.staleHours)
    }

    @Test
    fun `every tap goes where the card says - a dated card carries its quote to details`() = runSojournTest {
        val dates: DateRange = QuoteFixtures.canalStudioFortnight.dates
        val args = london.copy(checkIn = dates.start.toString(), checkOut = dates.end.toString(), adults = 2)
        val vm = viewModel(args)
        assertIs<ScreenState.Content<*>>(settled(vm).results)
        val result = vm.state.value.results.dataOrNull!!.stays.first()
        vm.effects.test {
            vm.onIntent(ResultsIntent.StayTapped(result))
            val quoteId = QuoteFixtures.canalStudioFortnight.id.value
            assertEquals(ResultsEffect.Navigate(StayDetailsKey(studio.id.value, quoteId, args)), awaitItem())
            vm.onIntent(ResultsIntent.SearchTapped)
            assertEquals(ResultsEffect.Navigate(SearchKey(args = args)), awaitItem())
            vm.onIntent(ResultsIntent.FiltersTapped)
            assertEquals(ResultsEffect.Navigate(FiltersKey(args)), awaitItem())
            vm.onIntent(ResultsIntent.MapTapped)
            assertEquals(ResultsEffect.Navigate(MapKey(args)), awaitItem())
            vm.onIntent(ResultsIntent.BackTapped)
            assertEquals(ResultsEffect.Back, awaitItem())
        }
    }

    @Test
    fun `the heart saves and pull to refresh runs the search again`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(ResultsIntent.SaveTapped(studio.id))
        assertTrue(settled(vm).results.dataOrNull!!.stays.single().saved)
        val before = stays.refreshCalls
        vm.onIntent(ResultsIntent.Pulled)
        assertTrue(vm.state.value.pulling)
        assertFalse(settled(vm).pulling)
        assertTrue(stays.refreshCalls > before)
    }
}
