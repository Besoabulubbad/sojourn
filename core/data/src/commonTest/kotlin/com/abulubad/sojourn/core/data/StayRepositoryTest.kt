package com.abulubad.sojourn.core.data

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.network.local.FakeNetworkConditions.Mode
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class StayRepositoryTest {

    @Test
    fun `the whole catalogue round-trips through the database unchanged`() = runDataTest {
        assertIs<AppResult.Success<Unit>>(stays.refresh())
        assertEquals(catalogueStays(), stays.observeStays().first())
    }

    @Test
    fun `a city query returns only that city`() = runDataTest {
        stays.refresh()
        val paris = stays.observeStays(CityId("paris")).first()
        assertTrue(paris.isNotEmpty())
        assertTrue(paris.all { it.cityId == CityId("paris") })
        assertEquals(catalogueStays().filter { it.cityId == CityId("paris") }, paris)
    }

    @Test
    fun `nothing is cached before the first refresh and an offline first refresh leaves it so`() = runDataTest {
        assertFalse(stays.hasCached())
        conditions.mode = Mode.Offline
        assertEquals(AppError.Offline, stays.refresh().errorOrNull())
        assertFalse(stays.hasCached())
        assertEquals(emptyList(), stays.observeStays().first())
    }

    @Test
    fun `a failed refresh keeps the cached stays and says why`() = runDataTest {
        val catalogue = catalogueStays()
        stays.refresh()
        conditions.mode = Mode.Offline
        assertEquals(AppError.Offline, stays.refresh(force = true).errorOrNull())
        conditions.mode = Mode.ServerError
        assertEquals(AppError.Remote(503), stays.refresh(force = true).errorOrNull())
        assertTrue(stays.hasCached())
        assertEquals(catalogue, stays.observeStays().first())
    }

    @Test
    fun `a refresh within fifteen minutes is skipped unless forced`() = runDataTest {
        stays.refresh()
        conditions.mode = Mode.Offline
        assertTrue(stays.refresh().isSuccess, "fresh: no network call")
        assertFalse(stays.refresh(force = true).isSuccess, "forced: goes to the network")
        clock.advance(16.minutes)
        assertFalse(stays.refresh().isSuccess, "stale: goes to the network")
    }

    @Test
    fun `a stay gone from the catalogue is removed by the next refresh in its scope`() = runDataTest {
        val ghost = catalogueStays().first().copy(id = StayId("ghost"))
        store.replaceScope(ghost.cityId, listOf(ghost))
        assertNotNull(stays.observeStay(ghost.id).first())
        stays.refresh(ghost.cityId)
        assertNull(stays.observeStay(ghost.id).first())
    }

    @Test
    fun `a city refresh leaves other cities alone`() = runDataTest {
        stays.refresh()
        val before = stays.observeStays(CityId("london")).first()
        stays.refresh(CityId("paris"), force = true)
        assertEquals(before, stays.observeStays(CityId("london")).first())
    }

    @Test
    fun `refreshing one stay stores it with its blocked dates and reviews`() = runDataTest {
        val id = catalogueStays().first().id
        assertNull(stays.observeStay(id).first())
        assertTrue(stays.refreshStay(id).isSuccess)
        val stay = assertNotNull(stays.observeStay(id).first())
        assertEquals(api.stay(id).getOrNull(), stay)
        assertTrue(stay.inclusions.isNotEmpty() && stay.descriptionTranslations.isNotEmpty())
        assertEquals(api.blockedDates(id).getOrNull(), stays.observeBlockedDates(id).first())
        val reviews = stays.observeReviews(id).first()
        assertEquals(api.reviews(id).getOrNull(), reviews)
        assertTrue(reviews.isNotEmpty())
        assertEquals(reviews.sortedByDescending { it.month }, reviews)
    }

    @Test
    fun `a list refresh keeps what Details fetched - blocked dates and reviews`() = runDataTest {
        // A stay row is updated in place: replacing it would cascade-delete the children a list does not carry.
        val id = catalogueStays().first().id
        stays.refreshStay(id)
        val blocked = stays.observeBlockedDates(id).first()
        val reviews = stays.observeReviews(id).first()
        assertTrue(blocked.isNotEmpty())
        stays.refresh(force = true)
        assertEquals(blocked, stays.observeBlockedDates(id).first())
        assertEquals(reviews, stays.observeReviews(id).first())
    }

    @Test
    fun `a stay the API no longer knows is removed when its details refresh`() = runDataTest {
        val ghost = catalogueStays().first().copy(id = StayId("ghost"))
        store.write(ghost, emptyList())
        assertIs<AppError.NotFound>(stays.refreshStay(ghost.id).errorOrNull())
        assertNull(stays.observeStay(ghost.id).first())
    }

    @Test
    fun `observing by ids returns the ones that exist`() = runDataTest {
        stays.refresh()
        val wanted = catalogueStays().take(3).map { it.id }.toSet()
        val found = stays.observeStays(wanted + StayId("missing")).first()
        assertEquals(wanted, found.map { it.id }.toSet())
    }

    @Test
    fun `the list re-emits when a refresh changes the database`() = runDataTest {
        stays.observeStays().test {
            assertEquals(0, awaitItem().size)
            stays.refresh()
            assertEquals(catalogueStays(), awaitItem())
        }
    }
}
