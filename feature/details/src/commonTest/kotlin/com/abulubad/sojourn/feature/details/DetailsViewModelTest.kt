package com.abulubad.sojourn.feature.details

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.GetQuote
import com.abulubad.sojourn.core.domain.GetStayDetails
import com.abulubad.sojourn.core.domain.ObserveQuote
import com.abulubad.sojourn.core.domain.StayDetails
import com.abulubad.sojourn.core.domain.ToggleSaved
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.HostText
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.QuoteProblem
import com.abulubad.sojourn.core.navigation.CheckoutKey
import com.abulubad.sojourn.core.navigation.GalleryKey
import com.abulubad.sojourn.core.navigation.MapKey
import com.abulubad.sojourn.core.navigation.SearchArgs
import com.abulubad.sojourn.core.navigation.StayDetailsKey
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.QuoteFixtures
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakePricingRepository
import com.abulubad.sojourn.core.testing.data.FakeSavedRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DetailsViewModelTest {

    // The fixed clock reads 1 March 2026; the studio needs at least five nights here.
    private val clock = FixedAppClock()
    private val quote = QuoteFixtures.canalStudioFortnight
    private val studio = StayFixtures.canalStudio.copy(
        policies = StayFixtures.canalStudio.policies + Policy.MinimumStay(5),
        descriptionTranslations = listOf(HostText("استوديو هادئ ومشرق.", "ar")),
    )
    private val stays = FakeStayRepository(remote = listOf(studio))
    private val pricing = FakePricingRepository { _, _, _ -> AppResult.Success(quote) }
    private val saved = FakeSavedRepository()
    private val london = SearchArgs(cityId = "london", adults = 2)

    private fun viewModel(quoteId: String? = null, args: SearchArgs = london) = DetailsViewModel(
        StayDetailsKey(studio.id.value, quoteId, args),
        GetStayDetails(stays, saved),
        ObserveQuote(pricing),
        GetQuote(stays, pricing, clock),
        ToggleSaved(saved),
        clock,
    )

    private suspend fun TestScope.settled(vm: DetailsViewModel): DetailsUiState {
        advanceUntilIdle()
        return vm.state.value
    }

    @Test
    fun `with the card's quote the bar shows its total and Reserve goes to checkout`() = runSojournTest {
        pricing.stored.value = mapOf(quote.id to quote)
        val vm = viewModel(quoteId = quote.id.value)
        val state = settled(vm)
        assertEquals(studio, state.stay)
        assertEquals(quote.display, state.price)
        vm.effects.test {
            vm.onIntent(DetailsIntent.PrimaryTapped)
            assertEquals(DetailsEffect.Navigate(CheckoutKey(quote.id.value)), awaitItem())
        }
    }

    @Test
    fun `without dates the bar shows a monthly from price and Choose dates opens the picker`() = runSojournTest {
        val vm = viewModel()
        val price = assertIs<PriceDisplay.From>(settled(vm).price)
        assertEquals(PriceBasis.Month, price.per)
        vm.onIntent(DetailsIntent.PrimaryTapped)
        assertEquals(DetailsSheet.Dates(month = YearMonth(2026, 3)), vm.state.value.sheet)
    }

    @Test
    fun `dates the stay cannot take are refused in its own numbers before the network`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(DetailsIntent.PrimaryTapped)
        vm.onIntent(DetailsIntent.DatesSelected(DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 14))))
        vm.onIntent(DetailsIntent.DatesConfirmed)
        val sheet = assertIs<DetailsSheet.Dates>(settled(vm).sheet)
        assertEquals(QuoteProblem.MinimumNights(5), sheet.problem)
        assertEquals(0, pricing.quoteCalls)
        assertNull(vm.state.value.quote)
    }

    @Test
    fun `priced dates fill the bar and close the sheet`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(DetailsIntent.PrimaryTapped)
        vm.onIntent(DetailsIntent.DatesSelected(quote.dates))
        vm.effects.test {
            vm.onIntent(DetailsIntent.DatesConfirmed)
            assertEquals(DetailsEffect.CloseSheet, awaitItem())
        }
        assertEquals(quote.display, vm.state.value.price)
        vm.onIntent(DetailsIntent.SheetDismissed)
        assertNull(vm.state.value.sheet)
    }

    @Test
    fun `offline pricing keeps the sheet open and says so`() = runSojournTest {
        pricing.offline = true
        val vm = viewModel()
        settled(vm)
        vm.onIntent(DetailsIntent.PrimaryTapped)
        vm.onIntent(DetailsIntent.DatesSelected(quote.dates))
        vm.onIntent(DetailsIntent.DatesConfirmed)
        val sheet = assertIs<DetailsSheet.Dates>(settled(vm).sheet)
        assertEquals(QuoteProblem.Failed(offline = true), sheet.problem)
    }

    @Test
    fun `a stay gone from the catalogue is an error and not a blank page`() = runSojournTest {
        stays.remote = emptyList()
        val details = settled(viewModel()).details
        assertIs<AppError.NotFound>(assertIs<ScreenState.Error>(details).error)
    }

    @Test
    fun `translate swaps to the reader's language and back`() = runSojournTest {
        val vm = viewModel()
        assertEquals(studio.description, settled(vm).description("ar"))
        vm.onIntent(DetailsIntent.TranslateTapped)
        assertEquals("ar", vm.state.value.description("ar")?.languageTag)
        // A reader in the host's language gets the original.
        assertEquals(studio.description, vm.state.value.description("en"))
        vm.onIntent(DetailsIntent.TranslateTapped)
        assertEquals(studio.description, vm.state.value.description("ar"))
    }

    @Test
    fun `photos and share and map and the heart go where the card says`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.effects.test {
            vm.onIntent(DetailsIntent.PhotoTapped(2))
            assertEquals(DetailsEffect.Navigate(GalleryKey(studio.id.value, 2)), awaitItem())
            vm.onIntent(DetailsIntent.ShareTapped)
            assertEquals(DetailsEffect.Share(studio), awaitItem())
            vm.onIntent(DetailsIntent.ShowOnMapTapped)
            assertEquals(DetailsEffect.Navigate(MapKey(london)), awaitItem())
            vm.onIntent(DetailsIntent.BackTapped)
            assertEquals(DetailsEffect.Back, awaitItem())
        }
        vm.onIntent(DetailsIntent.SaveTapped)
        assertTrue(assertIs<ScreenState.Content<StayDetails>>(settled(vm).details).data.saved)
    }
}
