package com.abulubad.sojourn.feature.bookings

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.domain.CancelBooking
import com.abulubad.sojourn.core.domain.GetBooking
import com.abulubad.sojourn.core.domain.ListBookings
import com.abulubad.sojourn.core.domain.Refund
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.navigation.BookingDetailsKey
import com.abulubad.sojourn.core.testing.FixedAppClock
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookingsViewModelTest {

    // The fixed clock reads 1 March 2026; the booking is the studio for 12–27 March, 15 nights.
    private val clock = FixedAppClock()
    private val quote = QuoteFixtures.canalStudioFortnight
    private val bookings = FakeBookingRepository()
    private val stays = FakeStayRepository(cached = listOf(StayFixtures.canalStudio))

    private suspend fun booked() = bookings.run {
        issued[quote.id] = quote
        (book(quote.id) as AppResult.Success).value
    }

    @Test
    fun `no bookings is the empty state and Explore stays switches tab`() = runSojournTest {
        val vm = BookingsViewModel(ListBookings(bookings, stays, clock), clock)
        advanceUntilIdle()
        assertEquals(ScreenState.Empty, vm.state.value.bookings)
        vm.effects.test {
            vm.onIntent(BookingsIntent.ExploreTapped)
            assertEquals(BookingsEffect.ShowExplore, awaitItem())
        }
    }

    @Test
    fun `an upcoming booking is under Upcoming with its guide and page`() = runSojournTest {
        val booking = booked()
        val vm = BookingsViewModel(ListBookings(bookings, stays, clock), clock)
        advanceUntilIdle()
        val item = vm.state.value.upcoming.single()
        assertTrue(vm.state.value.past.isEmpty())
        vm.onIntent(BookingsIntent.GuideTapped(item))
        assertEquals(item, vm.state.value.guide)
        vm.onIntent(BookingsIntent.GuideDismissed)
        assertNull(vm.state.value.guide)
        vm.effects.test {
            vm.onIntent(BookingsIntent.BookingTapped(item))
            assertEquals(BookingsEffect.Navigate(BookingDetailsKey(booking.id.value)), awaitItem())
            vm.onIntent(BookingsIntent.CopyTapped("482913"))
            assertEquals(BookingsEffect.Copy("482913"), awaitItem())
        }
    }

    @Test
    fun `cancelling says what comes back then moves the booking to Past`() = runSojournTest {
        val booking = booked()
        val vm = BookingDetailsViewModel(
            BookingDetailsKey(booking.id.value),
            GetBooking(bookings, stays, clock),
            CancelBooking(bookings),
            clock,
        )
        advanceUntilIdle()
        assertTrue(vm.state.value.canCancel)
        // The studio's free cancellation ended on 26 February: the first 7 of 15 nights are kept.
        assertIs<Refund.Partial>(vm.state.value.refund)
        vm.onIntent(BookingDetailsIntent.CancelTapped)
        vm.effects.test {
            vm.onIntent(BookingDetailsIntent.CancelConfirmed)
            assertEquals(BookingDetailsEffect.CloseSheet, awaitItem())
        }
        advanceUntilIdle()
        assertEquals(BookingStatus.Cancelled, vm.state.value.item?.booking?.status)
        val list = BookingsViewModel(ListBookings(bookings, stays, clock), clock)
        advanceUntilIdle()
        assertEquals(listOf(booking.id), list.state.value.past.map { it.booking.id })
    }

    @Test
    fun `cancelling offline keeps the sheet open and says so`() = runSojournTest {
        val booking = booked()
        bookings.offline = true
        val vm = BookingDetailsViewModel(
            BookingDetailsKey(booking.id.value),
            GetBooking(bookings, stays, clock),
            CancelBooking(bookings),
            clock,
        )
        advanceUntilIdle()
        vm.onIntent(BookingDetailsIntent.CancelTapped)
        vm.onIntent(BookingDetailsIntent.CancelConfirmed)
        advanceUntilIdle()
        assertEquals(BookingSheet.Cancel(error = AppError.Offline), vm.state.value.sheet)
        assertEquals(BookingStatus.Upcoming, vm.state.value.item?.booking?.status)
    }
}
