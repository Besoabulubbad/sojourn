package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.data.DataTestEnv.Companion.quarter
import com.abulubad.sojourn.core.database.Quote
import com.abulubad.sojourn.core.database.Quote_line
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.network.local.FakeNetworkConditions.Mode
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours

class PricingRepositoryTest {

    @Test
    fun `every surface reads back the same quote by its id - P-02`() = runDataTest {
        val stay = catalogueStays().first()
        val quote = assertNotNull(pricing.quote(stay.id, quarter(), Guests(2)).getOrNull())
        val stored = assertNotNull(pricing.observeQuote(quote.id).first())
        assertEquals(quote, stored)
        assertEquals(quote.display, stored.display)
    }

    @Test
    fun `a quote is stamped with when it was priced and keeps that time offline`() = runDataTest {
        val stay = catalogueStays().first()
        val pricedAt = clock.now()
        val online = assertNotNull(pricing.quote(stay.id, quarter(), Guests(2)).getOrNull())
        assertEquals(pricedAt, online.pricedAt)
        clock.advance(3.hours)
        conditions.mode = Mode.Offline
        assertEquals(pricedAt, pricing.quote(stay.id, quarter(), Guests(2)).getOrNull()?.pricedAt)
    }

    @Test
    fun `every stay in the catalogue stores a quote whose lines sum to its total - P-05`() = runDataTest {
        catalogueStays().forEach { stay ->
            val quote = assertNotNull(pricing.quote(stay.id, quarter(), Guests(1)).getOrNull(), stay.id.value)
            // PriceBreakdown checks P-05 again when the stored rows are read back.
            assertEquals(quote.total, pricing.observeQuote(quote.id).first()?.total, stay.id.value)
        }
    }

    @Test
    fun `offline the stored quote for the same request is returned`() = runDataTest {
        val stay = catalogueStays().first()
        val online = assertNotNull(pricing.quote(stay.id, quarter(), Guests(2)).getOrNull())
        conditions.mode = Mode.Offline
        assertEquals(online, pricing.quote(stay.id, quarter(), Guests(2)).getOrNull())
    }

    @Test
    fun `offline a request never priced fails offline`() = runDataTest {
        val stay = catalogueStays().first()
        pricing.quote(stay.id, quarter(), Guests(2))
        conditions.mode = Mode.Offline
        assertEquals(AppError.Offline, pricing.quote(stay.id, quarter(), Guests(1)).errorOrNull())
    }

    @Test
    fun `a broken rule comes back as a validation error`() = runDataTest {
        val stay = catalogueStays().first()
        val crowd = Guests(adults = stay.facts.sleeps + 1)
        assertEquals(AppError.Validation("too_many_guests"), pricing.quote(stay.id, quarter(), crowd).errorOrNull())
    }

    @Test
    fun `a stored quote whose lines do not sum to its total reads as missing`() = runDataTest {
        db.quoteQueries.upsert(Quote("q-bad", "london-01", "2027-07-01", "2027-07-08", 1, 0, 10_000, "GBP", null))
        db.quoteQueries.insertLine(Quote_line("q-bad", 0, "nights", 9_000, null, null))
        assertNull(pricing.observeQuote(QuoteId("q-bad")).first())
    }

    @Test
    fun `a quote never issued reads as missing`() = runDataTest {
        assertNull(pricing.observeQuote(QuoteId("nope")).first())
    }
}
