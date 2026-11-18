package com.abulubad.sojourn.core.database

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SojournDatabaseTest {

    private val driver = inMemoryDriver()
    private val db = sojournDatabase(driver)

    @AfterTest
    fun close() = driver.close()

    private fun stay(id: String, city: String = "london") = Stay(
        id = id, city_id = city, area_key = "area_paddington",
        title_text = "Bright studio by the canal", title_lang = "en",
        description_text = "A calm studio.", description_lang = "en",
        bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2,
        rating_score = 4.8, rating_count = 32,
        latitude = 51.5, longitude = -0.17, radius_metres = 350,
        from_minor = 9_200, from_currency = "GBP", from_basis = "night", from_excludes = "cleaning_fee",
        walk_minutes = 8, long_stay_discount_percent = 10, instant_book = 1,
    )

    @Test
    fun `a stay round-trips with its photos and amenities and policies`() {
        db.stayQueries.upsertStay(stay("london-01"))
        db.stayQueries.insertPhoto(Photo("london-01", 0, "p0", "https://x/a.jpg", null, null))
        db.stayQueries.insertPhoto(Photo("london-01", 1, "p1", "https://x/b.jpg", "Kitchen", "en"))
        db.stayQueries.insertAmenity(Stay_amenity("london-01", "wifi"))
        db.stayQueries.insertPolicy(
            Policy("london-01", 0, "free_cancellation_before", null, 14, null, null, null, null, null, null),
        )
        assertEquals("Bright studio by the canal", db.stayQueries.byId("london-01").executeAsOne().title_text)
        assertEquals(listOf("p0", "p1"), db.stayQueries.photosFor(listOf("london-01")).executeAsList().map { it.id })
        assertEquals(14, db.stayQueries.policiesFor("london-01").executeAsOne().days)
    }

    @Test
    fun `refreshing a stay replaces its children`() {
        db.stayQueries.upsertStay(stay("london-01"))
        db.stayQueries.insertAmenity(Stay_amenity("london-01", "wifi"))
        db.stayQueries.clearChildren("london-01")
        db.stayQueries.insertAmenity(Stay_amenity("london-01", "washer"))
        val amenities = db.stayQueries.amenitiesFor(listOf("london-01")).executeAsList().map { it.amenity_id }
        assertEquals(listOf("washer"), amenities)
    }

    @Test
    fun `stays filter by city`() {
        db.stayQueries.upsertStay(stay("london-01"))
        db.stayQueries.upsertStay(stay("paris-01", city = "paris"))
        assertEquals(listOf("paris-01"), db.stayQueries.inCity("paris").executeAsList().map { it.id })
        assertEquals(2L, db.stayQueries.count().executeAsOne())
    }

    @Test
    fun `quote lines cascade with their quote`() {
        db.quoteQueries.upsert(Quote("q1", "london-01", "2027-03-12", "2027-03-27", 2, 0, 144_000, "GBP", null))
        db.quoteQueries.insertLine(Quote_line("q1", 0, "nights", 138_000, 15, 9_200))
        db.quoteQueries.insertLine(Quote_line("q1", 1, "cleaning_fee", 6_000, null, null))
        val lines = db.quoteQueries.linesFor("q1").executeAsList()
        assertEquals(144_000L, lines.sumOf { it.amount_minor })
        driver.execute(null, "DELETE FROM quote WHERE id = 'q1'", 0)
        assertTrue(db.quoteQueries.linesFor("q1").executeAsList().isEmpty())
    }

    @Test
    fun `saving and unsaving a stay`() {
        db.savedQueries.save("london-01", 1_000)
        assertTrue(db.savedQueries.isSaved("london-01").executeAsOne())
        db.savedQueries.remove("london-01")
        assertEquals(false, db.savedQueries.isSaved("london-01").executeAsOne())
    }

    @Test
    fun `recent searches keep only the newest few`() {
        repeat(RECENTS) { db.searchRecentQueries.insert("london", null, null, 1, 0, null, null, it.toLong()) }
        db.searchRecentQueries.trimTo(KEEP)
        val latest = db.searchRecentQueries.latest(RECENTS.toLong()).executeAsList()
        assertEquals(KEEP.toInt(), latest.size)
        assertEquals((RECENTS - 1).toLong(), latest.first().searched_at)
        assertNull(latest.first().start_date)
    }

    private companion object {
        const val RECENTS = 8
        const val KEEP = 5L
    }
}
