package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.network.local.BundledCatalog
import com.abulubad.sojourn.core.testing.FixedAppClock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Invariants over the whole seed catalogue the app ships (P-03, P-05, N-03, the brand book's photo rules). */
class BundledCatalogTest {

    private val catalog = CatalogJson.parse(BundledCatalog.json)
    private val engine = CatalogEngine(catalog, FixedAppClock())

    @Test
    fun `the catalogue has 40 stays across 6 cities and every one maps to the model`() {
        assertEquals(6, catalog.cities.size)
        assertEquals(40, catalog.stays.size)
        assertEquals(40, assertNotNull(engine.stays(null).getOrNull()).size)
        assertEquals(6, assertNotNull(engine.cities().getOrNull()).size)
    }

    @Test
    fun `every stay has five to eight distinct photos and a known area`() {
        catalog.stays.forEach { stay ->
            assertTrue(stay.photos.size in PHOTOS, "${stay.id}: ${stay.photos.size} photos")
            assertEquals(stay.photos.size, stay.photos.toSet().size, "${stay.id}: repeated photo")
            assertTrue(stay.areaKey.startsWith("area_"), stay.id)
        }
    }

    @Test
    fun `every stay prices one week and one month and three months with lines that sum to the total`() {
        // After every seeded booked range (December 2026 to spring 2027).
        val start = LocalDate(2027, 7, 1)
        catalog.stays.forEach { dto ->
            listOf(WEEK, MONTH, QUARTER).filter { it >= dto.policies.minimumNights }.forEach { nights ->
                val dates = DateRange(start, start.plus(DatePeriod(days = nights)))
                val quote = engine.quote(com.abulubad.sojourn.core.model.StayId(dto.id), dates, Guests(1)).getOrNull()
                // PriceBreakdown checks P-05 on construction; reaching here with a quote proves it held.
                assertNotNull(quote, "${dto.id}: no quote for $nights nights")
            }
        }
    }

    private companion object {
        val PHOTOS = 5..8
        const val WEEK = 7
        const val MONTH = 30
        const val QUARTER = 90
    }
}
