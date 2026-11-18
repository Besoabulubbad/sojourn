package com.abulubad.sojourn.feature.map

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.GetCities
import com.abulubad.sojourn.core.domain.ObserveFilters
import com.abulubad.sojourn.core.domain.SearchStays
import com.abulubad.sojourn.core.domain.ToggleSaved
import com.abulubad.sojourn.core.domain.UpdateFilters
import com.abulubad.sojourn.core.model.GeoArea
import com.abulubad.sojourn.core.model.GeoBounds
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.MapKey
import com.abulubad.sojourn.core.navigation.ResultsKey
import com.abulubad.sojourn.core.navigation.SearchArgs
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapViewModelTest {

    private val clock = FixedAppClock()
    private val studio = StayFixtures.canalStudio

    /** A second London stay, east of the studio. */
    private val east = studio.copy(
        id = StayId("east-loft"),
        location = GeoArea(GeoPoint(51.5262, -0.0790), radiusMetres = 350),
    )
    private val stays = FakeStayRepository(remote = listOf(studio, east))
    private val pricing = FakePricingRepository { _, _, _ -> AppResult.Success(QuoteFixtures.canalStudioFortnight) }
    private val saved = FakeSavedRepository()
    private val preferences = FakePreferencesRepository()
    private val cities = FakeCityRepository(remote = listOf(StayFixtures.london), cached = listOf(StayFixtures.london))
    private val london = SearchArgs(cityId = "london")

    private fun viewModel(stayId: String? = null) = MapViewModel(
        MapKey(london, stayId),
        SearchStays(stays, pricing, saved, cities, clock),
        GetCities(cities),
        ObserveFilters(preferences),
        UpdateFilters(preferences),
        ToggleSaved(saved),
    )

    private suspend fun TestScope.settled(vm: MapViewModel): MapUiState {
        advanceUntilIdle()
        return vm.state.value
    }

    @Test
    fun `the map shows the same stays as the list and opens on the city`() = runSojournTest {
        val state = settled(viewModel())
        assertEquals(setOf(studio.id, east.id), state.stays.map { it.stay.id }.toSet())
        assertEquals(StayFixtures.london.location, state.cityCenter)
        assertNull(state.selectedStay)
        assertFalse(state.showSearchArea)
    }

    @Test
    fun `opened from Details the stay is selected with its card raised`() = runSojournTest {
        val state = settled(viewModel(stayId = studio.id.value))
        assertEquals(studio.id, state.selectedStay?.stay?.id)
    }

    @Test
    fun `a stay selected before another counts as seen`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(MapIntent.PinTapped(studio.id))
        vm.onIntent(MapIntent.CardShown(east.id))
        val state = vm.state.value
        assertEquals(east.id, state.selected)
        assertEquals(setOf(studio.id), state.seen)
        vm.onIntent(MapIntent.MapTapped)
        assertNull(vm.state.value.selected)
        assertEquals(setOf(studio.id, east.id), vm.state.value.seen)
    }

    @Test
    fun `search this area shows only after the guest moves and keeps the stays in view`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(MapIntent.PinTapped(studio.id))
        val eastOnly = GeoBounds(south = 51.52, west = -0.09, north = 51.53, east = -0.07)
        vm.onIntent(MapIntent.GuestMoved(eastOnly))
        assertTrue(vm.state.value.showSearchArea)
        vm.onIntent(MapIntent.SearchAreaTapped)
        val state = vm.state.value
        assertEquals(listOf(east.id), state.stays.map { it.stay.id })
        assertFalse(state.showSearchArea)
        // The studio is out of the area, so its card goes.
        assertNull(state.selected)
        vm.onIntent(MapIntent.ShowAllTapped)
        assertEquals(2, vm.state.value.stays.size)
    }

    @Test
    fun `a card opens Details with its quote and List shows the same search`() = runSojournTest {
        val vm = viewModel()
        val result = settled(vm).stays.first { it.stay.id == studio.id }
        vm.effects.test {
            vm.onIntent(MapIntent.StayTapped(result))
            val quoteId = (result.price as? PriceDisplay.Total)?.quoteId?.value
            assertEquals(MapEffect.Navigate(StayDetailsKey(studio.id.value, quoteId, london)), awaitItem())
            vm.onIntent(MapIntent.ListTapped)
            assertEquals(MapEffect.ShowList(ResultsKey(london)), awaitItem())
            vm.onIntent(MapIntent.FiltersTapped)
            assertEquals(MapEffect.Navigate(FiltersKey(london)), awaitItem())
            vm.onIntent(MapIntent.BackTapped)
            assertEquals(MapEffect.Back, awaitItem())
        }
    }
}
