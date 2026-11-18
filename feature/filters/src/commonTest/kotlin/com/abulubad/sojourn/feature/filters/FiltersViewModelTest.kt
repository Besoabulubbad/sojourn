package com.abulubad.sojourn.feature.filters

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.ObserveFilters
import com.abulubad.sojourn.core.domain.PreviewFilters
import com.abulubad.sojourn.core.domain.SearchStays
import com.abulubad.sojourn.core.domain.UpdateFilters
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.navigation.FiltersKey
import com.abulubad.sojourn.core.navigation.SearchArgs
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FiltersViewModelTest {

    // A studio (£2,484 a month, with a workspace and a washer) and a two-bedroom flat with parking (£4,500), in London.
    private val studio = StayFixtures.canalStudio
    private val flat = studio.copy(
        id = StayId("two-bed"),
        facts = StayFacts(bedrooms = 2, beds = 2, bathrooms = 2, sleeps = 4),
        amenities = setOf(Amenity.Parking),
        fromPrice = studio.fromPrice.copy(amount = Money.of(150, Currency.GBP)),
        longStayDiscountPercent = null,
    )
    private val preferences = FakePreferencesRepository()
    private val search = SearchStays(
        FakeStayRepository(remote = listOf(studio, flat)),
        FakePricingRepository { _, _, _ -> AppResult.Success(QuoteFixtures.canalStudioFortnight) },
        FakeSavedRepository(),
        FakeCityRepository(cached = listOf(StayFixtures.london)),
        FixedAppClock(),
    )

    private fun viewModel() = FiltersViewModel(
        FiltersKey(SearchArgs(cityId = "london")),
        ObserveFilters(preferences),
        UpdateFilters(preferences),
        PreviewFilters(search),
    )

    private suspend fun TestScope.settled(vm: FiltersViewModel): FiltersUiState {
        advanceUntilIdle()
        return vm.state.value
    }

    @Test
    fun `the sheet opens on the applied filters and counts them at once`() = runSojournTest {
        preferences.setFilters(Filters(amenities = setOf(Amenity.Workspace), sort = SortOrder.Rating))
        val state = settled(viewModel())
        assertEquals(setOf(Amenity.Workspace), state.draft.amenities)
        assertEquals(ApplyButton.Count(1), state.apply)
        assertEquals(PriceBasis.Month, state.preview?.basis)
        assertNotNull(state.prices)
    }

    @Test
    fun `a change is counted 300 ms after the last one and not applied until the button`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(FiltersIntent.BedroomsSelected(BedroomChoice.Studio))
        runCurrent()
        advanceTimeBy(200)
        vm.onIntent(FiltersIntent.AmenityTapped(Amenity.Workspace))
        advanceTimeBy(250)
        runCurrent()
        // Still the opening count: the second change restarted the wait.
        assertEquals(ApplyButton.Count(2), vm.state.value.apply)
        assertEquals(ApplyButton.Count(1), settled(vm).apply)
        assertEquals(Filters.None, preferences.filters.value)
        vm.effects.test {
            vm.onIntent(FiltersIntent.ApplyTapped)
            assertEquals(FiltersEffect.Close, awaitItem())
        }
        val applied = Filters(bedrooms = BedroomChoice.Studio, amenities = setOf(Amenity.Workspace))
        assertEquals(applied, preferences.filters.value)
    }

    @Test
    fun `nothing kept disables apply but keeps the slider`() = runSojournTest {
        val vm = viewModel()
        val prices = settled(vm).prices
        vm.onIntent(FiltersIntent.AmenityTapped(Amenity.Dishwasher))
        val state = settled(vm)
        assertEquals(ApplyButton.NoMatch, state.apply)
        assertEquals(prices, state.prices)
        vm.onIntent(FiltersIntent.ApplyTapped)
        assertEquals(Filters.None, preferences.filters.value)
    }

    @Test
    fun `the price range stores the cards' basis and the full bounds store none`() = runSojournTest {
        val vm = viewModel()
        val bounds = assertNotNull(settled(vm).prices).bounds
        val narrower = bounds.copy(max = bounds.min.copy(minor = bounds.min.minor + 10_000))
        vm.onIntent(FiltersIntent.PriceChanged(narrower))
        assertEquals(narrower, vm.state.value.draft.priceRange)
        assertEquals(PriceBasis.Month, vm.state.value.draft.basis)
        assertEquals(narrower, vm.state.value.shownRange)
        vm.onIntent(FiltersIntent.PriceChanged(bounds))
        assertNull(vm.state.value.draft.priceRange)
    }

    @Test
    fun `steppers at one and clear all mean any - the sort survives`() = runSojournTest {
        preferences.setFilters(Filters(sort = SortOrder.DistanceToCentre, instantBook = true))
        val vm = viewModel()
        settled(vm)
        vm.onIntent(FiltersIntent.BedsChanged(2))
        vm.onIntent(FiltersIntent.BathroomsChanged(2))
        vm.onIntent(FiltersIntent.FreeCancellationTapped)
        assertEquals(2, vm.state.value.draft.minBeds)
        vm.onIntent(FiltersIntent.BedsChanged(1))
        assertNull(vm.state.value.draft.minBeds)
        vm.onIntent(FiltersIntent.ClearAllTapped)
        assertEquals(Filters(sort = SortOrder.DistanceToCentre), vm.state.value.draft)
        assertTrue(preferences.filters.value.instantBook)
    }

    @Test
    fun `closing the sheet leaves without applying`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(FiltersIntent.AmenityTapped(Amenity.Washer))
        vm.effects.test {
            vm.onIntent(FiltersIntent.Dismissed)
            assertEquals(FiltersEffect.Back, awaitItem())
        }
        assertEquals(Filters.None, preferences.filters.value)
    }
}
