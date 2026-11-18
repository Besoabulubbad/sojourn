package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.domain.DomainFixtures.engine
import com.abulubad.sojourn.core.domain.DomainFixtures.london
import com.abulubad.sojourn.core.domain.DomainFixtures.march
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.StayFixtures
import com.abulubad.sojourn.core.testing.data.FakeCityRepository
import com.abulubad.sojourn.core.testing.data.FakePricingRepository
import com.abulubad.sojourn.core.testing.data.FakeSavedRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PreviewFiltersTest {

    private val previewFilters = PreviewFilters(
        SearchStays(
            FakeStayRepository(remote = london),
            FakePricingRepository(engine()),
            FakeSavedRepository(),
            FakeCityRepository(cached = listOf(StayFixtures.london)),
            FixedAppClock(),
        ),
    )

    private suspend fun preview(query: SearchQuery = SearchQuery(), filters: Filters = Filters.None): FilterPreview =
        assertNotNull(previewFilters(query, filters).first { !it.refreshing }.data)

    @Test
    fun `the histogram places every monthly price in whole hundreds - P-04`() = runSojournTest {
        // Studio £2,484, flat £3,600, family £4,500 a month.
        val found = preview()
        assertEquals(3, found.count)
        assertEquals(PriceBasis.Month, found.basis)
        val prices = assertNotNull(found.prices)
        assertEquals(MoneyRange(Money.of(2_400, Currency.GBP), Money.of(4_500, Currency.GBP)), prices.bounds)
        assertEquals(PreviewFilters.BUCKETS, prices.histogram.size)
        assertEquals(listOf(1, 1, 1), prices.histogram.filter { it > 0 })
        assertEquals(1, prices.histogram.first())
        assertEquals(1, prices.histogram.last())
    }

    @Test
    fun `the count applies the range but the histogram does not`() = runSojournTest {
        val range = MoneyRange(Money.of(2_400, Currency.GBP), Money.of(3_000, Currency.GBP))
        val found = preview(filters = Filters(priceRange = range, basis = PriceBasis.Month))
        assertEquals(1, found.count)
        assertEquals(3, assertNotNull(found.prices).histogram.sum())
        // A range on another basis does not apply (P-04).
        assertEquals(3, preview(filters = Filters(priceRange = range, basis = PriceBasis.Stay)).count)
    }

    @Test
    fun `the other filters narrow both the count and the histogram`() = runSojournTest {
        val found = preview(filters = Filters(amenities = setOf(Amenity.Workspace)))
        assertEquals(2, found.count)
        assertEquals(2, assertNotNull(found.prices).histogram.sum())
    }

    @Test
    fun `dated totals and nightly prices use their own basis and step`() = runSojournTest {
        val dated = preview(SearchQuery(dates = march, guests = Guests(2)))
        assertEquals(PriceBasis.Stay, dated.basis)
        val nightly = preview(SearchQuery(length = StayLength.Weekend))
        assertEquals(PriceBasis.Night, nightly.basis)
        // £92, £120 and £150 a night, in whole tens.
        val bounds = assertNotNull(nightly.prices).bounds
        assertEquals(Money.of(90, Currency.GBP) to Money.of(150, Currency.GBP), bounds.min to bounds.max)
    }

    @Test
    fun `nothing kept is a zero count and no histogram`() = runSojournTest {
        val found = preview(filters = Filters(amenities = setOf(Amenity.Parking)))
        assertEquals(0, found.count)
        assertNull(found.prices)
    }
}
