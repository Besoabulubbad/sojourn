package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.network.local.FakeNetworkConditions.Mode
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CityRepositoryTest {

    @Test
    fun `cities round-trip through the database with the most stays first`() = runDataTest {
        assertTrue(cities.refresh().isSuccess)
        val stored = cities.observeCities().first()
        assertEquals(api.cities().getOrNull().orEmpty().toSet(), stored.toSet())
        assertEquals(stored.sortedByDescending { it.stayCount }.map { it.stayCount }, stored.map { it.stayCount })
    }

    @Test
    fun `one city is observable by id`() = runDataTest {
        cities.refresh()
        assertEquals(CityId("lisbon"), cities.observeCity(CityId("lisbon")).first()?.id)
    }

    @Test
    fun `an offline refresh keeps the cached cities`() = runDataTest {
        cities.refresh()
        conditions.mode = Mode.Offline
        assertEquals(AppError.Offline, cities.refresh(force = true).errorOrNull())
        assertEquals(6, cities.observeCities().first().size)
    }
}
