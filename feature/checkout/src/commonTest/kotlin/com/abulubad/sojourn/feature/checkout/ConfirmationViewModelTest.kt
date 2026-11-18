package com.abulubad.sojourn.feature.checkout

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.GetBooking
import com.abulubad.sojourn.core.navigation.BookingConfirmedKey
import com.abulubad.sojourn.core.testing.QuoteFixtures
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeBookingRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ConfirmationViewModelTest {

    private val stays = FakeStayRepository(cached = listOf(StayFixtures.canalStudio))
    private val bookings = FakeBookingRepository()

    @Test
    fun `the confirmation shows the booking with its stay and goes on from there`() = runSojournTest {
        val quote = QuoteFixtures.canalStudioFortnight
        bookings.issued[quote.id] = quote
        val booking = assertNotNull(bookings.book(quote.id).getOrNull())
        val vm = ConfirmationViewModel(BookingConfirmedKey(booking.id.value), GetBooking(bookings, stays))
        advanceUntilIdle()
        val item = assertIs<ScreenState.Content<BookingItem>>(vm.state.value.booking).data
        assertEquals(booking, item.booking)
        assertEquals(StayFixtures.canalStudio, item.stay)
        vm.effects.test {
            vm.onIntent(ConfirmationIntent.ViewBookingTapped)
            assertEquals(ConfirmationEffect.ViewBooking(booking.id.value), awaitItem())
            vm.onIntent(ConfirmationIntent.DoneTapped)
            assertEquals(ConfirmationEffect.Done, awaitItem())
        }
    }

    @Test
    fun `a booking the device does not have is an error`() = runSojournTest {
        val vm = ConfirmationViewModel(BookingConfirmedKey("b-unknown"), GetBooking(bookings, stays))
        advanceUntilIdle()
        assertIs<AppError.NotFound>(assertIs<ScreenState.Error>(vm.state.value.booking).error)
    }
}
