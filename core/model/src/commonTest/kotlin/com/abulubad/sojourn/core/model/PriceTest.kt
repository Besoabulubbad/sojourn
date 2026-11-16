package com.abulubad.sojourn.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PriceTest {

    private val gbp = Currency.GBP

    @Test
    fun `breakdown lines must sum to the total`() {
        val nights = PriceLine(PriceLineKind.Nights, Money.of(1380, gbp), quantity = 15, unitAmount = Money.of(92, gbp))
        val cleaning = PriceLine(PriceLineKind.CleaningFee, Money.of(3, gbp))
        val breakdown = PriceBreakdown.of(listOf(nights, cleaning))

        assertEquals(Money.of(1383, gbp), breakdown.total)
        assertFailsWith<IllegalArgumentException> { PriceBreakdown(listOf(nights, cleaning), Money.of(1384, gbp)) }
    }

    @Test
    fun `a line with a quantity must equal unit times quantity`() {
        assertFailsWith<IllegalArgumentException> {
            PriceLine(PriceLineKind.Nights, Money.of(100, gbp), quantity = 2, unitAmount = Money.of(60, gbp))
        }
    }

    @Test
    fun `breakdowns never mix currencies`() {
        assertFailsWith<IllegalArgumentException> {
            PriceBreakdown.of(
                listOf(
                    PriceLine(PriceLineKind.Nights, Money.of(10, gbp)),
                    PriceLine(PriceLineKind.CityTax, Money.of(1, Currency.EUR)),
                ),
            )
        }
    }

    @Test
    fun `discounts are negative lines inside the same total`() {
        val breakdown = PriceBreakdown.of(
            listOf(
                PriceLine(PriceLineKind.Nights, Money.of(2760, gbp), quantity = 30, unitAmount = Money.of(92, gbp)),
                PriceLine(PriceLineKind.LongStayDiscount, -Money.of(276, gbp)),
            ),
        )
        assertEquals(Money.of(2484, gbp), breakdown.total)
        assertTrue(breakdown.lines[1].amount.isNegative)
    }

    @Test
    fun `a from price must state what it excludes`() {
        assertFailsWith<IllegalArgumentException> {
            PriceDisplay.From(
                Money.of(2100, gbp),
                PriceBasis.Month,
                emptyList(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            PriceDisplay.From(Money.of(2100, gbp), PriceBasis.Stay, listOf(PriceExclusion.Utilities))
        }
        val from = PriceDisplay.From(Money.of(2100, gbp), PriceBasis.Month, listOf(PriceExclusion.Utilities))
        assertEquals(PriceBasis.Month, from.per)
    }

    @Test
    fun `a total carries its quote id and nights`() {
        val total = PriceDisplay.Total(QuoteId("q1"), Money.of(1383, gbp), nights = 15)
        assertEquals(QuoteId("q1"), total.quoteId)
        assertFailsWith<IllegalArgumentException> { PriceDisplay.Total(QuoteId("q1"), Money.of(1, gbp), nights = 0) }
    }

    @Test
    fun `money arithmetic stays in minor units and one currency`() {
        val a = Money.of(1, gbp, minorRemainder = 50)
        val b = Money.of(2, gbp, minorRemainder = 75)
        assertEquals(Money(425, gbp), a + b)
        assertEquals(4L, (a + b).major)
        assertEquals(25L, (a + b).minorRemainder)
        assertEquals(Money(-125, gbp), a - b)
        assertEquals(-1L, (a - b).major, "major truncates toward zero and keeps the sign")
        assertEquals(25L, (a - b).minorRemainder)
        assertFailsWith<IllegalArgumentException> { a + Money.of(1, Currency.EUR) }
    }
}
