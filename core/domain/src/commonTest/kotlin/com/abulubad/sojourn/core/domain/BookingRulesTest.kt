package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeBookingRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class BookingRulesTest {

    /** 12–27 March, 15 nights, £1,500. */
    private val booking = Booking(
        id = BookingId("b-1"),
        reference = "SJ-ABCDEF",
        stayId = StayFixtures.canalStudio.id,
        quoteId = QuoteId("q-1"),
        dates = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27)),
        guests = Guests(2),
        total = Money.of(1_500, Currency.GBP),
        status = BookingStatus.Upcoming,
        createdAt = Instant.parse("2026-02-01T09:00:00Z"),
        keyCode = "482913",
    )
    private val freeUntil26Feb =
        StayFixtures.canalStudio.copy(policies = listOf(Policy.FreeCancellation(LocalDate(2026, 2, 26))))
    private val freeSevenDaysBefore = StayFixtures.canalStudio.copy(policies = listOf(Policy.FreeCancellationBefore(7)))

    @Test
    fun `status and the key code follow the calendar`() {
        assertEquals(BookingStatus.Upcoming, BookingRules.statusOn(booking, LocalDate(2026, 3, 11)))
        assertFalse(BookingRules.showsKeyCode(booking, LocalDate(2026, 3, 11)))
        assertEquals(BookingStatus.Active, BookingRules.statusOn(booking, LocalDate(2026, 3, 12)))
        assertTrue(BookingRules.showsKeyCode(booking, LocalDate(2026, 3, 12)))
        assertEquals(BookingStatus.Completed, BookingRules.statusOn(booking, LocalDate(2026, 3, 27)))
        assertFalse(BookingRules.showsKeyCode(booking, LocalDate(2026, 3, 27)))
        val cancelled = booking.copy(status = BookingStatus.Cancelled)
        assertEquals(BookingStatus.Cancelled, BookingRules.statusOn(cancelled, LocalDate(2026, 3, 12)))
    }

    @Test
    fun `a stay leads Explore from two days before arrival until check-out`() {
        assertFalse(BookingRules.isArriving(booking, LocalDate(2026, 3, 9)))
        assertTrue(BookingRules.isArriving(booking, LocalDate(2026, 3, 10)))
        assertTrue(BookingRules.isArriving(booking, LocalDate(2026, 3, 20)))
        assertFalse(BookingRules.isArriving(booking, LocalDate(2026, 3, 27)))
    }

    @Test
    fun `cancelling is free until the deadline then the first seven nights are kept`() {
        val early = BookingRules.refund(booking, freeUntil26Feb, LocalDate(2026, 2, 26))
        assertEquals(Refund.Full(booking.total), early)
        val late = assertIs<Refund.Partial>(BookingRules.refund(booking, freeUntil26Feb, LocalDate(2026, 2, 27)))
        assertEquals(Refund.Partial(Money.of(800, Currency.GBP), keptNights = 7), late)
        // Seven days before 12 March is 5 March.
        assertEquals(LocalDate(2026, 3, 5), BookingRules.freeCancellationUntil(booking, freeSevenDaysBefore))
        assertIs<Refund.Full>(BookingRules.refund(booking, freeSevenDaysBefore, LocalDate(2026, 3, 5)))
        val week = booking.copy(dates = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 19)))
        assertEquals(Refund.None, BookingRules.refund(week, null, LocalDate(2026, 3, 1)))
        assertFalse(BookingRules.canCancel(booking, LocalDate(2026, 3, 12)))
    }

    @Test
    fun `cancelling a booking marks it cancelled`() = runSojournTest {
        val bookings = FakeBookingRepository()
        bookings.bookings.value = listOf(booking)
        assertIs<AppResult.Success<Unit>>(CancelBooking(bookings)(booking.id))
        assertEquals(BookingStatus.Cancelled, bookings.observeBooking(booking.id).first()?.status)
        val clock = FixedAppClock(Instant.parse("2026-03-10T09:00:00Z"))
        assertEquals(null, ObserveArrivingStay(ListBookings(bookings, FakeStayRepository(), clock), clock)().first())
    }

    @Test
    fun `a completed stay earns a point per pound or euro or per 100 dinars`() {
        val completed = booking.copy(status = BookingStatus.Completed)
        assertEquals(0, RewardsRules.pointsFor(booking))
        assertEquals(0, RewardsRules.pointsFor(booking.copy(status = BookingStatus.Cancelled)))
        assertEquals(1_500, RewardsRules.pointsFor(completed))
        assertEquals(1_500, RewardsRules.pointsFor(completed.copy(total = Money.of(1_500, Currency.EUR, 99))))
        assertEquals(1_250, RewardsRules.pointsFor(completed.copy(total = Money.of(125_099, Currency.DZD))))
    }
}
