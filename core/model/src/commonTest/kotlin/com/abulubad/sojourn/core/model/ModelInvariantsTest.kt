package com.abulubad.sojourn.core.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelInvariantsTest {

    @Test
    fun `a date range counts nights as the day difference`() {
        val range = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))
        assertEquals(15, range.nights)
        assertTrue(LocalDate(2026, 3, 26) in range)
        assertFalse(LocalDate(2026, 3, 27) in range)
        assertFailsWith<IllegalArgumentException> { DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 12)) }
    }

    @Test
    fun `stay length shortcuts resolve from nights`() {
        assertEquals(StayLength.Weekend, StayLength.forNights(2))
        assertEquals(StayLength.Week, StayLength.forNights(10))
        assertEquals(StayLength.Month, StayLength.forNights(30))
        assertEquals(StayLength.Longer, StayLength.forNights(120))
    }

    @Test
    fun `zero bedrooms is a studio`() {
        assertTrue(StayFacts(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2).isStudio)
        assertFalse(StayFacts(bedrooms = 1, beds = 1, bathrooms = 1, sleeps = 2).isStudio)
    }

    @Test
    fun `guests need an adult`() {
        assertFailsWith<IllegalArgumentException> { Guests(adults = 0) }
        assertEquals(3, Guests(adults = 2, children = 1).total)
    }

    @Test
    fun `filters count each active filter - every amenity on its own and sort not at all`() {
        assertEquals(0, Filters.None.activeCount)
        val filters = Filters(
            priceRange = MoneyRange(Money.of(500, Currency.GBP), Money.of(3000, Currency.GBP)),
            amenities = setOf(Amenity.Wifi, Amenity.Washer),
            sort = SortOrder.Rating,
        )
        assertEquals(3, filters.activeCount)
        assertEquals(5, filters.copy(studioOnly = true, freeCancellation = true).activeCount)
        assertEquals(0, Filters(sort = SortOrder.DistanceToCentre).activeCount)
    }

    @Test
    fun `policies carry their numbers`() {
        assertFailsWith<IllegalArgumentException> { Policy.MinimumStay(0) }
        assertEquals(LocalDate(2026, 2, 26), Policy.FreeCancellation(LocalDate(2026, 2, 26)).until)
    }

    @Test
    fun `host text declares its language`() {
        assertFailsWith<IllegalArgumentException> { HostText("Bright studio", " ") }
    }
}
