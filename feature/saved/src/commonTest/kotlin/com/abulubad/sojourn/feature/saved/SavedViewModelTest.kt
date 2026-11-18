package com.abulubad.sojourn.feature.saved

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.ObserveSaved
import com.abulubad.sojourn.core.domain.RemoveSaved
import com.abulubad.sojourn.core.domain.RestoreSaved
import com.abulubad.sojourn.core.domain.SavedStay
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.PriceDisplay
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SavedViewModelTest {

    // The fixed clock reads 1 March 2026; the quote is the studio for 12–27 March.
    private val clock = FixedAppClock()
    private val studio = StayFixtures.canalStudio
    private val quote = QuoteFixtures.canalStudioFortnight
    private val stays = FakeStayRepository(remote = listOf(studio), cached = listOf(studio))
    private val pricing = FakePricingRepository { _, _, _ -> AppResult.Success(quote) }
    private val saved = FakeSavedRepository()

    private fun viewModel() = SavedViewModel(
        ObserveSaved(saved, stays, pricing, clock),
        RemoveSaved(saved),
        RestoreSaved(saved),
    )

    private suspend fun TestScope.cards(vm: SavedViewModel): List<SavedStay> {
        advanceUntilIdle()
        return (vm.state.value.saved as? ScreenState.Content)?.data.orEmpty()
    }

    @Test
    fun `nothing saved is the empty state`() = runSojournTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(ScreenState.Empty, vm.state.value.saved)
        vm.effects.test {
            vm.onIntent(SavedIntent.ExploreTapped)
            assertEquals(SavedEffect.ShowExplore, awaitItem())
        }
    }

    @Test
    fun `a stay saved with dates shows that total and opens Details for that search`() = runSojournTest {
        saved.toggle(studio.id, quote.dates, Guests(adults = 2))
        val vm = viewModel()
        val card = cards(vm).single()
        assertEquals(quote.display, card.result.price)
        vm.effects.test {
            vm.onIntent(SavedIntent.StayTapped(card))
            val key = assertIs<StayDetailsKey>(assertIs<SavedEffect.Navigate>(awaitItem()).key)
            assertEquals(quote.id.value, key.quoteId)
            assertEquals(quote.dates.start.toString(), key.args.checkIn)
            assertEquals(2, key.args.adults)
        }
    }

    @Test
    fun `removing takes the card off at once and undo puts it back`() = runSojournTest {
        saved.toggle(studio.id)
        val vm = viewModel()
        assertIs<PriceDisplay.From>(cards(vm).single().result.price)
        vm.effects.test {
            vm.onIntent(SavedIntent.RemoveTapped(studio.id))
            assertEquals(SavedEffect.ShowRemoved, awaitItem())
        }
        advanceUntilIdle()
        assertEquals(ScreenState.Empty, vm.state.value.saved)
        vm.onIntent(SavedIntent.UndoTapped)
        assertEquals(listOf(studio.id), cards(vm).map { it.result.stay.id })
    }
}
