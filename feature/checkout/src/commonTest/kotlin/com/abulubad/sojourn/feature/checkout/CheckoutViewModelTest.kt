package com.abulubad.sojourn.feature.checkout

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.CreateBooking
import com.abulubad.sojourn.core.domain.GetQuote
import com.abulubad.sojourn.core.domain.GetStayDetails
import com.abulubad.sojourn.core.domain.ObserveQuote
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBreakdown
import com.abulubad.sojourn.core.model.PriceLine
import com.abulubad.sojourn.core.model.PriceLineKind
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.QuoteProblem
import com.abulubad.sojourn.core.navigation.BookingConfirmedKey
import com.abulubad.sojourn.core.navigation.CheckoutKey
import com.abulubad.sojourn.core.platform.PaymentResult
import com.abulubad.sojourn.core.platform.PaymentSheet
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.QuoteFixtures
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeBookingRepository
import com.abulubad.sojourn.core.testing.data.FakePricingRepository
import com.abulubad.sojourn.core.testing.data.FakeSavedRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckoutViewModelTest {

    // The fixed clock reads 1 March 2026; the quote is the studio for 12–27 March, £1,383.
    private val clock = FixedAppClock()
    private val studio = StayFixtures.canalStudio
    private val shown = QuoteFixtures.canalStudioFortnight
    private val stays = FakeStayRepository(remote = listOf(studio))
    private val pricing = FakePricingRepository { _, dates, guests -> AppResult.Success(priced(dates, guests)) }
    private val bookings = FakeBookingRepository()
    private var payment: PaymentResult = PaymentResult.Authorized("token")
    private var paymentCalls = 0
    private val payments = PaymentSheet {
        paymentCalls++
        payment
    }

    init {
        pricing.stored.value = mapOf(shown.id to shown)
        bookings.issued[shown.id] = shown
    }

    /** £92 a night (or [nightly]) and £3 cleaning, under an id of its own. */
    private fun priced(dates: DateRange, guests: Guests, nightly: Long = 92): Quote {
        val unit = Money.of(nightly, Currency.GBP)
        return Quote(
            id = QuoteId("q-${dates.start}-${dates.end}-${guests.adults}-${guests.children}-$nightly"),
            stayId = studio.id,
            dates = dates,
            guests = guests,
            breakdown = PriceBreakdown.of(
                listOf(
                    PriceLine(PriceLineKind.Nights, unit * dates.nights, quantity = dates.nights, unitAmount = unit),
                    PriceLine(PriceLineKind.CleaningFee, Money.of(3, Currency.GBP)),
                ),
            ),
        )
    }

    private fun viewModel(quoteId: String = shown.id.value) = CheckoutViewModel(
        CheckoutKey(quoteId),
        ObserveQuote(pricing),
        GetStayDetails(stays, FakeSavedRepository()),
        GetQuote(stays, pricing, clock),
        CreateBooking(pricing, bookings),
        payments,
        clock,
    )

    private suspend fun TestScope.settled(vm: CheckoutViewModel): CheckoutUiState {
        advanceUntilIdle()
        return vm.state.value
    }

    @Test
    fun `checkout shows the quote the card showed with its stay`() = runSojournTest {
        val state = settled(viewModel())
        assertEquals(shown, state.quote)
        assertEquals(studio, state.stay)
        assertNull(state.previousTotal)
    }

    @Test
    fun `a quote the device does not have is an expired price and not a blank page`() = runSojournTest {
        val details = settled(viewModel(quoteId = "q-unknown")).details
        assertIs<AppError.NotFound>(assertIs<ScreenState.Error>(details).error)
    }

    @Test
    fun `paying charges the total shown then hands over to the confirmation`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.effects.test {
            payInWallet(vm)
            assertEquals(CheckoutEffect.CloseWallet, awaitItem())
            assertEquals(CheckoutEffect.PaymentFeedback(success = true), awaitItem())
            val booked = assertIs<CheckoutEffect.Booked>(awaitItem())
            val booking = bookings.bookings.value.single()
            assertEquals(BookingConfirmedKey(booking.id.value), booked.key)
            assertEquals(shown.total, booking.total)
        }
    }

    @Test
    fun `a second tap while paying does not pay twice`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        payInWallet(vm)
        vm.onIntent(CheckoutIntent.WalletConfirmed)
        vm.onIntent(CheckoutIntent.PayTapped)
        settled(vm)
        assertEquals(1, paymentCalls)
        assertEquals(1, bookings.bookings.value.size)
    }

    @Test
    fun `new dates are priced in place and the page says what the total was`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        val longer = DateRange(shown.dates.start, LocalDate(2026, 3, 29))
        vm.onIntent(CheckoutIntent.EditDatesTapped)
        assertEquals(shown.dates, assertIs<CheckoutSheet.EditDates>(vm.state.value.sheet).selection)
        vm.onIntent(CheckoutIntent.DatesSelected(longer))
        vm.effects.test {
            vm.onIntent(CheckoutIntent.EditConfirmed)
            assertEquals(CheckoutEffect.CloseSheet, awaitItem())
        }
        val state = vm.state.value
        assertEquals(longer, state.quote?.dates)
        assertEquals(shown.total, state.previousTotal)
    }

    @Test
    fun `guests the stay cannot sleep are refused in its own numbers and the page keeps its quote`() =
        runSojournTest {
            val vm = viewModel()
            settled(vm)
            vm.onIntent(CheckoutIntent.EditGuestsTapped)
            vm.onIntent(CheckoutIntent.GuestsChanged(Guests(adults = 2, children = 1)))
            vm.onIntent(CheckoutIntent.EditConfirmed)
            val state = settled(vm)
            val sheet = assertIs<CheckoutSheet.EditGuests>(state.sheet)
            assertEquals(QuoteProblem.TooManyGuests(studio.facts.sleeps), sheet.problem)
            assertFalse(sheet.quoting)
            assertEquals(shown, state.quote)
        }

    @Test
    fun `a declined payment keeps the page and books nothing`() = runSojournTest {
        payment = PaymentResult.Failed("declined")
        val vm = viewModel()
        settled(vm)
        vm.effects.test {
            payInWallet(vm)
            assertEquals(CheckoutEffect.CloseWallet, awaitItem())
            assertEquals(CheckoutEffect.PaymentFeedback(success = false), awaitItem())
        }
        val state = vm.state.value
        assertEquals(CheckoutNotice.PaymentFailed, state.notice)
        assertFalse(state.paying)
        assertEquals(shown, state.quote)
        assertTrue(bookings.bookings.value.isEmpty())
    }

    @Test
    fun `closing the payment sheet is not an error`() = runSojournTest {
        payment = PaymentResult.Cancelled
        val vm = viewModel()
        settled(vm)
        payInWallet(vm)
        val state = settled(vm)
        assertNull(state.notice)
        assertFalse(state.paying)
    }

    @Test
    fun `a total that changed while paying is shown and not charged - P-02`() = runSojournTest {
        // The API forgot the shown quote, and today's price for the same request is higher.
        bookings.issued.clear()
        pricing.quoteFor = { _, dates, guests -> AppResult.Success(priced(dates, guests, nightly = 99)) }
        val vm = viewModel()
        settled(vm)
        payInWallet(vm)
        val state = settled(vm)
        val fresh = priced(shown.dates, shown.guests, nightly = 99)
        assertEquals(fresh.total, state.quote?.total)
        assertEquals(shown.total, state.previousTotal)
        assertEquals(CheckoutNotice.PriceChanged(fresh.total), state.notice)
        assertTrue(bookings.bookings.value.isEmpty())
    }

    @Test
    fun `booking offline says so and keeps the page`() = runSojournTest {
        bookings.offline = true
        val vm = viewModel()
        settled(vm)
        payInWallet(vm)
        val state = settled(vm)
        assertEquals(CheckoutNotice.Offline, state.notice)
        assertFalse(state.paying)
    }

    @Test
    fun `pay opens the demo wallet and closing it charges nothing`() = runSojournTest {
        val vm = viewModel()
        settled(vm)
        vm.onIntent(CheckoutIntent.PayTapped)
        assertTrue(vm.state.value.wallet)
        assertEquals(0, paymentCalls)
        vm.onIntent(CheckoutIntent.WalletDismissed)
        settled(vm)
        assertFalse(vm.state.value.wallet)
        assertEquals(0, paymentCalls)
        assertTrue(bookings.bookings.value.isEmpty())
        vm.onIntent(CheckoutIntent.WalletConfirmed)
        settled(vm)
        assertEquals(0, paymentCalls, "a closed wallet cannot pay")
    }

    /** "Pay" on the page, then "Pay" in the demo wallet. */
    private fun payInWallet(vm: CheckoutViewModel) {
        vm.onIntent(CheckoutIntent.PayTapped)
        vm.onIntent(CheckoutIntent.WalletConfirmed)
    }
}
