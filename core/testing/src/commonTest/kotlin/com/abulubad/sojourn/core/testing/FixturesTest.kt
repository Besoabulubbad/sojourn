package com.abulubad.sojourn.core.testing

import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The shared fixtures match the numbers the design system shows, so screenshots and copy agree. */
class FixturesTest {

    @Test
    fun `the canal studio fortnight totals the brand book price`() {
        val quote = QuoteFixtures.canalStudioFortnight
        assertEquals(Money.of(1383, Currency.GBP), quote.total)
        assertEquals(15, quote.nights)
        assertEquals(quote.id, quote.display.quoteId)
    }

    @Test
    fun `the canal studio is a studio and the arabic stay is under the rating threshold`() {
        assertTrue(StayFixtures.canalStudio.facts.isStudio)
        assertTrue(requireNotNull(StayFixtures.arabicTitleStay.rating).count < 3)
    }
}
