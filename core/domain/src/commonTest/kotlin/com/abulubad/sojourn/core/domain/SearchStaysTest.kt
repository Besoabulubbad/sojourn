package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.domain.DomainFixtures.engine
import com.abulubad.sojourn.core.domain.DomainFixtures.family
import com.abulubad.sojourn.core.domain.DomainFixtures.flat
import com.abulubad.sojourn.core.domain.DomainFixtures.london
import com.abulubad.sojourn.core.domain.DomainFixtures.march
import com.abulubad.sojourn.core.domain.DomainFixtures.studio
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.testing.data.FakePricingRepository
import com.abulubad.sojourn.core.testing.data.FakeSavedRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchStaysTest {

    private val stays = FakeStayRepository(remote = london)
    private val pricing = FakePricingRepository(engine())
    private val saved = FakeSavedRepository()
    private val search = SearchStays(stays, pricing, saved)

    private suspend fun results(query: SearchQuery = SearchQuery(), filters: Filters = Filters.None) =
        assertNotNull(search(query, filters).first { !it.refreshing }.data)

    @Test
    fun `without dates every card shows a monthly from price with its exclusions - P-03`() = runSojournTest {
        val results = results()
        assertEquals(PriceBasis.Month, results.basis)
        assertEquals(london.map { it.id }.toSet(), results.stays.map { it.stay.id }.toSet())
        results.stays.forEach { result ->
            val price = assertIs<PriceDisplay.From>(result.price)
            assertEquals(PriceBasis.Month, price.per)
            assertTrue(price.excludes.isNotEmpty())
        }
    }

    @Test
    fun `a weekend search shows nightly from prices`() = runSojournTest {
        val results = results(SearchQuery(length = StayLength.Weekend))
        assertEquals(PriceBasis.Night, results.basis)
        assertEquals(studio.fromPrice, results.stays.first { it.stay.id == studio.id }.price)
    }

    @Test
    fun `capacity and the non-price filters narrow the results`() = runSojournTest {
        assertEquals(setOf(family.id, flat.id), results(SearchQuery(guests = Guests(3))).ids())
        assertEquals(setOf(family.id), results(filters = Filters(instantBook = true)).ids())
        assertEquals(setOf(studio.id, flat.id), results(filters = Filters(amenities = setOf(Amenity.Workspace))).ids())
        assertEquals(setOf(family.id, flat.id), results(filters = Filters(minBedrooms = 1)).ids())
    }

    @Test
    fun `a price range applies only on the basis the cards show - P-04`() = runSojournTest {
        val monthly = MoneyRange(Money.of(2_000, Currency.GBP), Money.of(4_000, Currency.GBP))
        // Studio £2,484 and flat £3,600 a month are in range; family £4,500 is not.
        assertEquals(setOf(studio.id, flat.id), results(filters = Filters(priceRange = monthly)).ids())
        val nightlyRange = Filters(priceRange = monthly, basis = PriceBasis.Night)
        assertEquals(london.map { it.id }.toSet(), results(filters = nightlyRange).ids(), "other basis: not applied")
    }

    @Test
    fun `with dates every card shows its quote total and quote id - P-02`() = runSojournTest {
        val results = results(SearchQuery(dates = march, guests = Guests(2)))
        assertEquals(PriceBasis.Stay, results.basis)
        results.stays.forEach { result ->
            val total = assertIs<PriceDisplay.Total>(result.price)
            assertEquals(pricing.stored.value[total.quoteId]?.display, total)
        }
    }

    @Test
    fun `with dates a stay the dates do not work for is left out`() = runSojournTest {
        val fourNights = march.copy(end = LocalDate(2026, 3, 16))
        // Family has a five-night minimum.
        assertEquals(setOf(studio.id, flat.id), results(SearchQuery(dates = fourNights)).ids())
    }

    @Test
    fun `with dates offline the stays already priced still show and the rest wait`() = runSojournTest {
        val query = SearchQuery(dates = march)
        results(query)
        pricing.offline = true
        val quotesBefore = pricing.quoteCalls
        val offline = SearchStays(stays, pricing, saved)(query, Filters.None, force = true).first { !it.refreshing }
        assertEquals(london.size, offline.data?.stays?.size)
        assertTrue(pricing.quoteCalls > quotesBefore)
        assertNull(offline.error, "every stay had a stored quote")
    }

    @Test
    fun `with dates offline and nothing priced the search is offline`() = runSojournTest {
        pricing.offline = true
        val offline = search(SearchQuery(dates = march), Filters.None).first { !it.refreshing }
        assertEquals(AppError.Offline, offline.error)
        assertEquals(emptyList(), offline.data?.stays)
    }

    @Test
    fun `price sorts order by the card price and a rating sort puts hidden ratings last - N-04`() = runSojournTest {
        val low = results(filters = Filters(sort = SortOrder.PriceLowToHigh)).stays.map { it.stay.id }
        assertEquals(listOf(studio.id, flat.id, family.id), low)
        val high = results(filters = Filters(sort = SortOrder.PriceHighToLow)).stays.map { it.stay.id }
        assertEquals(listOf(family.id, flat.id, studio.id), high)
        val rated = results(filters = Filters(sort = SortOrder.Rating)).stays
        assertEquals(listOf(studio.id, flat.id, family.id), rated.map { it.stay.id })
        assertNull(rated.last().rating, "two reviews: hidden")
    }

    @Test
    fun `the heart reflects saved stays`() = runSojournTest {
        saved.setSaved(flat.id, saved = true)
        assertEquals(setOf(flat.id), results().stays.filter { it.saved }.map { it.stay.id }.toSet())
    }

    private fun SearchResults.ids() = stays.map { it.stay.id }.toSet()
}
