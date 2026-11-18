package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.domain.DomainFixtures.family
import com.abulubad.sojourn.core.domain.DomainFixtures.march
import com.abulubad.sojourn.core.domain.DomainFixtures.studio
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class StayRulesTest {

    @Test
    fun `cards show totals with dates and from prices per night or month without - P-03`() {
        assertEquals(PriceBasis.Stay, StayRules.basisFor(SearchQuery(dates = march)))
        assertEquals(PriceBasis.Night, StayRules.basisFor(SearchQuery(length = StayLength.Weekend)))
        assertEquals(PriceBasis.Night, StayRules.basisFor(SearchQuery(length = StayLength.Week)))
        assertEquals(PriceBasis.Month, StayRules.basisFor(SearchQuery(length = StayLength.Month)))
        assertEquals(PriceBasis.Month, StayRules.basisFor(SearchQuery(length = StayLength.Longer)))
        assertEquals(PriceBasis.Month, StayRules.basisFor(SearchQuery()))
    }

    @Test
    fun `a monthly from price is thirty nights less the long-stay discount and keeps its exclusions`() {
        // £92 × 30 = £2,760, less 10 % = £2,484.
        val monthly = StayRules.fromPrice(studio, PriceBasis.Month)
        assertEquals(Money.of(2_484, Currency.GBP), monthly.amount)
        assertEquals(PriceBasis.Month, monthly.per)
        assertEquals(studio.fromPrice.excludes, monthly.excludes)
        assertEquals(Money.of(4_500, Currency.GBP), StayRules.fromPrice(family, PriceBasis.Month).amount)
        assertSame(studio.fromPrice, StayRules.fromPrice(studio, PriceBasis.Night))
    }

    @Test
    fun `ratings under three reviews are hidden - N-04`() {
        assertNull(StayRules.visibleRating(Rating(5.0, 2)))
        assertEquals(Rating(4.0, 3), StayRules.visibleRating(Rating(4.0, 3)))
        assertNull(StayRules.visibleRating(null))
    }

    @Test
    fun `booking problems are found before asking the network`() {
        val week = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 19))
        val booked = listOf(DateRange(LocalDate(2026, 3, 18), LocalDate(2026, 3, 20)))
        val before = listOf(DateRange(LocalDate(2026, 3, 10), LocalDate(2026, 3, 12)))
        assertEquals("minimum_nights", StayRules.bookingProblem(family, week.copyNights(3), Guests(1), emptyList()))
        assertEquals("too_many_guests", StayRules.bookingProblem(studio, week, Guests(adults = 3), emptyList()))
        assertEquals("unavailable", StayRules.bookingProblem(studio, week, Guests(1), booked))
        // A stay that ends on the check-in day does not overlap it.
        assertNull(StayRules.bookingProblem(studio, week, Guests(2), before))
    }

    @Test
    fun `a flexible month needs a free run of the length starting in that month - from today`() {
        val march = YearMonth(2026, 3)
        val today = LocalDate(2026, 3, 1)
        fun month(blocked: List<DateRange>) = StayRules.hasFreeRun(studio, StayLength.Month, march, blocked, today)
        assertTrue(month(emptyList()))
        // Booked from 3 March to 1 May: no 28-night run can start in March.
        assertFalse(month(listOf(DateRange(LocalDate(2026, 3, 3), LocalDate(2026, 5, 1)))))
        // Free from 31 March: a run may start on the last day and end in April.
        assertTrue(month(listOf(DateRange(LocalDate(2026, 3, 1), LocalDate(2026, 3, 31)))))
        assertFalse(
            StayRules.hasFreeRun(studio, StayLength.Weekend, march, emptyList(), LocalDate(2026, 4, 1)),
            "a month already over has no start day",
        )
        // The stay's own minimum (5 nights) beats a weekend: 1 to 5 March is too short a gap.
        val gap = listOf(
            DateRange(LocalDate(2026, 2, 1), LocalDate(2026, 3, 2)),
            DateRange(LocalDate(2026, 3, 6), LocalDate(2026, 6, 1)),
        )
        assertTrue(StayRules.hasFreeRun(studio, StayLength.Weekend, march, gap, today))
        assertFalse(StayRules.hasFreeRun(family, StayLength.Weekend, march, gap, today))
    }

    private fun DateRange.copyNights(nights: Int) =
        DateRange(start, LocalDate.fromEpochDays(start.toEpochDays() + nights))
}
